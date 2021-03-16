package com.idn.todoapp.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.idn.todoapp.R
import com.idn.todoapp.data.models.Priority
import com.idn.todoapp.data.models.ToDoData
import com.idn.todoapp.data.viewmodel.ToDoViewModel
import com.idn.todoapp.databinding.FragmentUpdateBinding
import com.idn.todoapp.fragments.SharedViewModel
import com.idn.todoapp.fragments.list.ListFragmentDirections
import kotlinx.android.synthetic.main.fragment_update.*

class UpdateFragment : Fragment() {

    private val args by navArgs<UpdateFragmentArgs>()
    private val mSharedViewModel: SharedViewModel by viewModels()
    private val mToDoViewModel: ToDoViewModel by viewModels()

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        binding.args = args

        // Set has Menu
        setHasOptionsMenu(true)

        binding.spinnerPrioritiesCurrent.onItemSelectedListener = mSharedViewModel.listener

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menu_save -> updateItem()
            R.id.menu_delete -> confirmItemRemoval()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateItem() {
        val title = edt_title_current.text.toString()
        val description = edt_description_current.text.toString()
        val getPriority = spinner_priorities_current.selectedItem.toString()

        val validation = mSharedViewModel.verifyDataFromUser(title, description)
        if (validation){
            // update current item
            val updateItem = ToDoData(
                args.currentItem.id,
                title,
                mSharedViewModel.parsePriority(getPriority),
                description
            )
            mToDoViewModel.updateData(updateItem)
            Toast.makeText(requireContext(), "Successfully Updated!", Toast.LENGTH_SHORT).show()
            // navigate back
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        } else {
            Toast.makeText(requireContext(), "Please fill out all fields!", Toast.LENGTH_SHORT).show()
        }
    }

    // show AlertDialog to confirm item removal
    private fun confirmItemRemoval() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete '${args.currentItem.title}'?")
            .setMessage("Are you sure want to remove '${args.currentItem.title}'?")
            .setPositiveButton("Yes"){ _, _ ->
                mToDoViewModel.deleteData(args.currentItem)
                Toast.makeText(
                    requireContext(),
                    "Successfully Removed: ${args.currentItem.title}",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_updateFragment_to_listFragment)
            }
            .setNegativeButton("No"){_,_ ->}
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}