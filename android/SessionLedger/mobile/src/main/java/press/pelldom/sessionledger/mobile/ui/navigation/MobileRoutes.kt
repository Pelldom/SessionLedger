package press.pelldom.sessionledger.mobile.ui.navigation

object MobileRoutes {
    const val ACTIVE = "active"
    const val SESSIONS = "sessions"
    const val SETTINGS = "settings"
    const val CATEGORIES = "categories"

    const val SESSION_DETAIL = "session_detail"
    const val SESSION_DETAIL_ROUTE = "session_detail/{sessionId}"
    const val SESSION_TIMING_EDIT = "session_timing_edit"
    const val SESSION_TIMING_EDIT_ROUTE = "session_timing_edit/{sessionId}"
    const val SESSION_BILLING_OVERRIDE = "session_billing_override"
    const val SESSION_BILLING_OVERRIDE_ROUTE = "session_billing_override/{sessionId}"

    const val CATEGORY_DETAIL = "category_detail"
    const val CATEGORY_DETAIL_ROUTE = "category_detail/{categoryId}"

    fun sessionDetailRoute(sessionId: String): String = "session_detail/$sessionId"
    fun sessionTimingEditRoute(sessionId: String): String = "session_timing_edit/$sessionId"
    fun sessionBillingOverrideRoute(sessionId: String): String = "session_billing_override/$sessionId"
    fun categoryDetailRoute(categoryId: String): String = "category_detail/$categoryId"
}

