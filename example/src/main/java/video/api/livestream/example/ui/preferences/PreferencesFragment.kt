package video.api.livestream.example.ui.preferences

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import video.api.livestream.ConfigurationHelper
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
        (findPreference(getString(R.string.video_resolution_key)) as ListPreference?)!!.apply {
            val resolutionsList = Resolution.values().map { it.toString() }.toTypedArray()
            entryValues = resolutionsList
            entries = resolutionsList
            if (value == null) {
                value = Resolution.RESOLUTION_720.toString()
            }
        }

        (findPreference(getString(R.string.video_fps_key)) as ListPreference?)!!.apply {
            val supportedFramerates = ConfigurationHelper.video.getSupportedFrameRates(
                requireContext(),
                "0"
            )
            entryValues.filter { fps ->
                supportedFramerates.any { it.contains(fps.toString().toInt()) }
            }.toTypedArray().run {
                this@apply.entries = this
                this@apply.entryValues = this
            }
        }

        (findPreference(getString(R.string.audio_sample_rate_key)) as ListPreference?)!!.apply {
            val supportedSampleRate =
                ConfigurationHelper.audio.getSupportedSampleRates()
            entries =
                supportedSampleRate.map { "${"%.1f".format(it.toString().toFloat() / 1000)} kHz" }
                    .toTypedArray()
            entryValues = supportedSampleRate.map { "$it" }.toTypedArray()
            if (entry == null) {
                value = "44100"
            }
        }
    }
}