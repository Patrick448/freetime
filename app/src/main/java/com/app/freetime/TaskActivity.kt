package com.app.freetime

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.freetime.Model.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class TaskActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val db = FirebaseFirestore.getInstance()
    private var tasks = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(tasks, this::toggleLike, this::deleteTask)
        recyclerView.adapter = adapter
        var dh = DataHandler()

        lifecycleScope.launch{
            dh.getAllTasks {
                fetchedTasks ->
                    adapter.updateList(fetchedTasks)
            }

        }

    }


    private fun toggleLike(task: Task) {
       /* val updatedTask = task.copy(isFavorite = !task.isFavorite)
        db.collection("tasks").document(task.id).update("favorite", updatedTask.isFavorite)
            .addOnSuccessListener {
                tasks.find { it.id == task.id }?.isFavorite = updatedTask.isFavorite
                adapter.updateList(tasks)
            }*/
    }

    private fun deleteTask(task: Task) {
        /*db.collection("tasks").document(task.id).delete()
            .addOnSuccessListener {
                tasks.removeAll { it.id == task.id }
                adapter.updateList(tasks)
            }*/
    }
}