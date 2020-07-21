package com.kenowa.kingsgame.ui.equipo

import android.annotation.SuppressLint
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
import com.kenowa.kingsgame.isUser
import com.kenowa.kingsgame.model.Player
import com.kenowa.kingsgame.model.Usuario
import com.kenowa.kingsgame.referenceDatabase
import kotlinx.android.synthetic.main.fragment_equipo_view.view.*

class EquipoViewFragment : Fragment(), EquipoRVAdapter.OnItemClickListener {
    private lateinit var idTeam: String
    private var allGoalkeepers: MutableList<Usuario> = mutableListOf()
    private var allDefenders: MutableList<Usuario> = mutableListOf()
    private var allMidfielders: MutableList<Usuario> = mutableListOf()
    private var allStrikers: MutableList<Usuario> = mutableListOf()
    private var allID: MutableList<String> = mutableListOf()
    private lateinit var goalkeeperAdapter: EquipoRVAdapter
    private lateinit var defenderAdapter: EquipoRVAdapter
    private lateinit var midfieldAdapter: EquipoRVAdapter
    private lateinit var strikerAdapter: EquipoRVAdapter

    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_equipo_view, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val safeArgs = EquipoViewFragmentArgs.fromBundle(it)
            idTeam = safeArgs.team
            root!!.tv_team.text = idTeam
        }

        loadPlayers()
        val item = R.layout.item_player

        root!!.rv_goalkeeper.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false
        )
        goalkeeperAdapter = EquipoRVAdapter(allGoalkeepers as ArrayList<Usuario>, item, this)
        root!!.rv_goalkeeper.adapter = goalkeeperAdapter

        root!!.rv_defender.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false
        )
        defenderAdapter = EquipoRVAdapter(allDefenders as ArrayList<Usuario>, item, this)
        root!!.rv_defender.adapter = defenderAdapter

        root!!.rv_midfield.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false
        )
        midfieldAdapter = EquipoRVAdapter(allMidfielders as ArrayList<Usuario>, item, this)
        root!!.rv_midfield.adapter = midfieldAdapter

        root!!.rv_striker.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false
        )
        strikerAdapter = EquipoRVAdapter(allStrikers as ArrayList<Usuario>, item, this)
        root!!.rv_striker.adapter = strikerAdapter
    }

    override fun onItemClick(usuario: Usuario) {
        val action = EquipoViewFragmentDirections.actionNavEquipoViewToNavPerfilView(usuario)
        findNavController().navigate(action)
    }

    private fun hideProgressBar() {
        root!!.progressBar.visibility = View.GONE
    }

    private fun loadPlayers() {
        val myRef = referenceDatabase("equipos")
        idPlayers(myRef)
        allID.clear()
        allGoalkeepers.clear()
        allDefenders.clear()
        allMidfielders.clear()
        allStrikers.clear()
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
        val myRef = referenceDatabase("usuarios")
        for (id in allID) {
            identifyUser(myRef, id)
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
                        identifyPosition(user)
                        break
                    }
                }
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun identifyPosition(user: Usuario?) {
        when (user?.posicion) {
            "Arquero" -> {
                allGoalkeepers.add(user)
                goalkeeperAdapter.notifyDataSetChanged()
            }
            "Defensa Central", "Defensa Lateral",
            "Carrilero", "Líbero" -> {
                allDefenders.add(user)
                defenderAdapter.notifyDataSetChanged()
            }
            "Volante Defensivo", "Volante Centro",
            "Volante en Banda", "Media Punta" -> {
                allMidfielders.add(user)
                midfieldAdapter.notifyDataSetChanged()
            }
            "Extremo", "Delantero Centro", "Segundo Delantero" -> {
                allStrikers.add(user)
                strikerAdapter.notifyDataSetChanged()
            }
        }
    }
}