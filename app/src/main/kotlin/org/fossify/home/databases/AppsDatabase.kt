package org.fossify.home.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.fossify.home.helpers.Converters
import org.fossify.home.interfaces.AppLaunchersDao
import org.fossify.home.interfaces.HiddenIconsDao
import org.fossify.home.interfaces.HomeScreenGridItemsDao
import org.fossify.home.interfaces.TabsDao
import org.fossify.home.models.AppLauncher
import org.fossify.home.models.DrawerTab
import org.fossify.home.models.HiddenIcon
import org.fossify.home.models.HomeScreenGridItem
import org.fossify.home.models.TabApp

@Database(
    entities = [
        AppLauncher::class,
        HomeScreenGridItem::class,
        HiddenIcon::class,
        DrawerTab::class,
        TabApp::class
    ],
    version = 6
)
@TypeConverters(Converters::class)
abstract class AppsDatabase : RoomDatabase() {

    abstract fun AppLaunchersDao(): AppLaunchersDao

    abstract fun HomeScreenGridItemsDao(): HomeScreenGridItemsDao

    abstract fun HiddenIconsDao(): HiddenIconsDao

    abstract fun TabsDao(): TabsDao

    companion object {
        private var db: AppsDatabase? = null

        fun getInstance(context: Context): AppsDatabase {
            if (db == null) {
                synchronized(AppsDatabase::class) {
                    if (db == null) {
                        db = Room.databaseBuilder(
                            context.applicationContext,
                            AppsDatabase::class.java,
                            "apps.db"
                        ).addMigrations(MIGRATION_5_6).build()
                    }
                }
            }
            return db!!
        }

        // drawer tabs: adds the `tabs` and `tab_apps` tables, existing data is untouched
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `tabs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT NOT NULL, `position` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `tab_apps` (`tab_id` INTEGER NOT NULL, `package_name` TEXT NOT NULL, `activity_name` TEXT NOT NULL, PRIMARY KEY(`tab_id`, `package_name`, `activity_name`))")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tab_apps_tab_id` ON `tab_apps` (`tab_id`)")
            }
        }
    }
}
