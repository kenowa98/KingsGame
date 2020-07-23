package com.kenowa.kingsgame.ui.partido

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
import com.kenowa.kingsgame.model.Reserva
import com.kenowa.kingsgame.referenceDatabase
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

        root!!.rv_reservas.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )

        reservaAdapter = ReservasRVAdapter(allReserves as ArrayList<Reserva>, this)
        root!!.rv_reservas.adapter = reservaAdapter

        root!!.bt_crear_partido.setOnClickListener {
            findNavController().navigate(R.id.action_nav_partido_to_nav_mapa)
        }
    }

    private fun hideProgressBar() {
        root!!.progressBar.visibility = View.GONE
    }

    override fun onItemClick(reserva: Reserva) {
    }

    private fun loadReserves() {
        val myRef = referenceDatabase("reservas")
        val userID = FirebaseAuth.getInstance().currentUser?.uid.toString()
        searchReserves(myRef, userID)
        allReserves.clear()
    }

    private fun searchReserves(
        myRef: DatabaseReference,
        userID: String
    ) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                var existReserve = false
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val reserva = datasnapshot.getValue(Reserva::class.java)
                    allReserves.add(reserva!!)
                    existReserve = true
                }
                if (existReserve) {
                    root!!.tv_aviso.visibility = View.GONE
                }
                hideProgressBar()
                reservaAdapter.notifyDataSetChanged()
            }
        }
        myRef.child(userID).addListenerForSingleValueEvent(postListener)
    }
}