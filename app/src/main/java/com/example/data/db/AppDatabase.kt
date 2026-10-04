package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DeploymentEntity
import com.example.data.model.WebBlockEntity
import com.example.data.model.WebsiteEntity

@Database(
    entities = [
        WebsiteEntity::class,
        WebBlockEntity::class,
        DeploymentEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun websiteDao(): WebsiteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE websites ADD COLUMN buttonStyle TEXT NOT NULL DEFAULT 'gradient'")
                db.execSQL("ALTER TABLE websites ADD COLUMN buttonRadius TEXT NOT NULL DEFAULT 'pill'")
                db.execSQL("ALTER TABLE websites ADD COLUMN customPrimaryColor TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE websites ADD COLUMN customBackgroundColor TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE websites ADD COLUMN animationStyle TEXT NOT NULL DEFAULT 'fade-up'")
                db.execSQL("ALTER TABLE websites ADD COLUMN enableVisitorThemeToggle INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE websites ADD COLUMN formEndpoint TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE websites ADD COLUMN ogImageUrl TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE websites ADD COLUMN pagesJson TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE web_blocks ADD COLUMN pageSlug TEXT NOT NULL DEFAULT 'index'")
                db.execSQL("ALTER TABLE web_blocks ADD COLUMN animationEffect TEXT NOT NULL DEFAULT 'default'")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "web_builder.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
