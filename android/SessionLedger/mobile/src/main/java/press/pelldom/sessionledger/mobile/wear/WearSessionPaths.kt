package press.pelldom.sessionledger.mobile.wear

object WearSessionPaths {
    const val START = "/session/start"
    const val PAUSE = "/session/pause"
    const val RESUME = "/session/resume"
    const val END = "/session/end"

    const val ACTIVE_SESSION_STATE = "/active_session_state"

    const val KEY_STATE = "state" // "NONE" | "RUNNING" | "PAUSED"
    const val KEY_START_TIME_MILLIS = "startTimeMillis"
}

