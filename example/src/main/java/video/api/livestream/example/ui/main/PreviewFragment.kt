package video.api.livestream.example.ui.main

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
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

    /**
     * Zooming gesture
     *
     * scaleFactor > 1 == Zooming in
     * scaleFactor < 1 == Zooming out
     *
     * scaleFactor will start at a value of 1 when the gesture is begun.
     * Then its value will persist until the gesture has ended.
     * If we save the zoomRatio in savedScale when the gesture has begun,
     * we can easily add a relative scale to the zoom.
     *
     * If we are zooming out, the scale is between 0-1.
     * Meaning we can use this as a percentage from the savedScale
     *
     * Zooming in is linear zoom
     * Zooming out is percentage zoom between 1f & savedScale
     */
    private val pinchGesture: ScaleGestureDetector by lazy {
        ScaleGestureDetector(
            binding.apiVideoView.context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                private var savedZoomRatio: Float = 1f
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    viewModel.zoomRatio = if (detector.scaleFactor < 1) {
                        savedZoomRatio * detector.scaleFactor
                    } else {
                        savedZoomRatio + ((detector.scaleFactor - 1))
                    }
                    return super.onScale(detector)
                }

                override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                    detector.currentSpan
                    savedZoomRatio = viewModel.zoomRatio
                    return super.onScaleBegin(detector)
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("MissingPermission", "ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()

        // Listen to touch for zoom
        binding.apiVideoView.setOnTouchListener { _, event ->
            pinchGesture.onTouchEvent(event)
        }

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