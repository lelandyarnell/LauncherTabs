package org.fossify.home.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "tab_apps",
    primaryKeys = ["tab_id", "package_name", "activity_name"],
    indices = [(Index(value = ["tab_id"]))]
)
data class TabApp(
    @ColumnInfo(name = "tab_id") var tabId: Long,
    @ColumnInfo(name = "package_name") var packageName: String,
    @ColumnInfo(name = "activity_name") var activityName: String,
) {
    fun getAppIdentifier() = "$packageName/$activityName"
}
