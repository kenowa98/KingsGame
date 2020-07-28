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
import com.kenowa.kingsgame.hideProgressBar
import com.kenowa.kingsgame.model.Player
import com.kenowa.kingsgame.model.Usuario
import com.kenowa.kingsgame.referenceDatabase
import com.kenowa.kingsgame.showMessage
import kotlinx.android.synthetic.main.fragment_player_edit.*
import kotlinx.android.synthetic.main.fragment_player_edit.view.*

class PlayerEditFragment : Fragment() {
    private lateinit var idTeam: String
    private lateinit var usuario: Usuario
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
            usuario = safeArgs.user
            root?.tv_nombre?.text = "Usuario: ${usuario.nombre} ${usuario.apellido}"
            identifyPlayer()
        }

        configureButtons()
    }

    private fun configureButtons() {
        val myRef = referenceDatabase("equipos")
        bt_eliminar.setOnClickListener {
            val nombre = "${usuario.nombre} ${usuario.apellido}"
            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage("Desea eliminar a $nombre?")
                    setPositiveButton("aceptar") { _, _ ->
                        deleteUser(nombre, myRef)
                    }
                    setNegativeButton("cancelar") { _, _ -> }
                }
                builder.create()
            }
            alertDialog?.show()
        }
        bt_guardar.setOnClickListener {
            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage("Desea guardar los cambios?")
                    setPositiveButton("aceptar") { _, _ ->
                        saveDataUser(myRef)
                    }
                    setNegativeButton("cancelar") { _, _ -> }
                }
                builder.create()
            }
            alertDialog?.show()
        }
        bt_continuar.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun saveDataUser(myRef: DatabaseReference) {
        myRef.child(idTeam).child(usuario.id!!).child("admin")
            .setValue(root!!.rbt_on.isChecked)
        updateView()
        showMessage(requireContext(), "Guardando cambios...")
    }

    private fun deleteUser(nombre: String, myRef: DatabaseReference) {
        val refUser = referenceDatabase("usuarios")
        val equipos = usuario.equipos - 1
        myRef.child(idTeam).child(usuario.id!!).removeValue()
        refUser.child(usuario.id!!).child("equipos").setValue(equipos)
        showMessage(requireContext(), "Eliminando a $nombre...")
        updateView()
    }

    private fun updateView() {
        root?.tv_nombre?.visibility = View.GONE
        root?.tv1?.visibility = View.GONE
        root?.rOptions?.visibility = View.GONE
        root?.bt_eliminar?.visibility = View.GONE
        root?.bt_guardar?.visibility = View.GONE
        root?.iv_team?.visibility = View.VISIBLE
        root?.bt_continuar?.visibility = View.VISIBLE
    }

    private fun identifyPlayer() {
        idPlayers()
    }

    private fun idPlayers() {
        val myRef = referenceDatabase("equipos")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val player = datasnapshot.getValue(Player::class.java)
                    if (player?.id == usuario.id) {
                        loadAdmin(player)
                        break
                    }
                }
                hideProgressBar(root?.progressBar!!)
            }
        }
        myRef.child(idTeam).addListenerForSingleValueEvent(postListener)
    }

    private fun loadAdmin(player: Player?) {
        if (player?.admin!!) {
            root?.rOptions?.check(R.id.rbt_on)
        } else {
            root?.rOptions?.check(R.id.rbt_off)
        }
    }
}