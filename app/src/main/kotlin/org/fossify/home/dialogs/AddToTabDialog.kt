package org.fossify.home.dialogs

import android.app.Activity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import org.fossify.commons.extensions.beGone
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.setupDialogStuff
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.commons.views.MyAppCompatCheckbox
import org.fossify.home.R
import org.fossify.home.databinding.DialogAddToTabBinding
import org.fossify.home.extensions.tabsDB
import org.fossify.home.models.DrawerTab

/** Checklist of all drawer tabs, used to choose which tabs a single app belongs to. */
class AddToTabDialog(
    val activity: Activity,
    val packageName: String,
    val activityName: String,
    val callback: () -> Unit
) {
    private val binding = DialogAddToTabBinding.inflate(activity.layoutInflater)
    private val checkboxes = LinkedHashMap<Long, MyAppCompatCheckbox>()

    init {
        ensureBackgroundThread {
            val tabs = activity.tabsDB.getTabs()
            val selectedTabIds = activity.tabsDB.getTabIdsForApp(packageName, activityName).toSet()
            activity.runOnUiThread {
                tabs.forEach { addCheckbox(it, selectedTabIds.contains(it.id)) }
                binding.addToTabPlaceholder.beVisibleIf(tabs.isEmpty())
                showDialog()
            }
        }
    }

    private fun showDialog() {
        activity.getAlertDialogBuilder()
            .setPositiveButton(org.fossify.commons.R.string.ok) { _, _ -> saveSelection() }
            .setNegativeButton(org.fossify.commons.R.string.cancel, null)
            .setNeutralButton(R.string.new_tab, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.add_to_tab) { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        createTab()
                    }
                }
            }
    }

    private fun createTab() {
        TabNameDialog(activity, R.string.new_tab) { name ->
            ensureBackgroundThread {
                val id = activity.tabsDB.createTab(name)
                activity.runOnUiThread {
                    binding.addToTabPlaceholder.beGone()
                    addCheckbox(DrawerTab(id, name, 0), true)
                    callback()
                }
            }
        }
    }

    private fun addCheckbox(tab: DrawerTab, isChecked: Boolean) {
        val checkbox = MyAppCompatCheckbox(activity).apply {
            text = tab.name
            this.isChecked = isChecked
            textSize = TEXT_SIZE_SP
            minHeight = resources.getDimensionPixelSize(R.dimen.drawer_tab_height)
            setColors(
                activity.getProperTextColor(),
                activity.getProperPrimaryColor(),
                activity.getProperBackgroundColor()
            )
        }

        binding.addToTabHolder.addView(
            checkbox,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )
        checkboxes[tab.id!!] = checkbox
    }

    private fun saveSelection() {
        val tabIds = checkboxes.filterValues { it.isChecked }.keys.toList()
        ensureBackgroundThread {
            activity.tabsDB.setAppTabs(packageName, activityName, tabIds)
            activity.runOnUiThread { callback() }
        }
    }

    companion object {
        private const val TEXT_SIZE_SP = 16f
    }
}
