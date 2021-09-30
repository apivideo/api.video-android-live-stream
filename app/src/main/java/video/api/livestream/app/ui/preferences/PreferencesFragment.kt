package video.api.livestream.app.ui.preferences

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import video.api.livestream.app.R
import video.api.livestream.enums.Resolution

class PreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        inflatesPreferences()
    }

    private fun inflatesPreferences() {
        (findPreference(getString(R.string.video_resolution_key)) as ListPreference?)?.apply {
            val resolutionsList = Resolution.values().map { it.toString() }.toTypedArray()
            entryValues = resolutionsList
            entries = resolutionsList
            if (value == null) {
                value = Resolution.RESOLUTION_720.toString()
            }
        }
    }

}