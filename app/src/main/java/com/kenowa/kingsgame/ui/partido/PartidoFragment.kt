package com.kenowa.kingsgame.ui.partido

import android.annotation.SuppressLint
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
import com.google.firebase.database.ValueEventListener
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.hideProgressBar
import com.kenowa.kingsgame.model.Reserva
import com.kenowa.kingsgame.referenceDatabase
import kotlinx.android.synthetic.main.fragment_partido.*
import kotlinx.android.synthetic.main.fragment_partido.view.*

class PartidoFragment : Fragment(), ReservasRVAdapter.OnItemClickListener {
    private var allReserves: MutableList<Reserva> = mutableListOf()
    private lateinit var reservaAdapter: ReservasRVAdapter
    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_partido, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadReserves()
        searchInvitations()
        configureButtons()

        root?.rv_reservas?.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )

        reservaAdapter = ReservasRVAdapter(allReserves as ArrayList<Reserva>, this)
        root?.rv_reservas?.adapter = reservaAdapter
    }

    override fun onItemClick(reserva: Reserva) {
        val action = PartidoFragmentDirections.actionNavPartidoToNavInvitacion(reserva)
        findNavController().navigate(action)
    }

    private fun configureButtons() {
        bt_crear_partido.setOnClickListener {
            findNavController().navigate(R.id.action_nav_partido_to_nav_mapa)
        }

        bt_mensaje_invitacion.setOnClickListener {
            findNavController().navigate(R.id.action_nav_partido_to_nav_persona)
        }
    }

    private fun searchInvitations() {
        val myRef = referenceDatabase("invitaciones")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                var solicitudes = 0
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    ++solicitudes
                }
                if (solicitudes != 0) {
                    root?.bt_mensaje_invitacion?.visibility = View.VISIBLE
                    if (solicitudes == 1) {
                        root?.bt_mensaje_invitacion?.text = "Tienes 1 invitación"
                    } else {
                        root?.bt_mensaje_invitacion?.text = "Tienes $solicitudes invitaciones"
                    }
                }
            }
        }
        myRef.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .addListenerForSingleValueEvent(postListener)
    }

    private fun loadReserves() {
        allReserves.clear()
        searchReserves()
    }

    private fun searchReserves() {
        val myRef = referenceDatabase("reservas")
        val userID = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                var existReserve = false
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val reserva = datasnapshot.value as Map<*, *>
                    if (isUserInReserva(reserva, userID)) {
                        existReserve = true
                    }
                }
                if (existReserve) {
                    root?.tv_aviso?.visibility = View.GONE
                } else {
                    root?.tv_aviso?.visibility = View.VISIBLE
                }
                root?.progressBar?.let { hideProgressBar(it) }
                reservaAdapter.notifyDataSetChanged()
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isUserInReserva(
        reserva: Map<*, *>,
        user: String
    ): Boolean {
        val keys = reserva.keys
        for (i in keys) {
            if (i == user) {
                val data = reserva[i] as Map<*, *>
                val userReserva = Reserva(
                    data["id"] as String?,
                    data["idUser"] as String,
                    data["idCancha"] as String,
                    data["idLugar"] as String,
                    data["fecha"] as String,
                    data["inicioHora"].toString().toInt(),
                    data["finHora"].toString().toInt(),
                    data["precio"].toString().toInt(),
                    data["admin"] as Boolean
                )
                allReserves.add(userReserva)
                return true
            }
        }
        return false
    }
}