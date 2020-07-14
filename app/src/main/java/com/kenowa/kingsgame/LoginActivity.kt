package com.kenowa.kingsgame

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onStart() {
        super.onStart()
        val user = mAuth.currentUser
        existCurrentUser(user)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        bt_registro.setOnClickListener { goToRegistroActivity() }
        bt_login.setOnClickListener { requestLogin() }
    }

    private fun existCurrentUser(user: FirebaseUser?) {
        if (user != null) {
            goToMainActivity()
        }
    }

    private fun requestLogin() {
        val email = et_email.text.toString()
        val clave = et_clave.text.toString()

        hideKeyboard()
        dataIncomplete(email, clave)
    }

    private fun dataIncomplete(
        email: String,
        clave: String
    ) {
        if (email.isEmpty() || clave.isEmpty()) {
            showMessage(this, "Hay campos vacíos")
        } else {
            mAuth.signInWithEmailAndPassword(email, clave)
                .addOnCompleteListener(
                    this
                ) { task -> okUser(task) }
        }
    }

    private fun okUser(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            goToMainActivity()
        } else {
            val errorCode = task.exception?.message.toString()
            identifyError(errorCode)
        }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun goToRegistroActivity() {
        startActivity(Intent(this, RegistroActivity::class.java))
    }

    private fun identifyError(error: String) {
        when (error) {
            "The password is invalid or the user does not have a password." -> {
                showMessage(this, "Clave incorrecta")
            }
            "The email address is badly formatted." -> {
                showMessage(this, "Correo mal redactado")
            }
            else -> {
                showMessage(this, "No registra usuario con este correo")
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}