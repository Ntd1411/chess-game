package kma.game.chess2d.ui.screen

import kma.game.chess2d.R

internal object MainMenuLayout {
    fun columnsFor(widthDp: Float) = if (widthDp >= 480f) 3 else 2
}
internal enum class MainMenuShortcut(val label: Int) {
    PROFILE(R.string.profile_title), JOURNAL(R.string.main_menu_journal), INVENTORY(R.string.main_menu_inventory),
    SHOP(R.string.shop_title), HISTORY(R.string.main_menu_history), SETTINGS(R.string.main_menu_settings),
}
