package engineer.yusrisahrul.chatapp.activites

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
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
import engineer.yusrisahrul.chatapp.network.ApiClient
import engineer.yusrisahrul.chatapp.network.ApiService
import engineer.yusrisahrul.chatapp.util.Constants
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_AVAILABILITY
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_COLLECTION_CHAT
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_COLLECTION_CONVERSATIONS
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_COLLECTION_USERS
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_FCM_TOKEN
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
import engineer.yusrisahrul.chatapp.util.Constants.Companion.REMOTE_MSG_DATA
import engineer.yusrisahrul.chatapp.util.Constants.Companion.REMOTE_MSG_REGISTRATION_IDS
import engineer.yusrisahrul.chatapp.util.Constants.Companion.getRemoteMsgHeaders
import engineer.yusrisahrul.chatapp.util.PreferenceManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ChatActivity : BaseActivity() {

    private val binding: ActivityChatBinding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }

    private lateinit var receiverUser: User
    private var chatMessages: MutableList<ChatMessage> = mutableListOf()
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore

    private lateinit var chatAdapter: ChatAdapter

    private var conversationId: String? = null

    private var isReceiverAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        loadReceiverDetails()
        init()
        setListeners()
        listenMessages()
    }

    override fun onResume() {
        super.onResume()
        listenAvailabilityOfReceiver()
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
            conversation[KEY_RECEIVER_NAME] = receiverUser.name ?: ""
            conversation[KEY_LAST_MESSAGE] = binding.inputMessage.text.toString()
            conversation[KEY_RECEIVER_IMAGE] = receiverUser.image
            conversation[KEY_TIMESTAMP] = Date()
            addConversation(conversation)
        }
        if (!isReceiverAvailable) {
            try {
                val token = JSONArray()
                token.put(receiverUser.token)

                val data = JSONObject()
                data.put(KEY_USER_ID, preferenceManager.getString(KEY_USER_ID))
                data.put(KEY_NAME, preferenceManager.getString(KEY_NAME))
                data.put(KEY_FCM_TOKEN, preferenceManager.getString(KEY_FCM_TOKEN))
                data.put(KEY_MESSAGE, binding.inputMessage.text.toString())

                val body = JSONObject()
                body.put(REMOTE_MSG_DATA, data)
                body.put(REMOTE_MSG_REGISTRATION_IDS, token)

                sendNotification(body.toString())
            } catch (e: Exception) {
                showToast(e.message)
            }
        }
        binding.inputMessage.text = null
    }

    private fun listenAvailabilityOfReceiver() {
        database.collection(
            KEY_COLLECTION_USERS
        ).document(receiverUser.id)
            .addSnapshotListener(this) { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    if (value.getLong(KEY_AVAILABILITY) != null) {
                        val availability = Objects.requireNonNull(
                            value.getLong(KEY_AVAILABILITY)
                        ) ?: -1
                        isReceiverAvailable = availability.toInt() == 1
                    }
                    receiverUser.token = value.getString(KEY_FCM_TOKEN)
                }
                binding.textAvailability.visibility =
                    if (isReceiverAvailable) View.VISIBLE
                    else View.GONE

            }
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

    private fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun sendNotification(messageBody: String) {
        ApiClient().getClient().create(ApiService::class.java).sendMessage(
            getRemoteMsgHeaders(),
            messageBody
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    try {
                        if (response.body() != null) {
                            val responseJson = JSONObject(response.body()!!)
                            val results = responseJson.getJSONArray("results")
                            if (responseJson.getInt("failure") == 1) {
                                val error = results.get(0) as JSONObject
                                showToast(error.getString("error"))
                                return
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    showToast("Notification sent successfully")
                } else {
                    showToast("Error: " + response.code())
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                showToast(t.message)
            }

        })
    }
}