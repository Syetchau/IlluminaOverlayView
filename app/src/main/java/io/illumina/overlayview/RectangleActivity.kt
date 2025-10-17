package io.illumina.overlayview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import com.google.android.material.tabs.TabLayout
import io.illumina.overlay.OverlayConfig
import io.illumina.overlay.Position
import io.illumina.overlayview.databinding.ActivityRectangleBinding

class RectangleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRectangleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRectangleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tabs.addTab(binding.tabs.newTab().setText("Tab One"))
        binding.tabs.addTab(binding.tabs.newTab().setText("Tab Two"))
        binding.tabs.addTab(binding.tabs.newTab().setText("Tab Three"))

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // Do something when tab is selected
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.tabs.doOnPreDraw { initOverlay() }
    }

    private fun initOverlay() {
        val config = listOf(
            OverlayConfig(
                view = binding.tabs,
                text = getString(R.string.overlay_description_3),
                overlayAnchorPosition = Position.Bottom
            )
        )
        binding.overlay.configs = arrayOf(*config.toTypedArray())
    }
}