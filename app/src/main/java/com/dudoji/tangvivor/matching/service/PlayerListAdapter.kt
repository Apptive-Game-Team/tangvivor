package com.dudoji.tangvivor.matching.service

import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dudoji.tangvivor.BaseDrawerActivity
import com.dudoji.tangvivor.R
import com.dudoji.tangvivor.matching.entity.User
import com.dudoji.tangvivor.repository.GameRepository

class PlayerListAdapter(val userList: List<User>, val activity: BaseDrawerActivity) : RecyclerView.Adapter<PlayerListAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView = itemView.findViewById<android.widget.TextView>(R.id.item_name)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): PlayerViewHolder {
        val itemView = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.game_item, parent, false)
        return PlayerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val user = userList[position]
        holder.itemView.setOnClickListener {
            if (GameRepository.isInGame) {
                val user = userList[position]
                activity.nearbyUserController.invite(user, GameRepository.currentSessionId)
            } else {
                Toast.makeText(
                    activity,
                    "You cannot invite players while not in a game.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        holder.nameTextView.text = user.name
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}