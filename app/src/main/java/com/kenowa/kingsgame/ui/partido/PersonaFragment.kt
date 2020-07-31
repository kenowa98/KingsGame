package com.kenowa.kingsgame.ui.partido

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
import com.google.firebase.database.ValueEventListener
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.hideProgressBar
import com.kenowa.kingsgame.isUser
import com.kenowa.kingsgame.model.Reserva
import com.kenowa.kingsgame.model.Usuario
import com.kenowa.kingsgame.referenceDatabase
import kotlinx.android.synthetic.main.fragment_persona.view.*

class PersonaFragment : Fragment(), PersonaRVAdapter.OnItemClickListener {
    private var allRequest: MutableList<Reserva> = mutableListOf()
    private lateinit var solicitudAdapter: PersonaRVAdapter
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

        loadRequest()

        root?.rv_solicitudes?.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )

        solicitudAdapter = PersonaRVAdapter(allRequest as ArrayList<Reserva>, this)
        root?.rv_solicitudes?.adapter = solicitudAdapter
    }

    override fun onItemClick(invitation: Reserva, case: Int) {
        when (case) {
            1 -> {
                searchUser(invitation)
            }
            2 -> {
                val alertDialog: AlertDialog? = activity?.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setMessage("Desea aceptar esta invitación?")
                        setPositiveButton("Sí") { _, _ ->
                            savePartido(invitation)
                        }
                        setNegativeButton("No") { _, _ -> }
                    }
                    builder.create()
                }
                alertDialog?.show()
            }
            3 -> {
                val alertDialog: AlertDialog? = activity?.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setMessage("Desea rechazar la invitación?")
                        setPositiveButton("Sí") { _, _ ->
                            deleteRequest(invitation)
                        }
                        setNegativeButton("No") { _, _ -> }
                    }
                    builder.create()
                }
                alertDialog?.show()
            }
        }
    }

    private fun deleteRequest(request: Reserva) {
        val myRef = referenceDatabase("invitaciones")
        val userID = FirebaseAuth.getInstance().currentUser?.uid.toString()
        request.id?.let { myRef.child(userID).child(it).removeValue() }
        loadRequest()
    }

    private fun savePartido(request: Reserva) {
        val refReserva = referenceDatabase("reservas")
        val refInvitacion = referenceDatabase("invitaciones")
        val userID = FirebaseAuth.getInstance().currentUser?.uid.toString()
        request.id?.let { refReserva.child(it).child(userID).setValue(request) }
        request.id?.let { refReserva.child(it).child(userID).child("idUser").setValue(userID) }
        request.id?.let { refReserva.child(it).child(userID).child("admin").setValue(false) }
        request.id?.let { refInvitacion.child(userID).child(it).removeValue() }
        loadRequest()
    }

    private fun searchUser(request: Reserva) {
        val myRef = referenceDatabase("usuarios")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val userID = request.idUser
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val user = datasnapshot.getValue(Usuario::class.java)
                    if (isUser(user?.id, userID)) {
                        if (user != null) {
                            val action =
                                PersonaFragmentDirections.actionNavPersonaToNavPerfilView(user)
                            findNavController().navigate(action)
                        }
                        break
                    }
                }
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun loadRequest() {
        allRequest.clear()
        searchRequest()
    }

    private fun searchRequest() {
        val myRef = referenceDatabase("invitaciones")
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val solicitud = datasnapshot.getValue(Reserva::class.java)
                    allRequest.add(solicitud!!)
                }
                hideProgressBar(root?.progressBar!!)
                solicitudAdapter.notifyDataSetChanged()
            }
        }
        if (userID != null) {
            myRef.child(userID).addListenerForSingleValueEvent(postListener)
        }
    }
}