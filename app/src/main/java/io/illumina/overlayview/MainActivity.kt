package io.illumina.overlayview

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import io.illumina.overlayview.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initClickEvent()
    }

    private fun initClickEvent() {
        binding.apply {
            btn1.setOnClickListener { navigateTo(CircleActivity::class.java) }
            btn2.setOnClickListener { navigateTo(RectangleActivity::class.java) }
            btn3.setOnClickListener { navigateTo(RoundedRectangleActivity::class.java) }
        }
    }

    private fun navigateTo(target: Class<*>) {
        val intent = Intent(this, target)
        startActivity(intent)
    }
}