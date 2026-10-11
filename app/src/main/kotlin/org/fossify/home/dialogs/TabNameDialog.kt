package org.fossify.home.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.setupDialogStuff
import org.fossify.commons.extensions.showKeyboard
import org.fossify.commons.extensions.toast
import org.fossify.commons.extensions.value
import org.fossify.home.R
import org.fossify.home.databinding.DialogRenameItemBinding

class TabNameDialog(
    val activity: Activity,
    titleId: Int,
    currentName: String = "",
    val callback: (name: String) -> Unit
) {

    init {
        val binding = DialogRenameItemBinding.inflate(activity.layoutInflater)
        binding.renameItemHint.hint = activity.getString(R.string.tab_name)
        binding.renameItemEdittext.setText(currentName)
        binding.renameItemEdittext.setSelection(currentName.length)

        activity.getAlertDialogBuilder()
            .setPositiveButton(org.fossify.commons.R.string.ok, null)
            .setNegativeButton(org.fossify.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, titleId) { alertDialog ->
                    alertDialog.showKeyboard(binding.renameItemEdittext)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = binding.renameItemEdittext.value.trim()
                        if (name.isEmpty()) {
                            activity.toast(org.fossify.commons.R.string.value_cannot_be_empty)
                        } else {
                            callback(name)
                            alertDialog.dismiss()
                        }
                    }
                }
            }
    }
}
