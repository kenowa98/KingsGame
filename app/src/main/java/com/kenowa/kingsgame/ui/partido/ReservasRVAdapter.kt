package com.kenowa.kingsgame.ui.partido

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.model.Reserva
import kotlinx.android.synthetic.main.item_reserva.view.*

class ReservasRVAdapter(
    private var reservasList: ArrayList<Reserva>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ReservasRVAdapter.ReservasViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReservasViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_reserva, parent, false)
        return ReservasViewHolder(itemView, onItemClickListener)
    }

    override fun getItemCount(): Int = reservasList.size

    override fun onBindViewHolder(holder: ReservasViewHolder, position: Int) {
        val reserva: Reserva = reservasList[position]
        holder.bindReserva(reserva)
    }

    class ReservasViewHolder(
        itemView: View,
        private var onItemClickListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private lateinit var reserve: Reserva

        @SuppressLint("SetTextI18n")
        fun bindReserva(reserva: Reserva) {
            reserve = reserva
            itemView.tv_fecha.text = reserva.fecha
            itemView.tv_msg.text = "Juega en ${reserva.idCancha}\n" +
                    "De ${reserva.inicioHora} a ${reserva.finHora}\n" +
                    "Total a pagar: ${reserva.precio} COP"
        }

        override fun onClick(v: View?) {
        }
    }

    interface OnItemClickListener {
        fun onItemClick(reserva: Reserva)
    }
}