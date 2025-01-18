package com.app.freetime

class Model {

    data class Tip (val id: String, val title : String, val text: String, var favorite: Boolean)
    data class Preferences(val shortBreakDuration: Int, val longBreakDuration: Int, val workSessionDuration: Int)
    data class Session(val duration: Int, val numberCycles: Int)
    data class Task (var id: String, var title: String)
    
}