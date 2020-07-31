package com.kenowa.kingsgame.ui.partido

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.model.Reserva
import kotlinx.android.synthetic.main.item_request.view.*

class PersonaRVAdapter(
    private var invitacionList: ArrayList<Reserva>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<PersonaRVAdapter.PersonaViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PersonaViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
        return PersonaViewHolder(itemView, onItemClickListener)
    }

    override fun getItemCount(): Int = invitacionList.size

    override fun onBindViewHolder(holder: PersonaViewHolder, position: Int) {
        val invitacion: Reserva = invitacionList[position]
        holder.bindSolicitud(invitacion)
    }

    class PersonaViewHolder(
        itemView: View,
        private var onItemClickListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private lateinit var invitation: Reserva

        @SuppressLint("SetTextI18n")
        fun bindSolicitud(reserva: Reserva) {
            invitation = reserva
            itemView.tv_titulo.text = "Partido"
            itemView.tv_msg.text = "Para jugar el ${invitation.fecha} de " +
                    "${invitation.inicioHora}:00 a ${invitation.finHora}:00 " +
                    "en ${invitation.idLugar}"

            itemView.setOnClickListener {
                clickItem(1)
            }

            itemView.bt_aceptar.setOnClickListener {
                clickItem(2)
            }

            itemView.bt_rechazar.setOnClickListener {
                clickItem(3)
            }
        }

        private fun clickItem(case: Int) {
            onItemClickListener.onItemClick(invitation, case)
        }

        override fun onClick(v: View?) {
        }
    }

    interface OnItemClickListener {
        fun onItemClick(invitation: Reserva, case: Int)
    }
}