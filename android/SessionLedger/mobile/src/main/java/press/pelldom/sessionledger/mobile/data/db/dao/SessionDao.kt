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

    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE state = 'ENDED' AND isArchived = 0 ORDER BY endTimeMs DESC")
    fun observeEndedSessionsNewestFirst(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE state = 'ENDED' AND isArchived = 0 ORDER BY endTimeMs DESC")
    fun observeActiveEndedSessionsNewestFirst(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE state = 'ENDED' AND isArchived = 1 ORDER BY archivedAtMillis DESC, endTimeMs DESC")
    fun observeArchivedEndedSessionsNewestFirst(): Flow<List<SessionEntity>>

    @Query(
        """
        SELECT * FROM sessions
        WHERE state = 'ENDED'
          AND isArchived = 0
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

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSession(id: String)

    @Query("UPDATE sessions SET isArchived = 1, archivedAtMillis = :archivedAtMillis WHERE id = :id")
    suspend fun archiveSession(id: String, archivedAtMillis: Long)

    @Query("UPDATE sessions SET isArchived = 0, archivedAtMillis = NULL WHERE id = :id")
    suspend fun unarchiveSession(id: String)

    @Query("UPDATE sessions SET categoryId = :toCategoryId WHERE categoryId = :fromCategoryId")
    suspend fun reassignCategory(fromCategoryId: String, toCategoryId: String)
}

