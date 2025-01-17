const express = require('express');
const { MongoClient, ObjectId } = require('mongodb');
const dotenv = require('dotenv');
const fs = require('fs');
const multer = require('multer');
const upload = multer({ storage: multer.memoryStorage() });
const storage = multer.memoryStorage();
const bodyParser = require('body-parser');
const Grid = require('gridfs-stream');
const mongoose = require('mongoose');
let gfs;
dotenv.config();

const app = express();
app.use(express.json()); 

app.use((err, req, res, next) => {
  if (err instanceof SyntaxError && err.status === 400 && 'body' in err) {
    console.error('Bad JSON:', req.body); // Log the bad JSON
    return res.status(400).send({ error: 'Invalid JSON received' });
  }
  next();
});
app.use(express.json({ limit: '10mb', timeout: 50000 }));
app.use(bodyParser.json({ limit: '10mb' })); // Increase the limit as necessary
app.use(bodyParser.urlencoded({ limit: '10mb', extended: true })); 

const PORT = 3000;
const client = new MongoClient(process.env.MONGO_URI);
let db, usersCollection, messagesCollection;

client.connect().then(() => {
  db = client.db('BazeDiplomski');
  usersCollection = db.collection('Users');
  messagesCollection = db.collection('Messages');
  console.log('Connected to MongoDB');
}).catch(console.error);

app.get('/setup', async (req, res) => {
  await usersCollection.insertMany([
    { id: '1', username: 'user1' },
    { id: '2', username: 'user2' }
  ]);
  await messagesCollection.insertMany([
    { id: '1', receiver: 'user1', sender: 'user2', message: 'Hello!', type: 'text', date: Date.now() }
  ]);
  try {
    const imageFilePath = './testImage.png';
    const imageData = fs.readFileSync(imageFilePath);
    const imageBase64 = imageData.toString('base64');

    const imageFilePath2 = './testImage2.png';
    const imageData2 = fs.readFileSync(imageFilePath2);
    const imageBase64_2 = imageData2.toString('base64');

    const messages = [
      { id: '2', receiver: 'user2', sender: 'user1', message: 'Hey there!', type: 'text' , date: Date.now()+1 },
      { id: '3', receiver: 'user2', sender: 'user1', message: 'How are you?', type: 'text' , date: Date.now()+2 },
      { id: '4', receiver: 'user1', sender: 'user2', message: imageBase64, type: 'image', date: Date.now()+3 },
      { id: '4', receiver: 'user1', sender: 'user2', message: imageBase64_2, type: 'image', date: Date.now()+4 }
    ];

    await messagesCollection.insertMany(messages);
    res.send('Test data inserted');

  } catch (error) {
    console.error(error);
    res.status(500).send('Error setting up messages');
  }
});

app.get('/users', async (req, res) => {
  const users = await usersCollection.find().toArray();
  res.json(users);
});

app.get('/users/:username', async (req, res) => {
  const { username } = req.params;
  const user = await usersCollection.findOne({ username });
      if (user) {
        res.json(user);
      }
      else if (!user) {
          return res.status(404).send('User not found');
      }
      else{
        return res.status(500).send(err);
      }
      
  });

  app.get('/messages/:username1/:username2', async (req, res) => {
    const { username1, username2 } = req.params;
  
    try {
      const messages = await messagesCollection.find({
        $or: [
          { sender: username1, receiver: username2 },
          { sender: username2, receiver: username1 }
        ]
      }).sort({ date: 1 }).toArray(); // Sort by date in ascending order (oldest first)
  
      res.json(messages);
    } catch (error) {
      console.error(error);
      res.status(500).send('Error fetching messages');
    }
  });

app.get('/home', async (req, res) => {
    try {
      const imageMessage = await messagesCollection.findOne({ type: 'image' });
  
      if (!imageMessage) {
        return res.status(404).send('<h1>No image messages found in the database</h1>');
      }
  
      const imageBase64 = imageMessage.message;
  
      const htmlContent = `
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Image Display</title>
        </head>
        <body>
          <h1>Image from Database</h1>
          <img src="data:image/png;base64,${imageBase64}" alt="Image from Database" style="max-width:100%; height:auto;" />
        </body>
        </html>
      `;
  
      res.send(htmlContent);
    } catch (error) {
      console.error(error);
      res.status(500).send('Error fetching image from the database');
    }
  });


  app.post('/messages', async (req, res) => {
    const { sender, receiver, message, type } = req.body;

    if (!sender || !receiver || !message || !type) {
      return res.status(400).send('All fields are required');
    }

    const newMessage = {
      sender,
      receiver,
      message,
      type,  // 'text' or 'image'
      date: Date.now()  // Add current timestamp
    };

    try {
      await messagesCollection.insertOne(newMessage);
      res.status(201).json(newMessage);  // Return the newly created message
    } catch (error) {
      console.error("Error inserting message", error);
      res.status(500).send('Failed to save message');
    }
  });


  app.post('/upload-video', upload.single('video'), async (req, res) => {
    if (!req.file) {
        return res.status(400).send('No file uploaded');
    }

    try {
        // Convert the file buffer to a readable stream
        const writeStream = gfs.createWriteStream({
            filename: req.file.originalname,
            content_type: req.file.mimetype,
        });

        // Pipe the file buffer to GridFS
        writeStream.write(req.file.buffer);
        writeStream.end();

        writeStream.on('finish', (file) => {
            console.log('Video uploaded successfully');
            res.json({ file: file });
        });

    } catch (err) {
        console.error(err);
        res.status(500).send('Error uploading video');
    }
});

app.get('/video/:filename', (req, res) => {
  const { filename } = req.params;

  gfs.files.findOne({ filename: filename }, (err, file) => {
      if (err || !file) {
          return res.status(404).send('Video not found');
      }

      const readStream = gfs.createReadStream({
          filename: file.filename,
      });

      res.set('Content-Type', file.contentType);
      readStream.pipe(res);
  });
});

// Start Server
app.listen(PORT, () => {
  console.log(`Server is running on http://localhost:${PORT}`);
});
