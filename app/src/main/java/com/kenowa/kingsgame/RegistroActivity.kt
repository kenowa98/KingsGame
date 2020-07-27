package com.kenowa.kingsgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.kenowa.kingsgame.model.Usuario
import kotlinx.android.synthetic.main.activity_registro.*

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        bt_guardar.setOnClickListener { fullRegister() }
    }

    private fun fullRegister() {
        val email = et_email.text.toString()
        val clave = et_clave.text.toString()
        val claveAgain = et_claveAgain.text.toString()

        hideKeyboard()
        showProgressBar(progressBar)
        isCompleteData(email, clave, claveAgain)
    }

    private fun isCompleteData(
        email: String,
        clave: String,
        claveAgain: String
    ) {
        if (email.isEmpty() || clave.isEmpty() || claveAgain.isEmpty()) {
            showMessage(this, "Hay campos vacíos")
            hideProgressBar(progressBar)
        } else {
            isEqualPasswords(email, clave, claveAgain)
        }
    }

    private fun isEqualPasswords(
        email: String,
        clave: String,
        claveAgain: String
    ) {
        if (clave == claveAgain) {
            val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
            mAuth.createUserWithEmailAndPassword(email, clave)
                .addOnCompleteListener(
                    this
                ) { task -> saveRegister(task, email) }
        } else {
            showMessage(this, "Las contraseñas no son iguales")
            hideProgressBar(progressBar)
        }
    }

    private fun saveRegister(
        task: Task<AuthResult>,
        email: String
    ) {
        if (task.isSuccessful) {
            createUser(email)
            showMessage(this, "Registro exitoso")
            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        } else {
            val errorCode = task.exception?.message.toString()
            hideProgressBar(progressBar)
            identifyError(errorCode)
        }
    }

    private fun createUser(email: String) {
        val myRef = referenceDatabase("usuarios")
        val idUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val user = Usuario(
            id = idUser,
            correo = email
        )
        myRef.child(idUser).setValue(user)
    }

    private fun identifyError(error: String) {
        when (error) {
            "The email address is already in use by another account." -> {
                showMessage(this, "Ya existe una cuenta con este correo")
            }
            "The email address is badly formatted." -> {
                showMessage(this, "Correo mal redactado")
            }
            else -> {
                showMessage(this, "La contraseña debe tener mínimo 6 caracteres")
            }
        }
    }
}