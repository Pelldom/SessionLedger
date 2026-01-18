package press.pelldom.sessionledger.mobile.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY isDefault DESC, archived ASC, name COLLATE NOCASE ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE archived = 0 ORDER BY name COLLATE NOCASE ASC")
    fun observeActiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY archived ASC, name COLLATE NOCASE ASC")
    fun observeAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: CategoryEntity)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE categories SET archived = 1, updatedAtMs = :updatedAtMs WHERE id = :id")
    suspend fun archive(id: String, updatedAtMs: Long)

    @Query("UPDATE categories SET archived = 0, updatedAtMs = :updatedAtMs WHERE id = :id")
    suspend fun unarchive(id: String, updatedAtMs: Long)
}

