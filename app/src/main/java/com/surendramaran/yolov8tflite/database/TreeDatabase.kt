import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.surendramaran.yolov8tflite.database.TreeDao
import com.surendramaran.yolov8tflite.entities.Tree

@Database(entities = [Tree::class], version = 1)
abstract class TreeDatabase : RoomDatabase() {

    abstract fun treeDao(): TreeDao

    companion object {
        private var instance: TreeDatabase? = null

        @Synchronized
        fun getInstance(ctx: Context): TreeDatabase {
            if(instance == null)
                instance = Room.databaseBuilder(ctx.applicationContext, TreeDatabase::class.java,
                    "tree_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build()

            return instance!!

        }

        private val roomCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }

    }
}