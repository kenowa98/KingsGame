package com.kenowa.kingsgame.ui.equipo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.model.Player
import com.kenowa.kingsgame.model.Usuario
import com.kenowa.kingsgame.referenceDatabase
import com.kenowa.kingsgame.showMessage
import kotlinx.android.synthetic.main.fragment_player_edit.view.*

class PlayerEditFragment : Fragment() {
    private lateinit var idTeam: String
    private lateinit var user: Usuario
    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_player_edit, container, false)
        return root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val safeArgs = PlayerEditFragmentArgs.fromBundle(it)
            idTeam = safeArgs.team
            user = safeArgs.usuario
            root!!.tv_nombre.text = "Usuario: ${user.nombre} ${user.apellido}"
            identifyPlayer()
        }

        configureButtons()
    }

    private fun configureButtons() {
        val myRef = referenceDatabase("equipos")
        root!!.bt_eliminar.setOnClickListener {
            val nombre = "${user.nombre} ${user.apellido}"
            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage("Desea eliminar a $nombre?")
                    setPositiveButton("aceptar") { _, _ ->
                        myRef.child(idTeam).child(user.id!!).removeValue()
                        updateView()
                        showMessage(requireContext(), "Eliminando a $nombre...")
                    }
                    setNegativeButton("cancelar") { _, _ -> }
                }
                builder.create()
            }
            alertDialog?.show()
        }
        root!!.bt_guardar.setOnClickListener {
            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage("Desea guardar los cambios?")
                    setPositiveButton("aceptar") { _, _ ->
                        myRef.child(idTeam).child(user.id!!).child("admin")
                            .setValue(root!!.rbt_on.isChecked)
                        updateView()
                        showMessage(requireContext(), "Guardando cambios...")
                    }
                    setNegativeButton("cancelar") { _, _ -> }
                }
                builder.create()
            }
            alertDialog?.show()
        }
        root!!.bt_continuar.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun updateView() {
        root!!.tv_nombre.visibility = View.GONE
        root!!.tv1.visibility = View.GONE
        root!!.rOptions.visibility = View.GONE
        root!!.bt_eliminar.visibility = View.GONE
        root!!.bt_guardar.visibility = View.GONE
        root!!.iv_team.visibility = View.VISIBLE
        root!!.bt_continuar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        root!!.progressBar.visibility = View.GONE
    }

    private fun identifyPlayer() {
        val myRef = referenceDatabase("equipos")
        idPlayers(myRef)
    }

    private fun idPlayers(myRef: DatabaseReference) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val player = datasnapshot.getValue(Player::class.java)
                    if (player?.id == user.id) {
                        loadAdmin(player)
                        break
                    }
                }
                hideProgressBar()
            }
        }
        myRef.child(idTeam).addListenerForSingleValueEvent(postListener)
    }

    private fun loadAdmin(player: Player?) {
        if (player?.admin!!) {
            root!!.rOptions.check(R.id.rbt_on)
        } else {
            root!!.rOptions.check(R.id.rbt_off)
        }
    }
}