package video.api.livestream.example.ui.preferences

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import video.api.livestream.app.R

class PreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.preferences, PreferencesFragment())
                .commit()
        }
    }
}