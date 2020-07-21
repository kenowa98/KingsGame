package com.kenowa.kingsgame.ui.perfil

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.kenowa.kingsgame.*
import com.kenowa.kingsgame.model.Usuario
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_perfil.*
import kotlinx.android.synthetic.main.fragment_perfil.view.*

class PerfilFragment : Fragment() {
    private var activeView = false
    private var usuario = Usuario(id = "")
    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_perfil, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPerfil()
        configureButtons()
    }

    private fun hideProgressBar() {
        root!!.progressBar.visibility = View.GONE
    }

    private fun configureButtons() {
        ibt_edit.setOnClickListener {
            val action = PerfilFragmentDirections.actionNavPerfilToNavPerfilRegistro(usuario)
            findNavController().navigate(action)
        }
        ibt_perfil.setOnClickListener { haveAccess() }
    }

    private fun haveAccess() {
        if (activeView) {
            val action = PerfilFragmentDirections.actionNavPerfilToNavPerfilView(usuario)
            findNavController().navigate(action)
            showMessage(requireContext(), "Abriendo la vista de tu perfil...")
        } else {
            showMessage(requireContext(), "Todavía no se puede cargar esta opción")
        }
    }

    private fun loadPerfil() {
        val myRef = referenceDatabase("usuarios")
        iv_foto.visibility = View.GONE
        identifyUser(myRef)
    }

    private fun identifyUser(myRef: DatabaseReference) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val userID = FirebaseAuth.getInstance().currentUser?.uid
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val user = datasnapshot.getValue(Usuario::class.java)
                    if (isUser(user?.id, userID)) {
                        haveData(user)
                        break
                    }
                }
                hideProgressBar()
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun haveData(user: Usuario?) {
        if (user?.nombre != "") {
            loadData(user)
            if (user != null) {
                usuario = user
            }
            activeView = true
        } else {
            showMessage(requireContext(), "Crea tu perfil!")
            hideProgressBar()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadData(user: Usuario?) {
        if (user != null) {
            root!!.tv_nombre.text = "${user.nombre} ${user.apellido}"
            root!!.tv_celular.text = "+57 ${user.celular}"
            root!!.tv_origen.text = user.origen
            root!!.tv_posicion.text = user.posicion
            loadBarrio(user)
            loadAge(user)
            loadGender(user)
            loadPhoto(user)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadBarrio(user: Usuario) {
        if (user.comuna == "Otro") {
            root!!.tv_barrio.text = "Vive fuera de Medellín"
        } else {
            root!!.tv_barrio.text = "Vive en el barrio ${user.barrio}"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadAge(user: Usuario) {
        val age = getAge(user)
        root!!.tv_edad.text = "$age años"
    }

    @SuppressLint("SetTextI18n")
    private fun loadGender(user: Usuario) {
        if (user.genero) {
            root!!.tv_genero.text = "Femenino"
        } else {
            root!!.tv_genero.text = "Masculino"
        }
    }

    private fun loadPhoto(user: Usuario) {
        if (user.foto.isNotEmpty()) {
            root!!.iv_foto.visibility = View.VISIBLE
            Picasso.get().load(user.foto).into(root!!.iv_foto)
        }
    }
}