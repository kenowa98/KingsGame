package com.kenowa.kingsgame.ui.busqueda

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kenowa.kingsgame.*
import com.kenowa.kingsgame.model.Player
import com.kenowa.kingsgame.model.Solicitud
import com.kenowa.kingsgame.model.Usuario
import kotlinx.android.synthetic.main.fragment_reclutar.*
import kotlinx.android.synthetic.main.fragment_reclutar.view.*

class ReclutarFragment : Fragment(), ReclutarRVAdapter.OnItemClickListener {
    private lateinit var idTeam: String
    private var allPlayer: MutableList<Usuario> = mutableListOf()
    private var allID: MutableList<String> = mutableListOf()
    private lateinit var playerAdapter: ReclutarRVAdapter
    private var players = 0
    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_reclutar, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val safeArgs = ReclutarFragmentArgs.fromBundle(it)
            idTeam = safeArgs.team
        }

        loadPlayers()
        configureSpinners()

        root?.rv_players?.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false
        )

        playerAdapter = ReclutarRVAdapter(allPlayer as ArrayList<Usuario>, this)
        root?.rv_players?.adapter = playerAdapter

        bt_filtrar.setOnClickListener {
            root?.progressBar?.let { it1 -> showProgressBar(it1) }
            loadPlayers()
            hideKeyboard()
        }

        root?.sp_comuna?.let { clickItemComuna(it) }
    }

    override fun onItemClick(usuario: Usuario, case: Int) {
        if (case == 1) {
            val action = ReclutarFragmentDirections.actionNavReclutarToNavPerfilView(usuario)
            findNavController().navigate(action)
        } else {
            val nombre = "${usuario.nombre} ${usuario.apellido}"
            val myRef = referenceDatabase("solicitudes")
            val alertDialog: AlertDialog? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage("Desea reclutar a $nombre?")
                    setPositiveButton("aceptar") { _, _ ->
                        val solicitud = Solicitud(id_equipo = idTeam)
                        myRef.child(usuario.id!!).child(idTeam).setValue(solicitud)
                        showMessage(requireContext(), "Solicitud enviada")
                    }
                    setNegativeButton("cancelar") { _, _ -> }
                }
                builder.create()
            }
            alertDialog?.show()
        }
    }

    private fun loadPlayers() {
        allPlayer.clear()
        allID.clear()
        players = 0
        searchMembers()
    }

    private fun searchMembers() {
        val myRef = referenceDatabase("equipos")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val player = datasnapshot.getValue(Player::class.java)
                    allID.add(player?.id!!)
                }
                loadFreePlayers()
            }
        }
        myRef.child(idTeam).addListenerForSingleValueEvent(postListener)
    }

    private fun loadFreePlayers() {
        searchFreePlayers()
    }

    private fun searchFreePlayers() {
        val myRef = referenceDatabase("usuarios")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val user = datasnapshot.getValue(Usuario::class.java)
                    isFree(user)
                }
                if (players == 0) {
                    root?.tv_aviso?.visibility = View.VISIBLE
                } else {
                    root?.tv_aviso?.visibility = View.GONE
                }
                hideProgressBar(root?.progressBar!!)
                playerAdapter.notifyDataSetChanged()
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isFree(user: Usuario?) {
        if (!allID.contains(user?.id)) {
            haveData(user)
        }
    }

    private fun haveData(user: Usuario?) {
        if (user?.nombre != "") {
            limitTeams(user)
        }
    }

    private fun limitTeams(user: Usuario?) {
        if (user?.equipos!! < 2) {
            havePosicion(user)
        }
    }

    private fun havePosicion(user: Usuario?) {
        if (root?.sp_posicion?.selectedItem.toString() == "Seleccione una posición") {
            haveComuna(user)
        } else {
            if (user?.posicion == root?.sp_posicion?.selectedItem.toString()) {
                haveComuna(user)
            }
        }
    }

    private fun haveComuna(user: Usuario?) {
        if (root?.sp_comuna?.selectedItem.toString() == "Seleccione una comuna") {
            haveEdadMin(user)
        } else {
            if (user?.comuna == root?.sp_comuna?.selectedItem.toString()) {
                enableBarrio(user)
            }
        }
    }

    private fun enableBarrio(user: Usuario?) {
        if (root?.rbt_si?.isChecked!!) {
            haveBarrio(user)
        } else {
            haveEdadMin(user)
        }
    }

    private fun haveBarrio(user: Usuario?) {
        if (user?.barrio == root?.sp_barrio?.selectedItem.toString()) {
            haveEdadMin(user)
        }
    }

    private fun haveEdadMin(user: Usuario?) {
        val age = user?.fecha?.let { getAge(it) }?.toInt()
        if (et_edadMin.text.toString().isEmpty()) {
            if (age != null) {
                haveEdadMax(user, age)
            }
        } else {
            if (age != null) {
                if (age >= et_edadMin.text.toString().toInt()) {
                    haveEdadMax(user, age)
                }
            }
        }
    }

    private fun haveEdadMax(
        user: Usuario?,
        age: Int
    ) {
        if (et_edadMax.text.toString().isEmpty()) {
            addUser(user)
        } else {
            if (age <= et_edadMax.text.toString().toInt()) {
                addUser(user)
            }
        }
    }

    private fun addUser(user: Usuario?) {
        if (user != null) {
            allPlayer.add(user)
            ++players
        }
    }

    private fun configureSpinners() {
        visualizeSpinner(
            root?.sp_posicion!!,
            R.array.reclutar_posiciones,
            requireContext()
        )
        visualizeSpinner(
            root?.sp_comuna!!,
            R.array.reclutar_comunas,
            requireContext()
        )
    }

    private fun clickItemComuna(spComuna: Spinner) {
        spComuna.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                pos: Int,
                id: Long
            ) {
                val item = parent.getItemAtPosition(pos)
                spinnerBarrio(item)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun spinnerBarrio(item: Any?) {
        if (item != "Seleccione una comuna" && item != "Otro") {
            root?.sp_barrio?.visibility = View.VISIBLE
            root?.tv_msg?.visibility = View.VISIBLE
            root?.rOptions?.visibility = View.VISIBLE
            identifyListBarrio(item)
        } else {
            root?.sp_barrio?.visibility = View.GONE
            root?.tv_msg?.visibility = View.GONE
            root?.rOptions?.visibility = View.GONE
        }
    }

    private fun identifyListBarrio(item: Any?) {
        var barrio = 0
        when (item) {
            "Aranjuez" -> {
                barrio = R.array.Aranjuez
            }
            "Belén" -> {
                barrio = R.array.Belen
            }
            "Buenos Aires" -> {
                barrio = R.array.BuenosAires
            }
            "Castilla" -> {
                barrio = R.array.Castilla
            }
            "Doce de Octubre" -> {
                barrio = R.array.DoceDeOctubre
            }
            "El Poblado" -> {
                barrio = R.array.ElPoblado
            }
            "Guayabal" -> {
                barrio = R.array.Guayabal
            }
            "La América" -> {
                barrio = R.array.LaAmerica
            }
            "La Candelaria" -> {
                barrio = R.array.LaCandelaria
            }
            "Laureles-Estadio" -> {
                barrio = R.array.Laureles
            }
            "Manrique" -> {
                barrio = R.array.Manrique
            }
            "Popular" -> {
                barrio = R.array.Popular
            }
            "Robledo" -> {
                barrio = R.array.Robledo
            }
            "San Javier" -> {
                barrio = R.array.SanJavier
            }
            "Santa Cruz" -> {
                barrio = R.array.SantaCruz
            }
            "Villa Hermosa" -> {
                barrio = R.array.VillaHermosa
            }
        }
        visualizeSpinner(root?.sp_barrio!!, barrio, requireContext())
    }
}