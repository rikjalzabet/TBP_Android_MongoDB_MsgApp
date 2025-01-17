package hr.foi.msgappmongodb.Activity

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import hr.foi.msgappmongodb.Adapters.MessageAdapter
import hr.foi.msgappmongodb.DataClass.Message
import hr.foi.msgappmongodb.DataClass.getApiService
import hr.foi.msgappmongodb.Helpers.HelperChatClass
import hr.foi.msgappmongodb.Helpers.loggedInUsername
import hr.foi.msgappmongodb.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Response
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.logging.Handler

class ChatActivity : AppCompatActivity() {

    private lateinit var messageListView: ListView
    private lateinit var messageEditText: EditText
    private lateinit var sendMessageButton: Button
    private lateinit var addAttachmentButton: Button

    private lateinit var chatUsername: String

    private lateinit var handler: Handler
    private lateinit var refreshRunnable: Runnable
    private var selectedFileUri: Uri? = null
    var helperChatClass=HelperChatClass()
    private var attachmentBase64: String? = null
    private var attachmentType: String? = null


    companion object {
        private const val REQUEST_CODE_SELECT_ATTACHMENT = 101
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        messageListView = findViewById(R.id.messageListView)
        messageEditText = findViewById(R.id.messageEditText)
        sendMessageButton = findViewById(R.id.sendMessageButton)
        addAttachmentButton = findViewById(R.id.addAttachmentButton)

        loggedInUsername = intent.getStringExtra("loggedInUsername") ?: "user1"
        chatUsername = intent.getStringExtra("chatUsername") ?: "user1"

        Log.i("ChatActivity123Y", "Logged in as: $loggedInUsername")
        Log.i("ChatActivity123Y", "chatting with: $chatUsername")
        fetchMessages(loggedInUsername, chatUsername)


        sendMessageButton.setOnClickListener {
            val messageContent = messageEditText.text.toString()
            var messageData = Message(
                sender = "",
                receiver = "",
                message = "",
                type = ""
            )
            if (!messageContent.isEmpty() || attachmentBase64 != null) {
                val messageType = attachmentType ?: "text"
                val logUser= loggedInUsername!!
                messageData = Message(
                    sender = logUser,
                    receiver = chatUsername,
                    message = attachmentBase64 ?: messageContent,
                    type = messageType
                )
                sendMessageToServer(messageData)
                attachmentBase64 = null
                attachmentType = null
            } else {
                Toast.makeText(this, "Enter a message or attach a file", Toast.LENGTH_SHORT).show()
            }
        }

        addAttachmentButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*", "audio/*"))
            startActivityForResult(Intent.createChooser(intent, "Select a file"), REQUEST_CODE_SELECT_ATTACHMENT)
        }

        CoroutineScope(Dispatchers.IO).launch {
            while(true){
                fetchMessages(loggedInUsername, chatUsername)
                Thread.sleep(5000)
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_ATTACHMENT && resultCode == RESULT_OK) {
            selectedFileUri = data?.data

            if (selectedFileUri != null) {
                val contentResolver = applicationContext.contentResolver
                val mimeType = contentResolver.getType(selectedFileUri!!)

                when {
                    mimeType?.startsWith("image/") == true -> {
                        try {
                            val inputStream = contentResolver.openInputStream(selectedFileUri!!)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            val base64Image = helperChatClass.encodeImageToBase64(bitmap)
                            attachmentBase64 = base64Image
                            attachmentType = "image"
                            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
                        }
                    }
                    mimeType?.startsWith("video/") == true -> {
                        attachmentBase64 = null  // Handle video as a URI or other mechanism
                        attachmentType = "video"
                        Toast.makeText(this, "Video selected", Toast.LENGTH_SHORT).show()
                    }
                    mimeType?.startsWith("audio/") == true -> {
                        attachmentBase64 = null  // Handle audio as a URI or other mechanism
                        attachmentType = "audio"
                        Toast.makeText(this, "Audio selected", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show()
                        selectedFileUri = null
                    }
                }
            }
        }
    }
    private fun fetchMessages(loggedInUsername: String?, chatUsername: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val apiService = getApiService()
            try {
                val messages = apiService.getMessagesBetweenUsers(loggedInUsername, chatUsername)

                withContext(Dispatchers.Main) {
                    val adapter = MessageAdapter(this@ChatActivity, messages)
                    messageListView.adapter = adapter

                    messageListView.setSelection(adapter.count - 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun sendMessageToServer(message: Message) {
        Log.i("ChatActivity123C", "Sending message with data: ${message.type}; ${message.sender}; ${message.receiver};")
        Log.i("ChatActivity123D", "Sending message with message: ${message.message};")
        lifecycleScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = RequestBody.create(jsonMediaType, message.toJson())

            val request = Request.Builder()
                .url("http://10.0.2.2:3000/messages")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        messageEditText.text.clear()
                        selectedFileUri = null
                        Toast.makeText(this@ChatActivity, "Message sent", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ChatActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun isBase64Image(str: String): Boolean {
        return str.startsWith("data:image")
    }

    data class Message(val sender: String, val receiver: String, val message: String, val type: String) {
        fun toJson(): String {
           /* return """
                {
                    "sender": "$sender",
                    "receiver": "$receiver",
                    "message": "$message",
                    "type": "$type"
                }
            """.trimIndent()*/
            return Gson().toJson(this)
        }
    }
}