package com.kenowa.kingsgame.ui.perfil

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.getAge
import com.kenowa.kingsgame.hideProgressBar
import com.kenowa.kingsgame.model.Usuario
import com.kenowa.kingsgame.showMessage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_perfil.*

class PerfilFragment : Fragment() {
    private var activeView = false

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
        configureButtons()
    }

    private fun configureButtons() {
        ibt_edit.setOnClickListener {
            findNavController().navigate(R.id.action_nav_perfil_to_nav_perfil_registro)
        }
        ibt_perfil.setOnClickListener {
            if (activeView) {
                findNavController().navigate(R.id.action_nav_perfil_to_nav_perfil_view)
                showMessage(requireContext(), "Abriendo la vista de tu perfil...")
            } else {
                showMessage(requireContext(), "Crea tu perfil!")
            }
        }
    }

    private fun loadPerfil() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("usuarios")
        identifyUser(myRef)
    }

    private fun identifyUser(myRef: DatabaseReference) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val userID = FirebaseAuth.getInstance().currentUser?.uid
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val user = datasnapshot.getValue(Usuario::class.java)
                    if (isUser(user, userID)) {
                        break
                    }
                }
                hideProgressBar(progressBar)
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isUser(
        user: Usuario?,
        id: String?
    ): Boolean {
        if (user?.id == id) {
            haveData(user)
            return true
        }
        return false
    }

    private fun haveData(user: Usuario?) {
        if (user?.nombre != "") {
            loadData(user)
            activeView = true
        } else {
            showMessage(requireContext(), "Crea tu perfil!")
            hideProgressBar(progressBar)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadData(user: Usuario?) {
        if (user != null) {
            tv_nombre.text = "${user.nombre} ${user.apellido}"
            tv_tit_origen.text = user.origen
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
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadBarrio(user: Usuario) {
        if (user.comuna == "Otro") {
            tv_barrio.text = "Vive fuera de Medellín"
        } else {
            tv_barrio.text = "Vive en el barrio ${user.barrio}"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadAge(user: Usuario) {
        val fecha = user.fecha
        val year = fecha.substring(0, 4).toInt()
        val month = fecha.substring(5, 7).toInt()
        val day = fecha.substring(8, 9).toInt()
        val age = getAge(year, month, day)
        tv_tit_edad.text = "$age años"
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