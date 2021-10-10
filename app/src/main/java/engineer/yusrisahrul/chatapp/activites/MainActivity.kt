package engineer.yusrisahrul.chatapp.activites

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import engineer.yusrisahrul.chatapp.R
import engineer.yusrisahrul.chatapp.databinding.ActivityMainBinding
import engineer.yusrisahrul.chatapp.databinding.ActivitySignUpBinding
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_COLLECTION_USERS
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_FCM_TOKEN
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_IMAGE
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_NAME
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_USER_ID
import engineer.yusrisahrul.chatapp.util.PreferenceManager

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var preferenceManager: PreferenceManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        loadUserDetails()
        getToken()
        setListeners()
    }

    private fun loadUserDetails() {
        with(binding) {
            textName.text = preferenceManager.getString(KEY_NAME)
            val bytes = Base64.decode(preferenceManager.getString(KEY_IMAGE), Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imageProfile.setImageBitmap(bitmap)
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
            .addOnSuccessListener {
                showToast("Token updated successfully")
            }
            .addOnFailureListener {
                showToast("Unable to update token")
            }
    }

    private fun setListeners() {
        binding.imageSignOut.setOnClickListener {
            signOut()
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