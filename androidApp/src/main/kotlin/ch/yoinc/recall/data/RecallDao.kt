package ch.yoinc.recall.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecallDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecallEntity)

    @Query("DELETE FROM recalls WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM recalls")
    fun getAll(): Flow<List<RecallEntity>>
}
