package com.kenowa.kingsgame.ui.perfil

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.getAge
import com.kenowa.kingsgame.hideProgressBar
import com.kenowa.kingsgame.model.Usuario
import com.kenowa.kingsgame.showMessage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_perfil_registro.*
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.set

@Suppress("DEPRECATION")
class PerfilRegistroFragment : Fragment() {
    private lateinit var fechaRegistro: String
    private var cal = Calendar.getInstance()

    private lateinit var user: Usuario

    private var checkSpinner = 0
    private val requestImageCapture = 1234

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

        dissapearFirstView()
        configureSpinners(view)
        dataUser(false)
        onItemComuna(view.findViewById(R.id.sp_comuna))
        configureButtons()
    }

    private fun configureSpinners(view: View) {
        visualizeSpinner(view.findViewById(R.id.sp_lugarNacimiento), R.array.lista_ciudades)
        visualizeSpinner(view.findViewById(R.id.sp_comuna), R.array.lista_comunas)
        visualizeSpinner(view.findViewById(R.id.sp_posicion), R.array.lista_posiciones)
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
    private fun configureButtons() {
        ibt_calendario.setOnClickListener { saveDate() }
        bt_actualizar.setOnClickListener { dataUser(true) }
        ibt_foto.setOnClickListener { dispatchTakePictureIntent() }
        bt_comenzar.setOnClickListener {
            findNavController().navigate(R.id.action_nav_perfil_registro_to_nav_perfil)
        }
    }

    private fun dataUser(click: Boolean) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("usuarios")
        val mAuth: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val email = mAuth?.email
        identifyUser(email, myRef, click)
    }

    private fun identifyUser(
        email: String?,
        myRef: DatabaseReference,
        click: Boolean
    ) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    user = datasnapshot.getValue(Usuario::class.java)!!
                    if (isUser(email, myRef, click)) {
                        break
                    }
                }
                hideProgressBar(progressBar)
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isUser(
        email: String?,
        myRef: DatabaseReference,
        click: Boolean
    ): Boolean {
        if (user.correo == email) {
            isClickedButton(myRef, click)
            return true
        }
        return false
    }

    private fun isClickedButton(
        myRef: DatabaseReference,
        click: Boolean
    ) {
        if (click) {
            isCompleteData(myRef)
        } else {
            isNewUser()
        }
    }

    private fun isCompleteData(myRef: DatabaseReference) {
        if (et_nombre.text.toString().isEmpty() ||
            et_apellido.text.toString().isEmpty() ||
            et_celular.text.toString().isEmpty() ||
            tv_fechaNacimiento.text.toString().isEmpty() ||
            sp_lugarNacimiento.selectedItem.toString() == "Seleccione una ciudad" ||
            sp_comuna.selectedItem.toString() == "Seleccione la comuna donde vive" ||
            sp_posicion.selectedItem.toString() == "Seleccione su posición habitual" ||
            user.foto == ""
        ) {
            showMessage(requireContext(), "Faltan datos")
        } else {
            isCorrectPhone(myRef)
        }
    }

    private fun isCorrectPhone(myRef: DatabaseReference) {
        if (et_celular.text.toString().length != 10) {
            showMessage(requireContext(), "El celular debe tener 10 dígitos!")
        } else {
            validateAge(myRef)
        }
    }

    private fun validateAge(myRef: DatabaseReference) {
        val age = loadAge()
        if (age != null) {
            when {
                age < 15 -> {
                    showMessage(requireContext(), "Debes tener más de 15 años")
                }
                age > 80 -> {
                    showMessage(requireContext(), "Máximo hasta 80 años")
                }
                else -> {
                    updatePerfil(myRef)
                    disappearAll()
                }
            }
        }
    }

    private fun loadAge(): Int? {
        val fecha = tv_fechaNacimiento.text.toString()
        val year = fecha.substring(0, 4).toInt()
        val month = fecha.substring(5, 7).toInt()
        val day = fecha.substring(8, 9).toInt()
        return getAge(year, month, day)?.toInt()
    }

    @SuppressLint("SetTextI18n")
    private fun isNewUser() {
        if (user.nombre == "") {
            bt_actualizar.text = "CREAR"
            ++checkSpinner
        } else {
            bt_actualizar.text = "ACTUALIZAR"
            et_nombre.setText(user.nombre)
            et_apellido.setText(user.apellido)
            et_celular.setText(user.celular)
            tv_fechaNacimiento.text = user.fecha
            loadDataInSpinner(R.array.lista_ciudades, sp_lugarNacimiento, user.origen)
            loadDataInSpinner(R.array.lista_comunas, sp_comuna, user.comuna)
            loadDataInSpinner(R.array.lista_posiciones, sp_posicion, user.posicion)
            loadGender()
            loadPhoto()
            spinnerBarrio(user.comuna)
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

    private fun loadGender() {
        if (user.genero) {
            rSex.check(R.id.rbt_femenino)
        } else {
            rSex.check(R.id.rbt_masculino)
        }
    }

    private fun loadPhoto() {
        if (user.foto.isNotEmpty()) {
            Picasso.get().load(user.foto).into(ibt_foto)
        }
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
        val idUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        myRef.child(idUser).updateChildren(childUpdate)
        context?.let { showMessage(it, "Cargando datos") }
    }

    private fun onItemComuna(spComuna: Spinner) {
        spComuna.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                pos: Int,
                id: Long
            ) {
                if (++checkSpinner > 2) {
                    val item = parent.getItemAtPosition(pos)
                    spinnerBarrio(item)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun spinnerBarrio(item: Any?) {
        if (item != "Seleccione la comuna donde vive" && item != "Otro") {
            sp_barrio.visibility = View.VISIBLE
            identifyListBarrio(item)
        } else {
            sp_barrio.visibility = View.GONE
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
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            barrio,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        sp_barrio.adapter = adapter
        loadBarrio(adapter)
    }

    private fun loadBarrio(adapter: ArrayAdapter<CharSequence>) {
        if (user.barrio != "") {
            val compareValue = user.barrio
            val spinnerPosition = adapter.getPosition(compareValue)
            sp_barrio.setSelection(spinnerPosition)
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
                fechaRegistro = simpleDateFormat.format(cal.time).toString()
                tv_fechaNacimiento.text = fechaRegistro
            }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
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
        bt_actualizar.visibility = View.GONE
    }

    private fun dissapearFirstView() {
        sp_barrio.visibility = View.GONE
        iv_team.visibility = View.GONE
        bt_comenzar.visibility = View.GONE
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
            ibt_foto.isDrawingCacheEnabled = true
            ibt_foto.buildDrawingCache()

            val imageBitmap = data?.extras?.get("data") as Bitmap
            ibt_foto.setImageBitmap(imageBitmap)
            savePhotoInStorage(imageBitmap)
        }
    }

    private fun savePhotoInStorage(imageBitmap: Bitmap) {
        val mStorage = FirebaseStorage.getInstance()
        val idUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val photoRef = mStorage.reference.child(idUser)
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)

        val data = baos.toByteArray()
        val uploadTask = photoRef.putBytes(data)
        uploadPhoto(uploadTask, photoRef)
    }

    private fun uploadPhoto(
        uploadTask: UploadTask,
        photoRef: StorageReference
    ) {
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            photoRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val childUpdate = HashMap<String, Any>()
                val urlPhoto = task.result.toString()
                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference("usuarios")
                val idUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
                childUpdate["foto"] = urlPhoto
                myRef.child(idUser).updateChildren(childUpdate)
            }
        }
    }
}