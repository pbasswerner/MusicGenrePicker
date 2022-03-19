package com.pison.pisonsdkandroidskeleton.landing

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.scheduler.createMainScheduler
import com.badoo.reaktive.scheduler.mainScheduler
import com.pison.client.PisonRemoteDevice
import com.pison.pisonsdkandroidskeleton.Application.Companion.sdk
import com.pison.shared.generated.ImuGesture

class LandingViewModel : ViewModel() {

    private var deviceDisposable: Disposable = Disposable()
    private var gestureDisposable: Disposable = Disposable()


    private val _connectedDevice = MutableLiveData<PisonRemoteDevice>()
    val connectedDevice: LiveData<PisonRemoteDevice>
        get() = _connectedDevice

    private val _gestureReceived = MutableLiveData<ImuGesture>()
    val gestureReceived: LiveData<ImuGesture>
        get() = _gestureReceived

    private val _gestureToNavigationMapping = MutableLiveData<Int>()
    val gestureToNavigationMapping: LiveData<Int>
        get() = _gestureToNavigationMapping


    fun disposeDisposables() {
        Log.d("LANDING", "disposed of all disposables")
        disposeOfGestureDisposable()
        disposeOfDeviceDisposable()
    }

    fun onConnectToDeviceButtonClicked() {
        connectToRemoteDevice()
    }

    fun connectToRemoteDevice() {
        deviceDisposable = sdk.monitorAnyDevice().subscribe(
            onNext = { pisonDevice ->
                Log.d("LANDING", "DEVICE CONNECTED SUCCESS")
                _connectedDevice.postValue(pisonDevice)
            },
            onError = { throwable ->
                Log.d("LANDING", "ERROR NO DEVICE CONNECTED $throwable")
            }
        )
    }

    fun onDeviceConnected(pisonRemoteDevice: PisonRemoteDevice) {
        monitorGestures(pisonRemoteDevice)
    }

    fun onGestureReceived(imuGesture: ImuGesture) {
        Log.d("LANDING", "ON GESTURERECEIVED CALLED WITH ${imuGesture.name.toString()}")
        when (imuGesture.name) {
            "ROLL_RIGHT" -> _gestureToNavigationMapping.postValue(1)
            "ROLL_LEFT" -> _gestureToNavigationMapping.postValue(2)
            "SWIPE_UP" -> _gestureToNavigationMapping.postValue(3)
            else -> {
                _gestureToNavigationMapping.postValue(4)
            }
        }
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

    private fun monitorGestures(pisonRemoteDevice: PisonRemoteDevice?) {
        Log.d("LANDING", "monitorGesturesCalled with $pisonRemoteDevice")

        if (pisonRemoteDevice != null) {
            gestureDisposable =
                pisonRemoteDevice.monitorGestures().observeOn(mainScheduler).subscribe(
                    onNext = { gestureReceived ->
                        Log.d("LANDING", "SUCCESS: Gesture made $gestureReceived")
                        onGestureReceived(gestureReceived)
                    },
                    onError = { throwable ->
                        Log.d("LANDING", "ERROR GESTURE: $throwable")
                    }
                )
        } else {
            Log.e("LANDING", "PISON DEVICE IS NULL")
        }
    }

    //todo clean this
    fun sanityCheck() {
        val disposablePisonRemoteDevice = sdk.monitorAnyDevice().observeOn(mainScheduler)
            .subscribe(
                onNext = { pisonDevice ->
                    Log.d("CHECKING", "SUCCESS: Device connected")
                    //monitoring gestures
                    val disposable =
                        pisonDevice.monitorGestures().observeOn(createMainScheduler()).subscribe(
                            onNext = { imuGesture ->
                                Log.d("CHECKING", "SUCCESS: Gesture made")
                                Log.d("CHECKING", imuGesture.name.toString())
                                Log.d("CHECKING", imuGesture.value.toString())
                            },
                            onError = { throwable ->
                                Log.d("CHECKING", "GESTURE ERROR: $throwable")
                            }
                        )

                    //monitoring activationStates
                    pisonDevice.monitorActivationStates().observeOn(mainScheduler).subscribe(
                        onNext = { activationStates ->
                            Log.d("CHECKING", "SUCCESS: Activation State Triggered")
                            Log.d("CHECKING", activationStates.index.name.toString())
                            Log.d("CHECKING", activationStates.index.value.toString())
                            Log.d("CHECKING", activationStates.thumb.name.toString())
                            Log.d("CHECKING", activationStates.thumb.value.toString())
                            Log.d("CHECKING", activationStates.watchCheck.name.toString())
                            Log.d("CHECKING", activationStates.watchCheck.value.toString())
                        },

                        onError = { throwable ->
                            Log.d("CHECKING", "ACTIVATION STATE ERROR: $throwable")
                        }
                    )

                    //monitoring
                    pisonDevice.monitorQuaternion().observeOn(mainScheduler).subscribe(
                        onNext = { quaternion ->
                            Log.d("CHECKING", "SUCCESS: Quartetion")
                            Log.d("CHECKING", quaternion.pitch.toString())
                            Log.d("CHECKING", quaternion.yaw.toString())
                            Log.d("CHECKING", quaternion.roll.toString())
                        },

                        onError = { throwable ->
                            Log.d("CHECKING", "QUARTETION ERROR: $throwable")
                        }
                    )

                },
                onError = { throwable ->
                    Log.d("CHECKING", "DEVICE CONNECTION ERROR $throwable")
                })
    }
}