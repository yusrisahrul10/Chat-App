package engineer.yusrisahrul.chatapp.models

import java.util.*

data class ChatMessage(
    val senderId: String?,
    val receiverId: String?,
    var message: String?,
    val dateTime: String?,
    var dateObject: Date?,
    val conversationId: String?,
    val conversationName: String?,
    val conversationImage: String
)
