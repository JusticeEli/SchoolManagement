package com.justice.schoolmanagement.presentation.ui.results

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ResultsViewModel @ViewModelInject constructor(private val repository: ResultsRepository) : ViewModel() {

    val getAllMarks = repository.getAllMarks()

    private val _resultEvents = Channel<ResultsFragment.Event>()
    val resultEvents = _resultEvents.receiveAsFlow()
    fun setEvent(event: ResultsFragment.Event) {
       viewModelScope.launch {
          when (event) {
             is ResultsFragment.Event.EditClicked -> {
                _resultEvents.send(ResultsFragment.Event.EditClicked(event.snapshot))
             }
          }
       }

    }
}