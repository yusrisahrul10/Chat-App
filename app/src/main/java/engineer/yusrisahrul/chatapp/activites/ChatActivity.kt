package engineer.yusrisahrul.chatapp.activites

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
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
import engineer.yusrisahrul.chatapp.util.Constants
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_COLLECTION_CHAT
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_COLLECTION_CONVERSATIONS
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_IMAGE
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_LAST_MESSAGE
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_MESSAGE
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_NAME
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_RECEIVER_ID
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_RECEIVER_IMAGE
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_RECEIVER_NAME
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_SENDER_ID
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_SENDER_IMAGE
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_SENDER_NAME
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

    private var conversationId: String? = null

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
        message[KEY_RECEIVER_ID] = receiverUser.id
        message[KEY_MESSAGE] = binding.inputMessage.text.toString()
        message[KEY_TIMESTAMP] = Date()
        database.collection(KEY_COLLECTION_CHAT).add(message)
        if (conversationId != null) {
            updateConversation(binding.inputMessage.text.toString())
        } else {
            val conversation = HashMap<String, Any>()
            conversation[KEY_SENDER_ID] = preferenceManager.getString(KEY_USER_ID)
            conversation[KEY_SENDER_NAME] = preferenceManager.getString(KEY_NAME)
            conversation[KEY_SENDER_IMAGE] = preferenceManager.getString(KEY_IMAGE)
            conversation[KEY_RECEIVER_ID] = receiverUser.id
            conversation[KEY_RECEIVER_NAME] = receiverUser.name
            conversation[KEY_LAST_MESSAGE] = binding.inputMessage.text.toString()
            conversation[KEY_RECEIVER_IMAGE] = receiverUser.image
            conversation[KEY_TIMESTAMP] = Date()
            addConversation(conversation)
        }
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

    private fun addConversation(conversation: HashMap<String, Any>) {
        database.collection(KEY_COLLECTION_CONVERSATIONS)
            .add(conversation)
            .addOnSuccessListener {
                conversationId = it.id
            }
    }

    private fun updateConversation(message: String) {
        val conversationId = conversationId ?: ""
        val documentReference = database.collection(
            KEY_COLLECTION_CONVERSATIONS
        ).document(conversationId)
        documentReference.update(KEY_LAST_MESSAGE, message, KEY_TIMESTAMP, Date())
    }

    private fun checkForConversation() {
        if (chatMessages.size != 0) {
            checkForConversationRemotely(preferenceManager.getString(KEY_USER_ID),
            receiverUser.id)
            checkForConversationRemotely(receiverUser.id,
            preferenceManager.getString(KEY_USER_ID))
        }
    }

    private fun checkForConversationRemotely(senderId: String, receiverId: String) {
        database.collection(KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(KEY_SENDER_ID, senderId)
            .whereEqualTo(KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener(conversationOnCompleteListener)
    }

    private val conversationOnCompleteListener = OnCompleteListener<QuerySnapshot> {
        val result = it.result?.documents?.size ?: 0
        if (it.isSuccessful && it.result != null && result > 0) {
            val documentSnapshot = it.result!!.documents[0]
            conversationId = documentSnapshot.id
        }
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
                        documentChange.document.getDate(KEY_TIMESTAMP),
                        null,
                        null,
                        ""
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
        if (conversationId == null) {
            checkForConversation()
        }
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