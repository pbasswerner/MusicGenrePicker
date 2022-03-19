package com.pison.pisonsdkandroidskeleton.genrePlayer

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.scheduler.mainScheduler
import com.pison.client.PisonRemoteDevice
import com.pison.pisonsdkandroidskeleton.Application
import com.pison.pisonsdkandroidskeleton.spotifyservice.PlayingState
import com.pison.pisonsdkandroidskeleton.spotifyservice.SpotifyService
import com.pison.shared.generated.ImuGesture
import com.pison.shared.generated.Quaternion
import com.spotify.protocol.types.Track
import java.lang.Integer.sum


class FlamencoPlayerViewModel : ViewModel() {


    private var deviceDisposable: Disposable = Disposable()
    private var gestureDisposable: Disposable = Disposable()
    private var quartenionDisposable: Disposable = Disposable()

    private val flamencoPlayListUri = "spotify:playlist:37i9dQZF1DWTfrrcwZAlVH"

    private val _connectedDeviceLiveData = MutableLiveData<PisonRemoteDevice>()
    val connectedDeviceLiveData: LiveData<PisonRemoteDevice>
        get() = _connectedDeviceLiveData

    private val _gestureLiveData = MutableLiveData<ImuGesture>()
    val gestureLiveData: LiveData<ImuGesture>
        get() = _gestureLiveData

    private val _quartenionLiveData = MutableLiveData<Quaternion>()
    val quarternionLiveData: LiveData<Quaternion>
        get() = _quartenionLiveData

    private val _trackLiveData = MutableLiveData<Track>()
    val trackLiveData: LiveData<Track>
        get() = _trackLiveData

    private val _playingStateLiveData = MutableLiveData<PlayingState>()
    val playingStateLiveData: LiveData<PlayingState>
        get() = _playingStateLiveData

    private val _songNameAndArtist = MutableLiveData<String>()
    val songNameAndArtist: LiveData<String>
        get() = _songNameAndArtist

    private val _trackImageLiveData = MutableLiveData<Bitmap>()
    val trackImageLiveData: LiveData<Bitmap>
        get() = _trackImageLiveData

    private val _songDurationLiveData = MutableLiveData<Long>()
    val songDurationLiveData: LiveData<Long>
        get() = _songDurationLiveData

    private val _seekBarProgress = MutableLiveData<Int>()
    val seekBarProgress: LiveData<Int>
        get() = _seekBarProgress


    private val _gestureToNavigationMappingLiveData = MutableLiveData<Int>()
    val gestureToNavigationMappingLiveData: LiveData<Int>
        get() = _gestureToNavigationMappingLiveData


    fun onPlayButtonClicked() {
        SpotifyService.play(flamencoPlayListUri)
    }

    fun onPauseButtonClicked() {
        SpotifyService.pause()
    }

    fun onResumeButtonClicked() {
        SpotifyService.resume()
    }

    fun disconnectSpotifyService() {
        SpotifyService.disconnect()
    }

    fun updatePlayingStateLiveData(playingState: PlayingState) {
        _playingStateLiveData.postValue(playingState)
    }

    fun updateTrackLiveData(track: Track) {
        _trackLiveData.postValue(track)
    }

    fun connectSpotifyService(context: Context) {
        SpotifyService.connect(context) { isConnected ->
            Log.d("FLAMENCO VIEWMODEL", "SpotifyAppRemoteConected")
            onSpotifyRemoteAppConnected()
        }
    }

    fun onSpotifyRemoteAppConnected() {
        SpotifyService.play(flamencoPlayListUri)

        SpotifyService.subscribeToChanges { track ->
            updateTrackLiveData(track)
            updateSongDurationLiveData(track.duration)

        }
        SpotifyService.getPlayingState { playingState ->
            updatePlayingStateLiveData(playingState)
        }


    }

    private fun updateSongDurationLiveData(duration: Long) {
        _songDurationLiveData.postValue(duration)
    }

    private fun updateTrackImageLiveData(trackImageBitmap: Bitmap) {
        _trackImageLiveData.postValue(trackImageBitmap)
    }


    fun onRemoteDeviceConnected(pisonDevice: PisonRemoteDevice) {
        monitorQuartenion(pisonDevice)
        monitorGestures(pisonDevice)
    }

