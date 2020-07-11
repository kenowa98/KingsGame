package com.kenowa.kingsgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_registro.*

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        bt_guardar.setOnClickListener { fullRegister() }
    }

    private fun fullRegister() {
        val email = et_correo.text.toString()
        val clave = et_clave.text.toString()
        val claveAgain = et_claveAgain.text.toString()

        hideKeyboard()
        dataIncomplete(email, clave, claveAgain)
    }

    private fun dataIncomplete(email: String, clave: String, claveAgain: String) {
        if (email.isEmpty() || clave.isEmpty() || claveAgain.isEmpty()) {
            showMessage(this, "Hay campos vacíos")
        } else {
            equalPassword(email, clave, claveAgain)
        }
    }

    private fun equalPassword(email: String, clave: String, claveAgain: String) {
        if (clave == claveAgain) {
            val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
            mAuth.createUserWithEmailAndPassword(email, clave)
                .addOnCompleteListener(
                    this
                ) { task -> saveRegister(task) }
        } else {
            showMessage(this, "Las contraseñas no son iguales")
        }
    }

    private fun saveRegister(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            //crearUsuarioEnBaseDeDatos()
            showMessage(this, "Registro exitoso")
            onBackPressed()
        } else {
            val errorCode = task.exception?.message.toString()
            identifyError(errorCode)
        }
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