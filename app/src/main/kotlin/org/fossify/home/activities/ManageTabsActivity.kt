package org.fossify.home.activities

import android.os.Bundle
import org.fossify.commons.dialogs.ConfirmationDialog
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getContrastColor
import org.fossify.commons.extensions.viewBinding
import org.fossify.commons.helpers.NavigationIcon
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.home.R
import org.fossify.home.adapters.ManageTabsAdapter
import org.fossify.home.databinding.ActivityManageTabsBinding
import org.fossify.home.dialogs.TabAppsPickerDialog
import org.fossify.home.dialogs.TabNameDialog
import org.fossify.home.extensions.hiddenIconsDB
import org.fossify.home.extensions.tabsDB
import org.fossify.home.helpers.getTabAssignableApps
import org.fossify.home.models.DrawerTab

class ManageTabsActivity : SimpleActivity(), ManageTabsAdapter.Listener {
    private val binding by viewBinding(ActivityManageTabsBinding::inflate)
    private lateinit var adapter: ManageTabsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupEdgeToEdge(padBottomSystem = listOf(binding.manageTabsList))
        setupMaterialScrollListener(binding.manageTabsList, binding.manageTabsAppbar)

        adapter = ManageTabsAdapter(this, this)
        binding.manageTabsList.adapter = adapter
        adapter.itemTouchHelper.attachToRecyclerView(binding.manageTabsList)

        binding.manageTabsFab.setOnClickListener { createTab() }
        refreshTabs()
    }

    override fun onResume() {
        super.onResume()
        setupTopAppBar(binding.manageTabsAppbar, NavigationIcon.Arrow)
        val primaryColor = getProperPrimaryColor()
        binding.manageTabsFab.setColors(primaryColor.getContrastColor(), primaryColor, primaryColor.getContrastColor())
    }

    private fun refreshTabs() {
        ensureBackgroundThread {
            val tabs = tabsDB.getTabs()
            val visibleApps = getTabAssignableApps().map { it.getLauncherIdentifier() }.toSet()
            val counts = tabsDB.getAllTabApps()
                .filter { visibleApps.contains(it.getAppIdentifier()) }
                .groupingBy { it.tabId }
                .eachCount()
            val hiddenCount = hiddenIconsDB.getHiddenIcons().size

            runOnUiThread {
                adapter.setData(tabs, counts, visibleApps.size, hiddenCount)
            }
        }
    }

    private fun createTab() {
        TabNameDialog(this, R.string.new_tab) { name ->
            ensureBackgroundThread {
                val id = tabsDB.createTab(name)
                runOnUiThread {
                    refreshTabs()
                    TabAppsPickerDialog(this, DrawerTab(id, name, 0)) { refreshTabs() }
                }
            }
        }
    }

    override fun onTabClicked(tab: DrawerTab) {
        TabNameDialog(this, R.string.rename_tab, tab.name) { name ->
            ensureBackgroundThread {
                tabsDB.renameTab(tab.id!!, name)
                refreshTabs()
            }
        }
    }

    override fun onTabDeleteClicked(tab: DrawerTab) {
        ConfirmationDialog(this, getString(R.string.delete_tab_confirmation, tab.name)) {
            ensureBackgroundThread {
                tabsDB.deleteTab(tab.id!!)
                refreshTabs()
            }
        }
    }

    override fun onTabAddAppsClicked(tab: DrawerTab) {
        TabAppsPickerDialog(this, tab) { refreshTabs() }
    }

    override fun onTabsReordered(tabs: List<DrawerTab>) {
        ensureBackgroundThread {
            tabsDB.updateTabs(tabs)
        }
    }
}
