package com.surendramaran.yolov8tflite.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.model.CardItem

class TreeCardAdapter(private val cardItems: List<CardItem>) :
    RecyclerView.Adapter<TreeCardAdapter.CardViewHolder>() {

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.treeCardView)
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        val textContent: TextView = itemView.findViewById(R.id.textContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tree_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val item = cardItems[position]
        holder.textTitle.text = item.title
        holder.textContent.text = item.content

        // Add click listener to handle expansion/collapse
        holder.cardView.setOnClickListener {
            val expanded = holder.textContent.visibility == View.VISIBLE
            holder.textContent.visibility = if (expanded) View.GONE else View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return cardItems.size
    }
}