package com.kenowa.kingsgame.ui.noticia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.model.Noticia
import kotlinx.android.synthetic.main.item_news.view.*

class NoticiasRVAdapter(
    private var noticiasList: ArrayList<Noticia>
) : RecyclerView.Adapter<NoticiasRVAdapter.NoticiasViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NoticiasViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NoticiasViewHolder(itemView)
    }

    override fun getItemCount(): Int = noticiasList.size

    override fun onBindViewHolder(holder: NoticiasViewHolder, position: Int) {
        val noticia: Noticia = noticiasList[position]
        holder.bindNoticia(noticia)
    }

    class NoticiasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindNoticia(noticia: Noticia) {
            itemView.tv_titulo.text = noticia.titulo
            itemView.tv_msg.text = noticia.msg
            imageNews(noticia)
        }

        private fun imageNews(noticia: Noticia) {
            when (noticia.caso) {
                "general" -> {
                    itemView.iv_situation.setImageResource(R.drawable.ic_info)
                }
                "descuento" -> {
                    itemView.iv_situation.setImageResource(R.drawable.ic_sale)
                }
                else -> {
                    itemView.iv_situation.setImageResource(R.drawable.ic_stadium)
                }
            }
        }
    }
}