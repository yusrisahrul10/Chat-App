package engineer.yusrisahrul.chatapp.activites

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import engineer.yusrisahrul.chatapp.R
import engineer.yusrisahrul.chatapp.databinding.ActivityChatBinding
import engineer.yusrisahrul.chatapp.databinding.ActivityMainBinding
import engineer.yusrisahrul.chatapp.models.User
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_USER

class ChatActivity : AppCompatActivity() {

    private val binding: ActivityChatBinding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }

    private lateinit var receiverUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        loadReceiverDetails()
        setListeners()
    }

    private fun loadReceiverDetails() {
        receiverUser = intent.getSerializableExtra(KEY_USER) as User
        with(binding) {
            textName.text = receiverUser.name
        }
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
    }
}