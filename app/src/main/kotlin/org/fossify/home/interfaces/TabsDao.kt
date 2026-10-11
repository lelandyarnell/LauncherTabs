package org.fossify.home.interfaces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.fossify.home.models.DrawerTab
import org.fossify.home.models.TabApp

@Dao
abstract class TabsDao {
    @Query("SELECT * FROM tabs ORDER BY position, id")
    abstract fun getTabs(): List<DrawerTab>

    @Query("SELECT COALESCE(MAX(position), -1) FROM tabs")
    abstract fun getMaxPosition(): Int

    @Insert
    abstract fun insertTab(tab: DrawerTab): Long

    @Update
    abstract fun updateTabs(tabs: List<DrawerTab>)

    @Query("UPDATE tabs SET name = :name WHERE id = :id")
    abstract fun renameTab(id: Long, name: String)

    @Query("DELETE FROM tabs WHERE id = :id")
    protected abstract fun deleteTabRow(id: Long)

    @Query("SELECT * FROM tab_apps")
    abstract fun getAllTabApps(): List<TabApp>

    @Query("SELECT * FROM tab_apps WHERE tab_id = :tabId")
    abstract fun getTabApps(tabId: Long): List<TabApp>

    @Query("SELECT tab_id FROM tab_apps WHERE package_name = :packageName AND activity_name = :activityName")
    abstract fun getTabIdsForApp(packageName: String, activityName: String): List<Long>

    @Query("SELECT DISTINCT package_name FROM tab_apps")
    abstract fun getTabAppPackages(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertTabApps(tabApps: List<TabApp>)

    @Query("DELETE FROM tab_apps WHERE tab_id = :tabId")
    protected abstract fun deleteAppsOfTab(tabId: Long)

    @Query("DELETE FROM tab_apps WHERE package_name = :packageName AND activity_name = :activityName")
    protected abstract fun deleteAppFromAllTabs(packageName: String, activityName: String)

    @Query("DELETE FROM tab_apps WHERE package_name = :packageName")
    abstract fun deletePackageFromTabs(packageName: String)

    @Transaction
    open fun createTab(name: String): Long {
        return insertTab(DrawerTab(id = null, name = name, position = getMaxPosition() + 1))
    }

    @Transaction
    open fun deleteTab(id: Long) {
        deleteAppsOfTab(id)
        deleteTabRow(id)
    }

    @Transaction
    open fun setTabApps(tabId: Long, tabApps: List<TabApp>) {
        deleteAppsOfTab(tabId)
        insertTabApps(tabApps)
    }

    @Transaction
    open fun setAppTabs(packageName: String, activityName: String, tabIds: List<Long>) {
        deleteAppFromAllTabs(packageName, activityName)
        insertTabApps(tabIds.map { TabApp(it, packageName, activityName) })
    }
}
