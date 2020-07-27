package com.kenowa.kingsgame.ui.perfil

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.getAge
import com.kenowa.kingsgame.hideProgressBar
import com.kenowa.kingsgame.model.Usuario
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_perfil_view.view.*

class PerfilViewFragment : Fragment() {
    private lateinit var usuario: Usuario
    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_perfil_view, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val safeArgs = PerfilViewFragmentArgs.fromBundle(it)
            usuario = safeArgs.user
            loadData()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadData() {
        root?.tv_apellido?.text = usuario.apellido
        root?.tv_nombre?.text = usuario.nombre
        root?.tv_posicion?.text = usuario.posicion
        root?.tv_origen?.text = usuario.origen
        loadAge()
        loadBarrio()
        loadPhoto()
        scores()
        hideProgressBar(root?.progressBar!!)
    }

    private fun loadAge() {
        val age = getAge(usuario.fecha)
        root?.tv_edad?.text = age
    }

    @SuppressLint("SetTextI18n")
    private fun loadBarrio() {
        if (usuario.comuna == "Otro") {
            root?.tv_sector?.text = "Vive fuera de Medellín"
        } else {
            root?.tv_sector?.text = "Sector: ${usuario.comuna} / ${usuario.barrio}"
        }
    }

    private fun loadPhoto() {
        if (usuario.foto.isNotEmpty()) {
            Picasso.get().load(usuario.foto).into(root?.iv_foto)
        }
    }

    private fun scores() {
        root?.tv_responsable?.text = "??/5.0"
        root?.tv_limpio?.text = "??/5.0"
        root?.tv_velocidad?.text = "??/5.0"
        root?.tv_salto?.text = "??/5.0"
        root?.tv_regate?.text = "??/5.0"
        root?.tv_resistencia?.text = "??/5.0"
        root?.tv_equipo?.text = "??/5.0"
    }
}