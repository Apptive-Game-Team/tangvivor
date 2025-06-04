package com.dudoji.tangvivor

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.dudoji.tangvivor.matching.service.NearbyUserController

abstract class BaseDrawerActivity: AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var toggle: ActionBarDrawerToggle
    val nearbyUserController by lazy {
        NearbyUserController(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
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


    protected fun setChildContent(layoutResId: Int) {
        val container: FrameLayout = findViewById(R.id.child_content)
        layoutInflater.inflate(layoutResId, container, true)
    }
}