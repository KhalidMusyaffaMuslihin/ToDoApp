package com.idn.todoapp.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.idn.todoapp.R
import com.idn.todoapp.data.models.ToDoData
import com.idn.todoapp.data.viewmodel.ToDoViewModel
import com.idn.todoapp.databinding.FragmentListBinding
import com.idn.todoapp.fragments.SharedViewModel
import com.idn.todoapp.fragments.list.adapter.ListAdapter
import com.idn.todoapp.utils.hideKeyboard
import jp.wasabeef.recyclerview.animators.LandingAnimator

class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    private val adapter: ListAdapter by lazy { ListAdapter() }

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Data Binding
        _binding = FragmentListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.mSharedViewModel = mSharedViewModel

//              pindahkan ke fungsi setupRecyclerview
//        rv_todo.adapter = adapter
//        rv_todo.layoutManager = LinearLayoutManager(requireActivity())

        // Setup RecyclerView
        setupRecyclerView()

        // Observe LiveData
        mToDoViewModel.getAllData.observe(viewLifecycleOwner, Observer { data ->
            mSharedViewModel.checkIfDatabaseEmpty(data)
            adapter.setData(data)
        })

        // Non-Aktifkan karena sudah digantikan oleh fungsi emptyDatabase di BindingAdapters
        /*mSharedViewModel.emptyDatabase.observe(viewLifecycleOwner, Observer {
            showEmptyDatabaseViews(it)
        })*/

        // set menu
        setHasOptionsMenu(true)

        // Hide soft Keyboard
        hideKeyboard(requireActivity())

        return binding.root
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.rvTodo
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.itemAnimator = LandingAnimator().apply {
            addDuration = 300
        }
        // Swipe to Delete
        swipeToDelete(recyclerView)
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = adapter.dataList[viewHolder.adapterPosition]
                // Delete Item
                mToDoViewModel.deleteData(deletedItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)

                // Restore Deleted Item
                restoreDeletedData(viewHolder.itemView, deletedItem)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedData(view: View, deletedItem: ToDoData) {
        val snackBar = Snackbar.make(
            view, "Deleted: '${deletedItem.title}'",
            Snackbar.LENGTH_LONG
        )
        snackBar.setAction("Undo") {
            mToDoViewModel.insertData(deletedItem)
        }
        snackBar.show()
    }

    // Non-Aktifkan karena sudah digantikan oleh fungsi emptyDatabase di BindingAdapters
/*    private fun showEmptyDatabaseViews(emptyDatabase: Boolean) {
        if (emptyDatabase){
            img_no_data.visibility = View.VISIBLE
            txt_no_data.visibility = View.VISIBLE
        } else {
            img_no_data.visibility = View.INVISIBLE
            txt_no_data.visibility = View.INVISIBLE
        }
    }*/

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete_all -> confirmRemoval()
            R.id.menu_priority_high -> mToDoViewModel.sortByHighPriority.observe(this, Observer {
                adapter.setData(it)
            })
            R.id.menu_priority_low -> mToDoViewModel.sortByLowPriority.observe(this, Observer {
                adapter.setData(it)
            })
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }


    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    private fun searchThroughDatabase(query: String) {
        val searchQuery = "%$query%"

        mToDoViewModel.searchDatabase(searchQuery).observe(this, Observer { list ->
            list?.let {
                adapter.setData(it)
            }
        })
    }

    // Show AlertDialog to Confirm Removal af All Items from Database Table
    private fun confirmRemoval() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Everything?")
            .setMessage("Are you sure want to remove everything?")
            .setPositiveButton("Yes") { _, _ ->
                mToDoViewModel.deleteAllData()
                Toast.makeText(
                    requireContext(),
                    "Successfully Removed Everything",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("No", null)
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}