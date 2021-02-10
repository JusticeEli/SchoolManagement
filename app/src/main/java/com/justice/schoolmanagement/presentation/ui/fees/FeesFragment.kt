package com.justice.schoolmanagement.presentation.ui.fees

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.firestore.ChangeEventListener
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.*
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentFeesBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.presentation.utils.Constants.COLLECTION_FEES
import es.dmoral.toasty.Toasty


class FeesFragment : Fragment(R.layout.fragment_fees) {


    private var feesFragmentRecyclerAdapter: FeesFragmentRecyclerAdapter? = null

    lateinit var studentSnapshot: DocumentSnapshot

    lateinit var binding: FragmentFeesBinding
    private val firebaseFirestore = FirebaseFirestore.getInstance()

    lateinit var navController: NavController

    lateinit var originalList: List<DocumentSnapshot>
    lateinit var searchView: SearchView

    companion object {
        const val DEFAULT_FEES = 5000
        private const val TAG = "FeesFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFeesBinding.bind(view)
        navController = findNavController()
        studentSnapshot = ApplicationClass.studentSnapshot!!
        initRecyclerViewAdapter();
        initTotalAmountOfFeesToBePayed()
        setOnClickListeners()
        setSwipeListenerForItems()
        setHasOptionsMenu(true)
        initProgressBar()
    }

    private fun initTotalAmountOfFeesToBePayed() {
        studentSnapshot.toObject(StudentData::class.java)?.totalFees?.let { binding.totalEdtTxt.setText(it.toString()) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_blog, menu)

        val searchItem = menu.findItem(R.id.searchItem)
        searchView = searchItem.actionView as SearchView



        super.onCreateOptionsMenu(menu, inflater)
    }


    private fun initRecyclerViewAdapter() {

        val query: Query = studentSnapshot!!.reference.collection(COLLECTION_FEES).orderBy("date")
        val firestoreRecyclerOptions = FirestoreRecyclerOptions.Builder<StudentFees>().setQuery(query, StudentFees::class.java).setLifecycleOwner(viewLifecycleOwner).build()


        feesFragmentRecyclerAdapter = FeesFragmentRecyclerAdapter(this, firestoreRecyclerOptions)
        binding.recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.recyclerView.setAdapter(feesFragmentRecyclerAdapter)
    }

    private fun setOnClickListeners() {
        binding.addFeesBtn.setOnClickListener(View.OnClickListener {

            ApplicationClass.studentSnapshot = studentSnapshot
            ApplicationClass.documentSnapshot = null
            findNavController().navigate(R.id.action_feesFragment_to_feesEditFragment)
        })

      /*  binding.editBtn.setOnClickListener {
            binding.totalEdtTxt.isEnabled = true
        }*/
        binding.saveBtn.setOnClickListener {
            saveTotalAmountFees()
        }


        feesFragmentRecyclerAdapter!!.snapshots.addChangeEventListener(object : ChangeEventListener {
            override fun onChildChanged(type: ChangeEventType, snapshot: DocumentSnapshot, newIndex: Int, oldIndex: Int) {

            }

            override fun onDataChanged() {
                recalculateBalance()
            }

            override fun onError(e: FirebaseFirestoreException) {
             }
        })
    }

    private fun recalculateBalance() {
        Log.d(TAG, "recalculateBalance: ")
        val totalFees = if (binding.totalEdtTxt.text.isNullOrEmpty()) 0 else binding.totalEdtTxt.text.toString().toInt()
        var counter = 0
        for (studentfees in feesFragmentRecyclerAdapter!!.snapshots) {
            counter += studentfees.payedAmount
        }
        val balance = totalFees - counter
        binding.balanceEdtTxt.setText(balance.toString())


    }

    private fun saveTotalAmountFees() {
        val fees = binding.totalEdtTxt.text.toString().trim()
        if (fees.isNullOrEmpty()) {
            Toasty.error(requireContext(), "Please fill the total amount fees").show()
            return
        }


        val map = mapOf("totalFees" to fees.toInt())
        showProgress(true)
        Log.d(TAG, "saveTotalAmountFees: ${fees}")
        studentSnapshot.reference.set(map, SetOptions.merge()).addOnSuccessListener {
            Log.d(TAG, "saveTotalAmountFees:  fees saved")
            recalculateBalance()
            Toasty.success(requireContext(), "Success saving total amount").show()
            showProgress(false)
        }
    }

    private fun setSwipeListenerForItems() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                feesFragmentRecyclerAdapter!!.deleteStudentFromDatabase(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(binding.recyclerView)
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    lateinit var dialog: AlertDialog

    fun showProgress(show: Boolean) {

        if (show) {
            dialog.show()

        } else {
            dialog.dismiss()

        }

    }

    private fun initProgressBar() {

        dialog = setProgressDialog(requireContext(), "Loading..")
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
    }

    fun setProgressDialog(context: Context, message: String): AlertDialog {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = message
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20.toFloat()
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(ll)

        val dialog = builder.create()
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }

    //end progressbar

}
