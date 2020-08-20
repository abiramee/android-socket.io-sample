package com.abiramee.tictactoe.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.abiramee.tictactoe.util.Constants
import com.abiramee.tictactoe.R
import com.abiramee.tictactoe.databinding.ActivityEnteringBinding

import kotlinx.android.synthetic.main.activity_entering.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

class EnteringActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnteringBinding

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_entering
        )

        init()
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun init() {
        binding.nickNameEditText.onAfterTextChanged().debounce(500).onEach {
            runOnUiThread {
                if (it.length > 2)
                    binding.roomNameEditText.setText(
                        "${it}${(1000..9999).random()}".toLowerCase()
                            .replace("\\s".toRegex(), "")
                    )
            }
        }.launchIn(GlobalScope)

        binding.floatingActionButtonNextActivity.setOnClickListener {
            onNextActivityRequested()
        }

        room_name_editText.onDone {
            onNextActivityRequested()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun onNextActivityRequested() {
        if (binding.nickNameEditText.text!!.isNotEmpty() && binding.roomNameEditText.text!!.isNotEmpty()) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(
                Constants.ROOM_NAME,
                binding.roomNameEditText.text.toString().toLowerCase()
            )
            intent.putExtra(Constants.NICK_NAME, binding.nickNameEditText.text.toString())
            startActivity(intent)
        } else {
            Toast.makeText(baseContext, "Fill the fields properly!", Toast.LENGTH_LONG).show()
        }
    }

    private fun EditText.onDone(callback: () -> Unit) {
        setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                callback.invoke()
            }
            false
        }
    }

    @ExperimentalCoroutinesApi
    fun EditText.onAfterTextChanged(): Flow<String> = callbackFlow {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                offer(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        awaitClose { addTextChangedListener(null) }
    }
}