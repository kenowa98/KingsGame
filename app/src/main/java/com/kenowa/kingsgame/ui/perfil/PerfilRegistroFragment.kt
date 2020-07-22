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
import com.google.firebase.database.*
import com.kenowa.kingsgame.*
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.model.Usuario
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_perfil_registro.*
import kotlinx.android.synthetic.main.fragment_perfil_registro.view.*
import java.util.*
import kotlin.collections.set

@Suppress("DEPRECATION")
class PerfilRegistroFragment : Fragment() {
    private lateinit var user: Usuario
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
            user = safeArgs.usuario
            isData(view)
        }

        clickItemComuna(view.findViewById(R.id.sp_comuna))
        configureButtons(view)
    }

    private fun isData(view: View) {
        if (user.nombre.isEmpty()) {
            loadPerfil(view)
        } else {
            isClickedButton(false, view)
        }
    }

    private fun hideProgressBar() {
        root!!.progressBar.visibility = View.GONE
    }

    private fun configureSpinners(view: View) {
        visualizeSpinner(
            view.findViewById(R.id.sp_lugarNacimiento),
            R.array.lista_ciudades,
            requireContext()
        )
        visualizeSpinner(view.findViewById(R.id.sp_comuna), R.array.lista_comunas, requireContext())
        visualizeSpinner(
            view.findViewById(R.id.sp_posicion),
            R.array.lista_posiciones,
            requireContext()
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun configureButtons(view: View) {
        ibt_calendario.setOnClickListener { saveDate(tv_fechaNacimiento, requireContext()) }
        bt_actualizar.setOnClickListener { isClickedButton(true, view) }
        ibt_foto.setOnClickListener { dispatchTakePictureIntent() }
        bt_comenzar.setOnClickListener {
            findNavController().navigate(R.id.action_nav_perfil_registro_to_nav_perfil)
        }
    }

    private fun loadPerfil(view: View) {
        val myRef = referenceDatabase("usuarios")
        identifyUser(myRef, view)
    }

    private fun identifyUser(myRef: DatabaseReference, view: View) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val userID = FirebaseAuth.getInstance().currentUser?.uid
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val usuario = datasnapshot.getValue(Usuario::class.java)
                    if (isUser(usuario?.id, userID)) {
                        if (usuario != null) {
                            user = usuario
                        }
                        isClickedButton(false, view)
                        break
                    }
                }
                hideProgressBar()
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isClickedButton(click: Boolean, view: View) {
        if (click) {
            isCompleteData()
        } else {
            isNewUser(view)
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
        if (root!!.et_celular.text.toString().length != 10) {
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
                    validateName()
                }
            }
        }
    }

    private fun validateName() {
        if (root!!.et_nombre.text.toString().contains(" ")) {
            showMessage(requireContext(), "Solo se admite 1 nombre y sin espacios")
        } else {
            if (root!!.et_nombre.text.toString().length > 12) {
                showMessage(requireContext(), "El nombre debe tener máximo 12 caracteres")
            } else {
                validateLastName()
            }
        }
    }

    private fun validateLastName() {
        if (root!!.et_apellido.text.toString().length > 12) {
            showMessage(requireContext(), "El apellido debe tener máximo 12 caracteres")
        } else {
            updatePerfil()
            disappearAll()
        }
    }

    private fun updatePerfil() {
        val myRef = referenceDatabase("usuarios")
        val childUpdate = HashMap<String, Any>()
        val nombre = rewriteName()
        val apellido = rewriteLastName()
        childUpdate["nombre"] = nombre
        childUpdate["apellido"] = apellido
        childUpdate["celular"] = root!!.et_celular.text.toString()
        childUpdate["fecha"] = root!!.tv_fechaNacimiento.text.toString()
        childUpdate["genero"] = !root!!.rbt_masculino.isChecked
        childUpdate["origen"] = root!!.sp_lugarNacimiento.selectedItem.toString()
        childUpdate["comuna"] = root!!.sp_comuna.selectedItem.toString()
        if (root!!.sp_comuna.selectedItem.toString() != "Otro") {
            childUpdate["barrio"] = root!!.sp_barrio.selectedItem.toString()
        }
        childUpdate["posicion"] = root!!.sp_posicion.selectedItem.toString()
        user.id?.let { myRef.child(it).updateChildren(childUpdate) }
        context?.let { showMessage(it, "Cargando datos...") }
    }

    private fun rewriteName(): String {
        var nombre = root!!.et_nombre.text.toString()
        nombre = nombre[0].toUpperCase() +
                nombre.substring(1, nombre.length).toLowerCase(Locale.ROOT)
        return nombre
    }

    private fun rewriteLastName(): String {
        val apellido = root!!.et_apellido.text.toString()
        var first = true
        var newApellido = ""
        for (i in apellido) {
            if (first) {
                newApellido += i.toUpperCase()
                first = false
            } else {
                if (i == ' ') {
                    newApellido += i
                    first = true
                } else {
                    newApellido += i.toLowerCase()
                }
            }
        }
        return newApellido
    }

    @SuppressLint("SetTextI18n")
    private fun isNewUser(view: View) {
        if (user.nombre == "") {
            root!!.bt_actualizar.text = "CREAR"
            configureSpinners(view)
            ++checkSpinner
        } else {
            root!!.bt_actualizar.text = "ACTUALIZAR"
            root!!.et_nombre.setText(user.nombre)
            root!!.et_apellido.setText(user.apellido)
            root!!.et_celular.setText(user.celular)
            root!!.tv_fechaNacimiento.text = user.fecha
            loadDataInSpinner(R.array.lista_ciudades, root!!.sp_lugarNacimiento, user.origen)
            loadDataInSpinner(R.array.lista_comunas, root!!.sp_comuna, user.comuna)
            loadDataInSpinner(R.array.lista_posiciones, root!!.sp_posicion, user.posicion)
            loadGender()
            loadPhoto()
            spinnerBarrio(user.comuna)
            flagFoto = true
        }
        hideProgressBar()
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
        if (user.genero) {
            root!!.rSex.check(R.id.rbt_femenino)
        } else {
            root!!.rSex.check(R.id.rbt_masculino)
        }
    }

    private fun loadPhoto() {
        if (user.foto.isNotEmpty()) {
            Picasso.get().load(user.foto).into(root!!.ibt_foto)
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
            root!!.sp_barrio.visibility = View.VISIBLE
            identifyListBarrio(item)
        } else {
            root!!.sp_barrio.visibility = View.GONE
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
        val adapter = visualizeSpinner(root!!.sp_barrio, barrio, requireContext())
        loadBarrio(adapter)
    }

    private fun loadBarrio(adapter: ArrayAdapter<CharSequence>) {
        if (user.barrio != "") {
            val compareValue = user.barrio
            val spinnerPosition = adapter.getPosition(compareValue)
            root!!.sp_barrio.setSelection(spinnerPosition)
        }
    }

    private fun disappearAll() {
        root!!.iv_team.visibility = View.VISIBLE
        root!!.bt_comenzar.visibility = View.VISIBLE
        root!!.et_nombre.visibility = View.GONE
        root!!.et_apellido.visibility = View.GONE
        root!!.linear1.visibility = View.GONE
        root!!.linear2.visibility = View.GONE
        root!!.tv2.visibility = View.GONE
        root!!.tv3.visibility = View.GONE
        root!!.tv4.visibility = View.GONE
        root!!.tv5.visibility = View.GONE
        root!!.tv6.visibility = View.GONE
        root!!.tv7.visibility = View.GONE
        root!!.tv_fechaNacimiento.visibility = View.GONE
        root!!.ibt_calendario.visibility = View.GONE
        root!!.ibt_foto.visibility = View.GONE
        root!!.rSex.visibility = View.GONE
        root!!.sp_lugarNacimiento.visibility = View.GONE
        root!!.sp_comuna.visibility = View.GONE
        root!!.sp_barrio.visibility = View.GONE
        root!!.sp_posicion.visibility = View.GONE
        root!!.bt_actualizar.visibility = View.GONE
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
            root!!.ibt_foto.isDrawingCacheEnabled = true
            root!!.ibt_foto.buildDrawingCache()

            val imageBitmap = data?.extras?.get("data") as Bitmap
            root!!.ibt_foto.setImageBitmap(imageBitmap)

            val myRef = FirebaseDatabase.getInstance().getReference("usuarios")
            val idUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
            savePhotoInStorage(imageBitmap, idUser, myRef)
            flagFoto = true
        }
    }
}