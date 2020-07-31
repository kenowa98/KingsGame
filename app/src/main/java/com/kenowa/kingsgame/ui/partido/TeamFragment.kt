package com.kenowa.kingsgame.ui.partido

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kenowa.kingsgame.*
import com.kenowa.kingsgame.model.Player
import com.kenowa.kingsgame.model.Reserva
import com.kenowa.kingsgame.model.Usuario
import com.kenowa.kingsgame.ui.busqueda.InvitacionFragmentArgs
import kotlinx.android.synthetic.main.fragment_team.*
import kotlinx.android.synthetic.main.fragment_team.view.*

class TeamFragment : Fragment() {
    private var allPlayers: MutableList<Usuario> = mutableListOf()
    private var allID: MutableList<String> = mutableListOf()
    private lateinit var reserva: Reserva
    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_team, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val safeArgs = InvitacionFragmentArgs.fromBundle(it)
            reserva = safeArgs.reserve
        }

        identifyTeam()
        configureButtons()
    }

    private fun configureButtons() {
        bt_team1.setOnClickListener {
            val idTeam = bt_team1.text.toString()
            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage("Desea invitar a los integrantes de $idTeam?")
                    setPositiveButton("Aceptar") { _, _ ->
                        showProgressBar(root?.progressBar!!)
                        loadPlayers()
                    }
                    setNegativeButton("Cancelar") { _, _ -> }
                }
                builder.create()
            }
            alertDialog?.show()
        }
    }

    private fun loadPlayers() {
        allID.clear()
        allPlayers.clear()
        idPlayers()
    }

    private fun idPlayers() {
        val myRef = referenceDatabase("equipos")
        val idTeam = bt_team1.text.toString()
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
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        for (id in allID) {
            if (id != userID) {
                val refInvitacion = referenceDatabase("invitaciones")
                refInvitacion.child(id).child(reserva.id!!).setValue(reserva)
            }
        }
        showMessage(requireContext(), "Invitación enviada")
        hideProgressBar(root?.progressBar!!)
    }

    private fun identifyTeam() {
        val myRef = referenceDatabase("equipos")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                var cont = 1
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val team = datasnapshot.value as Map<*, *>
                    if (isUserInTeam(team, cont)) {
                        ++cont
                        if (cont == 3) {
                            break
                        }
                    }
                }
                hideProgressBar(root?.progressBar!!)
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isUserInTeam(
        team: Map<*, *>,
        cont: Int
    ): Boolean {
        val keys = team.keys
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        for (i in keys) {
            if (i == userID) {
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
                root?.tv_1?.visibility = View.VISIBLE
                root?.bt_team1?.visibility = View.VISIBLE
                root?.bt_team1?.text = data["nombre"].toString()
            }
            2 -> {
                root?.tv_2?.visibility = View.VISIBLE
                root?.bt_team2?.visibility = View.VISIBLE
                root?.bt_team2?.text = data["nombre"].toString()
            }
        }
    }
}