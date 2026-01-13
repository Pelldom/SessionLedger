package press.pelldom.sessionledger.mobile.export;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004H\u0002JJ\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\r2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\r2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0010\u001a\u00020\u0011H\u0086@\u00a2\u0006\u0002\u0010\u0012J \u0010\u0013\u001a\u00020\u00042\u0006\u0010\u0014\u001a\u00020\r2\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018H\u0002J\u0010\u0010\u0019\u001a\u00020\u00042\u0006\u0010\u001a\u001a\u00020\u001bH\u0002J\u0010\u0010\u001c\u001a\u00020\u00042\u0006\u0010\u001d\u001a\u00020\u001eH\u0002J\u0010\u0010\u001f\u001a\u00020\u00042\u0006\u0010 \u001a\u00020!H\u0002\u00a8\u0006\""}, d2 = {"Lpress/pelldom/sessionledger/mobile/export/CsvExporter;", "", "()V", "csvEscape", "", "raw", "exportEndedSessions", "", "db", "Lpress/pelldom/sessionledger/mobile/data/db/AppDatabase;", "settingsRepo", "Lpress/pelldom/sessionledger/mobile/settings/SettingsRepository;", "startFilter", "", "endFilter", "categoryFilter", "context", "Landroid/content/Context;", "(Lpress/pelldom/sessionledger/mobile/data/db/AppDatabase;Lpress/pelldom/sessionledger/mobile/settings/SettingsRepository;Ljava/lang/Long;Ljava/lang/Long;Ljava/lang/String;Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "formatLocal", "epochMs", "zoneId", "Ljava/time/ZoneId;", "formatter", "Ljava/time/format/DateTimeFormatter;", "money2", "value", "", "roundingModeToCsv", "mode", "Lpress/pelldom/sessionledger/mobile/billing/RoundingMode;", "sourceToCsv", "source", "Lpress/pelldom/sessionledger/mobile/billing/SourceType;", "mobile_debug"})
public final class CsvExporter {
    
    public CsvExporter() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object exportEndedSessions(@org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.data.db.AppDatabase db, @org.jetbrains.annotations.NotNull()
    press.pelldom.sessionledger.mobile.settings.SettingsRepository settingsRepo, @org.jetbrains.annotations.Nullable()
    java.lang.Long startFilter, @org.jetbrains.annotations.Nullable()
    java.lang.Long endFilter, @org.jetbrains.annotations.Nullable()
    java.lang.String categoryFilter, @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.String formatLocal(long epochMs, java.time.ZoneId zoneId, java.time.format.DateTimeFormatter formatter) {
        return null;
    }
    
    private final java.lang.String roundingModeToCsv(press.pelldom.sessionledger.mobile.billing.RoundingMode mode) {
        return null;
    }
    
    private final java.lang.String sourceToCsv(press.pelldom.sessionledger.mobile.billing.SourceType source) {
        return null;
    }
    
    private final java.lang.String money2(double value) {
        return null;
    }
    
    private final java.lang.String csvEscape(java.lang.String raw) {
        return null;
    }
}