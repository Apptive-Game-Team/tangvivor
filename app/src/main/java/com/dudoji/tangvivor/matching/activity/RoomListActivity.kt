package com.dudoji.tangvivor.matching.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.dudoji.tangvivor.R
import com.dudoji.tangvivor.matching.entity.Room
import com.dudoji.tangvivor.repository.RoomRepository
import com.dudoji.tangvivor.repository.UserRepository
import kotlinx.coroutines.launch

class RoomListActivity : ComponentActivity() {
    lateinit var roomListRecyclerView: RecyclerView
    lateinit var reloadButton: Button
    lateinit var createRoomButton: Button
    lateinit var roomNameEdit: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_list)

        findViewById<TextView>(R.id.my_name).text = UserRepository.me.name

        roomListRecyclerView = findViewById(R.id.room_list_recycler_view)
        roomListRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        lifecycleScope.launch {
            val roomList = RoomRepository.getRooms()
            Log.d("RoomListActivity", "Room list fetched: $roomList")
            roomListRecyclerView.adapter = RoomListAdapter(roomList, this@RoomListActivity)
        }

        reloadButton = findViewById(R.id.reload_button)
        reloadButton.setOnClickListener {
            lifecycleScope.launch {
                val roomList = RoomRepository.getRooms()
                roomListRecyclerView.adapter = RoomListAdapter(roomList, this@RoomListActivity)
            }
        }
        roomNameEdit = findViewById(R.id.room_name)
        createRoomButton = findViewById(R.id.create_room_button)
        createRoomButton.setOnClickListener {
            val roomName = roomNameEdit.text.toString()
            if (roomName.isNotEmpty()) {
                lifecycleScope.launch {
                    RoomRepository.createRoom(roomName, this@RoomListActivity)
                    val roomList = RoomRepository.getRooms()
                    roomListRecyclerView.adapter = RoomListAdapter(roomList, this@RoomListActivity)
                }
            }
        }
    }
}

class RoomListAdapter(val roomList: List<Room>, val activity: RoomListActivity) : RecyclerView.Adapter<RoomListAdapter.RoomViewHolder>() {

    class RoomViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView = itemView.findViewById<android.widget.TextView>(R.id.item_name)
        val makerTextView = itemView.findViewById<android.widget.TextView>(R.id.item_maker)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RoomViewHolder {
        val itemView = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.game_item, parent, false)
        return RoomViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomList[position]
        holder.nameTextView.text = room.name
        activity.lifecycleScope.launch {
            holder.makerTextView.text = UserRepository.getUser(room.user1!!).name
        }

        holder.itemView.setOnClickListener {
            RoomRepository.db.collection(RoomRepository.COLLECTION_NAME)
                .document(room.name!!)
                .update("user2", UserRepository.me.name)
                .addOnSuccessListener {
                    Toast.makeText(
                        holder.itemView.context,
                        "Joined room: ${room.name}",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(holder.itemView.context, com.dudoji.tangvivor.game.GameActivity::class.java)
                    intent.putExtra("roomName", room.name)
                    intent.putExtra("me", 2)
                    holder.itemView.context.startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Log.d("RoomListAdapter", "Error joining room: ", e)
                    Toast.makeText(
                        holder.itemView.context,
                        "Failed to join room",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun getItemCount(): Int {
        return roomList.size
    }
}