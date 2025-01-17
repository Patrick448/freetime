package com.app.freetime

import android.util.Log
import com.app.freetime.Model.*
import com.google.firebase.Firebase
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import java.io.Serializable
import kotlin.collections.mutableListOf

class DataHandler {

    val db = Firebase.firestore

    /**
     * Fetches all documents from the specified Firestore collection and maps them to objects of type [T].
     *
     * @param T The type of object to map Firestore documents into.
     * @param collectionName The name of the Firestore collection to fetch.
     * @param mapper A function that converts a Firestore document into an object of type [T].
     * @param onSuccess A callback function that receives the list of mapped objects upon success.
     */
    private fun <T> getAllItems(
        collectionName: String,
        mapper: (QueryDocumentSnapshot) -> T,
        onSuccess: (MutableList<T>) -> Unit
    ) {
        db.collection(collectionName).get()
            .addOnFailureListener { error ->
                Log.e("DataHandler", "Error fetching $collectionName: ${error.message}")
            }
            .addOnSuccessListener { documents ->
                val itemList = documents.mapNotNull { mapper(it) }
                onSuccess(itemList.toMutableList())
            }
    }

    /**
     * Converts a Firestore document into a `Tip` object.
     *
     * @param document The Firestore document to convert.
     * @return A `Tip` object created from the document data.
     */
    private fun tipFromDocument(document: QueryDocumentSnapshot): Tip {
        return Tip(
            id = document.id,
            title = document.getString("title") ?: "",
            text = document.getString("text") ?: "",
            isFavorite = document.getBoolean("favorite") ?: false
        )
    }

    /**
     * Converts a Firestore document into a `Task` object.
     *
     * @param document The Firestore document to convert.
     * @return A `Task` object created from the document data.
     */
    private fun taskFromDocument(document: QueryDocumentSnapshot): Task {
        return Task(
            id = document.id,
            title = document.getString("title") ?: ""
        )
    }

    /**
     * Fetches all `Tip` objects from Firestore and returns them via the callback.
     *
     * @param onSuccess Callback function that receives the list of `Tip` objects upon success.
     */
    suspend fun getAllTips(onSuccess: (MutableList<Tip>) -> Unit) {
        getAllItems("tips", this::tipFromDocument, onSuccess)
    }

    /**
     * Fetches all `Task` objects from Firestore and returns them via the callback.
     *
     * @param onSuccess Callback function that receives the list of `Task` objects upon success.
     */
    suspend fun getAllTasks(onSuccess: (MutableList<Task>) -> Unit) {
        getAllItems("tasks", this::taskFromDocument, onSuccess)
    }

    /**
     * Adds a generic object to Firestore with an auto-generated ID.
     *
     * @param collectionName The name of the Firestore collection.
     * @param item The object to be added.
     * @param mapper Function that converts the object into a Firestore-compatible map.
     * @param onSuccess Callback executed when the object is successfully added, returning the generated ID.
     * @param onFailure Callback executed when an error occurs, providing the exception.
     */
    suspend fun <T> add(
        collectionName: String,
        item: T,
        mapper: (T) -> Map<String, Any>,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val documentData = mapper(item) // Convert object to Firestore document

        db.collection(collectionName)
            .add(documentData) // Firestore auto-generates ID
            .addOnSuccessListener { documentReference ->
                val generatedId = documentReference.id
                Log.d("DataHandler", "$collectionName added with ID: $generatedId")
                onSuccess(generatedId)
            }
            .addOnFailureListener { exception ->
                Log.e("DataHandler", "Error adding to $collectionName", exception)
                onFailure(exception)
            }
    }

    /**
     * Adds a Task to Firestore using the generic addObject function.
     *
     * @param task The Task to be added.
     * @param onSuccess Callback executed when the task is successfully added, returning the generated ID.
     * @param onFailure Callback executed when an error occurs.
     */
    suspend fun addTask(task: Task, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        add("tasks", task, ::taskToDocument, onSuccess, onFailure)
    }

    /**
     * Converts a Task object into a Firestore document map.
     *
     * @param task The Task object to convert.
     * @return A map representing the Firestore document.
     */
    fun taskToDocument(task: Task): Map<String, Any> {
        return mapOf(
            "title" to task.title // No ID because Firestore generates it
        )
    }

    suspend fun updateTask(task: Task, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val taskMap = hashMapOf(
            "title" to task.title
        )

        db.collection("tasks")
            .document(task.id)
            .set(taskMap)
            .addOnSuccessListener {
                Log.d("DataHandler", "Task added successfully")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("DataHandler", "Error adding task", exception)
                onFailure(exception)
            }
    }

    /**
     * Updates an existing Firestore document with new data.
     *
     * @param collectionName The name of the Firestore collection.
     * @param documentId The ID of the document to update.
     * @param item The object containing updated data.
     * @param mapper Function that converts the object into a Firestore-compatible map.
     * @param onSuccess Callback executed when the update is successful.
     * @param onFailure Callback executed when an error occurs.
     */
    fun <T> updateObject(
        collectionName: String,
        documentId: String,
        item: T,
        mapper: (T) -> Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val documentData = mapper(item) // Convert object to Firestore map

        db.collection(collectionName)
            .document(documentId) // Specify which document to update
            .update(documentData) // Update only the provided fields
            .addOnSuccessListener {
                Log.d("DataHandler", "$collectionName document updated: $documentId")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("DataHandler", "Error updating $collectionName document: $documentId", exception)
                onFailure(exception)
            }
    }

    /**
     * Updates a Task in Firestore using the generic updateObject function.
     *
     * @param taskId The ID of the Task document to update.
     * @param updatedTask The updated Task object.
     * @param onSuccess Callback executed when the update is successful.
     * @param onFailure Callback executed when an error occurs.
     */
    fun updateTask(taskId: String, updatedTask: Task, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        updateObject("tasks", taskId, updatedTask, ::taskToDocument, onSuccess, onFailure)
    }




    /**
     * Retrieves user preferences from Firestore.
     *
     * @return A `Preferences` object containing user settings.
     */
    private fun getPreferences(): Preferences {
        return TODO()
    }

    /**
     * Fetches all user sessions from Firestore.
     *
     * @return A mutable list of `Session` objects.
     */
    private fun getAllSessions(): MutableList<Session> {
        return TODO()
    }


    suspend fun delete(collectionName: String, id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        db.collection(collectionName).document(id).delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener{
                ex -> onFailure(ex)
            }
    }
}
