package ch.yoinc.recall.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecallEntity::class], version = 1, exportSchema = false)
abstract class RecallDatabase : RoomDatabase() {
    abstract fun recallDao(): RecallDao

    companion object {
        @Volatile private var instance: RecallDatabase? = null

        fun getInstance(context: Context): RecallDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RecallDatabase::class.java,
                    "recalls.db",
                ).build().also { instance = it }
            }
    }
}
