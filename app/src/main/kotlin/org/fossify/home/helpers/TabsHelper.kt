package org.fossify.home.helpers

import android.content.Context
import org.fossify.commons.extensions.normalizeString
import org.fossify.home.extensions.getDrawableForPackageName
import org.fossify.home.extensions.hiddenIconsDB
import org.fossify.home.extensions.launchersDB
import org.fossify.home.models.AppLauncher

/**
 * Apps that can be put into drawer tabs: everything shown in the drawer, hidden apps excluded,
 * sorted the same way as the drawer. Must be called on a background thread.
 */
fun Context.getTabAssignableApps(): List<AppLauncher> {
    val hiddenIcons = hiddenIconsDB.getHiddenIcons().map { it.getIconIdentifier() }.toSet()
    val launchers = IconCache.launchers.ifEmpty { launchersDB.getAppLaunchers() }
    return launchers
        .filter { !hiddenIcons.contains(it.getLauncherIdentifier()) && it.packageName != packageName }
        .distinctBy { it.getLauncherIdentifier() }
        .sortedWith(compareBy({ it.title.normalizeString().lowercase() }, { it.packageName }))
        .onEach {
            if (it.drawable == null) {
                it.drawable = getDrawableForPackageName(it.packageName)
            }
        }
}
