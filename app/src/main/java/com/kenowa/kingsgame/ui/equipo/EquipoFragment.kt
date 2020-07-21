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
import kotlinx.android.synthetic.main.fragment_equipo.view.*
import java.util.*

class EquipoFragment : Fragment() {
    private lateinit var team1: String
    private var adminTeam1: Boolean = false
    private lateinit var team2: String
    private var adminTeam2: Boolean = false

    private var teams: Int = 0

    private var usuario = Usuario(id = "")

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

        loadFirstView()
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
                    root!!.bt_solicitud.text = "Tienes 1 solicitud"
                } else {
                    root!!.bt_solicitud.text = "Tienes $solicitudes solicitudes"
                }
            }
        }
        myRef.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .addListenerForSingleValueEvent(postListener)
    }

    private fun configureButtons() {
        root!!.bt_crear.setOnClickListener {
            root!!.tv_msg3.visibility = View.VISIBLE
            root!!.et_nombre.visibility = View.VISIBLE
            root!!.bt_crear2.visibility = View.VISIBLE
            root!!.bt_cancelar.visibility = View.VISIBLE
            root!!.bt_crear.visibility = View.GONE
            root!!.linear3.visibility = View.GONE
        }
        root!!.bt_cancelar.setOnClickListener {
            extra()
            reloadFragment()
        }
        root!!.bt_crear2.setOnClickListener {
            extra()
            validationsName()
        }
        root!!.bt_solicitud.setOnClickListener {
            val action = EquipoFragmentDirections.actionNavEquipoToNavSolicitud(teams)
            findNavController().navigate(action)
        }
        root!!.ibt_team.setOnClickListener {
            val action = EquipoFragmentDirections.actionNavEquipoToNavEquipoView(team1)
            findNavController().navigate(action)
            showMessage(requireContext(), "Abriendo la vista del equipo...")
        }
        root!!.ibt_team2.setOnClickListener {
            val action = EquipoFragmentDirections.actionNavEquipoToNavEquipoView(team2)
            findNavController().navigate(action)
            showMessage(requireContext(), "Abriendo la vista del equipo...")
        }
        root!!.ibt_edit.setOnClickListener {
            val action = EquipoFragmentDirections.actionNavEquipoToNavEquipoEdit(team1, adminTeam1)
            findNavController().navigate(action)
        }
        root!!.ibt_edit2.setOnClickListener {
            val action = EquipoFragmentDirections.actionNavEquipoToNavEquipoEdit(team2, adminTeam2)
            findNavController().navigate(action)
        }
    }

    private fun extra() {
        hideKeyboard()
        showProgressBar()
    }

    private fun showProgressBar() {
        root!!.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        root!!.progressBar.visibility = View.GONE
    }

    private fun loadFirstView() {
        val myRef = referenceDatabase("usuarios")
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
                        if (user != null) {
                            usuario = user
                        }
                        haveData()
                        break
                    }
                }
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun haveData() {
        if (usuario.nombre == "") {
            root!!.iv_equipo.visibility = View.VISIBLE
            root!!.tv_msg.visibility = View.VISIBLE
            hideProgressBar()
        } else {
            val myRef = referenceDatabase("equipos")
            root!!.tv_msg1.visibility = View.VISIBLE
            teams = 0
            identifyTeam(myRef)
        }
    }

    private fun identifyTeam(myRef: DatabaseReference) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                var numEquipos = 2
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val team = datasnapshot.value as Map<*, *>
                    if (isUserInTeam(team, numEquipos)) {
                        --numEquipos
                        if (numEquipos == 0) {
                            break
                        }
                    }
                }
                if (numEquipos > 0) {
                    root!!.bt_crear.visibility = View.VISIBLE
                    root!!.linear3.visibility = View.VISIBLE
                }
                hideProgressBar()
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isUserInTeam(
        team: Map<*, *>,
        numEquipos: Int
    ): Boolean {
        val keys = team.keys
        for (i in keys) {
            if (i == usuario.id) {
                loadData(team, i, numEquipos)
                return true
            }
        }
        return false
    }

    private fun loadData(
        team: Map<*, *>,
        i: Any?,
        numEquipos: Int
    ) {
        val data = team[i] as Map<*, *>
        whatTeam(data, numEquipos)
    }

    private fun whatTeam(
        data: Map<*, *>,
        numEquipos: Int
    ) {
        when (numEquipos) {
            1 -> {
                root!!.linear2.visibility = View.VISIBLE
                root!!.tv_team2.text = data["nombre"].toString()
                root!!.bt_crear.visibility = View.GONE
                root!!.linear3.visibility = View.GONE
                team2 = data["nombre"].toString()
                adminTeam2 = (data["admin"] as Boolean)
                teams = 2
            }
            2 -> {
                root!!.linear1.visibility = View.VISIBLE
                root!!.tv_team.text = data["nombre"].toString()
                team1 = data["nombre"].toString()
                adminTeam1 = (data["admin"] as Boolean)
                teams = 1
            }
        }
    }

    private fun validationsName() {
        if (root!!.et_nombre.text.toString().isEmpty()) {
            showMessage(requireContext(), "Escribe un nombre")
            hideProgressBar()
        } else {
            limitChars()
        }
    }

    private fun limitChars() {
        if (root!!.et_nombre.text.toString().length > 20) {
            showMessage(requireContext(), "Máximo 20 caracteres")
            hideProgressBar()
        } else {
            val myRef = referenceDatabase("equipos")
            listTeam(myRef)
        }
    }

    private fun listTeam(myRef: DatabaseReference) {
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
            root!!.et_nombre.text.toString().toLowerCase(Locale.ROOT)
        ) {
            showMessage(requireContext(), "Este nombre ya está en uso")
            hideProgressBar()
            return true
        }
        return false
    }

    private fun createTeam(myRef: DatabaseReference) {
        val nombre = root!!.et_nombre.text.toString()
        val player = Player(
            id = FirebaseAuth.getInstance().currentUser?.uid,
            admin = true,
            nombre = nombre
        )
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            myRef.child(nombre).child(it).setValue(player)
        }
        showMessage(requireContext(), "Creando equipo...")
        reloadFragment()
    }

    private fun reloadFragment() {
        root!!.tv_msg3.visibility = View.GONE
        root!!.et_nombre.visibility = View.GONE
        root!!.bt_crear2.visibility = View.GONE
        root!!.bt_cancelar.visibility = View.GONE
        root!!.linear1.visibility = View.GONE
        root!!.linear2.visibility = View.GONE
        root!!.et_nombre.setText("")
        haveData()
    }
}