package com.abiramee.tictactoe.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.abiramee.tictactoe.util.Constants
import com.abiramee.tictactoe.R
import com.abiramee.tictactoe.databinding.FragmentWaitingDialogBinding


class WaitingDialogFragment : DialogFragment() {


    private lateinit var binding: FragmentWaitingDialogBinding;
    private lateinit var nickName: String
    private lateinit var roomName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onActivityCreated(arg0: Bundle?) {
        super.onActivityCreated(arg0)
        dialog!!.window!!.attributes.windowAnimations =
            R.style.DialogAnimation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_waiting_dialog,
            container,
            false
        )

        init()

        return binding.root
    }

    private fun init() {
        nickName = arguments!!.getString(Constants.NICK_NAME).toString()
        roomName = arguments!!.getString(Constants.ROOM_NAME).toString()

        binding.buttonShare.setOnClickListener {
            shareMessage()
        }
    }

    private fun shareMessage() {
        val sharingIntent = Intent(Intent.ACTION_SEND);
        sharingIntent.type = "text/plain";
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Inviation from ");
        sharingIntent.putExtra(
            Intent.EXTRA_TEXT,
            "$nickName invited to play TicTocToe. Join the room: $roomName"
        );
        startActivity(Intent.createChooser(sharingIntent, "Invitation"));
    }


    override fun onStart() {
        super.onStart()

        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()

        view!!.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        activity!!.finish()
                        return false
                    }
                }
                return false
            }
        })

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        dialog.window!!.statusBarColor = Color.WHITE

        return dialog
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
    }

}