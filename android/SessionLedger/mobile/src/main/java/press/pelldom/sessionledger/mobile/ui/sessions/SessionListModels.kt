package press.pelldom.sessionledger.mobile.ui.sessions

data class SessionListItemUiModel(
    val id: String,
    val dateText: String,
    val durationText: String,
    val categoryText: String,
    val amountText: String? = null,
)

