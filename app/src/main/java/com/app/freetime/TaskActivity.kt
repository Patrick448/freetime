package com.app.freetime

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
    private lateinit var editText: EditText
    private lateinit var addButton: Button

    private val db = FirebaseFirestore.getInstance()
    private var tasks = mutableListOf<Task>()
    var dh = DataHandler()
    var taskOnEdit: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        editText = findViewById(R.id.add_task_et)
        addButton = findViewById(R.id.add_task_bt)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(tasks, this::startUpdatingTask, this::deleteTask)
        recyclerView.adapter = adapter



        lifecycleScope.launch{
            dh.getAllTasks {
                fetchedTasks ->
                    tasks = fetchedTasks
                    adapter.updateList(fetchedTasks)
            }

        }

        addButton.setOnClickListener{
            val inputText = editText.text.toString() // Get text from EditText
            val task = Task("", inputText)
            lifecycleScope.launch{
                if(taskOnEdit == null){
                    dh.addTask(task,
                        {
                            tasks.add(task)
                            adapter.updateList(tasks)
                        },
                        {ex ->
                            Toast.makeText(baseContext, "Error adding task", Toast.LENGTH_SHORT).show()
                        })
                }
                else{
                    updateTask(taskOnEdit!!)
                }


            }
        }

    }

    private fun startUpdatingTask(task: Task){
        taskOnEdit = task
        editText.setText(task.title)
    }



    private fun updateTask(task: Task){
        var newTask = task.copy()
        newTask.title = editText.text.toString()

        lifecycleScope.launch{
            dh.updateTask(newTask, {
                tasks.removeAll{it.id == task.id }
                tasks.add(newTask)
                adapter.updateList(tasks)
                taskOnEdit = null
                editText.setText("")

            }, {
                Toast.makeText(baseContext, "Error updating task", Toast.LENGTH_SHORT).show()
            })
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
        lifecycleScope.launch{
            dh.delete("tasks", task.id,
                {
                    tasks.removeAll { it.id == task.id }
                    adapter.updateList(tasks)
                },
                {
                    Toast.makeText(baseContext, "Error deleting task", Toast.LENGTH_SHORT).show()
                }
            )
        }

        /*db.collection("tasks").document(task.id).delete()
            .addOnSuccessListener {
                tasks.removeAll { it.id == task.id }
                adapter.updateList(tasks)
            }*/
    }
}