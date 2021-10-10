package engineer.yusrisahrul.chatapp.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import engineer.yusrisahrul.chatapp.R
import engineer.yusrisahrul.chatapp.databinding.ActivitySignInBinding
import java.util.*
import kotlin.collections.HashMap

class SignInActivity : AppCompatActivity() {

    private val binding: ActivitySignInBinding by lazy {
        ActivitySignInBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        with(binding) {
            textCreateNewAccount.setOnClickListener {
                startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
            }
            buttonSignIn.setOnClickListener {
                addDataToFirestore()
            }
        }
    }

    private fun addDataToFirestore() {
        val database = FirebaseFirestore.getInstance()
        val data = HashMap<String, Any>()
        data["first_name"] = "Yusri"
        data["last_name"] = "Sahrul"
        database.collection("users")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Data Inserted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }
}