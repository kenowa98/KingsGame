package com.kenowa.kingsgame.ui.equipo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.hideProgressBar
import com.kenowa.kingsgame.model.Usuario
import kotlinx.android.synthetic.main.fragment_equipo.*

class EquipoFragment : Fragment() {
    //private var allPlayers: MutableList<Noticia> = mutableListOf()
    //private lateinit var equipoAdapter: EquipoRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_equipo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firstView()

        /*val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("equipos")
        val idUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val player = Player(idUser)

        myRef.child("De la Salle").child(idUser).setValue(player)*/
    }

    private fun firstView() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("usuarios")
        identifyUser(myRef)
    }

    private fun identifyUser(myRef: DatabaseReference) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val userID = FirebaseAuth.getInstance().currentUser?.uid
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val user = datasnapshot.getValue(Usuario::class.java)
                    if (isUser(user, userID)) {
                        break
                    }
                }
                hideProgressBar(progressBar)
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isUser(
        user: Usuario?,
        id: String?
    ): Boolean {
        if (user?.id == id) {
            apearFirstView(user)
            return true
        }
        return false
    }

    private fun apearFirstView(user: Usuario?) {
        if (user?.nombre == "") {
            iv_equipo.visibility = View.VISIBLE
            tv_msg.visibility = View.VISIBLE
        }
    }
/*
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPlayers()

        rv_noticias.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )

        equipoAdapter = EquipoRVAdapter(allPlayers as ArrayList<Noticia>)
        rv_noticias.adapter = equipoAdapter
    }

    private fun loadPlayers() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("equipos")
        identifyTeam(myRef)
    }

    private fun identifyTeam(myRef: DatabaseReference) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val equipo = datasnapshot.getValue(Equipo::class.java)
                    isTeam(equipo, )
                    //allPlayers.add(noticia!!)
                }
                equipoAdapter.notifyDataSetChanged()
            }
        }
        myRef.addValueEventListener(postListener)
    }

    private fun isTeam(
        equipo: Equipo?,
        email: String?
    ) : Boolean {
        if (equipo?.id == email) {
            haveData(user)
            return true
        }
        return false
    }*/
}