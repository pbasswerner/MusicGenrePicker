package com.pison.pisonsdkandroidskeleton.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.pison.pisonsdkandroidskeleton.R
import com.pison.pisonsdkandroidskeleton.databinding.LandingFragmentBinding

class LandingFragment : Fragment() {

    private lateinit var viewModel: LandingViewModel
    private lateinit var binding: LandingFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.landing_fragment,
            container,
            false
        )

        viewModel = ViewModelProvider(this).get(LandingViewModel::class.java)


        viewModel.connectedDevice.observe(viewLifecycleOwner, { pisonDevice ->
            viewModel.onDeviceConnected(pisonDevice)
        })

        viewModel.gestureReceived.observe(viewLifecycleOwner, { gesture ->
            viewModel.onGestureReceived(gesture)
        })

        viewModel.gestureToNavigationMapping.observe(viewLifecycleOwner, { gestureInt ->
            when (gestureInt) {
                1 -> navigateToFlamencoPlayer()
                2 -> navigateToRockPlayer()
                else -> makeToastForInvalidGesture()
            }
        })


        binding.connectToDeviceButton.setOnClickListener {
            viewModel.onConnectToDeviceButtonClicked()
        }

        viewModel.sanityCheck()

        return binding.root
    }

    private fun makeToastForInvalidGesture() {
        Toast.makeText(
            this.context,
            "Try to roll your wrist right or left, gesture not recognized",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun navigateToFlamencoPlayer() {
        if (view?.findNavController()?.currentDestination?.id == R.id.landingFragment) {
            view?.findNavController()
                ?.navigate(R.id.action_landingFragment_to_flamencoPlayerFragment)
        }
    }

    //todo extra screens when finished
    private fun navigateToRockPlayer() {
        //Todo add other screen here
    }

    private fun navigateToAlternativePlayer() {
        //Todo add other screen here
    }

    override fun onStop() {
        super.onStop()
        viewModel.disposeDisposables()
    }
}