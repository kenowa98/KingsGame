package com.kenowa.kingsgame.ui.partido

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kenowa.kingsgame.*
import com.kenowa.kingsgame.model.Cancha
import com.kenowa.kingsgame.model.Lugar
import com.kenowa.kingsgame.model.Reserva
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.android.synthetic.main.fragment_maps.view.*
import java.util.*

class MapsFragment : Fragment(), GoogleMap.OnMarkerClickListener {
    private lateinit var mMap: GoogleMap
    private var daySelect: String = ""
    private var dateSelect: String = ""
    private var namePlace: String = ""
    private var idCancha: String = ""
    private var hourSelect: Int = 0
    private var yearActual: Int = 0
    private var monthActual: Int = 0
    private var dayActual: Int = 0
    private var hourActual: Int = 0
    private var minuteActual: Int = 0
    private var juego: Int = 0
    private var pago: Int = 0
    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_maps, container, false)
        return root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        configureButtons(view)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun configureButtons(view: View) {
        ibt_calendario.setOnClickListener {
            saveDate(tv_fecha, tv_day, requireContext())
        }

        bt_filtrar.setOnClickListener {
            if (tv_fecha.text.toString() == "" ||
                sp_juego.selectedItem.toString() == "Seleccione el estilo de juego"
            ) {
                showMessage(requireContext(), "Hay campos vacíos")
            } else {
                mMap.clear()
                root?.progressBar?.let { it1 -> showProgressBar(it1) }
                juego = sp_juego.selectedItem.toString().toInt()
                saveActualDate()
                isValidDate()
            }
        }

        bt_reservar.setOnClickListener {
            if (dateSelect == "") {
                showMessage(requireContext(), "Ingrese una fecha")
            } else {
                if (juego == 0) {
                    showMessage(requireContext(), "Seleccione el modo de juego")
                } else {
                    root?.linear2?.visibility = View.GONE
                    root?.bt_reservar?.visibility = View.GONE
                    root?.iv_logo?.visibility = View.GONE
                    root?.tv_calificacion?.visibility = View.GONE
                    root?.tv_servicios?.visibility = View.GONE
                    root?.linear3?.visibility = View.VISIBLE
                    root?.tv_fecha2?.text = dateSelect
                    root?.tv_juego?.text = "Modalidad fútbol $juego"
                    searchCancha(view)
                }
            }
        }

        bt_crear.setOnClickListener {
            haveHour()
        }

        bt_cancelar.setOnClickListener {
            root?.linear2?.visibility = View.VISIBLE
            root?.bt_reservar?.visibility = View.VISIBLE
            root?.iv_logo?.visibility = View.VISIBLE
            root?.tv_calificacion?.visibility = View.VISIBLE
            root?.tv_servicios?.visibility = View.VISIBLE
            root?.linear3?.visibility = View.GONE
            root?.progressBar?.let { it1 -> showProgressBar(it1) }
            daySelect = ""
            dateSelect = ""
            juego = 0
            loadPlaces()
            configureSpinners()
        }

        bt_continuar.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun haveHour() {
        if (sp_hora.selectedItem.toString() == "Seleccione la hora de inicio") {
            showMessage(requireContext(), "Escoja una hora de inicio")
        } else {
            haveCancha()
        }
    }

    private fun haveCancha() {
        if (sp_cancha.selectedItem.toString() == "Elija la cancha") {
            showMessage(requireContext(), "Escoja una cancha")
        } else {
            isValidData()
        }
    }

    private fun isValidData() {
        hourSelect = sp_hora.selectedItem.toString().substring(0, 2).toInt()
        idCancha = sp_cancha.selectedItem.toString()
        val myRef = referenceDatabase("canchas")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val cancha = datasnapshot.getValue(Cancha::class.java)
                    if (cancha?.id == idCancha) {
                        isValidHour(cancha)
                    }
                }
            }
        }
        myRef.child(namePlace).addListenerForSingleValueEvent(postListener)
    }

    private fun isValidHour(cancha: Cancha) {
        when (daySelect) {
            "Monday" -> {
                val descp = cancha.lunes
                extractInfoHour(descp)
            }
            "Tuesday" -> {
                val descp = cancha.martes
                extractInfoHour(descp)
            }
            "Wednesday" -> {
                val descp = cancha.miercoles
                extractInfoHour(descp)
            }
            "Thursday" -> {
                val descp = cancha.jueves
                extractInfoHour(descp)
            }
            "Friday" -> {
                val descp = cancha.viernes
                extractInfoHour(descp)
            }
            "Saturday" -> {
                val descp = cancha.sabado
                extractInfoHour(descp)
            }
            "Sunday" -> {
                val descp = cancha.domingo
                extractInfoHour(descp)
            }
        }
    }

    private fun extractInfoHour(descp: String) {
        val len = descp.length
        val min = descp.substring(0, 2).toInt()
        val max = descp.substring(len - 9, len - 7).toInt()
        if (hourSelect in min..max) {
            costReserve(len, descp)
        } else {
            showMessage(requireContext(), "La hora de inicio no cumple con el horario del lugar")
        }
    }

    private fun costReserve(len: Int, descp: String) {
        when (len) {
            12 -> {
                pago = descp.substring(6, len).toInt()
                existReserve()
            }
            25 -> {
                twoOptions(descp, len)
            }
            38 -> {
                threeOptions(descp, len)
            }
            51 -> {
                fourOptions(descp, len)
            }
        }
    }

    private fun twoOptions(descp: String, len: Int) {
        val min = descp.substring(0, 2).toInt()
        val max = descp.substring(3, 5).toInt()
        pago = if (hourSelect in min..max) {
            descp.substring(6, 12).toInt()
        } else {
            descp.substring(len - 6, len).toInt()
        }
        existReserve()
    }

    private fun threeOptions(descp: String, len: Int) {
        var min = descp.substring(0, 2).toInt()
        var max = descp.substring(3, 5).toInt()
        if (hourSelect in min..max) {
            pago = descp.substring(6, 12).toInt()
        } else {
            min = descp.substring(13, 15).toInt()
            max = descp.substring(16, 18).toInt()
            pago = if (hourSelect in min..max) {
                descp.substring(19, 25).toInt()
            } else {
                descp.substring(len - 6, len).toInt()
            }
        }
        existReserve()
    }

    private fun fourOptions(descp: String, len: Int) {
        var min = descp.substring(0, 2).toInt()
        var max = descp.substring(3, 5).toInt()
        if (hourSelect in min..max) {
            pago = descp.substring(6, 12).toInt()
        } else {
            min = descp.substring(13, 15).toInt()
            max = descp.substring(16, 18).toInt()
            if (hourSelect in min..max) {
                pago = descp.substring(19, 25).toInt()
            } else {
                min = descp.substring(26, 28).toInt()
                max = descp.substring(29, 31).toInt()
                pago = if (hourSelect in min..max) {
                    descp.substring(32, 38).toInt()
                } else {
                    descp.substring(len - 6, len).toInt()
                }
            }
        }
        existReserve()
    }

    private fun existReserve() {
        val myRef = referenceDatabase("reservas")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                var permiso = true
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val reserva = datasnapshot.value as Map<*, *>
                    if (isReserve(reserva)) {
                        showMessage(requireContext(), "Ya existe esta reserva")
                        permiso = false
                        break
                    }
                }
                if (permiso) {
                    createMessage()
                }
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isReserve(reserva: Map<*, *>): Boolean {
        val keys = reserva.keys
        for (i in keys) {
            val data = reserva[i] as Map<*, *>
            if (data["idLugar"] == namePlace) {
                if (data["idCancha"] == idCancha) {
                    if (data["inicioHora"] == hourSelect) {
                        return true
                    }
                }
            }
            break
        }
        return false
    }

    private fun createMessage() {
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setMessage("Serían $pago COP. Desea hacer la reserva?")
                setPositiveButton("Sí") { _, _ ->
                    saveReserve()
                }
                setNegativeButton("No") { _, _ -> }
            }
            builder.create()
        }
        alertDialog?.show()
    }

    private fun saveReserve() {
        val myRef = referenceDatabase("reservas")
        val id = myRef.push().key
        val idUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val reserva = Reserva(
            id,
            idUser,
            idCancha,
            namePlace,
            dateSelect,
            hourSelect,
            hourSelect + 1,
            pago
        )
        if (id != null) {
            myRef.child(id).child(idUser).setValue(reserva)
            showMessage(requireContext(), "Reserva guardada")
            finishReserve()
        }
    }

    private fun finishReserve() {
        linear1.visibility = View.GONE
        linear3.visibility = View.GONE
        linear4.visibility = View.VISIBLE
    }

    private fun searchCancha(view: View) {
        val opciones: MutableList<String> = ArrayList()
        val myRef = referenceDatabase("canchas")
        root?.progressBar?.let { showProgressBar(it) }
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                var contador = 1
                opciones.add(0, "Elija la cancha")
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val cancha = datasnapshot.getValue(Cancha::class.java)
                    if (cancha?.juego == juego) {
                        opciones.add(contador, cancha.id)
                        ++contador
                    }
                }
                loadCancha(opciones, view)
            }
        }
        myRef.child(namePlace).addListenerForSingleValueEvent(postListener)
    }

    private fun loadCancha(opciones: MutableList<String>, view: View) {
        val spinner: Spinner = view.findViewById(R.id.sp_cancha)
        requireContext().let {
            ArrayAdapter(
                it,
                R.layout.spinner_item,
                opciones
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }
        root?.progressBar?.let { hideProgressBar(it) }
    }

    private fun isValidDate() {
        if (yearActual == tv_fecha.text.substring(0, 4).toInt()) {
            reviewMonth()
        } else {
            if (yearActual < tv_fecha.text.substring(0, 4).toInt()) {
                filterPlaces()
            } else {
                invalidFilter()
            }
        }
    }

    private fun reviewMonth() {
        if (monthActual == tv_fecha.text.substring(5, 7).toInt()) {
            reviewDay()
        } else {
            if (monthActual < tv_fecha.text.substring(5, 7).toInt()) {
                filterPlaces()
            } else {
                invalidFilter()
            }
        }
    }

    private fun reviewDay() {
        if (dayActual <= tv_fecha.text.substring(8, 10).toInt()) {
            filterPlaces()
        } else {
            invalidFilter()
        }
    }

    private fun filterPlaces() {
        daySelect = tv_day.text.toString()
        dateSelect = tv_fecha.text.toString()
        tv_fecha.text = ""
        loadPlaces()
    }

    private fun invalidFilter() {
        daySelect = ""
        dateSelect = ""
        juego = 0
        loadPlaces()
        showMessage(requireContext(), "La fecha elegida es obsoleta")
    }

    private fun saveActualDate() {
        val cal = Calendar.getInstance().time.toString()
        yearActual = cal.substring(30, 34).toInt()
        intMonth(cal)
        dayActual = cal.substring(8, 10).toInt()
        hourActual = cal.substring(11, 13).toInt()
        minuteActual = cal.substring(14, 16).toInt()
    }

    private fun intMonth(cal: String) {
        when (cal.substring(4, 7)) {
            "Jan" -> {
                monthActual = 1
            }
            "Feb" -> {
                monthActual = 2
            }
            "Mar" -> {
                monthActual = 3
            }
            "Apr" -> {
                monthActual = 4
            }
            "May" -> {
                monthActual = 5
            }
            "Jun" -> {
                monthActual = 6
            }
            "Jul" -> {
                monthActual = 7
            }
            "Aug" -> {
                monthActual = 8
            }
            "Sep" -> {
                monthActual = 9
            }
            "Oct" -> {
                monthActual = 10
            }
            "Nov" -> {
                monthActual = 11
            }
            "Dec" -> {
                monthActual = 12
            }
        }
    }

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        loadPlaces()
        configureSpinners()
    }

    private fun configureSpinners() {
        root?.sp_juego?.let {
            visualizeSpinner(
                it,
                R.array.lista_juego,
                requireContext()
            )
        }

        root?.sp_hora?.let {
            visualizeSpinner(
                it,
                R.array.lista_horas,
                requireContext()
            )
        }
    }

    private fun activateLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1234
            )
            return
        }
        mMap.isMyLocationEnabled = true
    }

    private fun loadPlaces() {
        val myRef = referenceDatabase("lugares")
        root?.linear1?.visibility = View.GONE
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val lugar = datasnapshot.getValue(Lugar::class.java)
                    if (lugar != null) {
                        haveDay(lugar)
                    }
                }
                root?.progressBar?.let { hideProgressBar(it) }
                activateLocation()
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(6.2227, -75.57748), 12F))
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun haveDay(lugar: Lugar) {
        if (daySelect.isEmpty()) {
            searchJuego(lugar)
        } else {
            okDayPlace(lugar)
        }
    }

    private fun okDayPlace(lugar: Lugar) {
        when (daySelect) {
            "Monday" -> {
                if (lugar.lunes != "No") {
                    searchJuego(lugar)
                }
            }
            "Tuesday" -> {
                if (lugar.martes != "No") {
                    searchJuego(lugar)
                }
            }
            "Wednesday" -> {
                if (lugar.miercoles != "No") {
                    searchJuego(lugar)
                }
            }
            "Thursday" -> {
                if (lugar.jueves != "No") {
                    searchJuego(lugar)
                }
            }
            "Friday" -> {
                if (lugar.viernes != "No") {
                    searchJuego(lugar)
                }
            }
            "Saturday" -> {
                if (lugar.sabado != "No") {
                    searchJuego(lugar)
                }
            }
            "Sunday" -> {
                if (lugar.domingo != "No") {
                    searchJuego(lugar)
                }
            }
        }
    }

    private fun searchJuego(lugar: Lugar?) {
        val myRef = referenceDatabase("canchas")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val cancha = datasnapshot.getValue(Cancha::class.java)
                    if (juego == 0) {
                        setMarker(lugar)
                        break
                    } else {
                        if (cancha?.juego == juego) {
                            setMarker(lugar)
                            break
                        }
                    }
                }
            }
        }
        lugar?.id?.let { myRef.child(it).addListenerForSingleValueEvent(postListener) }
    }

    private fun setMarker(lugar: Lugar?) {
        val posicion = LatLng(lugar!!.latitud, lugar.longitud)
        mMap.addMarker(MarkerOptions().position(posicion).title(lugar.id))
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        val myRef = referenceDatabase("lugares")
        root?.linear1?.visibility = View.VISIBLE
        root?.progressBar2?.let { it1 -> showProgressBar(it1) }
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val lugar = datasnapshot.getValue(Lugar::class.java)
                    if (lugar?.id == p0?.title) {
                        loadDataMarker(lugar)
                        namePlace = lugar?.id.toString()
                        break
                    }
                }
                root?.progressBar2?.let { hideProgressBar(it) }
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun loadDataMarker(lugar: Lugar?) {
        root?.tv_lugar?.text = lugar?.id
        root?.tv_direccion?.text = lugar?.address
        root?.tv_calificacion?.text = "Calificación: ${lugar?.calificacion.toString()}\n"
        root?.tv_horario?.text = "Horario:\n"
        root?.tv_servicios?.text = "Servicios:\n"
        loadSchedule(lugar)
        loadServices(lugar)
        loadPhoto(lugar)
    }

    @SuppressLint("SetTextI18n")
    private fun loadSchedule(lugar: Lugar?) {
        if (lugar?.lunes != "No") {
            root?.tv_horario?.text =
                root?.tv_horario?.text.toString() + "  Lunes: ${lugar?.lunes}\n"
        }
        if (lugar?.martes != "No") {
            root?.tv_horario?.text =
                root?.tv_horario?.text.toString() + "  Martes: ${lugar?.martes}\n"
        }
        if (lugar?.miercoles != "No") {
            root?.tv_horario?.text =
                root?.tv_horario?.text.toString() + "  Miércoles: ${lugar?.miercoles}\n"
        }
        if (lugar?.jueves != "No") {
            root?.tv_horario?.text =
                root?.tv_horario?.text.toString() + "  Jueves: ${lugar?.jueves}\n"
        }
        if (lugar?.viernes != "No") {
            root?.tv_horario?.text =
                root?.tv_horario?.text.toString() + "  Viernes: ${lugar?.viernes}\n"
        }
        if (lugar?.sabado != "No") {
            root?.tv_horario?.text =
                root?.tv_horario?.text.toString() + "  Sábado: ${lugar?.sabado}\n"
        }
        if (lugar?.domingo != "No") {
            root?.tv_horario?.text =
                root?.tv_horario?.text.toString() + "  Domingo: ${lugar?.domingo}\n"
        }
        if (lugar?.festivo != "No") {
            root?.tv_horario?.text =
                root?.tv_horario?.text.toString() + "  Festivo: ${lugar?.festivo}\n"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadServices(lugar: Lugar?) {
        if (lugar?.parqueadero!!) {
            root?.tv_servicios?.text = root?.tv_servicios?.text.toString() + "     parqueadero\n"
        }
        if (lugar.tienda) {
            root?.tv_servicios?.text = root?.tv_servicios?.text.toString() + "     tienda\n"
        }
        if (lugar.locker) {
            root?.tv_servicios?.text = root?.tv_servicios?.text.toString() + "     locker\n"
        }
        if (lugar.vestier) {
            root?.tv_servicios?.text = root?.tv_servicios?.text.toString() + "     vestier/baño\n"
        }
        if (lugar.peto) {
            root?.tv_servicios?.text =
                root?.tv_servicios?.text.toString() + "     préstamo de petos\n"
        }
        if (lugar.balon) {
            root?.tv_servicios?.text =
                root?.tv_servicios?.text.toString() + "     préstamo de balón\n"
        }
    }

    private fun loadPhoto(lugar: Lugar?) {
        if (lugar?.logo?.isNotEmpty()!!) {
            Picasso.get().load(lugar.logo).into(root?.iv_logo)
        }
    }
}