package press.pelldom.sessionledger.mobile.data.db

import androidx.room.TypeConverter
import press.pelldom.sessionledger.mobile.billing.RoundingDirection
import press.pelldom.sessionledger.mobile.billing.RoundingMode
import press.pelldom.sessionledger.mobile.billing.SessionState

class RoomConverters {

    @TypeConverter
    fun sessionStateToString(value: SessionState): String = value.name

    @TypeConverter
    fun stringToSessionState(value: String): SessionState = SessionState.valueOf(value)

    @TypeConverter
    fun roundingModeToString(value: RoundingMode?): String? = value?.name

    @TypeConverter
    fun stringToRoundingMode(value: String?): RoundingMode? =
        value?.let { RoundingMode.valueOf(it) }

    @TypeConverter
    fun roundingDirectionToString(value: RoundingDirection?): String? = value?.name

    @TypeConverter
    fun stringToRoundingDirection(value: String?): RoundingDirection? =
        value?.let { RoundingDirection.valueOf(it) }
}

