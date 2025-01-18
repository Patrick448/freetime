package com.app.freetime

import android.util.Log
import com.app.freetime.Model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import java.io.Serializable
import kotlin.collections.mutableListOf

class DataHandler {

    val db = Firebase.firestore
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()


    private fun  getCurrentUserCollection() : DocumentReference{
        if(auth.currentUser?.uid != null){
            return db.collection("users").document(auth.currentUser!!.uid)
        }else{
            throw RuntimeException("User not found, cannot access collections")
        }
    }

    /**
     * Fetches all documents from the specified Firestore collection and maps them to objects of type [T].
     *
     * @param T The type of object to map Firestore documents into.
     * @param collectionName The name of the Firestore collection to fetch.
     * @param mapper A function that converts a Firestore document into an object of type [T].
     * @param onSuccess A callback function that receives the list of mapped objects upon success.
     * @param isUserCollection Tells if the data should be looked on the collections belonging to the user or the general collection
     */


    private fun <T> getAllItems(
        collectionName: String,
        mapper: (QueryDocumentSnapshot) -> T,
        onSuccess: (MutableList<T>) -> Unit,
        isUserCollection: Boolean
    ) {
        val collectionRef: CollectionReference? = if (isUserCollection) {
            getCurrentUserCollection()?.collection(collectionName)
        } else {
            db.collection(collectionName) // Fetch global collection
        }

        if (collectionRef == null) {
            Log.e("DataHandler", "Cannot fetch items: Collection reference is null")
            return
        }

        collectionRef.get()
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
            favorite = document.getBoolean("favorite") ?: false
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
        getAllItems("tips", this::tipFromDocument, onSuccess, false)
    }

    /**
     * Fetches all `Task` objects from Firestore and returns them via the callback.
     *
     * @param onSuccess Callback function that receives the list of `Task` objects upon success.
     */
    suspend fun getAllTasks(onSuccess: (MutableList<Task>) -> Unit) {
        getAllItems("tasks", this::taskFromDocument, onSuccess, true)
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

        getCurrentUserCollection().collection(collectionName)
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
            "title" to task.title
        )
    }

    fun tipToDocument(tip: Tip): Map<String, Any> {
        return mapOf(
            "title" to tip.title,
            "text" to tip.text,
            "favorite" to tip.favorite

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

         getCurrentUserCollection().collection(collectionName)
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


    fun updateTip(tipId: String, updatedTip: Tip, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        updateObject("tips", tipId, updatedTip, ::tipToDocument, onSuccess, onFailure)
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


    /**
     * Converts a Firestore document into a `Preferences` object.
     *
     * @param document The Firestore document to convert.
     * @return A `Preferences` object created from the document data.
     */
    private fun preferencesFromDocument(document: QueryDocumentSnapshot): Preferences {
        return Preferences(
            shortBreakDuration = document.getLong("shortBreak")?.toInt() ?: 25,
            longBreakDuration = document.getLong("longBreak")?.toInt() ?: 5,
            workSessionDuration = document.getLong("focusTime")?.toInt() ?: 15
        )
    }

    /**
     * Converts a `Preferences` object into a Firestore-compatible map.
     *
     * @param preferences The `Preferences` object to convert.
     * @return A map representing the Firestore document.
     */
    fun preferencesToDocument(preferences: Preferences): Map<String, Any> {
        return mapOf(
            "focusTime" to preferences.workSessionDuration,
            "shortBreak" to preferences.shortBreakDuration,
            "longBreak" to preferences.longBreakDuration
        )
    }

    /**
     * Updates the user's preferences in Firestore.
     *
     * @param preferences The updated `Preferences` object.
     * @param onSuccess A callback function that is invoked upon successful update.
     * @param onFailure A callback function that is invoked if an error occurs during
     *                  the update process. It provides the exception encountered.
     */
    fun updatePreferences(
        preferences: Preferences,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        updateObject(
            collectionName = "preferences", // Firestore collection name where preferences are stored
            documentId = "preferences", // Use the current user's document ID
            item = preferences, // The updated preferences data
            mapper = ::preferencesToDocument, // Map the preferences to Firestore-compatible format
            onSuccess = onSuccess, // Success callback
            onFailure = onFailure // Failure callback
        )
    }

    fun updateOrCreatePreferences(preferences: Preferences, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val preferencesRef = getCurrentUserCollection().collection("preferences").document("preferences")

        // Check if the document exists
        preferencesRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // If the document exists, update it
                    val preferencesMap = preferencesToDocument(preferences) // Convert the preferences to a map
                    preferencesRef.set(preferencesMap) // Use `set` to overwrite the document
                        .addOnSuccessListener {
                            Log.d("DataHandler", "Preferences updated successfully.")
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("DataHandler", "Error updating preferences: ${exception.message}")
                            onFailure(exception)
                        }
                } else {
                    // If the document doesn't exist, create it
                    val preferencesMap = preferencesToDocument(preferences) // Convert the preferences to a map
                    preferencesRef.set(preferencesMap) // Use `set` to create the document
                        .addOnSuccessListener {
                            Log.d("DataHandler", "Preferences created successfully.")
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("DataHandler", "Error creating preferences: ${exception.message}")
                            onFailure(exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DataHandler", "Error checking if document exists: ${exception.message}")
                onFailure(exception)
            }
    }


    /**
     * Fetches the user's preferences from Firestore and returns them via the callback.
     *
     * @param onSuccess Callback function that receives the `Preferences` object upon success.
     * @param onFailure Callback function that is invoked if an error occurs during the fetch process.
     */
    suspend fun getPreferences(
        onSuccess: (Preferences) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getAllItems("preferences", this::preferencesFromDocument, { preferencesList ->
            // Since the user's preferences collection should only contain one document
            // we return the first item from the list or handle the case when no preferences are found.
            val preferences = preferencesList.firstOrNull()
            if (preferences != null) {
                onSuccess(preferences)
            } else {
                onFailure(Exception("Preferences not found"))
            }
        }, isUserCollection = true)
    }


}
