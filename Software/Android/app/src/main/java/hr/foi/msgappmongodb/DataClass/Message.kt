package hr.foi.msgappmongodb.DataClass

data class Message(
    val sender: String,
    val receiver: String,
    val message: String,
    val type: String,
    val date: Long
)
