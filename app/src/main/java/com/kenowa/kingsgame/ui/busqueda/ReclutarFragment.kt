package com.kenowa.kingsgame.ui.busqueda

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.model.Player
import com.kenowa.kingsgame.model.Solicitud
import com.kenowa.kingsgame.model.Usuario
import com.kenowa.kingsgame.referenceDatabase
import com.kenowa.kingsgame.showMessage
import kotlinx.android.synthetic.main.fragment_reclutar.view.*

class ReclutarFragment : Fragment(), ReclutarRVAdapter.OnItemClickListener {
    private lateinit var idTeam: String
    private var allPlayer: MutableList<Usuario> = mutableListOf()
    private var allID: MutableList<String> = mutableListOf()
    private lateinit var playerAdapter: ReclutarRVAdapter

    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_reclutar, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val safeArgs = ReclutarFragmentArgs.fromBundle(it)
            idTeam = safeArgs.team
        }

        loadPlayers()

        root!!.rv_players.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false
        )

        playerAdapter = ReclutarRVAdapter(allPlayer as ArrayList<Usuario>, this)
        root!!.rv_players.adapter = playerAdapter
    }

    private fun hideProgressBar() {
        root!!.progressBar.visibility = View.GONE
    }

    override fun onItemClick(usuario: Usuario, case: Int) {
        if (case == 1) {
            val action = ReclutarFragmentDirections.actionNavReclutarToNavPerfilView(usuario)
            findNavController().navigate(action)
        } else {
            val nombre = "${usuario.nombre} ${usuario.apellido}"
            val myRef = referenceDatabase("solicitudes")
            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage("Desea reclutar a $nombre?")
                    setPositiveButton("aceptar") { _, _ ->
                        val solicitud = Solicitud(id_equipo = idTeam)
                        myRef.child(usuario.id!!).child(idTeam).setValue(solicitud)
                        showMessage(requireContext(), "Solicitud enviada")
                    }
                    setNegativeButton("cancelar") { _, _ -> }
                }
                builder.create()
            }
            alertDialog?.show()
        }
    }

    private fun loadPlayers() {
        val myRef = referenceDatabase("equipos")
        allPlayer.clear()
        allID.clear()
        searchMembers(myRef)
    }

    private fun searchMembers(myRef: DatabaseReference) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val player = datasnapshot.getValue(Player::class.java)
                    allID.add(player?.id!!)
                }
                loadFreePlayers()
            }
        }
        myRef.child(idTeam).addListenerForSingleValueEvent(postListener)
    }

    private fun loadFreePlayers() {
        val myRef = referenceDatabase("usuarios")
        println(allID)
        searchFreePlayers(myRef)
    }

    private fun searchFreePlayers(myRef: DatabaseReference) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val user = datasnapshot.getValue(Usuario::class.java)
                    isFree(user)
                }
                hideProgressBar()
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isFree(user: Usuario?) {
        if (!allID.contains(user?.id)) {
            if (user?.nombre != "") {
                if (user?.equipos!! < 2) {
                    allPlayer.add(user)
                    playerAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}