package com.dudoji.tangvivor

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dudoji.tangvivor.matching.service.NearbyUserController
import com.dudoji.tangvivor.matching.service.PlayerListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class BaseDrawerActivity: AppCompatActivity() {

    companion object {
        private val playerListAdapter: PlayerListAdapter = PlayerListAdapter(
            listOf()
        )
        var nearbyUserController: NearbyUserController? = null
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var toggle: ActionBarDrawerToggle


    private val onDiscoverChanged: () -> Unit = {
        Log.d("BaseDrawerActivity", "onDiscoverChanged called")
        GlobalScope.launch(Dispatchers.Main) {
            Log.d("NearbySystem", "Updating nearby users in RecyclerView")
            val nearbyUsers = nearbyUserController?.getNearbyUsers()
            Log.d("NearbySystem", "Nearby users: $nearbyUsers")
            playerListAdapter.updateUserList(nearbyUsers!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        playerListAdapter.activity = this
        nearbyUserController?.context = this
        nearbyUserController?.nearbyController?.context = this
        recyclerView.adapter = playerListAdapter


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    fun onAuthenticationSuccess() {
        nearbyUserController = NearbyUserController(
            this,
            onDiscoverChanged
        )
    }

    protected fun setChildContent(layoutResId: Int) {
        val container: FrameLayout = findViewById(R.id.child_content)
        layoutInflater.inflate(layoutResId, container, true)
    }
}