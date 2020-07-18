package com.kenowa.kingsgame.ui.perfil

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.getAge
import com.kenowa.kingsgame.hideProgressBar
import com.kenowa.kingsgame.model.Usuario
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_perfil_view.*

class PerfilViewFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perfil_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPerfil()
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
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val user = datasnapshot.getValue(Usuario::class.java)
                    if (isUser(user)) {
                        break
                    }
                }
                hideProgressBar(progressBar)
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isUser(user: Usuario?): Boolean {
        if (user?.id == FirebaseAuth.getInstance().currentUser?.uid) {
            loadData(user)
            return true
        }
        return false
    }

    @SuppressLint("SetTextI18n")
    private fun loadData(user: Usuario?) {
        if (user != null) {
            tv_apellido.text = user.apellido
            tv_nombre.text = user.nombre
            tv_posicion.text = user.posicion
            tv_origen.text = user.origen
            loadAge(user)
            loadBarrio(user)
            loadPhoto(user)
        }
    }

    private fun loadPhoto(user: Usuario) {
        if (user.foto.isNotEmpty()) {
            Picasso.get().load(user.foto).into(iv_foto)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadBarrio(user: Usuario) {
        if (user.comuna == "Otro") {
            tv_sector.text = "Vive fuera de Medellín"
        } else {
            tv_sector.text = "Sector: ${user.comuna} / ${user.barrio}"
        }
    }

    private fun loadAge(user: Usuario) {
        val fecha = user.fecha
        val year = fecha.substring(0, 4).toInt()
        val month = fecha.substring(5, 7).toInt()
        val day = fecha.substring(8, 9).toInt()
        val age = getAge(year, month, day)
        tv_edad.text = age
    }
}