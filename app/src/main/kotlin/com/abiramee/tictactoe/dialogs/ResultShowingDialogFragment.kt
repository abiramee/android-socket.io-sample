package com.abiramee.tictactoe.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.abiramee.tictactoe.util.Constants
import com.abiramee.tictactoe.R
import com.abiramee.tictactoe.listeners.ResultAfterDecisionListener
import com.abiramee.tictactoe.databinding.FragmentResultShowingDialogBinding


class ResultShowingDialogFragment(private val resultAfterDecisionListener: ResultAfterDecisionListener) :
    DialogFragment() {

    private lateinit var binding: FragmentResultShowingDialogBinding;
    private lateinit var nickName: String

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
            R.layout.fragment_result_showing_dialog,
            container,
            false
        )

        init()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        nickName = arguments!!.getString(Constants.NICK_NAME).toString()

        binding.textView.text = when (arguments!!.getString(Constants.MATCH_TIE).isNullOrBlank()) {
            true ->
                "$nickName\n has won the match"
            false -> {
                binding.lottieAnimationView.setAnimation("tie.json")
                "The match\n has tied"
            }
        }

        binding.buttonExit.setOnClickListener{
            resultAfterDecisionListener.onExit()
            dismiss()
        }
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