package press.pelldom.sessionledger.mobile.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import press.pelldom.sessionledger.mobile.data.db.dao.CategoryDao
import press.pelldom.sessionledger.mobile.data.db.dao.SessionDao
import press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity

@Database(
    entities = [CategoryEntity::class, SessionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sessionledger.db"
                ).build().also { instance = it }
            }
        }
    }
}

