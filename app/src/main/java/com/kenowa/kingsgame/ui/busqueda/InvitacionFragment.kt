package com.kenowa.kingsgame.ui.busqueda

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
import com.google.firebase.database.ValueEventListener
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.hideProgressBar
import com.kenowa.kingsgame.isUser
import com.kenowa.kingsgame.model.Reserva
import com.kenowa.kingsgame.model.Usuario
import com.kenowa.kingsgame.referenceDatabase
import com.kenowa.kingsgame.ui.equipo.EquipoRVAdapter
import kotlinx.android.synthetic.main.fragment_invitacion.view.*
import java.util.*

class InvitacionFragment : Fragment(), EquipoRVAdapter.OnItemClickListener {
    private var allConfirmado: MutableList<Usuario> = mutableListOf()
    private var allID: MutableList<String> = mutableListOf()
    private lateinit var confirmadoAdapter: EquipoRVAdapter
    private lateinit var reserva: Reserva
    private var numConfirmado = 0
    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_invitacion, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val safeArgs = InvitacionFragmentArgs.fromBundle(it)
            reserva = safeArgs.reserve
        }

        loadConfirmados()

        root?.rv_confirmado?.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false
        )

        val item = R.layout.item_player

        confirmadoAdapter = EquipoRVAdapter(allConfirmado as ArrayList<Usuario>, item, this)
        root?.rv_confirmado?.adapter = confirmadoAdapter
    }

    override fun onItemClick(user: Usuario) {
        val action =
            InvitacionFragmentDirections.actionNavInvitacionToNavPerfilView(user)
        findNavController().navigate(action)
    }

    private fun loadConfirmados() {
        allID.clear()
        allConfirmado.clear()
        numConfirmado = 0
        idPlayers()
    }

    private fun idPlayers() {
        val myRef = referenceDatabase("reservas")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val reserve = datasnapshot.getValue(Reserva::class.java)
                    if (reserve != null) {
                        allID.add(reserve.idUser)
                        ++numConfirmado
                    }
                }
                searchUser()
            }
        }
        reserva.id?.let { myRef.child(it).addValueEventListener(postListener) }
    }

    @SuppressLint("SetTextI18n")
    private fun searchUser() {
        for (id in allID) {
            identifyUser(id)
        }
        root?.tv_confirmados?.text = "Hay $numConfirmado persona(s) confirmada(s)"
        root?.progressBar?.let { hideProgressBar(it) }
    }

    private fun identifyUser(id: String) {
        val myRef = referenceDatabase("usuarios")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val user = datasnapshot.getValue(Usuario::class.java)
                    if (isUser(user?.id, id)) {
                        if (user != null) {
                            allConfirmado.add(user)
                        }
                        break
                    }
                }
                confirmadoAdapter.notifyDataSetChanged()
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }
}