package com.example.heytravel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.btn_language)
        button.setOnClickListener {
            val intent = Intent(this, LanguageActivity::class.java)
            startActivity(intent)
        }

        val button2 = findViewById<Button>(R.id.btn_newplace)
        button2.setOnClickListener {
            replaceFragment(MapsFragment())
        }

        val button3 = findViewById<Button>(R.id.btn_selectplace)
        button3.setOnClickListener {
            val intent = Intent(this, SelectPlace::class.java)
            startActivity(intent)
        }
    }
    private fun replaceFragment(fragment: Fragment){
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}