package press.pelldom.sessionledger.mobile.data.db.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0018\u0010\t\u001a\u0004\u0018\u00010\n2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u0016\u0010\f\u001a\u00020\u00032\u0006\u0010\r\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0014\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00110\u0010H\'J\u0014\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00110\u0010H\'J\u001e\u0010\u0013\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\u0014\u001a\u00020\u00032\u0006\u0010\r\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000e\u00a8\u0006\u0015"}, d2 = {"Lpress/pelldom/sessionledger/mobile/data/db/dao/CategoryDao;", "", "archive", "", "id", "", "updatedAtMs", "", "(Ljava/lang/String;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getById", "Lpress/pelldom/sessionledger/mobile/data/db/entities/CategoryEntity;", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insert", "category", "(Lpress/pelldom/sessionledger/mobile/data/db/entities/CategoryEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "observeActiveCategories", "Lkotlinx/coroutines/flow/Flow;", "", "observeAllCategories", "unarchive", "update", "mobile_debug"})
@androidx.room.Dao()
public abstract interface CategoryDao {
    
    @androidx.room.Query(value = "SELECT * FROM categories WHERE archived = 0 ORDER BY name COLLATE NOCASE ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity>> observeActiveCategories();
    
    @androidx.room.Query(value = "SELECT * FROM categories ORDER BY archived ASC, name COLLATE NOCASE ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity>> observeAllCategories();
    
    @androidx.room.Query(value = "SELECT * FROM categories WHERE id = :id LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity> $completion);
    
    @androidx.room.Insert(onConflict = 3)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity category, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object update(@org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity category, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE categories SET archived = 1, updatedAtMs = :updatedAtMs WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object archive(@org.jetbrains.annotations.NotNull()
    java.lang.String id, long updatedAtMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE categories SET archived = 0, updatedAtMs = :updatedAtMs WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object unarchive(@org.jetbrains.annotations.NotNull()
    java.lang.String id, long updatedAtMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}