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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.edward.nyansapo.wrappers.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentFeesBinding
import com.justice.schoolmanagement.presentation.ui.fees.FeesFragment.Event.EditFees
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect

const val DEFAULT_FEES = 5000
const val DATE = "date"

class FeesFragment : Fragment(R.layout.fragment_fees) {

    private val TAG = "FeesFragment"

    private lateinit var adapter: FeesAdapter


    lateinit var binding: FragmentFeesBinding

    lateinit var navController: NavController

    lateinit var searchView: SearchView


    private val viewModel: FeesViewModel by viewModels()
    private val navArgs: FeesFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFeesBinding.bind(view)
        initProgressBar()
        navController = findNavController()
        Log.d(TAG, "onViewCreated: student:${navArgs.student}")
        setHasOptionsMenu(true)

        initRecyclerViewAdapter();
        setOnClickListeners()
        setSwipeListenerForItems()
        subScribeToObservers()
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.getAllFees.collect {
                Log.d(TAG, "subScribeToObservers: getAllFees:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        adapter.submitList(it.data)
                    }
                    Resource.Status.EMPTY -> {
                        showProgress(false)
                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        showToastInfo("Error: ${it.exception?.message}")
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.feesEvents.collect {
                Log.d(TAG, "subScribeToObservers: feesEvents")

                when (it) {
                    is EditFees -> {
                        editFees(it.snapshot)
                    }
                    is Event.DeleteFees -> {
                        deleteFees(it.snapshot)
                    }
                    is Event.SwipedFees -> {
                        deleteFees(it.snapshot)
                    }
                    is Event.AddFees -> {
                        goToAddFeesScreen()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.getStudent.collect {
                Log.d(TAG, "subScribeToObservers: getStudent:${it.status.name}")
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        viewModel.setCurrentStudent(it.data!!)
                        initTotalAmountOfFeesToBePayed(it.data!!)

                    }
                    Resource.Status.ERROR -> {

                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.saveTotalAmountFeesStatus.collect {
                when (it.status) {
                    Resource.Status.LOADING -> {

                    }
                    Resource.Status.SUCCESS -> {
                        viewModel.setEvent(Event.RecalculateBalance(it.data!!, adapter.currentList))

                    }
                    Resource.Status.ERROR -> {

                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.recalculateBalanceStatus.collect {
                setBalance(it)
            }
        }
    }

    private fun setBalance(balance: String) {
        binding.balanceEdtTxt.setText(balance)
    }

    private fun goToAddFeesScreen() {
        findNavController().navigate(R.id.action_feesFragment_to_feesEditFragment)

    }

    private fun deleteFees(snapshot: DocumentSnapshot) {
        MaterialAlertDialogBuilder(requireContext()).setBackground(requireActivity().getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which ->
            val pos = adapter.currentList.indexOf(snapshot)
            adapter.notifyItemChanged(pos)

        }.setPositiveButton("yes") { dialog, which ->
            viewModel.setEvent(FeesFragment.Event.DeleteFeesConfirmed(snapshot))
        }.show()

    }

    private fun editFees(it: DocumentSnapshot) {
        val studentFees = it.toObject(StudentFees::class.java)!!
        navController.navigate(FeesFragmentDirections.actionFeesFragmentToFeesEditFragment(studentFees))
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun initTotalAmountOfFeesToBePayed(snapshot: DocumentSnapshot) {
        snapshot.toObject(StudentData::class.java)?.totalFees?.let { binding.totalEdtTxt.setText(it.toString()) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_blog, menu)

        val searchItem = menu.findItem(R.id.searchItem)
        searchView = searchItem.actionView as SearchView



        super.onCreateOptionsMenu(menu, inflater)
    }


    private fun initRecyclerViewAdapter() {

        adapter = FeesAdapter({ onDelete(it) }, { onEdit(it) })
        binding.recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.recyclerView.setAdapter(adapter)
    }

    private fun onEdit(it: DocumentSnapshot) {
        viewModel.setEvent(EditFees(it))
    }

    private fun onDelete(it: DocumentSnapshot) {
        viewModel.setEvent(Event.DeleteFees(it))
    }

    private fun setOnClickListeners() {
        binding.addFeesBtn.setOnClickListener {
            viewModel.setEvent(Event.AddFees)

        }


        binding.saveBtn.setOnClickListener {
            val fees = getFees()
            viewModel.setEvent(Event.SaveTotalAmount(fees))
        }


    }

    private fun getFees(): String {
        return binding.totalEdtTxt.text.toString()

    }

    private fun setSwipeListenerForItems() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val snapshot = adapter.currentList[viewHolder.bindingAdapterPosition]
                viewModel.setEvent(Event.SwipedFees(snapshot))
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

    sealed class Event {
        data class SwipedFees(val snapshot: DocumentSnapshot) : Event()
        data class DeleteFees(val snapshot: DocumentSnapshot) : Event()
        data class DeleteFeesConfirmed(val snapshot: DocumentSnapshot) : Event()
        data class EditFees(val snapshot: DocumentSnapshot) : Event()
        object AddFees : Event()
        data class SaveTotalAmount(val totalAmount: String) : Event()
        data class RecalculateBalance(val fees: Int, val feesList: List<DocumentSnapshot>) : Event()
    }

}
