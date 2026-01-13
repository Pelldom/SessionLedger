package press.pelldom.sessionledger.mobile.data.db;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0014\u0010\u0003\u001a\u0004\u0018\u00010\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u0007J\u0014\u0010\u0007\u001a\u0004\u0018\u00010\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\bH\u0007J\u0010\u0010\t\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\nH\u0007J\u0014\u0010\u000b\u001a\u0004\u0018\u00010\u00062\b\u0010\u0005\u001a\u0004\u0018\u00010\u0004H\u0007J\u0014\u0010\f\u001a\u0004\u0018\u00010\b2\b\u0010\u0005\u001a\u0004\u0018\u00010\u0004H\u0007J\u0010\u0010\r\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u0004H\u0007\u00a8\u0006\u000e"}, d2 = {"Lpress/pelldom/sessionledger/mobile/data/db/RoomConverters;", "", "()V", "roundingDirectionToString", "", "value", "Lpress/pelldom/sessionledger/mobile/billing/RoundingDirection;", "roundingModeToString", "Lpress/pelldom/sessionledger/mobile/billing/RoundingMode;", "sessionStateToString", "Lpress/pelldom/sessionledger/mobile/billing/SessionState;", "stringToRoundingDirection", "stringToRoundingMode", "stringToSessionState", "mobile_debug"})
public final class RoomConverters {
    
    public RoomConverters() {
        super();
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String sessionStateToString(@org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.billing.SessionState value) {
        return null;
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final press.pelldom.sessionledger.mobile.billing.SessionState stringToSessionState(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
        return null;
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String roundingModeToString(@org.jetbrains.annotations.Nullable()
    press.pelldom.sessionledger.mobile.billing.RoundingMode value) {
        return null;
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.Nullable()
    public final press.pelldom.sessionledger.mobile.billing.RoundingMode stringToRoundingMode(@org.jetbrains.annotations.Nullable()
    java.lang.String value) {
        return null;
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String roundingDirectionToString(@org.jetbrains.annotations.Nullable()
    press.pelldom.sessionledger.mobile.billing.RoundingDirection value) {
        return null;
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.Nullable()
    public final press.pelldom.sessionledger.mobile.billing.RoundingDirection stringToRoundingDirection(@org.jetbrains.annotations.Nullable()
    java.lang.String value) {
        return null;
    }
}