package press.pelldom.sessionledger.mobile.billing;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J \u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\u0006\u0010\t\u001a\u00020\nJ \u0010\u000b\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\u0006\u0010\t\u001a\u00020\n\u00a8\u0006\r"}, d2 = {"Lpress/pelldom/sessionledger/mobile/billing/BillingEngine;", "", "()V", "calculateForEndedSession", "Lpress/pelldom/sessionledger/mobile/billing/BillingResult;", "session", "Lpress/pelldom/sessionledger/mobile/data/db/entities/SessionEntity;", "category", "Lpress/pelldom/sessionledger/mobile/data/db/entities/CategoryEntity;", "settings", "Lpress/pelldom/sessionledger/mobile/settings/GlobalSettings;", "resolveConfig", "Lpress/pelldom/sessionledger/mobile/billing/ResolvedBillingConfig;", "mobile_debug"})
public final class BillingEngine {
    @org.jetbrains.annotations.NotNull()
    public static final press.pelldom.sessionledger.mobile.billing.BillingEngine INSTANCE = null;
    
    private BillingEngine() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.ResolvedBillingConfig resolveConfig(@org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity session, @org.jetbrains.annotations.Nullable()
    press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity category, @org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.settings.GlobalSettings settings) {
        return null;
    }
    
    /**
     * Computes billing for an ENDED session.
     * Assumes session.endTimeMs is non-null and session.state == ENDED.
     */
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.BillingResult calculateForEndedSession(@org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity session, @org.jetbrains.annotations.Nullable()
    press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity category, @org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.settings.GlobalSettings settings) {
        return null;
    }
}