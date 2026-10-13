package kma.game.chess2d.ui.art

import kma.game.chess2d.R

/** Cạnh dài tối đa (px) dùng khi nạp từng nhóm asset. Dùng cùng hằng để chia sẻ cache. */
object ArtSizes {
    const val SCREEN = 1280
    const val LOGO = 1280
    const val BUTTON = 1024
    const val BOARD = 1024
    const val PORTRAIT = 512
    const val PIECE = 512
    const val OVERLAY = 384
    const val ICON = 256
}

/** Nạp trước ảnh trong splash; menu được nạp cuối để còn trong cache khi chuyển màn. */
object ArtAssets {
    val mainMenu: List<ArtSpec> = listOf(
        ArtSpec(R.drawable.main_menu_background, ArtSizes.SCREEN),
        ArtSpec(R.drawable.main_menu_header_frame, ArtSizes.BUTTON),
        ArtSpec(R.drawable.main_menu_avatar, ArtSizes.PORTRAIT),
        ArtSpec(R.drawable.main_menu_exp_fill, ArtSizes.BUTTON),
        ArtSpec(R.drawable.main_menu_mode_tower, ArtSizes.BUTTON),
        ArtSpec(R.drawable.main_menu_mode_ai, ArtSizes.BUTTON),
        ArtSpec(R.drawable.main_menu_mode_local, ArtSizes.BUTTON),
        ArtSpec(R.drawable.main_menu_mode_lan, ArtSizes.BUTTON),
        ArtSpec(R.drawable.main_menu_mode_spectate, ArtSizes.BUTTON),
        ArtSpec(R.drawable.main_menu_journal, ArtSizes.ICON),
        ArtSpec(R.drawable.main_menu_inventory, ArtSizes.ICON),
        ArtSpec(R.drawable.main_menu_achievements, ArtSizes.ICON),
        ArtSpec(R.drawable.main_menu_history, ArtSizes.ICON),
        ArtSpec(R.drawable.main_menu_settings, ArtSizes.ICON),
        ArtSpec(R.drawable.panel_currency_counter, ArtSizes.BUTTON),
        ArtSpec(R.drawable.icon_currency_gold, ArtSizes.ICON),
        ArtSpec(R.drawable.icon_currency_crystal, ArtSizes.ICON),
    )

    val preload: List<ArtSpec> = listOf(
        ArtSpec(R.drawable.bg_splash, ArtSizes.SCREEN),
        ArtSpec(R.drawable.splash_loading_frame, ArtSizes.BUTTON),
        ArtSpec(R.drawable.splash_loading_fill, ArtSizes.BUTTON),
        ArtSpec(R.drawable.button_primary_normal, ArtSizes.BUTTON),
        ArtSpec(R.drawable.button_primary_pressed, ArtSizes.BUTTON),
        ArtSpec(R.drawable.button_secondary_normal, ArtSizes.BUTTON),
        ArtSpec(R.drawable.button_small_normal, ArtSizes.BUTTON),
        ArtSpec(R.drawable.board_tile_light, ArtSizes.OVERLAY),
        ArtSpec(R.drawable.board_tile_dark, ArtSizes.OVERLAY),
        ArtSpec(R.drawable.board_tile_selected, ArtSizes.OVERLAY),
        ArtSpec(R.drawable.board_tile_move, ArtSizes.OVERLAY),
        ArtSpec(R.drawable.board_tile_attack, ArtSizes.OVERLAY),
        ArtSpec(R.drawable.board_tile_check, ArtSizes.OVERLAY),
        ArtSpec(R.drawable.board_tile_last_move, ArtSizes.OVERLAY),
        ArtSpec(R.drawable.chess_piece_white_king, ArtSizes.PIECE),
        ArtSpec(R.drawable.chess_piece_white_queen, ArtSizes.PIECE),
        ArtSpec(R.drawable.chess_piece_white_rook, ArtSizes.PIECE),
        ArtSpec(R.drawable.chess_piece_white_bishop, ArtSizes.PIECE),
        ArtSpec(R.drawable.chess_piece_white_knight, ArtSizes.PIECE),
        ArtSpec(R.drawable.chess_piece_white_pawn, ArtSizes.PIECE),
        ArtSpec(R.drawable.chess_piece_black_king, ArtSizes.PIECE),
        ArtSpec(R.drawable.chess_piece_black_queen, ArtSizes.PIECE),
        ArtSpec(R.drawable.chess_piece_black_rook, ArtSizes.PIECE),
        ArtSpec(R.drawable.chess_piece_black_bishop, ArtSizes.PIECE),
        ArtSpec(R.drawable.chess_piece_black_knight, ArtSizes.PIECE),
        ArtSpec(R.drawable.chess_piece_black_pawn, ArtSizes.PIECE),
    ) + mainMenu
}
