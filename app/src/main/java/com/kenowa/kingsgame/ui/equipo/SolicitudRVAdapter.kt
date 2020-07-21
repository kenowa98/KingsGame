package com.kenowa.kingsgame.ui.equipo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.model.Solicitud
import kotlinx.android.synthetic.main.item_request.view.*

class SolicitudRVAdapter(
    private var requestList: ArrayList<Solicitud>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<SolicitudRVAdapter.SolicitudViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SolicitudViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
        return SolicitudViewHolder(itemView, onItemClickListener)
    }

    override fun getItemCount(): Int = requestList.size

    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        val solicitud: Solicitud = requestList[position]
        holder.bindSolicitud(solicitud)
    }

    class SolicitudViewHolder(
        itemView: View,
        private var onItemClickListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private lateinit var request: Solicitud

        fun bindSolicitud(solicitud: Solicitud) {
            request = solicitud
            itemView.tv_titulo.text = solicitud.id_equipo
            itemView.tv_msg.text = solicitud.msg

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
            onItemClickListener.onItemClick(request, case)
        }

        override fun onClick(v: View?) {
        }
    }

    interface OnItemClickListener {
        fun onItemClick(request: Solicitud, case: Int)
    }
}