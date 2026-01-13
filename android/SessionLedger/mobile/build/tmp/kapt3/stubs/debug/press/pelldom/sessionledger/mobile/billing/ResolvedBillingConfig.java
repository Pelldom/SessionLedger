package press.pelldom.sessionledger.mobile.billing;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u001d\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BW\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\b\u0010\b\u001a\u0004\u0018\u00010\t\u0012\u0006\u0010\n\u001a\u00020\u0005\u0012\u0006\u0010\u000b\u001a\u00020\f\u0012\u0006\u0010\r\u001a\u00020\u0005\u0012\u0006\u0010\u000e\u001a\u00020\u0003\u0012\u0006\u0010\u000f\u001a\u00020\u0005\u0012\u0006\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\u0002\u0010\u0012J\t\u0010#\u001a\u00020\u0003H\u00c6\u0003J\t\u0010$\u001a\u00020\u0011H\u00c6\u0003J\t\u0010%\u001a\u00020\u0005H\u00c6\u0003J\t\u0010&\u001a\u00020\u0007H\u00c6\u0003J\u000b\u0010\'\u001a\u0004\u0018\u00010\tH\u00c6\u0003J\t\u0010(\u001a\u00020\u0005H\u00c6\u0003J\t\u0010)\u001a\u00020\fH\u00c6\u0003J\t\u0010*\u001a\u00020\u0005H\u00c6\u0003J\t\u0010+\u001a\u00020\u0003H\u00c6\u0003J\t\u0010,\u001a\u00020\u0005H\u00c6\u0003Jo\u0010-\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t2\b\b\u0002\u0010\n\u001a\u00020\u00052\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\u00052\b\b\u0002\u0010\u000e\u001a\u00020\u00032\b\b\u0002\u0010\u000f\u001a\u00020\u00052\b\b\u0002\u0010\u0010\u001a\u00020\u0011H\u00c6\u0001J\u0013\u0010.\u001a\u00020/2\b\u00100\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00101\u001a\u000202H\u00d6\u0001J\t\u00103\u001a\u00020\u0011H\u00d6\u0001R\u0011\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u000e\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\u000f\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\r\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0018R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0016R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0018R\u0013\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0011\u0010\n\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u0018\u00a8\u00064"}, d2 = {"Lpress/pelldom/sessionledger/mobile/billing/ResolvedBillingConfig;", "", "ratePerHour", "", "rateSource", "Lpress/pelldom/sessionledger/mobile/billing/SourceType;", "roundingMode", "Lpress/pelldom/sessionledger/mobile/billing/RoundingMode;", "roundingDirection", "Lpress/pelldom/sessionledger/mobile/billing/RoundingDirection;", "roundingSource", "minTimeSeconds", "", "minTimeSource", "minCharge", "minChargeSource", "currency", "", "(DLpress/pelldom/sessionledger/mobile/billing/SourceType;Lpress/pelldom/sessionledger/mobile/billing/RoundingMode;Lpress/pelldom/sessionledger/mobile/billing/RoundingDirection;Lpress/pelldom/sessionledger/mobile/billing/SourceType;JLpress/pelldom/sessionledger/mobile/billing/SourceType;DLpress/pelldom/sessionledger/mobile/billing/SourceType;Ljava/lang/String;)V", "getCurrency", "()Ljava/lang/String;", "getMinCharge", "()D", "getMinChargeSource", "()Lpress/pelldom/sessionledger/mobile/billing/SourceType;", "getMinTimeSeconds", "()J", "getMinTimeSource", "getRatePerHour", "getRateSource", "getRoundingDirection", "()Lpress/pelldom/sessionledger/mobile/billing/RoundingDirection;", "getRoundingMode", "()Lpress/pelldom/sessionledger/mobile/billing/RoundingMode;", "getRoundingSource", "component1", "component10", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "hashCode", "", "toString", "mobile_debug"})
public final class ResolvedBillingConfig {
    private final double ratePerHour = 0.0;
    @org.jetbrains.annotations.NotNull()
    private final press.pelldom.sessionledger.mobile.billing.SourceType rateSource = null;
    @org.jetbrains.annotations.NotNull()
    private final press.pelldom.sessionledger.mobile.billing.RoundingMode roundingMode = null;
    @org.jetbrains.annotations.Nullable()
    private final press.pelldom.sessionledger.mobile.billing.RoundingDirection roundingDirection = null;
    @org.jetbrains.annotations.NotNull()
    private final press.pelldom.sessionledger.mobile.billing.SourceType roundingSource = null;
    private final long minTimeSeconds = 0L;
    @org.jetbrains.annotations.NotNull()
    private final press.pelldom.sessionledger.mobile.billing.SourceType minTimeSource = null;
    private final double minCharge = 0.0;
    @org.jetbrains.annotations.NotNull()
    private final press.pelldom.sessionledger.mobile.billing.SourceType minChargeSource = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String currency = null;
    
    public ResolvedBillingConfig(double ratePerHour, @org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.billing.SourceType rateSource, @org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.billing.RoundingMode roundingMode, @org.jetbrains.annotations.Nullable()
    press.pelldom.sessionledger.mobile.billing.RoundingDirection roundingDirection, @org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.billing.SourceType roundingSource, long minTimeSeconds, @org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.billing.SourceType minTimeSource, double minCharge, @org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.billing.SourceType minChargeSource, @org.jetbrains.annotations.NotNull()
    java.lang.String currency) {
        super();
    }
    
    public final double getRatePerHour() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.SourceType getRateSource() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.RoundingMode getRoundingMode() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final press.pelldom.sessionledger.mobile.billing.RoundingDirection getRoundingDirection() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.SourceType getRoundingSource() {
        return null;
    }
    
    public final long getMinTimeSeconds() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.SourceType getMinTimeSource() {
        return null;
    }
    
    public final double getMinCharge() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.SourceType getMinChargeSource() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCurrency() {
        return null;
    }
    
    public final double component1() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.SourceType component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.RoundingMode component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final press.pelldom.sessionledger.mobile.billing.RoundingDirection component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.SourceType component5() {
        return null;
    }
    
    public final long component6() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.SourceType component7() {
        return null;
    }
    
    public final double component8() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.SourceType component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.ResolvedBillingConfig copy(double ratePerHour, @org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.billing.SourceType rateSource, @org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.billing.RoundingMode roundingMode, @org.jetbrains.annotations.Nullable()
    press.pelldom.sessionledger.mobile.billing.RoundingDirection roundingDirection, @org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.billing.SourceType roundingSource, long minTimeSeconds, @org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.billing.SourceType minTimeSource, double minCharge, @org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.billing.SourceType minChargeSource, @org.jetbrains.annotations.NotNull()
    java.lang.String currency) {
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