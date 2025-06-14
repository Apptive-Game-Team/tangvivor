package com.dudoji.tangvivor.matching.activity

import RoomListAdapter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.dudoji.tangvivor.BaseDrawerActivity
import com.dudoji.tangvivor.R
import com.dudoji.tangvivor.repository.GameRepository
import com.dudoji.tangvivor.repository.ImageRespository
import com.dudoji.tangvivor.repository.RoomRepository
import com.dudoji.tangvivor.repository.UserRepository
import kotlinx.coroutines.launch
import com.google.android.gms.games.PlayGames

class RoomListActivity : BaseDrawerActivity() {
    lateinit var roomListRecyclerView: RecyclerView
    lateinit var reloadButton: Button
    lateinit var createRoomButton: Button
    lateinit var roomNameEdit: EditText

    lateinit var headImageImageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContent(R.layout.activity_room_list)
        findViewById<ImageButton>(R.id.leader_board_button).setOnClickListener{
            openLeaderboard()
        }

        findViewById<TextView>(R.id.my_name).text = UserRepository.me?.name

        headImageImageView = findViewById(R.id.head_image)
        headImageImageView.setOnClickListener {
            openGallery()
        }
        if (ImageRespository.imageUri != null) {
            headImageImageView.setImageURI(ImageRespository.imageUri)
        }

        roomListRecyclerView = findViewById(R.id.room_list_recycler_view)
        roomListRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        checkMatching()

        reloadButton = findViewById(R.id.reload_button)
        reloadButton.setOnClickListener {
            checkMatching()
        }
        roomNameEdit = findViewById(R.id.room_name)
        createRoomButton = findViewById(R.id.create_room_button)
        createRoomButton.setOnClickListener {
            val roomName = roomNameEdit.text.toString()
            if (roomName.isNotEmpty()) {
                lifecycleScope.launch {
                    RoomRepository.createRoom(roomName, this@RoomListActivity)
                    GameRepository.saveGame(roomName)
                    GameRepository.enterGame(roomName, this@RoomListActivity, 1)
                }
            }
        }
    }

    fun openLeaderboard() {
        PlayGames.getLeaderboardsClient(this)
            .getLeaderboardIntent(getString(R.string.score_leaderboard_id))
            .addOnSuccessListener(this){intent ->
                startActivityForResult(intent, 1001)
            }
            .addOnFailureListener(this) { e ->
                Log.e("MatchingSystem", "Failed to get leaderboard intent", e)
                Toast.makeText(this, "Failed to open leaderboard", Toast.LENGTH_SHORT).show()
            }
    }

    fun checkMatching() {
        lifecycleScope.launch {
            val roomList = RoomRepository.getRooms()
            Log.d("MatchingSystem", "Checking matching: $roomList")
            roomListRecyclerView.adapter = RoomListAdapter(roomList, this@RoomListActivity)
        }
    }

    private val getImageFromGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                Log.d("Gallery", "Image URI: $it")
                ImageRespository.imageUri = it
                headImageImageView.setImageURI(it)
            }
        }

    // 갤러리 열기 호출
    fun openGallery() {
        getImageFromGallery.launch("image/*")
    }
}