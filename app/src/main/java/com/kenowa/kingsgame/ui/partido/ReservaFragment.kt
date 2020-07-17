package com.kenowa.kingsgame.ui.partido

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.model.Reserva
import com.kenowa.kingsgame.model.Usuario
import kotlinx.android.synthetic.main.fragment_reserva.*

@Suppress("DEPRECATION")
class ReservaFragment : Fragment() {
    private var allReserves: MutableList<Reserva> = mutableListOf()
    private lateinit var reservasAdapter: ReservasRVAdapter

    private var isStarted = false
    private var progressStatus = 0
    private var handler: Handler? = null

    private lateinit var reservaIDPlayer: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reserva, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadData("usuarios")

        runProgressBar()

        rv_reservas.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )

        reservasAdapter = ReservasRVAdapter(allReserves as ArrayList<Reserva>)
        rv_reservas.adapter = reservasAdapter
    }

    private fun runProgressBar() {
        handler = Handler(Handler.Callback {
            if (isStarted) {
                progressStatus++
            }
            handler?.sendEmptyMessageDelayed(0, 100)

            true
        })
        handler?.sendEmptyMessage(0)
    }

    private fun loadData(nameDataBase: String) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(nameDataBase)

        if (nameDataBase == "usuarios") {
            val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
            val actual: FirebaseUser? = mAuth.currentUser
            val email = actual?.email
            identifyUser(email, myRef)
        } else {
            identifyReserves(myRef)
        }
    }

    private fun identifyUser(
        email: String?,
        myRef: DatabaseReference
    ) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val user = datasnapshot.getValue(Usuario::class.java)
                    if (isUser(user, email)) {
                        break
                    }
                }
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun identifyReserves(myRef: DatabaseReference) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val reserva = datasnapshot.getValue(Reserva::class.java)
                    isReserva(reserva)
                }
                reservasAdapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isUser(
        user: Usuario?,
        email: String?
    ): Boolean {
        if (user?.correo == email) {
            reservaIDPlayer = user?.id.toString()
            loadData("reservas")
            return true
        }
        return false
    }

    private fun isReserva(reserva: Reserva?) {
        if (reserva?.idPlayer == reservaIDPlayer) {
            allReserves.add(reserva)
        }
    }
}