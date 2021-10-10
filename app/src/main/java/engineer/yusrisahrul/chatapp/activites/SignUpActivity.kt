package engineer.yusrisahrul.chatapp.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import engineer.yusrisahrul.chatapp.R
import engineer.yusrisahrul.chatapp.databinding.ActivitySignInBinding
import engineer.yusrisahrul.chatapp.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private val binding: ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setListeners()
    }

    fun setListeners() {
        with(binding) {
            textSignIn.setOnClickListener {
                startActivity(Intent(this@SignUpActivity, SignUpActivity::class.java))
            }
        }
    }
}