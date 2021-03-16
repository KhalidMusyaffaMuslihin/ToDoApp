package com.idn.todoapp.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.idn.todoapp.data.ToDoDatabase
import com.idn.todoapp.data.models.ToDoData
import com.idn.todoapp.data.repository.ToDoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ToDoViewModel(application: Application): AndroidViewModel(application) {
    private val toDoDao = ToDoDatabase.getDatabase(application).toDoDao()
    private val repository: ToDoRepository

    val getAllData: LiveData<List<ToDoData>>
    val sortByHighPriority: LiveData<List<ToDoData>>
    val sortByLowPriority: LiveData<List<ToDoData>>

    init {
        repository = ToDoRepository(toDoDao)
        getAllData = repository.getAllData
        sortByHighPriority = repository.sortByHighPriority
        sortByLowPriority = repository.sortByLowPriority
    }

    fun insertData(toDoData: ToDoData){
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertData(toDoData)
        }
    }

    fun updateData(toDoData: ToDoData){
        viewModelScope.launch(Dispatchers.IO){
            repository.updateData(toDoData)
        }
    }

    fun deleteData(toDoData: ToDoData){
        viewModelScope.launch(Dispatchers.IO){
            repository.deleteData(toDoData)
        }
    }

    fun deleteAllData(){
        viewModelScope.launch(Dispatchers.IO){
            repository.deleteAllData()
        }
    }

    fun searchDatabase(searchQuery: String): LiveData<List<ToDoData>>{
        return repository.searchDatabase(searchQuery)
    }


}