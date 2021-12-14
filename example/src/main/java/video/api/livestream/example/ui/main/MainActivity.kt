package video.api.livestream.example.ui.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions3.RxPermissions
import video.api.livestream.app.R
import video.api.livestream.app.databinding.ActivityMainBinding
import video.api.livestream.example.ui.preferences.PreferencesActivity
import video.api.livestream.example.ui.utils.DialogHelper

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val rxPermissions: RxPermissions by lazy { RxPermissions(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

    override fun onResume() {
        super.onResume()

        rxPermissions
            .requestEachCombined(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .subscribe { permission ->
                if (!permission.granted) {
                    showPermissionErrorAndFinish()
                } else {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, PreviewFragment())
                        .commitNow()
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                goToPreferencesActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun goToPreferencesActivity() {
        val intent = Intent(this, PreferencesActivity::class.java)
        startActivity(intent)
    }

    private fun showPermissionErrorAndFinish() {
        DialogHelper.showPermissionAlertDialog(this) { this.finish() }
    }
}