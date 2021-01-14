package com.justice.schoolmanagement.presentation.ui.teacher

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
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentTeachersBinding
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.presentation.utils.Constants

class TeachersFragment : Fragment(R.layout.fragment_teachers) {
    lateinit var originalList: List<DocumentSnapshot>
    lateinit var searchView: SearchView

    private var teachersActivityRecyclerAdapter: TeachersActivityRecyclerAdapter? = null
    lateinit var teacherFilterAdapter: TeacherFilterAdapter


    lateinit var binding: FragmentTeachersBinding
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTeachersBinding.bind(view)
        navController = findNavController()
        setUpRecyclerViewAdapter();
        setSwipeListenerForItems()
        setHasOptionsMenu(true)
        initProgressBar()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_blog, menu)
        val searchItem = menu.findItem(R.id.searchItem)
        searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {

                Log.d(TAG, "onQueryTextChange: Text: ${s}")

                if (s.isNullOrEmpty()) {
                    startUsingRealTimeAdapter()
                    Log.d(TAG, "onQueryTextChange: startUsingRealTimeAdapter")

                } else {
                    startUsingParentFilter(s.trim())
                    Log.d(TAG, "onQueryTextChange:  startUsingParentFilter ")

                }
                return true

            }
        })



        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun startUsingRealTimeAdapter() {
        binding.recyclerView.adapter = teachersActivityRecyclerAdapter
    }

    private fun startUsingParentFilter(string: String) {
        originalList = mutableListOf<DocumentSnapshot>()
        (originalList as MutableList<DocumentSnapshot>)?.clear()
        teachersActivityRecyclerAdapter?.snapshots?.forEachIndexed { index, _ ->
            (originalList as MutableList<DocumentSnapshot>).add(teachersActivityRecyclerAdapter!!.snapshots.getSnapshot(index))
        }
        //     teacherFilterAdapter.submitList(originalList)
        binding.recyclerView.adapter = teacherFilterAdapter

        teacherFilterAdapter.getFilter().filter(string)

    }

    private fun setUpRecyclerViewAdapter() {
        teacherFilterAdapter = TeacherFilterAdapter(this)

        val query: Query = firebaseFirestore.collection(Constants.COLLECTION_TEACHERS)
        val firestoreRecyclerOptions = FirestoreRecyclerOptions.Builder<TeacherData>().setQuery(query) { snapshot ->
            val teacherData = snapshot.toObject(TeacherData::class.java)
            teacherData!!.id = snapshot.id
            teacherData
        }.setLifecycleOwner(viewLifecycleOwner).build()
        teachersActivityRecyclerAdapter = TeachersActivityRecyclerAdapter(this, firestoreRecyclerOptions)
        binding.recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.recyclerView.setAdapter(teachersActivityRecyclerAdapter)
    }

    private fun setSwipeListenerForItems() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                teachersActivityRecyclerAdapter!!.deleteTeacherDataFromDatabase(viewHolder.adapterPosition)
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
    companion object {
        private const val TAG = "TeachersFragment"
    }
}
