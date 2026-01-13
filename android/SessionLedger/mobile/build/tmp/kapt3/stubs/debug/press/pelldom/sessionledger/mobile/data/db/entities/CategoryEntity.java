package press.pelldom.sessionledger.mobile.data.db.entities;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b&\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001Bk\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\b\u0012\u0006\u0010\u0010\u001a\u00020\u000e\u0012\u0006\u0010\u0011\u001a\u00020\u000e\u00a2\u0006\u0002\u0010\u0012J\t\u0010&\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\'\u001a\u00020\u000eH\u00c6\u0003J\t\u0010(\u001a\u00020\u0003H\u00c6\u0003J\t\u0010)\u001a\u00020\u0006H\u00c6\u0003J\u0010\u0010*\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0018J\u000b\u0010+\u001a\u0004\u0018\u00010\nH\u00c6\u0003J\u000b\u0010,\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\u0010\u0010-\u001a\u0004\u0018\u00010\u000eH\u00c6\u0003\u00a2\u0006\u0002\u0010\u001dJ\u0010\u0010.\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0018J\t\u0010/\u001a\u00020\u000eH\u00c6\u0003J|\u00100\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010\u0010\u001a\u00020\u000e2\b\b\u0002\u0010\u0011\u001a\u00020\u000eH\u00c6\u0001\u00a2\u0006\u0002\u00101J\u0013\u00102\u001a\u00020\u00062\b\u00103\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00104\u001a\u000205H\u00d6\u0001J\t\u00106\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0010\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0015\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u0010\u0019\u001a\u0004\b\u0017\u0010\u0018R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0015\u0010\r\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\n\n\u0002\u0010\u001e\u001a\u0004\b\u001c\u0010\u001dR\u0015\u0010\u000f\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u0010\u0019\u001a\u0004\b\u001f\u0010\u0018R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001bR\u0013\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u0013\u0010\t\u001a\u0004\u0018\u00010\n\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0011\u0010\u0011\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u0016\u00a8\u00067"}, d2 = {"Lpress/pelldom/sessionledger/mobile/data/db/entities/CategoryEntity;", "", "id", "", "name", "archived", "", "defaultHourlyRate", "", "roundingMode", "Lpress/pelldom/sessionledger/mobile/billing/RoundingMode;", "roundingDirection", "Lpress/pelldom/sessionledger/mobile/billing/RoundingDirection;", "minBillableSeconds", "", "minChargeAmount", "createdAtMs", "updatedAtMs", "(Ljava/lang/String;Ljava/lang/String;ZLjava/lang/Double;Lpress/pelldom/sessionledger/mobile/billing/RoundingMode;Lpress/pelldom/sessionledger/mobile/billing/RoundingDirection;Ljava/lang/Long;Ljava/lang/Double;JJ)V", "getArchived", "()Z", "getCreatedAtMs", "()J", "getDefaultHourlyRate", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getId", "()Ljava/lang/String;", "getMinBillableSeconds", "()Ljava/lang/Long;", "Ljava/lang/Long;", "getMinChargeAmount", "getName", "getRoundingDirection", "()Lpress/pelldom/sessionledger/mobile/billing/RoundingDirection;", "getRoundingMode", "()Lpress/pelldom/sessionledger/mobile/billing/RoundingMode;", "getUpdatedAtMs", "component1", "component10", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;ZLjava/lang/Double;Lpress/pelldom/sessionledger/mobile/billing/RoundingMode;Lpress/pelldom/sessionledger/mobile/billing/RoundingDirection;Ljava/lang/Long;Ljava/lang/Double;JJ)Lpress/pelldom/sessionledger/mobile/data/db/entities/CategoryEntity;", "equals", "other", "hashCode", "", "toString", "mobile_debug"})
@androidx.room.Entity(tableName = "categories")
public final class CategoryEntity {
    @androidx.room.PrimaryKey()
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String name = null;
    private final boolean archived = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double defaultHourlyRate = null;
    @org.jetbrains.annotations.Nullable()
    private final press.pelldom.sessionledger.mobile.billing.RoundingMode roundingMode = null;
    @org.jetbrains.annotations.Nullable()
    private final press.pelldom.sessionledger.mobile.billing.RoundingDirection roundingDirection = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Long minBillableSeconds = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double minChargeAmount = null;
    private final long createdAtMs = 0L;
    private final long updatedAtMs = 0L;
    
    public CategoryEntity(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, boolean archived, @org.jetbrains.annotations.Nullable()
    java.lang.Double defaultHourlyRate, @org.jetbrains.annotations.Nullable()
    press.pelldom.sessionledger.mobile.billing.RoundingMode roundingMode, @org.jetbrains.annotations.Nullable()
    press.pelldom.sessionledger.mobile.billing.RoundingDirection roundingDirection, @org.jetbrains.annotations.Nullable()
    java.lang.Long minBillableSeconds, @org.jetbrains.annotations.Nullable()
    java.lang.Double minChargeAmount, long createdAtMs, long updatedAtMs) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getName() {
        return null;
    }
    
    public final boolean getArchived() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getDefaultHourlyRate() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final press.pelldom.sessionledger.mobile.billing.RoundingMode getRoundingMode() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final press.pelldom.sessionledger.mobile.billing.RoundingDirection getRoundingDirection() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long getMinBillableSeconds() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getMinChargeAmount() {
        return null;
    }
    
    public final long getCreatedAtMs() {
        return 0L;
    }
    
    public final long getUpdatedAtMs() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    public final long component10() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    public final boolean component3() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final press.pelldom.sessionledger.mobile.billing.RoundingMode component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final press.pelldom.sessionledger.mobile.billing.RoundingDirection component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component8() {
        return null;
    }
    
    public final long component9() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, boolean archived, @org.jetbrains.annotations.Nullable()
    java.lang.Double defaultHourlyRate, @org.jetbrains.annotations.Nullable()
    press.pelldom.sessionledger.mobile.billing.RoundingMode roundingMode, @org.jetbrains.annotations.Nullable()
    press.pelldom.sessionledger.mobile.billing.RoundingDirection roundingDirection, @org.jetbrains.annotations.Nullable()
    java.lang.Long minBillableSeconds, @org.jetbrains.annotations.Nullable()
    java.lang.Double minChargeAmount, long createdAtMs, long updatedAtMs) {
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