package com.dudoji.tangvivor.matching.service

import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dudoji.tangvivor.BaseDrawerActivity
import com.dudoji.tangvivor.R
import com.dudoji.tangvivor.matching.entity.User
import com.dudoji.tangvivor.repository.GameRepository

class PlayerListAdapter(var userList: List<User>) : RecyclerView.Adapter<PlayerListAdapter.PlayerViewHolder>() {

    var activity: BaseDrawerActivity? = null

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView = itemView.findViewById<TextView>(R.id.item_name)
    }

    fun updateUserList(newUserList: List<User>) {
        userList = newUserList
        notifyDataSetChanged()
        Log.d("NearbySystem", "User list updated with ${userList.size} users")
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): PlayerViewHolder {
        val itemView = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.player_item, parent, false)
        return PlayerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val user = userList[position]
        holder.nameTextView.text = user.name
        Log.d("NearbySystem", "Binding user: ${user.name} at position $position")
        holder.itemView.setOnClickListener {
            if (GameRepository.isInGame) {
                BaseDrawerActivity.nearbyUserController?.invite(user, GameRepository.currentSessionId)
            } else {
                Toast.makeText(
                    activity,
                    "You cannot invite players while not in a game.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d("NearbySystem", "User list size: ${userList.size}")
        return userList.size
    }
}