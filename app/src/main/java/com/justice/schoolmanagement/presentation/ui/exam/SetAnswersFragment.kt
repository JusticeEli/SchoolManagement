package com.justice.schoolmanagement.presentation.ui.exam

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentSetAnswersBinding
import com.justice.schoolmanagement.presentation.utils.Constants
import com.justice.schoolmanagement.presentation.utils.Constants.teachersAnswers
import java.lang.reflect.Type
import java.util.*


class SetAnswersFragment : Fragment(R.layout.fragment_set_answers) {

    companion object {
        private const val TAG = "SetAnswersFragment"
    }

    lateinit var sharedPreferences: SharedPreferences
    private val KEY_ANSWERS = "teacher_answers"
    lateinit var answerAdapter: ExamAnswerAdapter
    lateinit var binding: FragmentSetAnswersBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSetAnswersBinding.bind(view)
        sharedPreferences = requireContext().getSharedPreferences("shared_pref_teacher_answers", Context.MODE_PRIVATE)

        fetchAnswerFromSharedPref()
        initRecyclerView()
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.submitBtn.setOnClickListener {
            submitBtnClicked()
        }
    }

    private fun submitBtnClicked() {
        saveAnswersToSharedPref()
        findNavController().popBackStack()
    }

    private fun initRecyclerView() {
        Constants.teachersAnswers.sortBy {
            it.number
        }

        answerAdapter = ExamAnswerAdapter(teachersAnswers)
        binding.recyclerView.apply {
            setLayoutManager(LinearLayoutManager(requireContext()))
            setHasFixedSize(true)
            setAdapter(answerAdapter)

        }

    }


    fun saveAnswersToSharedPref() {
        val gson = Gson()
        val json = gson.toJson(Constants.teachersAnswers)

        sharedPreferences.edit().putString(KEY_ANSWERS, json).commit()

    }

    fun fetchAnswerFromSharedPref() {
        Log.d(TAG, "fetchAnswerFromSharedPref: ")

        val gson = Gson()
        val json: String? = sharedPreferences.getString(KEY_ANSWERS, null)
        if (json == null) {
            Log.d(TAG, "fetchAnswerFromSharedPref: No default answers found")
            Constants.teachersAnswers.clear()
            for (i in 0..49) {
                val answer = Answer()
                answer.choice = "a"
                answer.number = i
                Constants.teachersAnswers.add(answer)
            }
        } else {
            Log.d(TAG, "fetchAnswerFromSharedPref: default anwers were found")
            val type: Type = object : TypeToken<ArrayList<Answer?>?>() {}.getType()
            Constants.teachersAnswers = gson.fromJson(json, type)
        }


    }

}