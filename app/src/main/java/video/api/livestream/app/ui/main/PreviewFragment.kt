package video.api.livestream.app.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import video.api.livestream.app.R
import video.api.livestream.app.databinding.FragmentPreviewBinding
import video.api.livestream.app.ui.utils.DialogHelper

class PreviewFragment : Fragment() {
    private val viewModel: PreviewViewModel by viewModels()
    private lateinit var binding: FragmentPreviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()

        viewModel.buildLiveStream(binding.apiVideoView)
        binding.liveButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.startStream()
            } else {
                viewModel.stopStream()
            }
        }

        binding.switchButton.setOnClickListener {
            viewModel.switchCamera()
        }


        binding.muteButton.setOnClickListener {
            viewModel.toggleMute()
        }

        viewModel.onAuthError.observe(viewLifecycleOwner) {
            showError(getString(R.string.error), getString(R.string.authentication_failed))
        }

        viewModel.onConnectionFailed.observe(viewLifecycleOwner) {
            showError(getString(R.string.error), it)
        }

        viewModel.onDisconnect.observe(viewLifecycleOwner) {
            showDisconnection()
        }
    }

    private fun showError(title: String, message: String) {
        binding.liveButton.isChecked = false
        DialogHelper.showAlertDialog(requireContext(), title, message)
    }

    private fun showDisconnection() {
        Toast.makeText(requireContext(), getString(R.string.disconnection), Toast.LENGTH_SHORT)
            .show()
    }
}