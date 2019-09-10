package com.remotecontol.Dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.remotecontol.R

class PeerConnectionDialog(val peerAdapter: PeerAdapter) : DialogFragment() {

    lateinit var dListener: DialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.peer_dialog_title)
            builder.setSingleChoiceItems(
                peerAdapter,
                0,
                DialogInterface.OnClickListener { dialog, which ->
                    dListener.onPeerSelect(peerAdapter.getItem(which)[0])
                    dialog.dismiss()
                })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    interface DialogListener {
        fun onPeerSelect(peer: String?)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            dListener = context as DialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement NoticeDialogListener")
            )
        }
    }

}