package com.pison.pisonsdkandroidskeleton.genrePlayer

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.pison.pisonsdkandroidskeleton.R
import com.pison.pisonsdkandroidskeleton.databinding.FlamencoPlayerFragmentBinding
import com.pison.pisonsdkandroidskeleton.spotifyservice.PlayingState


class FlamencoPlayerFragment : Fragment() {

    private lateinit var viewModel: FlamencoPlayerViewModel
    private lateinit var binding: FlamencoPlayerFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.flamenco_player_fragment,
            container,
            false
        )
        viewModel = ViewModelProvider(this).get(FlamencoPlayerViewModel::class.java)

        viewModel.connectedDeviceLiveData.observe(viewLifecycleOwner, Observer { pisonDevice ->
            viewModel.onRemoteDeviceConnected(pisonDevice)
        })

        viewModel.gestureLiveData.observe(viewLifecycleOwner, Observer { gesture ->
            viewModel.onGestureReceived(gesture)
        })

        viewModel.gestureToNavigationMappingLiveData.observe(viewLifecycleOwner, { gestureInt ->
            when (gestureInt) {
                1 -> navigateToRockPlayer()
                else -> makeToastForInvalidGesture()
            }
        })

        viewModel.quarternionLiveData.observe(viewLifecycleOwner, Observer { quartenion ->
            viewModel.onQuartenionReceived(quartenion)

        })

        viewModel.seekBarProgress.observe(viewLifecycleOwner, Observer { progress ->
            updateSeekBar(progress)
        })

        viewModel.trackLiveData.observe(viewLifecycleOwner, Observer { track ->
            viewModel.onTrackReceived(track)
        })

        viewModel.songNameAndArtist.observe(
            viewLifecycleOwner,
            Observer { formattedSongAndArtistName ->
                updateSongNameAndArtist(formattedSongAndArtistName)
            })

        viewModel.playingStateLiveData.observe(viewLifecycleOwner, Observer { playingState ->
            updatePlayingStateUi(playingState)
        })

        viewModel.trackImageLiveData.observe(viewLifecycleOwner, Observer { image ->
            updateTrackImage(image)
        })


        binding.playButton.setOnClickListener {
            viewModel.onPlayButtonClicked()
            showPauseButton()
        }

        binding.pauseButton.setOnClickListener {
            viewModel.onPauseButtonClicked()
            showResumeButton()
        }

        binding.resumeButton.setOnClickListener {
            viewModel.onResumeButtonClicked()
            showPauseButton()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.disconnectSpotifyService()

        activity?.let {
            viewModel.connectSpotifyService(it)
        }

        viewModel.connectToRemoteDevice()
        setSeekbar()
    }

    private fun setSeekbar() {
        binding.croller.progress = 0
        binding.croller.max = 180
        binding.croller.sweepAngle = 180
        binding.croller.label = ""
    }

    private fun showPlayButton() {
        binding.playButton.visibility = View.VISIBLE
        binding.pauseButton.visibility = View.GONE
        binding.resumeButton.visibility = View.GONE
    }

    private fun showPauseButton() {
        binding.playButton.visibility = View.GONE
        binding.pauseButton.visibility = View.VISIBLE
        binding.resumeButton.visibility = View.GONE
    }

    private fun showResumeButton() {
        binding.playButton.visibility = View.GONE
        binding.pauseButton.visibility = View.GONE
        binding.resumeButton.visibility = View.VISIBLE
    }

    private fun updateTrackImage(bitmap: Bitmap) {
        binding.trackImageView.setImageBitmap(bitmap)
    }

    private fun updateSeekBar(progress: Int) {
        binding.croller.progress = progress
    }

    private fun updateSongNameAndArtist(songNameAndArtist: String) {
        binding.songName.text = songNameAndArtist
    }


    private fun updatePlayingStateUi(playingState: PlayingState) {
        when (playingState) {
            PlayingState.PLAYING -> showPauseButton()
            PlayingState.STOPPED -> showPlayButton()
            PlayingState.PAUSED -> showResumeButton()
        }
    }

    private fun makeToastForInvalidGesture() {
        Toast.makeText(
            this.context,
            "Gesture not recognized, try rolling left to navigate to Rock Genre Player",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun navigateToRockPlayer() {
        //todo navigateToRockPlayer
    }

    override fun onStop() {
        super.onStop()
        viewModel.disconnectSpotifyService()
        viewModel.disposeDisposables()
    }
}