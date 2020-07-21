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
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.isUser
import com.kenowa.kingsgame.model.Player
import com.kenowa.kingsgame.model.Usuario
import com.kenowa.kingsgame.referenceDatabase
import com.kenowa.kingsgame.showMessage
import kotlinx.android.synthetic.main.fragment_equipo_edit.view.*

class EquipoEditFragment : Fragment(), EquipoRVAdapter.OnItemClickListener {
    private lateinit var idTeam: String
    private var admin: Boolean = false
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
            idTeam = safeArgs.team
            admin = safeArgs.admin
            root!!.tv_team.text = idTeam
        }

        loadPlayers()
        val item = R.layout.item_player_edit

        root!!.rv_players.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false
        )

        equipoAdapter = EquipoRVAdapter(allPlayers as ArrayList<Usuario>, item, this)
        root!!.rv_players.adapter = equipoAdapter

        configureButtons()
    }

    private fun configureButtons() {
        root!!.bt_salir.setOnClickListener {
            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage("Desea salir del equipo?")
                    setPositiveButton("aceptar") { _, _ ->
                        showProgressBar()
                        inheritAdmin()
                    }
                    setNegativeButton("cancelar") { _, _ -> }
                }
                builder.create()
            }
            alertDialog?.show()
        }

        root!!.bt_reclutar.setOnClickListener {
            if (admin) {
                val action = EquipoEditFragmentDirections.actionNavEquipoEditToNavReclutar(idTeam)
                findNavController().navigate(action)
            } else {
                showMessage(requireContext(), "No tienes permiso para editar")
            }
        }

        root!!.bt_continuar.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onItemClick(usuario: Usuario) {
        if (admin) {
            val action =
                EquipoEditFragmentDirections.actionNavEquipoEditToNavPlayerEdit(idTeam, usuario)
            findNavController().navigate(action)
        } else {
            showMessage(requireContext(), "No tienes permiso para editar")
        }
    }

    private fun showProgressBar() {
        root!!.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        root!!.progressBar.visibility = View.GONE
    }

    private fun loadPlayers() {
        val myRef = referenceDatabase("equipos")
        idPlayers(myRef)
        allID.clear()
        allPlayers.clear()
    }

    private fun idPlayers(myRef: DatabaseReference) {
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
        val userID = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val myRef = referenceDatabase("usuarios")
        for (id in allID) {
            if (id != userID) {
                identifyUser(myRef, id)
            }
        }
        hideProgressBar()
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
                        equipoAdapter.notifyDataSetChanged()
                        break
                    }
                }
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
                showMessage(requireContext(), "Saliendo de $idTeam")
                hideProgressBar()
                myRef.child(idTeam).child(userID).removeValue()
                updateView()
            }
        }
        myRef.child(idTeam).addListenerForSingleValueEvent(postListener)
    }

    private fun updateView() {
        root!!.linear1.visibility = View.GONE
        root!!.rv_players.visibility = View.GONE
        root!!.bt_reclutar.visibility = View.GONE
        root!!.bt_salir.visibility = View.GONE
        root!!.iv_team.visibility = View.VISIBLE
        root!!.bt_continuar.visibility = View.VISIBLE
    }
}