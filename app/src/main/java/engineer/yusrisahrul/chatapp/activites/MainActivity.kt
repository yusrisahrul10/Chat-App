package engineer.yusrisahrul.chatapp.activites

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import engineer.yusrisahrul.chatapp.R
import engineer.yusrisahrul.chatapp.adapter.RecentConversationsAdapter
import engineer.yusrisahrul.chatapp.databinding.ActivityMainBinding
import engineer.yusrisahrul.chatapp.databinding.ActivitySignUpBinding
import engineer.yusrisahrul.chatapp.models.ChatMessage
import engineer.yusrisahrul.chatapp.models.User
import engineer.yusrisahrul.chatapp.util.Constants
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_COLLECTION_CONVERSATIONS
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_COLLECTION_USERS
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_FCM_TOKEN
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_IMAGE
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_LAST_MESSAGE
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_NAME
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_RECEIVER_ID
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_RECEIVER_IMAGE
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_RECEIVER_NAME
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_SENDER_ID
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_SENDER_IMAGE
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_SENDER_NAME
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_TIMESTAMP
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_USER_ID
import engineer.yusrisahrul.chatapp.util.PreferenceManager

class MainActivity : BaseActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var preferenceManager: PreferenceManager

    private var conversations: MutableList<ChatMessage> = mutableListOf()
    private lateinit var conversationsAdapter: RecentConversationsAdapter
    private lateinit var database: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        init()
        loadUserDetails()
        getToken()
        setListeners()
        listenConversations()
    }

    private fun init() {
        conversationsAdapter = RecentConversationsAdapter(conversations) {item -> onConversationClicked(item)}
        binding.conversationsRecyclerView.adapter = conversationsAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun onConversationClicked(chatMessage: ChatMessage) {
        val user = User(
            chatMessage.conversationName!!,
            "",
            chatMessage.conversationImage,
            "",
            chatMessage.conversationId!!
        )
        startActivity(Intent(this, ChatActivity::class.java).also {
            it.putExtra(Constants.KEY_USER, user)
        })
    }

    private fun loadUserDetails() {
        with(binding) {
            textName.text = preferenceManager.getString(KEY_NAME)
            val bytes = Base64.decode(preferenceManager.getString(KEY_IMAGE), Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imageProfile.setImageBitmap(bitmap)
        }
    }

    private fun listenConversations() {
        database.collection(KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(KEY_SENDER_ID, preferenceManager.getString(KEY_USER_ID))
            .addSnapshotListener(eventListener)
        database.collection(KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(KEY_RECEIVER_ID, preferenceManager.getString(KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    private val eventListener = EventListener<QuerySnapshot> {
        value, error ->
        if (error != null) {
            return@EventListener
        }
        if (value != null) {
            for (documentChange: DocumentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    val senderId = documentChange.document.getString(KEY_SENDER_ID)
                    val chatMessage = ChatMessage(
                        senderId,
                        documentChange.document.getString(KEY_RECEIVER_ID),
                        documentChange.document.getString(KEY_LAST_MESSAGE),
                        "",
                        documentChange.document.getDate(KEY_TIMESTAMP),
                        if (preferenceManager.getString(KEY_USER_ID) == senderId)
                            documentChange.document.getString(KEY_RECEIVER_ID)
                        else documentChange.document.getString(KEY_SENDER_ID),
                        if (preferenceManager.getString(KEY_USER_ID) == senderId)
                            documentChange.document.getString(KEY_RECEIVER_NAME)
                        else documentChange.document.getString(KEY_SENDER_NAME),
                        if (preferenceManager.getString(KEY_USER_ID) == senderId)
                            documentChange.document.getString(KEY_RECEIVER_IMAGE)!!
                        else documentChange.document.getString(KEY_SENDER_IMAGE)!!,
                    )
                    conversations.add(chatMessage)
                } else if (documentChange.type == DocumentChange.Type.MODIFIED) {
                    for (i in 0 until conversations.size) {
                        val senderId = documentChange.document.getString(KEY_SENDER_ID)
                        val receiverId = documentChange.document.getString(KEY_RECEIVER_ID)
                        if (conversations[i].senderId == senderId && conversations[i].receiverId == receiverId) {
                            conversations[i].message = documentChange.document.getString(
                                KEY_LAST_MESSAGE)
                            conversations[i].dateObject = documentChange.document.getDate(
                                KEY_TIMESTAMP
                            )
                            break
                        }
                    }
                }
            }
            conversations.sortWith {
                    (_, _, _, _, dateObject1), (_, _, _, _, dateObject2) ->
                dateObject2!!.compareTo(
                    dateObject1
                )
            }
            conversationsAdapter.notifyDataSetChanged()
            binding.conversationsRecyclerView.smoothScrollToPosition(0)
            binding.conversationsRecyclerView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)
    }

    private fun updateToken(token: String) {
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(KEY_COLLECTION_USERS).document(
            preferenceManager.getString(KEY_USER_ID)
        )
        documentReference.update(KEY_FCM_TOKEN, token)
            .addOnFailureListener {
                showToast("Unable to update token")
            }
    }

    private fun setListeners() {
        with(binding) {
            imageSignOut.setOnClickListener {
                signOut()
            }
            fabNewChat.setOnClickListener {
                startActivity(Intent(this@MainActivity, UsersActivity::class.java))
            }
        }
    }

    private fun signOut() {
        showToast("Signing out...")
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(KEY_COLLECTION_USERS).document(
            preferenceManager.getString(KEY_USER_ID)
        )
        val updates = HashMap<String, Any>()
        updates[KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(updates)
            .addOnSuccessListener {
                preferenceManager.clear()
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                showToast("Unable to sign out")
            }
    }
}