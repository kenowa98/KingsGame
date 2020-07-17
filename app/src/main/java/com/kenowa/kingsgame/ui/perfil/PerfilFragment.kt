package com.kenowa.kingsgame.ui.perfil

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.getAge
import com.kenowa.kingsgame.model.Usuario
import com.kenowa.kingsgame.showMessage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_perfil.*

@Suppress("DEPRECATION")
class PerfilFragment : Fragment() {
    private var isStarted = false
    private var progressStatus = 0
    private var handler: Handler? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iv_foto.visibility = View.GONE
        loadPerfil()
        runProgressBar()
        ibt_edit.setOnClickListener {
            findNavController().navigate(R.id.action_nav_perfil_to_nav_perfil_registro)
        }
    }

    private fun runProgressBar() {
        handler = Handler(Handler.Callback {
            if (isStarted) {
                progressStatus++
            }
            handler?.sendEmptyMessageDelayed(0, 100)

            true
        })
        handler?.sendEmptyMessage(0)
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
            haveData(user)
        }
    }

    private fun haveData(user: Usuario?) {
        if (user?.nombre != "") {
            loadData(user)
        } else {
            showMessage(requireContext(), "Crea tu perfil!")
            progressBar.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadData(user: Usuario?) {
        if (user != null) {
            tv_nombre.text = user.nombre + " " + user.apellido
            tv_origen.text = user.origen
            tv_posicion.text = user.posicion
            loadBarrio(user)
            loadAge(user)
            loadGender(user)
            loadPhoto(user)
        }
    }

    private fun loadPhoto(user: Usuario) {
        if (user.foto.isNotEmpty()) {
            iv_foto.visibility = View.VISIBLE
            Picasso.get().load(user.foto).into(iv_foto)
            progressBar.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadBarrio(user: Usuario) {
        if (user.comuna == "Otro") {
            tv_barrio.text = "Vive fuera de Medellín"
        } else {
            tv_barrio.text = "Vive en " + user.barrio
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadAge(user: Usuario) {
        val fecha = user.fecha
        val year = fecha.substring(0, 4).toInt()
        val month = fecha.substring(5, 7).toInt()
        val day = fecha.substring(8, 9).toInt()
        val age = getAge(year, month, day)
        tv_edad.text = "$age años"
    }

    @SuppressLint("SetTextI18n")
    private fun loadGender(user: Usuario) {
        if (user.genero) {
            tv_genero.text = "Femenino"
        } else {
            tv_genero.text = "Masculino"
        }
    }
}