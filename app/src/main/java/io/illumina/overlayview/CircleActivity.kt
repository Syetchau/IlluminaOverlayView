package io.illumina.overlayview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import io.illumina.overlay.OverlayConfig
import io.illumina.overlay.Position
import io.illumina.overlayview.databinding.ActivityCircleBinding

class CircleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCircleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCircleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Wait for FAB to be laid out
        binding.fab.doOnPreDraw { initFABOverlay() }
        // binding.iv.doOnPreDraw { initImageOverlay() }
    }

    private fun initFABOverlay() {
        // Set the config to the view
        val config = listOf(
            OverlayConfig(
                view = binding.fab,
                text = getString(R.string.overlay_description_1),
                overlayAnchorPosition = Position.Top
            )
        )
        binding.overlay.overlayLineLength = 250f
        binding.overlay.configs = arrayOf(*config.toTypedArray())
    }
}