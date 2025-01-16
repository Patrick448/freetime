package com.app.freetime

import android.util.Log
import android.widget.Toast
import com.app.freetime.Model.*
import com.google.firebase.Firebase
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlin.collections.mutableListOf

class DataHandler {

    val db = Firebase.firestore

//    private fun tipFromDocument(document: QueryDocumentSnapshot): Tip{
//        val title = document["title"].toString()
//        val text = document["text"].toString()
//        val isFavorite: Boolean = document["favorite"] as Boolean
//        val tip :Tip = Tip(id = "", title=title, text = text, isFavorite = isFavorite)
//        return tip
//    }


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
     * Converts a Firestore document into a Tip object.
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
     * Converts a Firestore document into a Task object.
     */
    private fun taskFromDocument(document: QueryDocumentSnapshot): Task {
        return Task(
            id = document.id,
            title = document.getString("title") ?: ""
        )
    }

    /**
     * Fetches all Tips from Firestore and returns them via the callback.
     */
    suspend fun getAllTips(onSuccess: (MutableList<Tip>) -> Unit) {
        getAllItems("tips", this::tipFromDocument, onSuccess)
    }

    /**
     * Fetches all Tasks from Firestore and returns them via the callback.
     */
    suspend fun getAllTasks(onSuccess: (MutableList<Task>) -> Unit) {
        getAllItems("tasks", this::taskFromDocument, onSuccess)
    }


    private fun <T> objectFromDocument(document: QueryDocumentSnapshot): T{
        return TODO()
    }

    private fun getPreferences() : Preferences{
        return TODO()
    }

    private fun getAllSessions(): MutableList<Session>{
        return TODO()
    }


   /* suspend fun  getAllTips(onSuccess: (MutableList<Tip>) -> Unit){
        var list = listOf<Tip>()

        db.collection("tips").get()
            .addOnFailureMutableListener{
                    info ->
                Log.d("data-handler", info.toString())
            }
            .addOnSuccessMutableListener { documents ->
                Log.d("data-handler", "Test log")
                for (document in documents) {
                    list = list.plus(tipFromDocument(document))
                }

                onSuccess(list)
            }
    }
*/
    public fun <T> getAll(name: String): MutableList<T>{
        var list = mutableListOf<T>()

        db.collection(name).get()
            .addOnFailureListener{
                    info ->
                Log.d("data-handler", info.toString())
            }
            .addOnSuccessListener { documents ->
                Log.d("data-handler", "Test log")
                for (document in documents) {
                    list.plus(objectFromDocument<T>(document))
                }
            }

        return list
    }
}