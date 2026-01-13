package press.pelldom.sessionledger.mobile.billing;

/**
 * Where a setting/value came from when resolving precedence.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lpress/pelldom/sessionledger/mobile/billing/SourceType;", "", "(Ljava/lang/String;I)V", "SESSION", "CATEGORY", "GLOBAL", "NONE", "mobile_debug"})
public enum SourceType {
    /*public static final*/ SESSION /* = new SESSION() */,
    /*public static final*/ CATEGORY /* = new CATEGORY() */,
    /*public static final*/ GLOBAL /* = new GLOBAL() */,
    /*public static final*/ NONE /* = new NONE() */;
    
    SourceType() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<press.pelldom.sessionledger.mobile.billing.SourceType> getEntries() {
        return null;
    }
}