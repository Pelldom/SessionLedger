package press.pelldom.sessionledger.mobile.data.db.entities;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b/\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B\u009f\u0001\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\u0005\u0012\u0006\u0010\n\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0010\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u000e\u0012\u0006\u0010\u0015\u001a\u00020\u0003\u0012\u0006\u0010\u0016\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0017J\t\u00100\u001a\u00020\u0003H\u00c6\u0003J\u000b\u00101\u001a\u0004\u0018\u00010\u0010H\u00c6\u0003J\u000b\u00102\u001a\u0004\u0018\u00010\u0012H\u00c6\u0003J\u0010\u00103\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001cJ\u0010\u00104\u001a\u0004\u0018\u00010\u000eH\u00c6\u0003\u00a2\u0006\u0002\u0010\u001fJ\t\u00105\u001a\u00020\u0003H\u00c6\u0003J\t\u00106\u001a\u00020\u0005H\u00c6\u0003J\t\u00107\u001a\u00020\u0005H\u00c6\u0003J\u0010\u00108\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001cJ\t\u00109\u001a\u00020\bH\u00c6\u0003J\t\u0010:\u001a\u00020\u0005H\u00c6\u0003J\t\u0010;\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010<\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010=\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0010\u0010>\u001a\u0004\u0018\u00010\u000eH\u00c6\u0003\u00a2\u0006\u0002\u0010\u001fJ\u00b4\u0001\u0010?\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\u00052\b\b\u0002\u0010\n\u001a\u00020\u00052\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00102\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00122\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u000e2\b\b\u0002\u0010\u0015\u001a\u00020\u00032\b\b\u0002\u0010\u0016\u001a\u00020\u0005H\u00c6\u0001\u00a2\u0006\u0002\u0010@J\u0013\u0010A\u001a\u00020B2\b\u0010C\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010D\u001a\u00020EH\u00d6\u0001J\t\u0010F\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\u0015\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0019R\u0015\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\n\n\u0002\u0010\u001d\u001a\u0004\b\u001b\u0010\u001cR\u0015\u0010\r\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\n\n\u0002\u0010 \u001a\u0004\b\u001e\u0010\u001fR\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0019R\u0011\u0010\n\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0015\u0010\u0013\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\n\n\u0002\u0010\u001d\u001a\u0004\b$\u0010\u001cR\u0015\u0010\u0014\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\n\n\u0002\u0010 \u001a\u0004\b%\u0010\u001fR\u0013\u0010\f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u0019R\u0011\u0010\t\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010#R\u0013\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010)R\u0013\u0010\u000f\u001a\u0004\u0018\u00010\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010+R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010#R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010.R\u0011\u0010\u0016\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010#\u00a8\u0006G"}, d2 = {"Lpress/pelldom/sessionledger/mobile/data/db/entities/SessionEntity;", "", "id", "", "startTimeMs", "", "endTimeMs", "state", "Lpress/pelldom/sessionledger/mobile/billing/SessionState;", "pausedTotalMs", "lastStateChangeTimeMs", "categoryId", "notes", "hourlyRateOverride", "", "roundingModeOverride", "Lpress/pelldom/sessionledger/mobile/billing/RoundingMode;", "roundingDirectionOverride", "Lpress/pelldom/sessionledger/mobile/billing/RoundingDirection;", "minBillableSecondsOverride", "minChargeAmountOverride", "createdOnDevice", "updatedAtMs", "(Ljava/lang/String;JLjava/lang/Long;Lpress/pelldom/sessionledger/mobile/billing/SessionState;JJLjava/lang/String;Ljava/lang/String;Ljava/lang/Double;Lpress/pelldom/sessionledger/mobile/billing/RoundingMode;Lpress/pelldom/sessionledger/mobile/billing/RoundingDirection;Ljava/lang/Long;Ljava/lang/Double;Ljava/lang/String;J)V", "getCategoryId", "()Ljava/lang/String;", "getCreatedOnDevice", "getEndTimeMs", "()Ljava/lang/Long;", "Ljava/lang/Long;", "getHourlyRateOverride", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getId", "getLastStateChangeTimeMs", "()J", "getMinBillableSecondsOverride", "getMinChargeAmountOverride", "getNotes", "getPausedTotalMs", "getRoundingDirectionOverride", "()Lpress/pelldom/sessionledger/mobile/billing/RoundingDirection;", "getRoundingModeOverride", "()Lpress/pelldom/sessionledger/mobile/billing/RoundingMode;", "getStartTimeMs", "getState", "()Lpress/pelldom/sessionledger/mobile/billing/SessionState;", "getUpdatedAtMs", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;JLjava/lang/Long;Lpress/pelldom/sessionledger/mobile/billing/SessionState;JJLjava/lang/String;Ljava/lang/String;Ljava/lang/Double;Lpress/pelldom/sessionledger/mobile/billing/RoundingMode;Lpress/pelldom/sessionledger/mobile/billing/RoundingDirection;Ljava/lang/Long;Ljava/lang/Double;Ljava/lang/String;J)Lpress/pelldom/sessionledger/mobile/data/db/entities/SessionEntity;", "equals", "", "other", "hashCode", "", "toString", "mobile_debug"})
@androidx.room.Entity(tableName = "sessions")
public final class SessionEntity {
    @androidx.room.PrimaryKey()
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    private final long startTimeMs = 0L;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Long endTimeMs = null;
    @org.jetbrains.annotations.NotNull()
    private final press.pelldom.sessionledger.mobile.billing.SessionState state = null;
    private final long pausedTotalMs = 0L;
    private final long lastStateChangeTimeMs = 0L;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String categoryId = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String notes = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double hourlyRateOverride = null;
    @org.jetbrains.annotations.Nullable()
    private final press.pelldom.sessionledger.mobile.billing.RoundingMode roundingModeOverride = null;
    @org.jetbrains.annotations.Nullable()
    private final press.pelldom.sessionledger.mobile.billing.RoundingDirection roundingDirectionOverride = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Long minBillableSecondsOverride = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double minChargeAmountOverride = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String createdOnDevice = null;
    private final long updatedAtMs = 0L;
    
