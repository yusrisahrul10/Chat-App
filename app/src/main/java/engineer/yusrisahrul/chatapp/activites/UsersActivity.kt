package engineer.yusrisahrul.chatapp.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import engineer.yusrisahrul.chatapp.R
import engineer.yusrisahrul.chatapp.adapter.UsersAdapter
import engineer.yusrisahrul.chatapp.databinding.ActivityMainBinding
import engineer.yusrisahrul.chatapp.databinding.ActivityUsersBinding
import engineer.yusrisahrul.chatapp.models.User
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_COLLECTION_USERS
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_EMAIL
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_FCM_TOKEN
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_IMAGE
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_NAME
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_USER
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_USER_ID
import engineer.yusrisahrul.chatapp.util.PreferenceManager

class UsersActivity : AppCompatActivity() {

    private val binding: ActivityUsersBinding by lazy {
        ActivityUsersBinding.inflate(layoutInflater)
    }

    private lateinit var preferenceManager: PreferenceManager

    private val userAdapter: UsersAdapter by lazy {
        UsersAdapter(this) {item -> onUserClicked(item)}
    }

    private val users : MutableList<User> by lazy {
        mutableListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        getUsers()
        setListeners()
    }

    private fun showErrorMessage() {
        with(binding.textErrorMessage) {
            text = String.format("%s", "No user available")
            visibility = View.VISIBLE
        }
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getUsers() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener {
                loading(false)
                val currentUserId = preferenceManager.getString(KEY_USER_ID)
                if (it.isSuccessful && it.result != null) {
                    for (queryDocumentSnapshot: QueryDocumentSnapshot in it.result!!) {
                        if (currentUserId == queryDocumentSnapshot.id) {
                            continue
                        }
                        val user = User(
                            queryDocumentSnapshot.getString(KEY_NAME)!!,
                            queryDocumentSnapshot.getString(KEY_EMAIL),
                            queryDocumentSnapshot.getString(KEY_IMAGE)!!,
                            queryDocumentSnapshot.getString(KEY_FCM_TOKEN),
                            queryDocumentSnapshot.id
                        )
                        users.add(user)
                    }
                    if (users.size > 0) {
                        userAdapter.setData(users)
                        with(binding.usersRecyclerView) {
                            adapter = userAdapter
                            visibility = View.VISIBLE
                        }
                    } else {
                        showErrorMessage()
                    }
                } else {
                    showErrorMessage()
                }
            }
    }

    private fun loading(isLoading: Boolean) {
        with(binding) {
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun onUserClicked(user: User) {
        startActivity(Intent(this, ChatActivity::class.java).also {
            it.putExtra(KEY_USER, user)
        })
        finish()
    }
}