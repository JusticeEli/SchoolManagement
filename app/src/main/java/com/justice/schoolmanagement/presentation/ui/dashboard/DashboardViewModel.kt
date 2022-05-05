package com.justice.schoolmanagement.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justice.schoolmanagement.utils.exhaustive
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DashboardViewModel: ViewModel() {

    private val _dashBoardEvents= Channel<DashboardFragment.Event>()
    val dashBoardEvents=_dashBoardEvents.receiveAsFlow()

    fun setEvent(event:DashboardFragment.Event){
        viewModelScope.launch {
            when(event){
                DashboardFragment.Event.TeacherClicked -> _dashBoardEvents.send(DashboardFragment.Event.TeacherClicked)
                DashboardFragment.Event.ParentClicked ->  _dashBoardEvents.send(DashboardFragment.Event.ParentClicked)
                DashboardFragment.Event.StudentClicked ->  _dashBoardEvents.send(DashboardFragment.Event.StudentClicked)
                DashboardFragment.Event.ClassesClicked ->  _dashBoardEvents.send(DashboardFragment.Event.ClassesClicked)
                DashboardFragment.Event.SubjectsClicked ->  _dashBoardEvents.send(DashboardFragment.Event.SubjectsClicked)
                DashboardFragment.Event.ResultsClicked ->  _dashBoardEvents.send(DashboardFragment.Event.ResultsClicked)
            }.exhaustive
        }
    }
}