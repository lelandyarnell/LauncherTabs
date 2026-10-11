package org.fossify.home.dialogs

import android.app.Activity
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.normalizeString
import org.fossify.commons.extensions.onTextChangeListener
import org.fossify.commons.extensions.realScreenSize
import org.fossify.commons.extensions.setupDialogStuff
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.home.R
import org.fossify.home.adapters.TabAppsPickerAdapter
import org.fossify.home.databinding.DialogTabAppsPickerBinding
import org.fossify.home.extensions.tabsDB
import org.fossify.home.helpers.getTabAssignableApps
import org.fossify.home.models.AppLauncher
import org.fossify.home.models.DrawerTab
import org.fossify.home.models.TabApp

/** Searchable checklist of all drawer apps, used to choose the apps of a single tab. */
class TabAppsPickerDialog(
    val activity: Activity,
    val tab: DrawerTab,
    val callback: () -> Unit
) {
    private val binding = DialogTabAppsPickerBinding.inflate(activity.layoutInflater)
    private var apps = emptyList<AppLauncher>()

    init {
        ensureBackgroundThread {
            val allApps = activity.getTabAssignableApps()
            val selected = activity.tabsDB.getTabApps(tab.id!!).map { it.getAppIdentifier() }.toMutableSet()
            activity.runOnUiThread {
                apps = allApps
                showDialog(selected)
            }
        }
    }

    private fun showDialog(selected: MutableSet<String>) {
        val adapter = TabAppsPickerAdapter(activity, selected)
        binding.tabAppsPickerList.adapter = adapter
        binding.tabAppsPickerList.layoutParams.height = (activity.realScreenSize.y * LIST_HEIGHT_RATIO).toInt()
        adapter.setApps(apps)
        binding.tabAppsPickerPlaceholder.beVisibleIf(apps.isEmpty())

        binding.tabAppsPickerSearch.onTextChangeListener { query ->
            val filtered = if (query.isEmpty()) {
                apps
            } else {
                apps.filter { it.title.normalizeString().contains(query.normalizeString(), ignoreCase = true) }
            }
            adapter.setApps(filtered)
            binding.tabAppsPickerPlaceholder.beVisibleIf(filtered.isEmpty())
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(org.fossify.commons.R.string.ok) { _, _ -> save(selected) }
            .setNegativeButton(org.fossify.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(
                    view = binding.root,
                    dialog = this,
                    titleText = activity.getString(R.string.choose_tab_apps, tab.name)
                )
            }
    }

    private fun save(selected: Set<String>) {
        val tabId = tab.id!!
        // apps that are currently hidden or not loaded keep their existing membership
        val visibleIdentifiers = apps.map { it.getLauncherIdentifier() }.toSet()
        ensureBackgroundThread {
            val untouched = activity.tabsDB.getTabApps(tabId).filter { !visibleIdentifiers.contains(it.getAppIdentifier()) }
            val chosen = apps.filter { selected.contains(it.getLauncherIdentifier()) }
                .map { TabApp(tabId, it.packageName, it.activityName) }
            activity.tabsDB.setTabApps(tabId, untouched + chosen)
            activity.runOnUiThread { callback() }
        }
    }

    companion object {
        private const val LIST_HEIGHT_RATIO = 0.5f
    }
}
