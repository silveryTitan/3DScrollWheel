package com.silverytitan.scrollwheel3d

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import androidx.recyclerview.widget.LinearSnapHelper
import com.silverytitan.scrollwheel3d.adapter.MenuAdapter
import com.silverytitan.scrollwheel3d.view.ScrollWheelLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val menuList = mutableListOf(
        R.mipmap.ic_launcher,
        R.mipmap.ic_launcher,
        R.mipmap.ic_launcher,
        R.mipmap.ic_launcher,
        R.mipmap.ic_launcher,
        R.mipmap.ic_launcher,
        R.mipmap.ic_launcher,
        R.mipmap.ic_launcher,
        R.mipmap.ic_launcher,
        R.mipmap.ic_launcher,
        R.mipmap.ic_launcher
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val layoutManger = ScrollWheelLayoutManager()
        rv_menu.layoutManager = layoutManger
        val menuAdapter = MenuAdapter(menuList)
        rv_menu.adapter = menuAdapter
        LinearSnapHelper().attachToRecyclerView(rv_menu)
        iv_left.setOnClickListener {
            if (rv_menu.size > 0 && layoutManger.getCenterPosition() > 0) rv_menu.smoothScrollToPosition(
                layoutManger.getCenterPosition() - 1
            )
        }
        iv_right.setOnClickListener {
            if (rv_menu.size > 0 && layoutManger.getCenterPosition() < menuAdapter.itemCount - 1) rv_menu.smoothScrollToPosition(
                layoutManger.getCenterPosition() + 1
            )
        }
    }
}