package com.justice.schoolmanagement.presentation.ui.exam

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.justice.schoolmanagement.R

class ExamAnswerAdapter internal constructor(private val answerList: MutableList<Answer>) : RecyclerView.Adapter<ExamAnswerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_set_answers, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(answerList[position])
    }

    override fun getItemCount(): Int {
        return answerList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val numberTxtView: TextView
        private val radioGroup: RadioGroup
        private val A_rb: RadioButton
        private val B_rb: RadioButton
        private val C_rb: RadioButton
        private val D_rb: RadioButton
        fun bind(answer: Answer) {
            numberTxtView.setText("${
                answer.number + 1
            }")
            when (answer.choice?.toLowerCase()) {
                "a" -> A_rb.isChecked = true
                "b" -> B_rb.isChecked = true
                "c" -> C_rb.isChecked = true
                "d" -> D_rb.isChecked = true
            }
        }

        init {
            numberTxtView = itemView.findViewById(R.id.numberTxtView)
            radioGroup = itemView.findViewById(R.id.radioGroup)
            A_rb = itemView.findViewById(R.id.aRB)
            B_rb = itemView.findViewById(R.id.bRB)
            C_rb = itemView.findViewById(R.id.cRB)
            D_rb = itemView.findViewById(R.id.dRB)
            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                val radioButton = itemView.findViewById<RadioButton>(checkedId)
                Log.d(TAG, "onCheckedChanged: Question " + adapterPosition + " radiobutton " + radioButton.text.toString() + " checked")
                answerList[adapterPosition].choice=radioButton.text.toString().toLowerCase()
            }
        }
    }

    companion object {
        private const val TAG = "ExamAnswerAdapter"
    }
}