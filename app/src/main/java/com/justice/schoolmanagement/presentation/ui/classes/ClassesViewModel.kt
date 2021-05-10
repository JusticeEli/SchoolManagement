package com.justice.schoolmanagement.presentation.ui.classes


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ClassesViewModel:ViewModel() {
    private val _classesEvents = Channel<ClassesFragment.Event>()
    val classesEvents=_classesEvents.receiveAsFlow()

    fun setEvent(event:ClassesFragment.Event){
        viewModelScope.launch {
            when(event){
                is ClassesFragment.Event.ClassChoosen->{
                    _classesEvents.send(ClassesFragment.Event.ClassChoosen(event.classNumber))

                }
            }
        }
    }
}