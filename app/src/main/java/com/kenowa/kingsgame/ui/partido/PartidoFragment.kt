package com.kenowa.kingsgame.ui.partido

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kenowa.kingsgame.R

class PartidoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_partido, container, false)
    }

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bt_tu_reserva.setOnClickListener {
            enableOption()
        }
    }

    private fun enableOption() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("usuarios")
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val actual: FirebaseUser? = mAuth.currentUser
        val email = actual?.email
        identifyUser(email, myRef)
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

    private fun isUser(
        user: Usuario?,
        email: String?
    ): Boolean {
        if (user?.correo == email) {
            haveData(user)
            return true
        }
        return false
    }

    private fun haveData(user: Usuario?) {
        if (user?.nombre != "") {
            findNavController().navigate(R.id.action_nav_partido_to_nav_tu_reserva)
        } else {
            showMessage(requireContext(), "Necesitas un perfil")
        }
    }*/
}