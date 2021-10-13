package engineer.yusrisahrul.chatapp.models

import java.io.Serializable

data class User(
    val name: String?,
    val email: String?,
    val image: String,
    var token: String?,
    val id: String,
) : Serializable
