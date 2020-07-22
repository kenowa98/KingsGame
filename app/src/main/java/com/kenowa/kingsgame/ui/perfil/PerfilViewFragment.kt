package com.kenowa.kingsgame.ui.perfil

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.getAge
import com.kenowa.kingsgame.isUser
import com.kenowa.kingsgame.model.Usuario
import com.kenowa.kingsgame.referenceDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_perfil_view.view.*

class PerfilViewFragment : Fragment() {
    private lateinit var user: Usuario

    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_perfil_view, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val safeArgs = PerfilViewFragmentArgs.fromBundle(it)
            user = safeArgs.usuario
            isData()
        }
    }

    private fun isData() {
        if (user.id.isNullOrEmpty()) {
            loadPerfil()
        } else {
            loadData()
        }
    }

    private fun hideProgressBar() {
        root!!.progressBar.visibility = View.GONE
    }

    private fun loadPerfil() {
        val myRef = referenceDatabase("usuarios")
        identifyUser(myRef)
    }

    private fun identifyUser(myRef: DatabaseReference) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val userID = FirebaseAuth.getInstance().currentUser?.uid
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val usuario = datasnapshot.getValue(Usuario::class.java)
                    if (isUser(usuario?.id, userID)) {
                        if (usuario != null) {
                            user = usuario
                        }
                        loadData()
                        break
                    }
                }
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    @SuppressLint("SetTextI18n")
    private fun loadData() {
        root!!.tv_apellido.text = user.apellido
        root!!.tv_nombre.text = user.nombre
        root!!.tv_posicion.text = user.posicion
        root!!.tv_origen.text = user.origen
        loadAge()
        loadBarrio()
        loadPhoto()
        scores()
        hideProgressBar()
    }

    private fun loadAge() {
        val age = getAge(user.fecha)
        root!!.tv_edad.text = age
    }

    @SuppressLint("SetTextI18n")
    private fun loadBarrio() {
        if (user.comuna == "Otro") {
            root!!.tv_sector.text = "Vive fuera de Medellín"
        } else {
            root!!.tv_sector.text = "Sector: ${user.comuna} / ${user.barrio}"
        }
    }

    private fun loadPhoto() {
        if (user.foto.isNotEmpty()) {
            Picasso.get().load(user.foto).into(root!!.iv_foto)
        }
    }

    private fun scores() {
        root!!.tv_responsable.text = "??/5.0"
        root!!.tv_limpio.text = "??/5.0"
        root!!.tv_velocidad.text = "??/5.0"
        root!!.tv_salto.text = "??/5.0"
        root!!.tv_regate.text = "??/5.0"
        root!!.tv_resistencia.text = "??/5.0"
        root!!.tv_equipo.text = "??/5.0"
    }
}