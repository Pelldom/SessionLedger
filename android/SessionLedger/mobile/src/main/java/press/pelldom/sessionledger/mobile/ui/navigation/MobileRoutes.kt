package press.pelldom.sessionledger.mobile.ui.navigation

object MobileRoutes {
    const val ACTIVE = "active"
    const val SESSIONS = "sessions"
    const val SETTINGS = "settings"
    const val CATEGORIES = "categories"

    const val SESSION_DETAIL = "session_detail"
    const val SESSION_DETAIL_ROUTE = "session_detail/{sessionId}"

    fun sessionDetailRoute(sessionId: String): String = "session_detail/$sessionId"
}

