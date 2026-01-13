package press.pelldom.sessionledger.mobile.billing;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\tH\u0002J9\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\t2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u0002\u00a2\u0006\u0002\u0010\u0012J\b\u0010\u0013\u001a\u00020\u0014H\u0007J\b\u0010\u0015\u001a\u00020\u0014H\u0007J\b\u0010\u0016\u001a\u00020\u0014H\u0007J\b\u0010\u0017\u001a\u00020\u0014H\u0007J\b\u0010\u0018\u001a\u00020\u0014H\u0007\u00a8\u0006\u0019"}, d2 = {"Lpress/pelldom/sessionledger/mobile/billing/BillingEngineTest;", "", "()V", "categoryWithMinCharge", "Lpress/pelldom/sessionledger/mobile/data/db/entities/CategoryEntity;", "minCharge", "", "categoryWithMinTimeSeconds", "minTimeSeconds", "", "endedSession", "Lpress/pelldom/sessionledger/mobile/data/db/entities/SessionEntity;", "minutesTracked", "categoryId", "", "hourlyRateOverride", "roundingModeOverride", "Lpress/pelldom/sessionledger/mobile/billing/RoundingMode;", "(JLjava/lang/String;Ljava/lang/Double;Lpress/pelldom/sessionledger/mobile/billing/RoundingMode;)Lpress/pelldom/sessionledger/mobile/data/db/entities/SessionEntity;", "testCategoryMinCharge", "", "testCategoryMinTime", "testGlobalDefaultsRoundingUp", "testMinTimeAndMinCharge", "testSessionOverridesAndCategoryMinTime", "mobile_debugUnitTest"})
public final class BillingEngineTest {
    
    public BillingEngineTest() {
        super();
    }
    
    @org.junit.Test()
    public final void testGlobalDefaultsRoundingUp() {
    }
    
    @org.junit.Test()
    public final void testCategoryMinTime() {
    }
    
    @org.junit.Test()
    public final void testCategoryMinCharge() {
    }
    
    @org.junit.Test()
    public final void testSessionOverridesAndCategoryMinTime() {
    }
    
    @org.junit.Test()
    public final void testMinTimeAndMinCharge() {
    }
    
    private final press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity endedSession(long minutesTracked, java.lang.String categoryId, java.lang.Double hourlyRateOverride, press.pelldom.sessionledger.mobile.billing.RoundingMode roundingModeOverride) {
        return null;
    }
    
    private final press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity categoryWithMinTimeSeconds(long minTimeSeconds) {
        return null;
    }
    
    private final press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity categoryWithMinCharge(double minCharge) {
        return null;
    }
}