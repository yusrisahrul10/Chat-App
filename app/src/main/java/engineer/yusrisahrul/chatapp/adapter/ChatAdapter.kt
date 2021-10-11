package engineer.yusrisahrul.chatapp.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import engineer.yusrisahrul.chatapp.databinding.ItemContainerReceivedMessageBinding
import engineer.yusrisahrul.chatapp.databinding.ItemContainerSentMessageBinding
import engineer.yusrisahrul.chatapp.models.ChatMessage
import engineer.yusrisahrul.chatapp.adapter.ChatAdapter.SentMessageViewHolder




class ChatAdapter(
    private val chatMessage: List<ChatMessage>,
    private val receiverProfileImage: Bitmap,
    private val senderId: String

) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.SENT.ordinal -> SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                false)
            )
            else -> ReceivedMessageViewHolder(
                ItemContainerReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == ViewType.SENT.ordinal) {
            (holder as SentMessageViewHolder).setData(chatMessage[position])
        } else {
            (holder as ReceivedMessageViewHolder).setData(chatMessage[position], receiverProfileImage)

        }
    }

    override fun getItemCount(): Int = chatMessage.size

    override fun getItemViewType(position: Int): Int {
        return if (chatMessage[position].senderId.equals(senderId)) ViewType.SENT.ordinal
        else ViewType.RECEIVED.ordinal
    }

    enum class ViewType {
        SENT,
        RECEIVED
    }

    class SentMessageViewHolder(private val view: ItemContainerSentMessageBinding) : RecyclerView.ViewHolder(view.root) {
        fun setData(chatMessage: ChatMessage) {
            view.textMessage.text = chatMessage.message
            view.textDateTime.text = chatMessage.dateTime
        }
    }

    class ReceivedMessageViewHolder(private val view: ItemContainerReceivedMessageBinding) : RecyclerView.ViewHolder(view.root) {
        fun setData(chatMessage: ChatMessage, receiverProfilImage: Bitmap) {
            view.textMessage.text = chatMessage.message
            view.textDateTime.text = chatMessage.dateTime
            view.imageProfile.setImageBitmap(receiverProfilImage)
        }
    }
}