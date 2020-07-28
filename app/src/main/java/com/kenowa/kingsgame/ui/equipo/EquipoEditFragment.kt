package com.kenowa.kingsgame.ui.equipo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.kenowa.kingsgame.*
import com.kenowa.kingsgame.model.Player
import com.kenowa.kingsgame.model.Usuario
import kotlinx.android.synthetic.main.fragment_equipo_edit.*
import kotlinx.android.synthetic.main.fragment_equipo_edit.view.*
import java.util.*

class EquipoEditFragment : Fragment(), EquipoRVAdapter.OnItemClickListener {
    private lateinit var idTeam: String
    private var firstTime: Boolean = true
    private var admin: Boolean = false
    private var usuario = Usuario()
    private var allPlayers: MutableList<Usuario> = mutableListOf()
    private var allID: MutableList<String> = mutableListOf()
    private lateinit var equipoAdapter: EquipoRVAdapter
    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_equipo_edit, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val safeArgs = EquipoEditFragmentArgs.fromBundle(it)
            if (firstTime) {
                idTeam = safeArgs.team
                firstTime = false
            }
            admin = safeArgs.admin
            usuario = safeArgs.user
            root?.tv_team?.text = idTeam
        }

        loadPlayers()
        val item = R.layout.item_player_edit

        root?.rv_players?.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false
        )

        equipoAdapter = EquipoRVAdapter(allPlayers as ArrayList<Usuario>, item, this)
        root?.rv_players?.adapter = equipoAdapter

        configureButtons()
    }

    private fun configureButtons() {
        bt_salir.setOnClickListener {
            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage("Desea salir del equipo?")
                    setPositiveButton("aceptar") { _, _ ->
                        showProgressBar(root?.progressBar!!)
                        inheritAdmin()
                    }
                    setNegativeButton("cancelar") { _, _ -> }
                }
                builder.create()
            }
            alertDialog?.show()
        }

        bt_reclutar.setOnClickListener {
            if (admin) {
                val action = EquipoEditFragmentDirections.actionNavEquipoEditToNavReclutar(idTeam)
                findNavController().navigate(action)
            } else {
                showMessage(requireContext(), "No tienes permiso para editar")
            }
        }

        ibt_edit.setOnClickListener {
            if (admin) {
                root?.et_nombre?.visibility = View.VISIBLE
                root?.linear1?.visibility = View.VISIBLE
            } else {
                showMessage(requireContext(), "No tienes permiso para editar")
            }
        }

        bt_guardar.setOnClickListener {
            hideKeyboard()
            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage("Desea guardar los cambios?")
                    setPositiveButton("aceptar") { _, _ ->
                        showProgressBar(root?.progressBar!!)
                        validationsName()
                    }
                    setNegativeButton("cancelar") { _, _ -> }
                }
                builder.create()
            }
            alertDialog?.show()
        }

        bt_cancelar.setOnClickListener {
            dissapearElements()
        }

        bt_continuar.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onItemClick(user: Usuario) {
        if (admin) {
            val action =
                EquipoEditFragmentDirections.actionNavEquipoEditToNavPlayerEdit(idTeam, user)
            findNavController().navigate(action)
        } else {
            showMessage(requireContext(), "No tienes permiso para editar")
        }
    }

    private fun loadPlayers() {
        allID.clear()
        allPlayers.clear()
        idPlayers()
    }

    private fun idPlayers() {
        val myRef = referenceDatabase("equipos")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val player = datasnapshot.getValue(Player::class.java)
                    allID.add(player?.id!!)
                }
                searchUser()
            }
        }
        myRef.child(idTeam).addValueEventListener(postListener)
    }

    private fun searchUser() {
        val userID = usuario.id
        val myRef = referenceDatabase("usuarios")
        for (id in allID) {
            if (id != userID) {
                identifyUser(myRef, id)
            }
        }
        hideProgressBar(root?.progressBar!!)
    }

    private fun identifyUser(
        myRef: DatabaseReference,
        id: String
    ) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val user = datasnapshot.getValue(Usuario::class.java)
                    if (isUser(user?.id, id)) {
                        allPlayers.add(user!!)
                        break
                    }
                }
                equipoAdapter.notifyDataSetChanged()
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun inheritAdmin() {
        val myRef = referenceDatabase("equipos")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val userID = FirebaseAuth.getInstance().currentUser?.uid.toString()
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val player = datasnapshot.getValue(Player::class.java)
                    if (player?.id != userID) {
                        player?.id?.let {
                            myRef.child(idTeam).child(it).child("admin").setValue(true)
                        }
                        break
                    }
                }
                myRef.child(idTeam).child(userID).removeValue()
                dataUser()
            }
        }
        myRef.child(idTeam).addListenerForSingleValueEvent(postListener)
    }

    private fun dataUser() {
        val refUser = referenceDatabase("usuarios")
        val equipos = usuario.equipos - 1
        refUser.child(usuario.id!!).child("equipos").setValue(equipos)
        showMessage(requireContext(), "Saliendo de $idTeam")
        hideProgressBar(root?.progressBar!!)
        updateView()
    }

    private fun updateView() {
        root?.linear1?.visibility = View.GONE
        root?.linear2?.visibility = View.GONE
        root?.et_nombre?.visibility = View.GONE
        root?.rv_players?.visibility = View.GONE
        root?.bt_reclutar?.visibility = View.GONE
        root?.bt_salir?.visibility = View.GONE
        root?.iv_team?.visibility = View.VISIBLE
        root?.bt_continuar?.visibility = View.VISIBLE
    }

    private fun dissapearElements() {
        root?.et_nombre?.visibility = View.GONE
        root?.linear1?.visibility = View.GONE
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
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val player = datasnapshot.getValue(Player::class.java)
                    myRef.child(nombre).child(player?.id!!).setValue(player)
                    myRef.child(nombre).child(player.id).child("nombre").setValue(nombre)
                }
                deleteBeforeTeam(myRef)
            }
        }
        myRef.child(idTeam).addListenerForSingleValueEvent(postListener)
    }

    private fun deleteBeforeTeam(myRef: DatabaseReference) {
        myRef.child(idTeam).removeValue()
        idTeam = root?.et_nombre?.text.toString()
        root?.tv_team?.text = idTeam
        root?.et_nombre?.setText("")
        dissapearElements()
        showMessage(requireContext(), "Cambiando nombre del equipo...")
        loadPlayers()
    }
}