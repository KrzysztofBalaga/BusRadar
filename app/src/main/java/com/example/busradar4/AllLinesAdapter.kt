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
import android.graphics.Color
import android.widget.Button
import android.util.TypedValue

class AllLinesAdapter(
    private var lines: List<String>,
    private val favManager: FavouriteManager,
    private val onUpdate: () -> Unit
) : RecyclerView.Adapter<AllLinesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view as Button
    }

    //ozywienie guziczkow
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val btn = Button(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                150
            )
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }
        return ViewHolder(btn)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val line = lines[position]
        holder.button.text = line

        updateButtonStyle(holder.button, favManager.isFavourite(line))

        //dodawanie do ulubionyxh
        holder.button.setOnClickListener {
            if (favManager.isFavourite(line)) {
                favManager.removeFromFavourites(line)
            } else {
                favManager.addToFavourites(line)
            }
            notifyItemChanged(position)
            onUpdate()
        }
    }

    //kolorowanko zaznaczonych
    private fun updateButtonStyle(button: Button, isFav: Boolean) {
        if (isFav) {
            button.setBackgroundColor(Color.parseColor("#0493BF"))
            button.setTextColor(Color.BLACK)
        } else {
            button.setBackgroundColor(Color.LTGRAY)
            button.setTextColor(Color.DKGRAY)
        }
    }

    override fun getItemCount() = lines.size

    fun updateList(newList: List<String>) {
        lines = newList
        notifyDataSetChanged()
    }
}