    public SessionEntity(@org.jetbrains.annotations.NotNull()
    java.lang.String id, long startTimeMs, @org.jetbrains.annotations.Nullable()
    java.lang.Long endTimeMs, @org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.billing.SessionState state, long pausedTotalMs, long lastStateChangeTimeMs, @org.jetbrains.annotations.Nullable()
    java.lang.String categoryId, @org.jetbrains.annotations.Nullable()
    java.lang.String notes, @org.jetbrains.annotations.Nullable()
    java.lang.Double hourlyRateOverride, @org.jetbrains.annotations.Nullable()
    press.pelldom.sessionledger.mobile.billing.RoundingMode roundingModeOverride, @org.jetbrains.annotations.Nullable()
    press.pelldom.sessionledger.mobile.billing.RoundingDirection roundingDirectionOverride, @org.jetbrains.annotations.Nullable()
    java.lang.Long minBillableSecondsOverride, @org.jetbrains.annotations.Nullable()
    java.lang.Double minChargeAmountOverride, @org.jetbrains.annotations.NotNull()
    java.lang.String createdOnDevice, long updatedAtMs) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    public final long getStartTimeMs() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long getEndTimeMs() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.SessionState getState() {
        return null;
    }
    
    public final long getPausedTotalMs() {
        return 0L;
    }
    
    public final long getLastStateChangeTimeMs() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCategoryId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getNotes() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getHourlyRateOverride() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final press.pelldom.sessionledger.mobile.billing.RoundingMode getRoundingModeOverride() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final press.pelldom.sessionledger.mobile.billing.RoundingDirection getRoundingDirectionOverride() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long getMinBillableSecondsOverride() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getMinChargeAmountOverride() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCreatedOnDevice() {
        return null;
    }
    
    public final long getUpdatedAtMs() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final press.pelldom.sessionledger.mobile.billing.RoundingMode component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final press.pelldom.sessionledger.mobile.billing.RoundingDirection component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component13() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component14() {
        return null;
    }
    
    public final long component15() {
        return 0L;
    }
    
    public final long component2() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.SessionState component4() {
        return null;
    }
    
    public final long component5() {
        return 0L;
    }
    
    public final long component6() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, long startTimeMs, @org.jetbrains.annotations.Nullable()
    java.lang.Long endTimeMs, @org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.billing.SessionState state, long pausedTotalMs, long lastStateChangeTimeMs, @org.jetbrains.annotations.Nullable()
    java.lang.String categoryId, @org.jetbrains.annotations.Nullable()
    java.lang.String notes, @org.jetbrains.annotations.Nullable()
    java.lang.Double hourlyRateOverride, @org.jetbrains.annotations.Nullable()
    press.pelldom.sessionledger.mobile.billing.RoundingMode roundingModeOverride, @org.jetbrains.annotations.Nullable()
    press.pelldom.sessionledger.mobile.billing.RoundingDirection roundingDirectionOverride, @org.jetbrains.annotations.Nullable()
    java.lang.Long minBillableSecondsOverride, @org.jetbrains.annotations.Nullable()
    java.lang.Double minChargeAmountOverride, @org.jetbrains.annotations.NotNull()
    java.lang.String createdOnDevice, long updatedAtMs) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}