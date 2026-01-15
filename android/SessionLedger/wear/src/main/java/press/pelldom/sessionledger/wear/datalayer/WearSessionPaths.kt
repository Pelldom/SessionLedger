package press.pelldom.sessionledger.wear.datalayer

object WearSessionPaths {
    const val START = "/session/start"
    const val PAUSE = "/session/pause"
    const val RESUME = "/session/resume"
    const val END = "/session/end"

    const val SESSION_STATE = "/session/state"

    const val KEY_STATE = "state" // "NONE" | "RUNNING" | "PAUSED"
    const val KEY_START_TIME_MILLIS = "startTimeMillis"
    const val KEY_ELAPSED_MILLIS = "elapsedMillis"
}

