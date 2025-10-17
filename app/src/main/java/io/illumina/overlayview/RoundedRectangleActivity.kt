package io.illumina.overlayview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import io.illumina.overlay.OverlayConfig
import io.illumina.overlay.Position
import io.illumina.overlayview.databinding.ActivityRoundedRectangleBinding

class RoundedRectangleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoundedRectangleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoundedRectangleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.iv.doOnPreDraw { initOverlay() }
    }

    private fun initOverlay() {
        val config = listOf(
            OverlayConfig(
                view = binding.iv,
                text = "This is app image.",
                overlayAnchorPosition = Position.End
            )
        )
        binding.overlay.configs = arrayOf(*config.toTypedArray())
    }
}