    private fun monitorGestures(pisonDevice: PisonRemoteDevice) {
        Log.d("LANDING", "monitorGesturesCalled with $pisonDevice")

        if (pisonDevice != null) {
            gestureDisposable =
                pisonDevice.monitorGestures().observeOn(mainScheduler).subscribe(
                    onNext = { gestureReceived ->
                        Log.d("FLAMENCO", "SUCCESS: Gesture made $gestureReceived")
                        onGestureReceived(gestureReceived)
                    },
                    onError = { throwable ->
                        Log.d("FLAMENCO", "ERROR GESTURE: $throwable")
                    }
                )
        } else {
            Log.e("LANDING", "PISON DEVICE IS NULL")
        }
    }

    private fun monitorQuartenion(pisonDevice: PisonRemoteDevice) {
        quartenionDisposable = pisonDevice.monitorQuaternion().observeOn(mainScheduler).subscribe(
            onNext = { quaternion ->
                Log.d("FLAMENCO PLAYER", "SUCCESS: Quartetion")
                Log.d("FLAMENCO PLAYER", quaternion.pitch.toString())
                Log.d("FLAMENCO PLAYER", quaternion.yaw.toString())
                Log.d("FLAMENCO PLAYER", quaternion.roll.toString())
                updateQuartenionLiveData(quaternion)
            },

            onError = { throwable ->
                Log.d("FLAMENCO PLAYER", "QUARTETION ERROR: $throwable")
            }
        )
    }

    private fun updateQuartenionLiveData(quaternion: Quaternion) {
        _quartenionLiveData.postValue(quaternion)
    }


    fun onGestureReceived(gesture: ImuGesture) {
        Log.d("FLAMENCO", "ON GESTURERECEIVED CALLED WITH ${gesture.name.toString()}")
        when (gesture.name) {
            "ROLL_RIGHT" -> _gestureToNavigationMappingLiveData.postValue(1)
            "ROLL_LEFT" -> _gestureToNavigationMappingLiveData.postValue(2)
            else -> {
                _gestureToNavigationMappingLiveData.postValue(3)
            }
        }
    }

    fun onQuartenionReceived(quartenion: Quaternion) {

        if (quartenion.pitch > 89) {
            Log.d("FLAMENCO", "Quartenion pitch is higher than 89")
            //updateSeekBarProgress(progress)
            updateSeekBarProgress(sum(quartenion.yaw.toInt(), 180))
        }
    }

    fun onTrackReceived(track: Track) {
        //update image
        formatSongNameAndArtist(track)

        updateSongDurationLiveData(track.duration)

        SpotifyService.getCurrentTrackImage { trackImage ->
            updateTrackImageLiveData(trackImage)
        }
        SpotifyService.getPlayingState { playingState ->
            updatePlayingStateLiveData(playingState)
        }
    }

    private fun formatSongNameAndArtist(track: Track) {
        val artist = track.artist.name
        val songName = track.name
        _songNameAndArtist.value = "$songName by $artist"
    }


    fun connectToRemoteDevice() {
        deviceDisposable = Application.sdk.monitorAnyDevice().subscribe(
            onNext = { pisonDevice ->
                Log.d("FLAMENCO PLAYER", "DEVICE CONNECTED SUCCESS")
                _connectedDeviceLiveData.postValue(pisonDevice)
            },
            onError = { throwable ->
                Log.d("FLAMENCO PLAYER", "ERROR NO DEVICE CONNECTED $throwable")
            }
        )
    }

    fun disposeDisposables() {
        Log.d("FLAMENCO PLAYER", "disposed of all disposables")
        disposeOfGestureDisposable()
        disposeOfDeviceDisposable()
        disposeOfQuartenionDisposable()
    }

    fun disposeOfDeviceDisposable() {
        if (!deviceDisposable.isDisposed) {
            deviceDisposable.dispose()
        }
    }

    fun disposeOfGestureDisposable() {
        if (!deviceDisposable.isDisposed) {
            deviceDisposable.dispose()
        }
    }

    fun disposeOfQuartenionDisposable() {
        if (!quartenionDisposable.isDisposed) {
            quartenionDisposable.dispose()
        }
    }

    private fun updateSeekBarProgress(progress: Int) {
        _seekBarProgress.postValue(progress)
    }

}


