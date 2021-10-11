package engineer.yusrisahrul.chatapp.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import engineer.yusrisahrul.chatapp.databinding.ItemContainerRecentConversationBinding
import engineer.yusrisahrul.chatapp.models.ChatMessage
import engineer.yusrisahrul.chatapp.models.User

class RecentConversationsAdapter(
    private val chatMessage: List<ChatMessage>,
    private val listeners: (ChatMessage) -> Unit
) : RecyclerView.Adapter<RecentConversationsAdapter.ViewHolder>() {

    private fun getUserImage(encodedImage: String) : Bitmap =
        BitmapFactory.decodeByteArray(
            Base64.decode(encodedImage, Base64.DEFAULT), 0,
            Base64.decode(encodedImage, Base64.DEFAULT).size)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = ViewHolder(
        ItemContainerRecentConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.view) {
            imageProfile.setImageBitmap(getUserImage(chatMessage[position].conversationImage))
            textName.text = chatMessage[position].conversationName
            textRecentMessage.text = chatMessage[position].message

            root.setOnClickListener {
                listeners(chatMessage[position])
            }
        }
    }

    override fun getItemCount(): Int = chatMessage.size

    class ViewHolder(val view: ItemContainerRecentConversationBinding) : RecyclerView.ViewHolder(view.root)
}