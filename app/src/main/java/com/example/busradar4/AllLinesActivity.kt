package com.example.busradar4

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager

class AllLinesActivity : AppCompatActivity() {
    private lateinit var favManager: FavouriteManager
    private lateinit var adapter: AllLinesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lines_layout)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAllLines)
        setSupportActionBar(toolbar)
        // strzałeczka do tyłu
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        favManager = FavouriteManager(this)
        val allLines = intent.getStringArrayListExtra("ALL_LINES") ?: arrayListOf()

        val recyclerView = findViewById<RecyclerView>(R.id.allLinesRV)

        adapter = AllLinesAdapter(allLines, favManager){}

        recyclerView.layoutManager = GridLayoutManager(this, 5)
        recyclerView.adapter = adapter

        // Wyszukiwarka
        findViewById<EditText>(R.id.searchLineET).addTextChangedListener { text ->
            val filtered = allLines.filter { it.contains(text.toString()) }
            adapter.updateList(filtered)
        }
    }
}
