package com.kenowa.kingsgame

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.kenowa.kingsgame.model.Usuario
import kotlinx.android.synthetic.main.activity_login.*

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var progressBar: ProgressBar? = null
    private var i = 0
    private val handler = Handler()

    override fun onStart() {
        super.onStart()
        existCurrentUser(mAuth.currentUser)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        progressBar = findViewById(R.id.progressBar)
        hideProgressBar(findViewById(R.id.progressBar))

        bt_registro.setOnClickListener { goToRegistroActivity() }
        bt_login.setOnClickListener {
            showProgressBar(findViewById(R.id.progressBar))
            requestLogin()
        }
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
        isCompleteData(email, clave)
    }

    private fun isCompleteData(
        email: String,
        clave: String
    ) {
        if (email.isEmpty() || clave.isEmpty()) {
            showMessage(this, "Hay campos vacíos")
            hideProgressBar(findViewById(R.id.progressBar))
            i = 0
        } else {
            mAuth.signInWithEmailAndPassword(email, clave)
                .addOnCompleteListener(
                    this
                ) { task ->
                    okUser(task)
                }
        }
    }

    private fun okUser(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            runProgressBar()
            loadPerfil()
        } else {
            val errorCode = task.exception?.message.toString()
            identifyError(errorCode)
            hideProgressBar(findViewById(R.id.progressBar))
            i = 0
        }
    }

    private fun runProgressBar() {
        i = progressBar!!.progress
        Thread(Runnable {
            while (i < 100) {
                i += 2
                handler.post {
                    progressBar!!.progress = i
                }
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }).start()
    }

    private fun loadPerfil() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("usuarios")
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val actual: FirebaseUser? = mAuth.currentUser
        val email = actual?.email
        identifyUser(email, myRef)
    }

    private fun identifyUser(
        email: String?,
        myRef: DatabaseReference
    ) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val user = datasnapshot.getValue(Usuario::class.java)
                    isUser(user, email)
                }
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isUser(
        user: Usuario?,
        email: String?
    ) {
        if (user?.correo == email) {
            goToMainActivity()
            showMessage(this, "Bienvenid@ $email")
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