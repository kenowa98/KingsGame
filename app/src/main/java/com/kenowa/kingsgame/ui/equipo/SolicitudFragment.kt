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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.model.Player
import com.kenowa.kingsgame.model.Solicitud
import com.kenowa.kingsgame.referenceDatabase
import com.kenowa.kingsgame.showMessage
import kotlinx.android.synthetic.main.fragment_solicitud.view.*

class SolicitudFragment : Fragment(), SolicitudRVAdapter.OnItemClickListener {
    private var allRequest: MutableList<Solicitud> = mutableListOf()
    private lateinit var solicitudAdapter: SolicitudRVAdapter

    private var numTeams: Int = 0

    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_solicitud, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val safeArgs = SolicitudFragmentArgs.fromBundle(it)
            numTeams = safeArgs.teams
        }

        loadRequest()

        root!!.rv_solicitudes.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )

        solicitudAdapter = SolicitudRVAdapter(allRequest as ArrayList<Solicitud>, this)
        root!!.rv_solicitudes.adapter = solicitudAdapter
    }

    private fun hideProgressBar() {
        root!!.progressBar.visibility = View.GONE
    }

    override fun onItemClick(request: Solicitud, case: Int) {
        when (case) {
            1 -> {
                val action =
                    SolicitudFragmentDirections.actionNavSolicitudToNavEquipoView(request.id_equipo)
                findNavController().navigate(action)
            }
            2 -> {
                if (numTeams >= 2) {
                    showMessage(requireContext(), "Ya no puedes unirte a más equipos")
                } else {
                    val refEquipo = referenceDatabase("equipos")
                    val refSolicitud = referenceDatabase("solicitudes")
                    val userID = FirebaseAuth.getInstance().currentUser?.uid.toString()
                    val nombreTeam = request.id_equipo
                    val player = Player(
                        admin = false,
                        id = userID,
                        nombre = nombreTeam
                    )
                    ++numTeams
                    refEquipo.child(nombreTeam).child(userID).setValue(player)
                    refSolicitud.child(userID).child(request.id_equipo).removeValue()
                    loadRequest()
                }
            }
            3 -> {
                val myRef = referenceDatabase("solicitudes")
                val userID = FirebaseAuth.getInstance().currentUser?.uid.toString()
                myRef.child(userID).child(request.id_equipo).removeValue()
                loadRequest()
            }
        }
    }

    private fun loadRequest() {
        val myRef = referenceDatabase("solicitudes")
        allRequest.clear()
        searchRequest(myRef)
    }

    private fun searchRequest(myRef: DatabaseReference) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                var cont = 0
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    ++cont
                    val solicitud = datasnapshot.getValue(Solicitud::class.java)
                    allRequest.add(solicitud!!)
                }
                if (cont == 0) {
                    root!!.tv_aviso.visibility = View.VISIBLE
                }
                hideProgressBar()
                solicitudAdapter.notifyDataSetChanged()
            }
        }
        myRef.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .addListenerForSingleValueEvent(postListener)
    }
}