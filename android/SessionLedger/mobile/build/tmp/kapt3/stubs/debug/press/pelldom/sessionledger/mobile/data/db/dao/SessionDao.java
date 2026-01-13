package press.pelldom.sessionledger.mobile.data.db.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0000\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\u0018\u0010\n\u001a\u0004\u0018\u00010\b2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J$\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\b0\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u0011\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u0010\u0010\u0014\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0015H\'J\u0014\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\f0\u0015H\'J\u0016\u0010\u0017\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\u0013\u00a8\u0006\u0018"}, d2 = {"Lpress/pelldom/sessionledger/mobile/data/db/dao/SessionDao;", "", "deleteById", "", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getActiveSession", "Lpress/pelldom/sessionledger/mobile/data/db/entities/SessionEntity;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getById", "getEndedSessionsInRange", "", "startMs", "", "endMs", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insert", "session", "(Lpress/pelldom/sessionledger/mobile/data/db/entities/SessionEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "observeActiveSession", "Lkotlinx/coroutines/flow/Flow;", "observeEndedSessionsNewestFirst", "update", "mobile_debug"})
@androidx.room.Dao()
public abstract interface SessionDao {
    
    @androidx.room.Query(value = "SELECT * FROM sessions WHERE state != \'ENDED\' ORDER BY startTimeMs DESC LIMIT 1")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity> observeActiveSession();
    
    @androidx.room.Query(value = "SELECT * FROM sessions WHERE state != \'ENDED\' ORDER BY startTimeMs DESC LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getActiveSession(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM sessions WHERE id = :id LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM sessions WHERE state = \'ENDED\' ORDER BY startTimeMs DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity>> observeEndedSessionsNewestFirst();
    
    @androidx.room.Query(value = "\n        SELECT * FROM sessions\n        WHERE state = \'ENDED\'\n          AND startTimeMs >= :startMs\n          AND startTimeMs < :endMs\n        ORDER BY startTimeMs ASC\n    ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getEndedSessionsInRange(long startMs, long endMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity>> $completion);
    
    @androidx.room.Insert(onConflict = 3)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity session, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object update(@org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity session, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM sessions WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}