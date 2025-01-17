package hr.foi.msgappmongodb.DataClass

data class SendMessageResponse(
    val success: Boolean,    // Indicates whether the message was sent successfully
    val message: String,     // Message from the server, for example: "Message sent"
    val messageId: String?   // Optionally, the message ID if returned by the server
)
