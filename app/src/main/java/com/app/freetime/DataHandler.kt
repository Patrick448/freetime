package com.app.freetime

import android.util.Log
import android.widget.Toast
import com.app.freetime.Model.*
import com.google.firebase.Firebase
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore

class DataHandler {

    val db = Firebase.firestore

    private fun tipFromDocument(document: QueryDocumentSnapshot): Tip{
        val title = document["title"].toString()
        val text = document["text"].toString()
        val isFavorite: Boolean = document["favorite"] as Boolean
        val tip :Tip = Tip(id = "", title=title, text = text, isFavorite = isFavorite)
        return tip
    }

    private fun <T> objectFromDocument(document: QueryDocumentSnapshot): T{
        return TODO()
    }

    private fun getPreferences() : Preferences{
        return TODO()
    }

    private fun getAllSessions(): List<Session>{
        return TODO()
    }


    suspend fun  getAllTips(onSuccess: (List<Tip>) -> Unit){
        var list = listOf<Tip>()

        db.collection("tips").get()
            .addOnFailureListener{
                    info ->
                Log.d("data-handler", info.toString())
            }
            .addOnSuccessListener { documents ->
                Log.d("data-handler", "Test log")
                for (document in documents) {
                    list = list.plus(tipFromDocument(document))
                }

                onSuccess(list)
            }
    }

    public fun <T> getAll(name: String): List<T>{
        var list = listOf<T>()

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