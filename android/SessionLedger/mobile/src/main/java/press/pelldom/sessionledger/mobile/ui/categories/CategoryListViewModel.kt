package press.pelldom.sessionledger.mobile.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.data.db.DefaultCategory
import press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity

class CategoryListViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getInstance(app)

    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories

    init {
        viewModelScope.launch(Dispatchers.IO) {
            db.categoryDao()
                .observeAllCategories()
                .distinctUntilChanged()
                .collect { list ->
                    _categories.value = list.sortedWith(
                        compareByDescending<CategoryEntity> { it.id == DefaultCategory.UNCATEGORIZED_ID }
                            .thenBy { it.name.lowercase() }
                    )
                }
        }
    }

    fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val entity = CategoryEntity(
                id = UUID.randomUUID().toString(),
                name = trimmed,
                isDefault = false,
                archived = false,
                createdAtMs = now,
                updatedAtMs = now
            )
            db.categoryDao().insert(entity)
        }
    }

    fun renameCategory(category: CategoryEntity, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        if (category.id == DefaultCategory.UNCATEGORIZED_ID) return
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            db.categoryDao().update(category.copy(name = trimmed, updatedAtMs = now))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        if (category.id == DefaultCategory.UNCATEGORIZED_ID) return
        viewModelScope.launch(Dispatchers.IO) {
            db.sessionDao().reassignCategory(
                fromCategoryId = category.id,
                toCategoryId = DefaultCategory.UNCATEGORIZED_ID
            )
            db.categoryDao().deleteById(category.id)
        }
    }
}

