package com.justice.schoolmanagement.presentation.ui.fees

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.student.StudentsFragment.Companion.STUDENT_ARGS
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class FeesViewModel @ViewModelInject constructor(private val repository: FeesRepository, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {


    private val TAG = "FeesViewModel"

    val getAllFees = repository.getAllFees(savedStateHandle.get<StudentData>(STUDENT_ARGS)!!.id!!)
    val getStudent = repository.getStudent(savedStateHandle.get<StudentData>(STUDENT_ARGS)!!.id!!)
    private val currentStudent = MutableLiveData<DocumentSnapshot>()
    fun setCurrentStudent(snapshot: DocumentSnapshot) {
        currentStudent.value = snapshot
    }

    private val _feesEvents = Channel<FeesFragment.Event>()
    val feesEvents = _feesEvents.receiveAsFlow()
    fun setEvent(event: FeesFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is FeesFragment.Event.DeleteFees -> {
                    _feesEvents.send(FeesFragment.Event.DeleteFees(event.snapshot))

                }
                is FeesFragment.Event.SwipedFees -> {
                    _feesEvents.send(FeesFragment.Event.SwipedFees(event.snapshot))

                }
                is FeesFragment.Event.DeleteFeesConfirmed -> {
                    deleteFees(event.snapshot)

                }
                is FeesFragment.Event.EditFees -> {
                    _feesEvents.send(FeesFragment.Event.EditFees(event.snapshot))

                }
                is FeesFragment.Event.AddFees -> {
                    _feesEvents.send(FeesFragment.Event.AddFees)
                }
                is FeesFragment.Event.SaveTotalAmount -> {
                    saveTotalAmountFees(event.totalAmount)
                }
                is FeesFragment.Event.RecalculateBalance -> {
                    recalculateBalance(event.fees, event.feesList)
                }
            }
        }

    }

    private val _recalculateBalanceStatus = Channel<String>()
    val recalculateBalanceStatus = _recalculateBalanceStatus.receiveAsFlow()

    private suspend fun recalculateBalance(feesToBePaid: Int, feesList: List<DocumentSnapshot>) {
        Log.d(TAG, "recalculateBalance: ")
        var feesAlreadyPaid = 0

        feesList.forEach {
            val singleFee = it.toObject(StudentFees::class.java)!!
            feesAlreadyPaid += singleFee.payedAmount
        }

        val balance = feesToBePaid - feesAlreadyPaid
    _recalculateBalanceStatus.send(balance.toString())

    }

    private suspend fun saveTotalAmountFees(originalFees: String) {
        if (fieldIsEmpty(originalFees)) {
            _saveTotalAmountFeesStatus.send(Resource.error(Exception("Please Fill the Fees Field!!")))
        } else {

            var trimedFees = trimData(originalFees)

            try {
                val fees = trimedFees.toInt()
                uploadFees(fees)
            } catch (e: Exception) {
                _saveTotalAmountFeesStatus.send(Resource.error(e))
                e.printStackTrace()
            }


        }
    }

    private suspend fun uploadFees(fees: Int) {
        Log.d(TAG, "uploadFees: fees:$fees")
        val map = mapOf("totalFees" to fees)

        repository.uploadFees(map, currentStudent.value!!).collect {
            Log.d(TAG, "uploadFees: ${it.status.name}")
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {
                    _saveTotalAmountFeesStatus.send(Resource.success(fees))

                }
                Resource.Status.ERROR -> {
                    _saveTotalAmountFeesStatus.send(Resource.error(it.exception))

                }
            }
        }


    }

    private fun trimData(totalAmount: String): String {
        return totalAmount.trim()
    }

    private fun fieldIsEmpty(totalAmount: String): Boolean {
        return totalAmount.isBlank()
    }

    private val _deleteFeesStatus = Channel<Resource<DocumentSnapshot>>()
    val deleteFeesStatus = _deleteFeesStatus.receiveAsFlow()

    private suspend fun deleteFees(snapshot: DocumentSnapshot) {
        repository.deleteFees(snapshot).collect {
            Log.d(TAG, "deleteFees: ${it.status.name}")
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {

                }
                Resource.Status.ERROR -> {

                }
            }

        }
    }

    private val _saveTotalAmountFeesStatus = Channel<Resource<Int>>()
    val saveTotalAmountFeesStatus = _saveTotalAmountFeesStatus.receiveAsFlow()


}