package press.pelldom.sessionledger.mobile.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity

@Dao
interface SessionDao {

    // Active session (at most one). Enforced by app logic in v1.
    @Query("SELECT * FROM sessions WHERE state != 'ENDED' ORDER BY startTimeMs DESC LIMIT 1")
    fun observeActiveSession(): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE state != 'ENDED' ORDER BY startTimeMs DESC LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?

    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SessionEntity?

    @Query("SELECT * FROM sessions WHERE state = 'ENDED' ORDER BY endTimeMs DESC")
    fun observeEndedSessionsNewestFirst(): Flow<List<SessionEntity>>

    @Query(
        """
        SELECT * FROM sessions
        WHERE state = 'ENDED'
          AND startTimeMs >= :startMs
          AND startTimeMs < :endMs
        ORDER BY startTimeMs ASC
    """
    )
    suspend fun getEndedSessionsInRange(startMs: Long, endMs: Long): List<SessionEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: SessionEntity)

    @Update
    suspend fun update(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE sessions SET categoryId = :toCategoryId WHERE categoryId = :fromCategoryId")
    suspend fun reassignCategory(fromCategoryId: String, toCategoryId: String)
}

