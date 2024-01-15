package com.surendramaran.yolov8tflite
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.surendramaran.yolov8tflite.database.TreeDao
import com.surendramaran.yolov8tflite.entities.Tree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Tree::class], version = 1)
abstract class TreeDatabase : RoomDatabase() {

    abstract fun treeDao(): TreeDao

    companion object {
        @Volatile
        private var INSTANCE: TreeDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): TreeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    TreeDatabase::class.java,
                    "tree_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(TreeDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class TreeDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.treeDao())
                    }
                }
            }
        }
        suspend fun populateDatabase(treeDao: TreeDao) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.
            treeDao.deleteAllTrees()

            val newTree = Tree(
                latitude = 0.3232,
                longitude = 0.4323,
                isUploaded = false,
                ripe = 0,
                underripe = 0,
                unripe = 0,
                flower = 0,
                abnromal = 0
            )

            treeDao.insert(newTree)
        }

    }
}