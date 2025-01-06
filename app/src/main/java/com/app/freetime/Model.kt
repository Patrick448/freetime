package com.app.freetime

class Model {

    data class Tip(val id: String, val title : String, val text: String, val isFavorite: Boolean)
    data class Preferences(val shortBreakDuration: Int, val longBreakDuration: Int, val workSessionDuration: Int)
    data class Session(val duration: Int, val numberCycles: Int)
    
}