package com.kenowa.kingsgame.ui.perfil

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.getAge
import com.kenowa.kingsgame.model.Usuario
import com.kenowa.kingsgame.showMessage
import kotlinx.android.synthetic.main.fragment_perfil_registro.*
import java.util.*
import kotlin.collections.set


class PerfilRegistroFragment : Fragment() {
    private lateinit var fecha: String
    private var cal = Calendar.getInstance()
    private lateinit var user: Usuario
    private var check = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perfil_registro, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sp_barrio.visibility = View.GONE
        iv_team.visibility = View.GONE
        bt_comenzar.visibility = View.GONE

        appearBarrio(view.findViewById(R.id.sp_comuna))

        visualizeSpinner(view.findViewById(R.id.sp_lugarNacimiento), R.array.lista_ciudades)
        visualizeSpinner(view.findViewById(R.id.sp_comuna), R.array.lista_comunas)
        visualizeSpinner(view.findViewById(R.id.sp_posicion), R.array.lista_posiciones)

        update(false)

        ibt_calendario.setOnClickListener { saveDate() }
        bt_crear.setOnClickListener { update(true) }
        bt_actualizar.setOnClickListener { update(true) }
        bt_comenzar.setOnClickListener {
            findNavController().navigate(R.id.action_nav_perfil_registro_to_nav_perfil)
        }
    }

    private fun update(click: Boolean) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("usuarios")
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val actual: FirebaseUser? = mAuth.currentUser
        val email = actual?.email
        identifyUser(email, myRef, click)
    }

    private fun identifyUser(
        email: String?,
        myRef: DatabaseReference, click: Boolean
    ) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    user = datasnapshot.getValue(Usuario::class.java)!!
                    isUser(email, myRef, click)
                }
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isUser(
        email: String?,
        myRef: DatabaseReference,
        click: Boolean
    ) {
        if (user.correo == email) {
            isClickedButton(myRef, click)
        }
    }

    private fun isClickedButton(
        myRef: DatabaseReference,
        click: Boolean
    ) {
        if (click) {
            completeData(myRef)
        } else {
            isNewUser()
        }
    }

    private fun completeData(myRef: DatabaseReference) {
        if (et_nombre.text.toString().isEmpty() ||
            et_apellido.text.toString().isEmpty() ||
            et_celular.text.toString().isEmpty() ||
            tv_fechaNacimiento.text.toString().isEmpty() ||
            sp_lugarNacimiento.selectedItem.toString() == "Seleccione una ciudad" ||
            sp_comuna.selectedItem.toString() == "Seleccione la comuna donde vive" ||
            sp_posicion.selectedItem.toString() == "Seleccione su posición habitual"
        ) {
            showMessage(requireContext(), "Faltan datos")
        } else {
            correctCellphone(myRef)
        }
    }

    private fun correctCellphone(myRef: DatabaseReference) {
        if (et_celular.text.toString().length != 10) {
            showMessage(requireContext(), "El celular debe tener 10 dígitos!")
        } else {
            validateAge(myRef)
        }
    }

    private fun validateAge(myRef: DatabaseReference) {
        val age = loadAge()
        if (age != null) {
            if (age < 15) {
                showMessage(requireContext(), "Debes tener más de 15 años")
            } else {
                updatePerfil(myRef)
                disappearAll()
            }
        }
    }

    private fun isNewUser() {
        if (user.nombre == "") {
            bt_crear.visibility = View.VISIBLE
            bt_actualizar.visibility = View.GONE
            ++check
        } else {
            et_nombre.setText(user.nombre)
            et_apellido.setText(user.apellido)
            et_celular.setText(user.celular)
            tv_fechaNacimiento.text = user.fecha
            loadDataInSpinner(R.array.lista_ciudades, sp_lugarNacimiento, user.origen)
            loadDataInSpinner(R.array.lista_comunas, sp_comuna, user.comuna)
            selectBarrio(user.comuna)
            loadDataInSpinner(R.array.lista_posiciones, sp_posicion, user.posicion)
            loadGender()
            bt_crear.visibility = View.GONE
            bt_actualizar.visibility = View.VISIBLE
        }
    }

    private fun loadGender() {
        if (user.genero) {
            rSex.check(R.id.rbt_femenino)
        } else {
            rSex.check(R.id.rbt_masculino)
        }
    }

    private fun loadDataInSpinner(
        lista: Int,
        spinner: Spinner,
        compareValue: String
    ) {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            lista,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter
        val spinnerPosition = adapter.getPosition(compareValue)
        spinner.setSelection(spinnerPosition)
    }

    private fun updatePerfil(myRef: DatabaseReference) {
        val childUpdate = HashMap<String, Any>()
        childUpdate["nombre"] = et_nombre.text.toString()
        childUpdate["apellido"] = et_apellido.text.toString()
        childUpdate["celular"] = et_celular.text.toString()
        childUpdate["fecha"] = tv_fechaNacimiento.text.toString()
        childUpdate["genero"] = !rbt_masculino.isChecked
        childUpdate["origen"] = sp_lugarNacimiento.selectedItem.toString()
        childUpdate["comuna"] = sp_comuna.selectedItem.toString()
        if (sp_comuna.selectedItem.toString() != "Otro") {
            childUpdate["barrio"] = sp_barrio.selectedItem.toString()
        }
        childUpdate["posicion"] = sp_posicion.selectedItem.toString()
        user.id?.let { myRef.child(it).updateChildren(childUpdate) }
        context?.let { showMessage(it, "Cargando datos") }
    }

    private fun appearBarrio(spComuna: Spinner) {
        spComuna.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                pos: Int,
                id: Long
            ) {
                if (++check > 2) {
                    val item = parent.getItemAtPosition(pos)
                    selectBarrio(item)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun selectBarrio(item: Any?) {
        if (item != "Seleccione la comuna donde vive" && item != "Otro") {
            sp_barrio.visibility = View.VISIBLE
            displayBarrios(item)
        } else {
            sp_barrio.visibility = View.GONE
        }
    }

    private fun displayBarrios(item: Any?) {
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
        viewBarrio(barrio)
    }

    private fun viewBarrio(barrio: Int) {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            barrio,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        updateBarrio(adapter)
    }

    private fun updateBarrio(adapter: ArrayAdapter<CharSequence>) {
        sp_barrio.adapter = adapter
        if (user.barrio != "") {
            val compareValue = user.barrio
            val spinnerPosition = adapter.getPosition(compareValue)
            sp_barrio.setSelection(spinnerPosition)
        }
    }

    private fun visualizeSpinner(
        spinner: Spinner,
        lista: Int
    ) {
        ArrayAdapter.createFromResource(
            requireContext(),
            lista,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun saveDate() {
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val format = "yyyy-MM-dd"
                val simpleDateFormat = SimpleDateFormat(format, Locale.US)
                fecha = simpleDateFormat.format(cal.time).toString()
                tv_fechaNacimiento.text = fecha
            }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadAge(): Int? {
        val fecha = tv_fechaNacimiento.text.toString()
        val year = fecha.substring(0, 4).toInt()
        val month = fecha.substring(5, 7).toInt()
        val day = fecha.substring(8, 9).toInt()
        return getAge(year, month, day)?.toInt()
    }

    private fun disappearAll() {
        iv_team.visibility = View.VISIBLE
        bt_comenzar.visibility = View.VISIBLE
        et_nombre.visibility = View.GONE
        et_apellido.visibility = View.GONE
        et_celular.visibility = View.GONE
        linear1.visibility = View.GONE
        linear2.visibility = View.GONE
        tv1.visibility = View.GONE
        tv2.visibility = View.GONE
        tv3.visibility = View.GONE
        tv4.visibility = View.GONE
        tv5.visibility = View.GONE
        tv6.visibility = View.GONE
        tv7.visibility = View.GONE
        tv_fechaNacimiento.visibility = View.GONE
        ibt_calendario.visibility = View.GONE
        ibt_foto.visibility = View.GONE
        rSex.visibility = View.GONE
        rbt_femenino.visibility = View.GONE
        rbt_masculino.visibility = View.GONE
        sp_lugarNacimiento.visibility = View.GONE
        sp_comuna.visibility = View.GONE
        sp_barrio.visibility = View.GONE
        sp_posicion.visibility = View.GONE
        bt_crear.visibility = View.GONE
        bt_actualizar.visibility = View.GONE
    }
}