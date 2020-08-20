package com.abiramee.tictactoe.models

data class InitialData (val userName : String, val roomName : String)
data class SendMessage(val userName : String, val messageContent: String, val roomName: String)
data class Tick(val count: Int, val roomName: String)
data class RoomName(val roomName: String)
data class Move(val move: Int, val roomName: String)