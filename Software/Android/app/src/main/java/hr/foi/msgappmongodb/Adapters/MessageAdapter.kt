package hr.foi.msgappmongodb.Adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import hr.foi.msgappmongodb.DataClass.Message
import hr.foi.msgappmongodb.Helpers.HelperChatClass
import hr.foi.msgappmongodb.R

class MessageAdapter(context: Context, private val messages: List<Message>) : ArrayAdapter<Message>(context, 0, messages) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val message = getItem(position)
        var view = convertView

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false)
        }

        val senderTextView: TextView = view!!.findViewById(R.id.messageSenderText)
        val messageTextView: TextView = view.findViewById(R.id.messageContentText)
        val imageView: ImageView = view.findViewById(R.id.messageImageContent)
        val helperChatClass = HelperChatClass()
        // Set the sender's username
        senderTextView.text = "${message?.sender}:"

        // Check message type and set appropriate content
        if (message?.type == "text") {
            messageTextView.text = message.message
            messageTextView.visibility = View.VISIBLE
            imageView.visibility = View.GONE
        } else if (message?.type == "image") {
            messageTextView.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            val decodedImage = helperChatClass.decodeBase64ToBitmap(message.message)
            imageView.setImageBitmap(decodedImage)
        }

        return view
    }
}
