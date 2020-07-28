package com.kenowa.kingsgame.ui.equipo

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
import com.kenowa.kingsgame.model.Player
import com.kenowa.kingsgame.model.Usuario
import kotlinx.android.synthetic.main.fragment_equipo.*
import kotlinx.android.synthetic.main.fragment_equipo.view.*
import java.util.*

class EquipoFragment : Fragment() {
    private lateinit var team1: String
    private var adminTeam1: Boolean = false
    private lateinit var team2: String
    private var adminTeam2: Boolean = false

    private var usuario = Usuario()
    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_equipo, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchUser()
        searchInvitations()
        configureButtons()
    }

    private fun searchInvitations() {
        val myRef = referenceDatabase("solicitudes")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                var solicitudes = 0
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    ++solicitudes
                }
                if (solicitudes == 1) {
                    root?.bt_solicitud?.text = "Tienes 1 solicitud"
                } else {
                    root?.bt_solicitud?.text = "Tienes $solicitudes solicitudes"
                }
            }
        }
        myRef.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .addListenerForSingleValueEvent(postListener)
    }

    private fun configureButtons() {
        bt_crear.setOnClickListener {
            root?.tv_msg3?.visibility = View.VISIBLE
            root?.et_nombre?.visibility = View.VISIBLE
            root?.linear4?.visibility = View.VISIBLE
            root?.bt_crear?.visibility = View.GONE
            root?.linear3?.visibility = View.GONE
        }
        bt_cancelar.setOnClickListener {
            extra()
            reloadFragment()
        }
        bt_crear2.setOnClickListener {
            extra()
            validationsName()
        }
        bt_solicitud.setOnClickListener {
            val action = EquipoFragmentDirections.actionNavEquipoToNavSolicitud(usuario)
            findNavController().navigate(action)
        }
        ibt_team.setOnClickListener {
            val action = EquipoFragmentDirections.actionNavEquipoToNavEquipoView(team1)
            findNavController().navigate(action)
            showMessage(requireContext(), "Abriendo la vista del equipo...")
        }
        ibt_team2.setOnClickListener {
            val action = EquipoFragmentDirections.actionNavEquipoToNavEquipoView(team2)
            findNavController().navigate(action)
            showMessage(requireContext(), "Abriendo la vista del equipo...")
        }
        ibt_edit.setOnClickListener {
            val action =
                EquipoFragmentDirections.actionNavEquipoToNavEquipoEdit(team1, adminTeam1, usuario)
            findNavController().navigate(action)
        }
        ibt_edit2.setOnClickListener {
            val action =
                EquipoFragmentDirections.actionNavEquipoToNavEquipoEdit(team2, adminTeam2, usuario)
            findNavController().navigate(action)
        }
    }

    private fun extra() {
        hideKeyboard()
        showProgressBar(root?.progressBar!!)
    }

    private fun searchUser() {
        val myRef = referenceDatabase("usuarios")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val userID = FirebaseAuth.getInstance().currentUser?.uid
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val user = datasnapshot.getValue(Usuario::class.java)
                    if (isUser(user?.id, userID)) {
                        if (user != null) {
                            usuario = user
                            haveData()
                        }
                        break
                    }
                }
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun haveData() {
        if (usuario.nombre == "") {
            root?.iv_equipo?.visibility = View.VISIBLE
            root?.tv_msg?.visibility = View.VISIBLE
            hideProgressBar(root?.progressBar!!)
        } else {
            root?.tv_msg1?.visibility = View.VISIBLE
            identifyTeam()
        }
    }

    private fun identifyTeam() {
        val myRef = referenceDatabase("equipos")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (usuario.equipos != 0) {
                    var numEquipos = usuario.equipos
                    var cont = 1
                    for (datasnapshot: DataSnapshot in snapshot.children) {
                        val team = datasnapshot.value as Map<*, *>
                        if (isUserInTeam(team, cont)) {
                            ++cont
                            --numEquipos
                            if (numEquipos == 0) {
                                break
                            }
                        }
                    }
                }
                showRequests()
                hideProgressBar(root?.progressBar!!)
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun showRequests() {
        if (usuario.equipos < 2) {
            root?.bt_crear?.visibility = View.VISIBLE
            root?.linear3?.visibility = View.VISIBLE
        }
    }

    private fun isUserInTeam(
        team: Map<*, *>,
        cont: Int
    ): Boolean {
        val keys = team.keys
        for (i in keys) {
            if (i == usuario.id) {
                loadData(team, i, cont)
                return true
            }
        }
        return false
    }

    private fun loadData(
        team: Map<*, *>,
        i: Any?,
        cont: Int
    ) {
        val data = team[i] as Map<*, *>
        whatTeam(data, cont)
    }

    private fun whatTeam(
        data: Map<*, *>,
        cont: Int
    ) {
        when (cont) {
            1 -> {
                root?.linear1?.visibility = View.VISIBLE
                root?.tv_team?.text = data["nombre"].toString()
                team1 = data["nombre"].toString()
                adminTeam1 = (data["admin"] as Boolean)
            }
            2 -> {
                root?.linear2?.visibility = View.VISIBLE
                root?.tv_team2?.text = data["nombre"].toString()
                root?.bt_crear?.visibility = View.GONE
                root?.linear3?.visibility = View.GONE
                team2 = data["nombre"].toString()
                adminTeam2 = (data["admin"] as Boolean)
            }
        }
    }

    private fun validationsName() {
        if (root?.et_nombre?.text.toString().isEmpty()) {
            showMessage(requireContext(), "Escribe un nombre")
            hideProgressBar(root?.progressBar!!)
        } else {
            limitChars()
        }
    }

    private fun limitChars() {
        if (root?.et_nombre?.text.toString().length > 20) {
            showMessage(requireContext(), "Máximo 20 caracteres")
            hideProgressBar(root?.progressBar!!)
        } else {
            listTeam()
        }
    }

    private fun listTeam() {
        val myRef = referenceDatabase("equipos")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                var permiso = true
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val team = datasnapshot.value as Map<*, *>
                    if (isValidName(team)) {
                        permiso = false
                        break
                    }
                }
                if (permiso) {
                    createTeam(myRef)
                }
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isValidName(team: Map<*, *>): Boolean {
        val data = team[team.keys.first()] as Map<*, *>
        if (data["nombre"].toString().toLowerCase(Locale.ROOT) ==
            root?.et_nombre?.text.toString().toLowerCase(Locale.ROOT)
        ) {
            showMessage(requireContext(), "Este nombre ya está en uso")
            hideProgressBar(root?.progressBar!!)
            return true
        }
        return false
    }

    private fun createTeam(myRef: DatabaseReference) {
        val nombre = root?.et_nombre?.text.toString()
        val player = Player(
            id = FirebaseAuth.getInstance().currentUser?.uid,
            admin = true,
            nombre = nombre
        )
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            myRef.child(nombre).child(it).setValue(player)
        }
        val refUser = referenceDatabase("usuarios")
        val equipos = usuario.equipos + 1
        refUser.child(usuario.id!!).child("equipos").setValue(equipos)
        showMessage(requireContext(), "Creando equipo...")
        reloadFragment()
    }

    private fun reloadFragment() {
        root?.tv_msg3?.visibility = View.GONE
        root?.et_nombre?.visibility = View.GONE
        root?.linear1?.visibility = View.GONE
        root?.linear2?.visibility = View.GONE
        root?.linear4?.visibility = View.GONE
        root?.et_nombre?.setText("")
        searchUser()
    }
}