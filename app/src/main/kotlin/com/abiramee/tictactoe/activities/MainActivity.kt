package com.abiramee.tictactoe.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.abiramee.tictactoe.*
import com.abiramee.tictactoe.util.Constants.Companion.MATCH_TIE
import com.abiramee.tictactoe.databinding.ActivityMainBinding
import com.abiramee.tictactoe.dialogs.ResultShowingDialogFragment
import com.abiramee.tictactoe.dialogs.WaitingDialogFragment
import com.abiramee.tictactoe.listeners.ResultAfterDecisionListener
import com.abiramee.tictactoe.models.*
import com.abiramee.tictactoe.util.Constants
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity(), View.OnClickListener,
    ResultAfterDecisionListener {

    private lateinit var socket: Socket
    private lateinit var binding: ActivityMainBinding
    private lateinit var gson: Gson;
    private lateinit var waitingDialogFragment: WaitingDialogFragment
    private lateinit var resultShowingDialogFragment: ResultShowingDialogFragment
    private lateinit var roomName: String
    private lateinit var nickName: String
    private lateinit var opponentNickName: String
    private lateinit var timer: CountDownTimer
    private var userTurn = 1
    private var competitorId = 1
    private var moves = IntArray(9) { it * -1 }
    private lateinit var imageButtonGridList: List<ImageButton>

    @ObsoleteCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_main
        )
        binding.onClickListener = this
        binding.lifecycleOwner = this

        init()
    }

    @ObsoleteCoroutinesApi
    private fun init() {
        imageButtonGridList = listOf(
            binding.imageButtonGrid1,
            binding.imageButtonGrid2,
            binding.imageButtonGrid3,
            binding.imageButtonGrid4,
            binding.imageButtonGrid5,
            binding.imageButtonGrid6,
            binding.imageButtonGrid7,
            binding.imageButtonGrid8,
            binding.imageButtonGrid9
        )

        roomName = intent.getStringExtra(Constants.ROOM_NAME)
        nickName = intent.getStringExtra(Constants.NICK_NAME)


        gson = Gson()
        waitingDialogFragment =
            WaitingDialogFragment()
        resultShowingDialogFragment =
            ResultShowingDialogFragment(this)

        showWaitingDialog()

        GlobalScope.launch {
            delay(2000)
            triggerSocket()
        }
    }

    @ObsoleteCoroutinesApi
    private fun triggerSocket() {
        try {
            socket = IO.socket("http://192.168.31.116:3000/")
        } catch (e: Exception) {
            runOnUiThread{
                Toast.makeText(this, "Connection error!", Toast.LENGTH_LONG).show()
            }
        }

        socket.connect()
        socket.on(Socket.EVENT_CONNECT, onConnect)
        socket.on("updateTick", onTick)
        socket.on("newUserToRoom", onNewUser)
        socket.on("userLeftChatRoom", onUserLeft)
        socket.on("userCount", onUserCount)
        socket.on("updateUserTurn", onChangeUserTurn)
        socket.on("updateMove", onMove)
        socket.on("announceWin", onWin)
        socket.on("announceTie", onTie)
    }


    private var onConnect = Emitter.Listener {
        val initialData = InitialData(nickName, roomName)
        socket.emit("subscribe", gson.toJson(initialData))
    }

    private var onTie = Emitter.Listener {
        runOnUiThread {
            showResultShowingDialog(3)
        }
    }

    private var onWin = Emitter.Listener {
        runOnUiThread {
            showResultShowingDialog(2)
        }
    }

    private var onMove = Emitter.Listener {
        moves[it[0] as Int] = when (competitorId) {
            1 -> 2
            else -> 1
        }

        runOnUiThread {
            updateOpponentMove(it[0] as Int)
        }
    }

    private fun updateOpponentMove(move: Int) {
        imageButtonGridList[move].setImageDrawable(
            iconSelector(
                when (competitorId) {
                    1 -> 2
                    else -> 1
                }
            )
        )
    }

    private var onTick = Emitter.Listener {
        runOnUiThread {
            updateTimeCountDown(2, it[0] as Int)
        }
    }

    private var onChangeUserTurn = Emitter.Listener {
        if (userTurn == 2) {
            userTurn = 1
            runOnUiThread {
                setDefaultNickNames()
                startTicker()
            }
        }
    }

    @ObsoleteCoroutinesApi
    private var onNewUser = Emitter.Listener {
        val users = gson.fromJson(it[0].toString(), Array<String>::class.java).asList()
        if (users.size > 1) {
            runOnUiThread {
                opponentNickName = when (users[1]) {
                    nickName -> {
                        userTurn = 2
                        competitorId = 2
                        users[0]
                    }
                    else -> {
                        startTicker()
                        users[1]
                    }
                }
                setDefaultNickNames()
            }
        }
    }

    private var onUserLeft = Emitter.Listener {
        runOnUiThread {
            Toast.makeText(this, "${it[0]} left!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    @ObsoleteCoroutinesApi
    private var onUserCount = Emitter.Listener {
        if (it[0] as Int > 2) {
            runOnUiThread {
                Toast.makeText(this, "Room users limit already crossed!", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else if (it[0] as Int == 2) {
            waitingDialogFragment.dismiss()
        }
    }

    private fun showWaitingDialog() {
        val bundle = Bundle()
        bundle.putString(Constants.NICK_NAME, nickName)
        bundle.putString(Constants.ROOM_NAME, roomName)
        waitingDialogFragment.arguments = bundle
        waitingDialogFragment.show(supportFragmentManager, waitingDialogFragment.tag)
    }

    private fun showResultShowingDialog(type: Int) {
        val bundle = Bundle()
        bundle.putString(
            Constants.NICK_NAME, when (type) {
                1 -> nickName
                2 -> opponentNickName
                else -> nickName
            }
        )
        if (type == 3) {
            bundle.putString(MATCH_TIE, MATCH_TIE)

        }
        resultShowingDialogFragment.arguments = bundle
        resultShowingDialogFragment.show(supportFragmentManager, resultShowingDialogFragment.tag)
    }

    override fun onDestroy() {
        super.onDestroy()
        val sendData =
            SendMessage(nickName, "hello", roomName)
        socket.emit("unsubscribe", gson.toJson(sendData))
        socket.disconnect()
    }

    override fun onClick(v: View?) {
        if (userTurn != 2) {
            v!!.isClickable = false
            when (v.id) {
                R.id.imageButton_grid_1 -> performSelectStatus(binding.imageButtonGrid1, 0)
                R.id.imageButton_grid_2 -> performSelectStatus(binding.imageButtonGrid2, 1)
                R.id.imageButton_grid_3 -> performSelectStatus(binding.imageButtonGrid3, 2)
                R.id.imageButton_grid_4 -> performSelectStatus(binding.imageButtonGrid4, 3)
                R.id.imageButton_grid_5 -> performSelectStatus(binding.imageButtonGrid5, 4)
                R.id.imageButton_grid_6 -> performSelectStatus(binding.imageButtonGrid6, 5)
                R.id.imageButton_grid_7 -> performSelectStatus(binding.imageButtonGrid7, 6)
                R.id.imageButton_grid_8 -> performSelectStatus(binding.imageButtonGrid8, 7)
                R.id.imageButton_grid_9 -> performSelectStatus(binding.imageButtonGrid9, 8)
            }
        }
    }

    private fun performSelectStatus(imageButton: ImageButton, movePosition: Int) {
        imageButton.setImageDrawable(iconSelector(competitorId))
        moves[movePosition] = this.competitorId
        Log.e("move", gson.toJson(moves).toString())
        socket.emit("newMove", gson.toJson(
            Move(
                movePosition,
                roomName
            )
        ))

        timer.cancel()

        when {
            checkWinner() -> {
                Log.e("checkWinner", "check")
                showResultShowingDialog(1)
                socket.emit("onWin", gson.toJson(
                    RoomName(
                        roomName
                    )
                ))
            }
            isDraw() -> {
                socket.emit("onTie", gson.toJson(
                    RoomName(
                        roomName
                    )
                ))
                showResultShowingDialog(3)
            }
            else -> {
                userTurn = 2
                setDefaultNickNames()
                socket.emit("changeUserTurn", gson.toJson(
                    RoomName(
                        roomName
                    )
                ))
            }
        }
    }

    private fun iconSelector(userType: Int): Drawable {
        return when (userType) {
            1 -> getDrawable(R.drawable.ic_zero)
            else -> getDrawable(R.drawable.ic_close)
        }!!
    }

    private fun checkWinner(): Boolean {
        return ((moves[0] == moves[1] && moves[1] == moves[2]) || (moves[3] == moves[4] && moves[4] == moves[5])
                || (moves[6] == moves[7] && moves[7] == moves[8]) || (moves[0] == moves[3] && moves[3] == moves[6]) ||
                (moves[1] == moves[4] && moves[4] == moves[7]) || (moves[2] == moves[5] && moves[5] == moves[8])
                || (moves[0] == moves[4] && moves[4] == moves[8]) || (moves[2] == moves[4] && moves[4] == moves[6]))
    }

    private fun isDraw(): Boolean {
        return moves.count { it > 0 } == 9
    }

    @SuppressLint("SetTextI18n")
    private fun updateTimeCountDown(user: Int, time: Int) {
        when (user) {
            1 -> {
                binding.textViewUser1.text = "$nickName : $time"
                binding.textViewUser1.setTextColor(Color.parseColor("#0055FF"))
                binding.textViewUser1.setTypeface(
                    resources.getFont(R.font.nunito_bold),
                    Typeface.BOLD
                );
            }

            2 -> {
                binding.textViewUser2.text = "$opponentNickName : $time"
                binding.textViewUser2.setTextColor(Color.parseColor("#0055FF"))
                binding.textViewUser2.setTypeface(
                    resources.getFont(R.font.nunito_bold),
                    Typeface.BOLD
                );
            }
        }
    }

    private fun setDefaultNickNames() {
        binding.textViewUser1.text = nickName
        binding.textViewUser1.setTextColor(Color.BLACK)
        binding.textViewUser1.setTypeface(resources.getFont(R.font.nunito), Typeface.NORMAL);
        binding.textViewUser2.text = opponentNickName
        binding.textViewUser2.setTextColor(Color.BLACK)
        binding.textViewUser2.setTypeface(resources.getFont(R.font.nunito), Typeface.NORMAL);
    }

    private fun startTicker() {
        timer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                runOnUiThread {
                    socket.emit(
                        "newTick",
                        gson.toJson(
                            Tick(
                                (millisUntilFinished / 1000).toInt(),
                                roomName
                            )
                        )
                    )
                    updateTimeCountDown(userTurn, (millisUntilFinished / 1000).toInt())
                }
            }

            override fun onFinish() {
                userTurn = 2
                setDefaultNickNames()
                socket.emit("changeUserTurn", gson.toJson(
                    RoomName(
                        roomName
                    )
                ))
            }
        }
        timer.start()
    }

    override fun onExit() {
        finish()
    }

}
