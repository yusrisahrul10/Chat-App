package engineer.yusrisahrul.chatapp.activites

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import engineer.yusrisahrul.chatapp.R
import engineer.yusrisahrul.chatapp.adapter.ChatAdapter
import engineer.yusrisahrul.chatapp.adapter.UsersAdapter
import engineer.yusrisahrul.chatapp.databinding.ActivityChatBinding
import engineer.yusrisahrul.chatapp.databinding.ActivityMainBinding
import engineer.yusrisahrul.chatapp.models.ChatMessage
import engineer.yusrisahrul.chatapp.models.User
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_COLLECTION_CHAT
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_MESSAGE
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_RECEIVER_ID
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_SENDER_ID
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_TIMESTAMP
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_USER
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_USER_ID
import engineer.yusrisahrul.chatapp.util.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {

    private val binding: ActivityChatBinding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }

    private lateinit var receiverUser: User
    private var chatMessages: MutableList<ChatMessage> = mutableListOf()
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore

    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        loadReceiverDetails()
        init()
        setListeners()
        listenMessages()
    }

    private fun init() {
        preferenceManager = PreferenceManager(this)
        chatAdapter = ChatAdapter(chatMessages, getBitmapFromEncodedString(receiverUser.image), preferenceManager.getString(
            KEY_USER_ID))
        binding.chatRecyclerView.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage() {
        val message = HashMap<String, Any>()
        message[KEY_SENDER_ID] = preferenceManager.getString(KEY_USER_ID)
        message[KEY_RECEIVER_ID] = receiverUser.id ?: ""
        message[KEY_MESSAGE] = binding.inputMessage.text.toString()
        message[KEY_TIMESTAMP] = Date()
        database.collection(KEY_COLLECTION_CHAT).add(message)
        binding.inputMessage.text = null
    }

    private fun listenMessages() {
        database.collection(KEY_COLLECTION_CHAT)
            .whereEqualTo(KEY_SENDER_ID, preferenceManager.getString(KEY_USER_ID))
            .whereEqualTo(KEY_RECEIVER_ID, receiverUser.id)
            .addSnapshotListener(eventListener)
        database.collection(KEY_COLLECTION_CHAT)
            .whereEqualTo(KEY_SENDER_ID, receiverUser.id)
            .whereEqualTo(KEY_RECEIVER_ID, preferenceManager.getString(KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    private val eventListener = EventListener<QuerySnapshot> {
        value, error ->
        if (error != null) {
            return@EventListener
        }; if (value != null) {
            val count = chatMessages.size
            for (documentChange: DocumentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    val chatMessage = ChatMessage(
                        documentChange.document.getString(KEY_SENDER_ID),
                        documentChange.document.getString(KEY_RECEIVER_ID),
                        documentChange.document.getString(KEY_MESSAGE),
                        getReadableDateTime(documentChange.document.getDate(KEY_TIMESTAMP)),
                        documentChange.document.getDate(KEY_TIMESTAMP)
                    )
                    chatMessages.add(chatMessage)
                }
            }
        chatMessages.sortWith { (_, _, _, _, dateObject1), (_, _, _, _, dateObject2) ->
            dateObject1!!.compareTo(
                dateObject2
            )
        }
        if (count == 0) {
            chatAdapter.notifyDataSetChanged()
        } else {
            chatAdapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
            binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
        }
        binding.chatRecyclerView.visibility = View.VISIBLE
    }
        binding.progressBar.visibility = View.GONE
    }

    private fun getBitmapFromEncodedString(encodedImage: String?) : Bitmap =
        BitmapFactory.decodeByteArray(Base64.decode(encodedImage, Base64.DEFAULT), 0,
            Base64.decode(encodedImage, Base64.DEFAULT).size)

    private fun loadReceiverDetails() {
        receiverUser = intent.getSerializableExtra(KEY_USER) as User
        with(binding) {
            textName.text = receiverUser.name
        }
    }

    private fun setListeners() {
        with(binding) {
            imageBack.setOnClickListener {
                onBackPressed()
            }
            layoutSend.setOnClickListener {
                sendMessage()
            }
        }

    }

    private fun getReadableDateTime(date: Date?) : String {
        val dateFormat = date ?: Date(0)
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(dateFormat)
    }
}