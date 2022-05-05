package com.justice.schoolmanagement.presentation.ui.admin

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import com.justice.schoolmanagement.utils.Constants
import com.justice.schoolmanagement.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AdminViewModel @ViewModelInject constructor(private val repository: AdminRepository) : ViewModel() {

    private val TAG = "AdminViewModel"

    private val _fetchDataFromSharedPrefStatus = Channel<Resource<AdminData>>()
    val fetchDataFromSharedPrefStatus = _fetchDataFromSharedPrefStatus.receiveAsFlow()
    private val _adminEvents = Channel<AdminFragment.Event>()
    val adminEvents = _adminEvents.receiveAsFlow()


    fun setEvent(event: AdminFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is AdminFragment.Event.CheckIfAdminDataExists -> {
                    checkIfAdminDataExists()
                }
                is AdminFragment.Event.SubmitClicked -> {
                    submitClicked(event.adminData)
                }
                is AdminFragment.Event.SaveAdminDataInDB -> {
                    saveAdminDataInDb(event.adminData)
                }
                is AdminFragment.Event.CheckIfUserIsSetup -> {
                    checkIfUserIsSetUp()

                }
                is AdminFragment. Event.GoToDashBoard -> {
                    _adminEvents.send(AdminFragment.Event.GoToDashBoard)
                }
                is AdminFragment.Event.GoToSetupScreen -> {
                    _adminEvents.send(AdminFragment.Event.GoToSetupScreen)
                }
            }
        }

    }
    private val _checkIfUserIsSetupStatus = Channel<Resource<DocumentSnapshot>>()
    val checkIfUserIsSetupStatus = _checkIfUserIsSetupStatus.receiveAsFlow()

    private suspend fun checkIfUserIsSetUp() {
        Log.d(TAG, "checkIfUserIsSetUp: ")
        _checkIfUserIsSetupStatus.send(Resource.loading(""))
        viewModelScope.launch {
            repository.checkIfUserIsSetup().collect {
                Log.d(TAG, "checkIfUserIsSetUp: ${it.status.name}")
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
    }
    private suspend fun saveAdminDataInDb(adminData: AdminData) {
        if (fieldsAreEmpty(adminData)) {
            _saveAdminDataStatus.send(Resource.error(java.lang.Exception("Please Fill All Fields !!")))
            return
        }
        trimAdminData(adminData)
        repository.saveAdminDataInSharedPref(adminData)
        saveAdmin(adminData)
    }

    private suspend fun saveAdmin(adminData: AdminData) {
        _saveAdminDataStatus.send(Resource.loading(""))
        repository.saveAdmin(adminData).collect {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    _saveAdminDataStatus.send(Resource.success(it.data!!))
                }
                Resource.Status.ERROR -> {
                    _saveAdminDataStatus.send(it)
                }
            }
        }
    }





    private fun trimAdminData(adminData: AdminData) {
        adminData.institutionCode = adminData.institutionCode.trim()
        adminData.name = adminData.name.trim()
        adminData.phone = adminData.phone.trim()
    }

    private fun fieldsAreEmpty(adminData: AdminData): Boolean {
        return (adminData.institutionCode.isBlank() || adminData.name.isBlank() || adminData.phone.isBlank())
    }

    private val _saveAdminDataStatus = Channel<Resource<AdminData>>()
    val saveAdminDataStatus = _saveAdminDataStatus.receiveAsFlow()

    private suspend fun submitClicked(adminData: AdminData) {
        _saveAdminDataStatus.send(Resource.loading(""))

        if (adminData.institutionCode.isBlank()) {
            _saveAdminDataStatus.send(Resource.error(Exception("Please Fill Institution Code !!")))
            return
        }
        tryGettingAdminDataFromDatabase(adminData)


    }

    private suspend fun tryGettingAdminDataFromDatabase(adminData: AdminData) {
        repository.getAdminData(adminData.institutionCode).collect {
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {
                    if (it.data!!.exists()) {
                        val adminData = it.data!!.toObject(AdminData::class.java)!!
                        _saveAdminDataStatus.send(Resource.success(adminData))
                    } else {
                        _saveAdminDataStatus.send(Resource.empty())
                    }

                }
                Resource.Status.ERROR -> {
                    _saveAdminDataStatus.send(Resource.error(java.lang.Exception(it.message)))

                }
            }

        }
    }

    private suspend fun checkIfAdminDataExists() {
        _fetchDataFromSharedPrefStatus.send(Resource.loading(""))
        repository.checkIfInstitutionCodeExits().collect {
            when (it.status) {
                Resource.Status.SUCCESS -> {

                    val gson = Gson()
                    val adminData = gson.fromJson(it.data, AdminData::class.java)
                    Log.d(TAG, "checkIfInstitutionCodeExits: code: ${adminData.institutionCode}")
                    Constants.DOCUMENT_CODE = adminData.institutionCode
                    _fetchDataFromSharedPrefStatus.send(Resource.success(adminData))
                }
                Resource.Status.ERROR -> {
                    _fetchDataFromSharedPrefStatus.send(Resource.error(it.exception))

                }
            }
        }
    }

}