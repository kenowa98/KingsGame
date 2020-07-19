package com.kenowa.kingsgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.kenowa.kingsgame.model.Noticia
import com.kenowa.kingsgame.ui.noticia.NoticiasRVAdapter
import kotlinx.android.synthetic.main.activity_espectador.*

class EspectadorActivity : AppCompatActivity() {
    private var allNews: MutableList<Noticia> = mutableListOf()
    private lateinit var noticiasAdapter: NoticiasRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_espectador)

        loadNews()

        rv_noticias.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )

        noticiasAdapter = NoticiasRVAdapter(allNews as ArrayList<Noticia>)
        rv_noticias.adapter = noticiasAdapter
    }

    private fun loadNews() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("noticias")
        addNewsOnList(myRef)
    }

    private fun addNewsOnList(myRef: DatabaseReference) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val noticia = datasnapshot.getValue(Noticia::class.java)
                    filterNews(noticia)
                }
                hideProgressBar(progressBar)
                noticiasAdapter.notifyDataSetChanged()
            }
        }
        myRef.addValueEventListener(postListener)
    }

    private fun filterNews(noticia: Noticia?) {
        if (noticia?.caso == "partido" || noticia?.caso == "torneo") {
            allNews.add(noticia)
        }
    }
}