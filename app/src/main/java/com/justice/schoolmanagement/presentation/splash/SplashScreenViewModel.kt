package com.justice.schoolmanagement.presentation.splash

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import com.justice.schoolmanagement.presentation.ui.admin.AdminData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class SplashScreenViewModel @ViewModelInject constructor(private val repository: SplashScreenRepository) : ViewModel() {

    private val TAG = "SplashScreenViewModel"

    private val _userSignUpProcessStatus = Channel<Resource<FirebaseUser>>()
    val userSignUpProcessStatus = _userSignUpProcessStatus.receiveAsFlow()
   val checkIsUserLoggedInStatus = repository.checkIsUserLoggedIn()
    private val _userSetupStatus = Channel<Resource<DocumentSnapshot>>()
    val userSetupStatus = _userSetupStatus.receiveAsFlow()
    private val _splashScreenEvents = Channel<SplashScreenActivity.Event>()
    val splashScreenEvents = _splashScreenEvents.receiveAsFlow()


    fun setEvent(event: SplashScreenActivity.Event) {
        viewModelScope.launch {
            when (event) {
                is SplashScreenActivity.Event.CheckIfInstitutionCodeExists -> {
                    checkIfInstitutionCodeExists()
                }
                is SplashScreenActivity.Event.CheckIfUserIsSetup -> {
                    checkIfUserIsSetUp()

                }
                is SplashScreenActivity.Event.GoToDashBoard -> {
                    _splashScreenEvents.send(SplashScreenActivity.Event.GoToDashBoard)
                }
                is SplashScreenActivity.Event.GoToSetupScreen -> {
                    _splashScreenEvents.send(SplashScreenActivity.Event.GoToSetupScreen)
                }
                is SplashScreenActivity.Event.GoToAdminScreen -> {
                    _splashScreenEvents.send(SplashScreenActivity.Event.GoToAdminScreen)
                }

            }
        }
    }

    private val _checkIfUserIsSetupStatus = Channel<Resource<DocumentSnapshot>>()
    val checkIfUserIsSetupStatus = _checkIfUserIsSetupStatus.receiveAsFlow()

    private suspend fun checkIfUserIsSetUp() {
        _checkIfUserIsSetupStatus.send(Resource.loading(""))
        repository.checkIfUserIsSetup().collect {
            when (it.status) {
                Resource.Status.LOADING -> {
                    _checkIfUserIsSetupStatus.send(it)
                }
                Resource.Status.SUCCESS -> {
                    if (it.data!!.exists()) {
                        _checkIfUserIsSetupStatus.send(it)
                    } else {
                        _checkIfUserIsSetupStatus.send(Resource.error(Exception("Document Does not exist")))
                    }
                }
                Resource.Status.ERROR -> {
                    _checkIfUserIsSetupStatus.send(it)
                }
            }
        }

    }

    private val _checkIfInstitutionCodeExistsStatus = Channel<Resource<AdminData>>()
    val checkIfInstitutionCodeExistsStatus = _checkIfInstitutionCodeExistsStatus.receiveAsFlow()

    private suspend fun checkIfInstitutionCodeExists() {
        _checkIfInstitutionCodeExistsStatus.send(Resource.loading(""))
        repository.checkIfInstitutionCodeExits().collect {
            when (it.status) {
                Resource.Status.SUCCESS -> {

                    val gson = Gson()
                    val adminData = gson.fromJson(it.data, AdminData::class.java)
                    Log.d(TAG, "checkIfInstitutionCodeExits: code: ${adminData.institutionCode}")
                      _checkIfInstitutionCodeExistsStatus.send(Resource.success(adminData))
                }
                Resource.Status.ERROR -> {
                    _checkIfInstitutionCodeExistsStatus.send(Resource.error(it.exception))

                }
            }
        }

    }
}