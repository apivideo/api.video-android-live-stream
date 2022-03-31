package video.api.livestream.example.ui.main

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import video.api.livestream.app.R
import video.api.livestream.app.databinding.FragmentPreviewBinding
import video.api.livestream.example.ui.utils.DialogHelper

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
                /**
                 * Lock orientation in live to avoid stream interruption if
                 * user turns the device.
                 */
                requireActivity().requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LOCKED
                viewModel.startStream()
            } else {
                viewModel.stopStream()
                requireActivity().requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }

        binding.switchButton.setOnClickListener {
            viewModel.switchCamera()
        }


        binding.muteButton.setOnClickListener {
            viewModel.toggleMute()
        }

        viewModel.onError.observe(viewLifecycleOwner) {
            binding.liveButton.isChecked = false
            manageError(getString(R.string.error), it)
        }

        viewModel.onDisconnect.observe(viewLifecycleOwner) {
            binding.liveButton.isChecked = false
            showDisconnection()
        }
    }

    private fun manageError(title: String, message: String) {
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        DialogHelper.showAlertDialog(requireContext(), title, message)
    }

    private fun showDisconnection() {
        Toast.makeText(requireContext(), getString(R.string.disconnection), Toast.LENGTH_SHORT)
            .show()
    }
}