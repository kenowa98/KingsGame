package com.kenowa.kingsgame.ui.perfil

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kenowa.kingsgame.*
import com.kenowa.kingsgame.model.Usuario
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_perfil_registro.*
import kotlinx.android.synthetic.main.fragment_perfil_registro.view.*
import java.util.*
import kotlin.collections.set

@Suppress("DEPRECATION")
class PerfilRegistroFragment : Fragment() {
    private lateinit var usuario: Usuario
    private var flagFoto = false
    private var checkSpinner = 0
    private val requestImageCapture = 1234
    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_perfil_registro, container, false)
        return root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val safeArgs = PerfilRegistroFragmentArgs.fromBundle(it)
            usuario = safeArgs.user
            existData()
        }

        clickItemComuna(root?.sp_comuna!!)
        configureButtons()
    }

    private fun existData() {
        if (usuario.nombre.isEmpty()) {
            searchUser()
        } else {
            clickButton(false)
        }
    }

    private fun configureSpinners() {
        visualizeSpinner(
            root?.sp_lugarNacimiento!!,
            R.array.lista_ciudades,
            requireContext()
        )
        visualizeSpinner(
            root?.sp_comuna!!,
            R.array.lista_comunas,
            requireContext()
        )
        visualizeSpinner(
            root?.sp_posicion!!,
            R.array.lista_posiciones,
            requireContext()
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun configureButtons() {
        ibt_calendario.setOnClickListener { saveDate(tv_fechaNacimiento, null, requireContext()) }
        bt_actualizar.setOnClickListener { clickButton(true) }
        ibt_foto.setOnClickListener { dispatchTakePictureIntent() }
        bt_comenzar.setOnClickListener {
            findNavController().navigate(R.id.action_nav_perfil_registro_to_nav_perfil)
        }
    }

    private fun searchUser() {
        val myRef = referenceDatabase("usuarios")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val userID = FirebaseAuth.getInstance().currentUser?.uid
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val user = datasnapshot.getValue(Usuario::class.java)
                    if (isUser(user?.id, userID)) {
                        if (user != null) {
                            usuario = user
                            clickButton(false)
                        }
                        break
                    }
                }
                hideProgressBar(root?.progressBar!!)
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun clickButton(click: Boolean) {
        if (click) {
            isCompleteData()
        } else {
            isNewUser()
        }
    }

    private fun isCompleteData() {
        if (et_nombre.text.toString().isEmpty() ||
            et_apellido.text.toString().isEmpty() ||
            et_celular.text.toString().isEmpty() ||
            tv_fechaNacimiento.text.toString().isEmpty() ||
            sp_lugarNacimiento.selectedItem.toString() == "Seleccione una ciudad" ||
            sp_comuna.selectedItem.toString() == "Seleccione la comuna donde vive" ||
            sp_posicion.selectedItem.toString() == "Seleccione su posición habitual" ||
            !flagFoto
        ) {
            showMessage(requireContext(), "Faltan datos")
        } else {
            isCorrectPhone()
        }
    }

    private fun isCorrectPhone() {
        if (et_celular.text.toString().length != 10) {
            showMessage(requireContext(), "El celular debe tener 10 dígitos!")
        } else {
            validateAge()
        }
    }

    private fun validateAge() {
        val age = getAge(tv_fechaNacimiento.text.toString())?.toInt()
        if (age != null) {
            when {
                age < 15 -> {
                    showMessage(requireContext(), "Debes tener más de 15 años")
                }
                age > 80 -> {
                    showMessage(requireContext(), "Máximo hasta 80 años")
                }
                else -> {
                    updatePerfil()
                    disappearAll()
                }
            }
        }
    }

    private fun updatePerfil() {
        val myRef = referenceDatabase("usuarios")
        val childUpdate = HashMap<String, Any>()
        val nombre = rewriteData(root?.et_nombre?.text.toString())
        val apellido = rewriteData(root?.et_apellido?.text.toString())
        childUpdate["nombre"] = nombre
        childUpdate["apellido"] = apellido
        childUpdate["celular"] = root?.et_celular?.text.toString()
        childUpdate["fecha"] = root?.tv_fechaNacimiento?.text.toString()
        childUpdate["genero"] = !root?.rbt_masculino?.isChecked!!
        childUpdate["origen"] = root?.sp_lugarNacimiento?.selectedItem.toString()
        childUpdate["comuna"] = root?.sp_comuna?.selectedItem.toString()
        if (root?.sp_comuna?.selectedItem.toString() != "Otro") {
            childUpdate["barrio"] = root?.sp_barrio?.selectedItem.toString()
        }
        childUpdate["posicion"] = root?.sp_posicion?.selectedItem.toString()
        usuario.id?.let { myRef.child(it).updateChildren(childUpdate) }
        context?.let { showMessage(it, "Cargando datos...") }
    }

    private fun rewriteData(data: String): String {
        var first = true
        var new = ""
        for (i in data) {
            if (first) {
                new += i.toUpperCase()
                first = false
            } else {
                if (i == ' ') {
                    new += i
                    first = true
                } else {
                    new += i.toLowerCase()
                }
            }
        }
        return new
    }

    @SuppressLint("SetTextI18n")
    private fun isNewUser() {
        if (usuario.nombre == "") {
            root?.bt_actualizar?.text = "CREAR"
            configureSpinners()
            ++checkSpinner
        } else {
            root?.bt_actualizar?.text = "ACTUALIZAR"
            root?.et_nombre?.setText(usuario.nombre)
            root?.et_apellido?.setText(usuario.apellido)
            root?.et_celular?.setText(usuario.celular)
            root?.tv_fechaNacimiento?.text = usuario.fecha
            loadDataInSpinner(R.array.lista_ciudades, root?.sp_lugarNacimiento!!, usuario.origen)
            loadDataInSpinner(R.array.lista_comunas, root?.sp_comuna!!, usuario.comuna)
            loadDataInSpinner(R.array.lista_posiciones, root?.sp_posicion!!, usuario.posicion)
            loadGender()
            loadPhoto()
            spinnerBarrio(usuario.comuna)
            flagFoto = true
        }
        hideProgressBar(root?.progressBar!!)
    }

    private fun loadDataInSpinner(
        lista: Int,
        spinner: Spinner,
        compareValue: String
    ) {
        val adapter = visualizeSpinner(spinner, lista, requireContext())
        val spinnerPosition = adapter.getPosition(compareValue)
        spinner.setSelection(spinnerPosition)
    }

    private fun loadGender() {
        if (usuario.genero) {
            root?.rSex?.check(R.id.rbt_femenino)
        } else {
            root?.rSex?.check(R.id.rbt_masculino)
        }
    }

    private fun loadPhoto() {
        if (usuario.foto.isNotEmpty()) {
            Picasso.get().load(usuario.foto).into(root?.ibt_foto)
        }
    }

    private fun clickItemComuna(spComuna: Spinner) {
        spComuna.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                pos: Int,
                id: Long
            ) {
                if (++checkSpinner > 1) {
                    val item = parent.getItemAtPosition(pos)
                    spinnerBarrio(item)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun spinnerBarrio(item: Any?) {
        if (item != "Seleccione la comuna donde vive" && item != "Otro") {
            root?.sp_barrio?.visibility = View.VISIBLE
            identifyListBarrio(item)
        } else {
            root?.sp_barrio?.visibility = View.GONE
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
        showBarrio(barrio)
    }

    private fun showBarrio(barrio: Int) {
        val adapter = visualizeSpinner(root?.sp_barrio!!, barrio, requireContext())
        loadBarrio(adapter)
    }

    private fun loadBarrio(adapter: ArrayAdapter<CharSequence>) {
        if (usuario.barrio != "") {
            val compareValue = usuario.barrio
            val spinnerPosition = adapter.getPosition(compareValue)
            root?.sp_barrio?.setSelection(spinnerPosition)
        }
    }

    private fun disappearAll() {
        root?.iv_team?.visibility = View.VISIBLE
        root?.bt_comenzar?.visibility = View.VISIBLE
        root?.et_nombre?.visibility = View.GONE
        root?.et_apellido?.visibility = View.GONE
        root?.linear1?.visibility = View.GONE
        root?.linear2?.visibility = View.GONE
        root?.tv2?.visibility = View.GONE
        root?.tv3?.visibility = View.GONE
        root?.tv4?.visibility = View.GONE
        root?.tv5?.visibility = View.GONE
        root?.tv6?.visibility = View.GONE
        root?.tv7?.visibility = View.GONE
        root?.tv_fechaNacimiento?.visibility = View.GONE
        root?.ibt_calendario?.visibility = View.GONE
        root?.ibt_foto?.visibility = View.GONE
        root?.rSex?.visibility = View.GONE
        root?.sp_lugarNacimiento?.visibility = View.GONE
        root?.sp_comuna?.visibility = View.GONE
        root?.sp_barrio?.visibility = View.GONE
        root?.sp_posicion?.visibility = View.GONE
        root?.bt_actualizar?.visibility = View.GONE
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                startActivityForResult(takePictureIntent, requestImageCapture)
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestImageCapture && resultCode == Activity.RESULT_OK) {
            root?.ibt_foto?.isDrawingCacheEnabled = true
            root?.ibt_foto?.buildDrawingCache()

            val imageBitmap = data?.extras?.get("data") as Bitmap
            root?.ibt_foto?.setImageBitmap(imageBitmap)

            val myRef = FirebaseDatabase.getInstance().getReference("usuarios")
            val idUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
            savePhotoInStorage(imageBitmap, idUser, myRef)
            flagFoto = true
        }
    }
}