package com.app.freetime

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.freetime.Model.Task

class TaskAdapter(
    private var tasks: MutableList<Task>,
    private val onLikeClicked: (Task) -> Unit,
    private val onDeleteClicked: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.task_title)
        val textTextView: TextView = itemView.findViewById(R.id.task_text)
        val likeButton: Button = itemView.findViewById(R.id.like_button)
        val deleteButton: Button = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.titleTextView.text = task.title
        //holder.textTextView.text = task.text
       // holder.likeButton.setImageResource(if (task.isFavorite) R.drawable.ic_liked else R.drawable.ic_unliked)

        holder.likeButton.setOnClickListener { onLikeClicked(task) }
        holder.deleteButton.setOnClickListener { onDeleteClicked(task) }
    }

    override fun getItemCount() = tasks.size

    fun updateList(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
}