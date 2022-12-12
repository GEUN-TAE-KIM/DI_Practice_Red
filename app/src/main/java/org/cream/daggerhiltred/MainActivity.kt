package org.cream.daggerhiltred

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.cream.daggerhiltred.di.GameComponent
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    lateinit var gameComponent: GameComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        (applicationContext as GameApplication).appComponent.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}