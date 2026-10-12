package kma.game.chess2d.ui.art

import kma.game.chess2d.R

/** Cạnh dài tối đa (px) dùng khi nạp từng nhóm asset. Mọi nơi dùng ảnh phải dùng đúng hằng này để chia sẻ cache. */
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

/** Danh sách ảnh nạp trước ở splash: nền và trang trí sảnh, nút, bàn cờ và quân cờ. */
object ArtAssets {
    val preload: List<ArtSpec> = listOf(
        ArtSpec(R.drawable.bg_splash, ArtSizes.SCREEN),
        ArtSpec(R.drawable.logo_chess, ArtSizes.LOGO),
        ArtSpec(R.drawable.bg_main_menu, ArtSizes.SCREEN),
        ArtSpec(R.drawable.character_protagonist_fullbody, ArtSizes.BOARD),
        ArtSpec(R.drawable.decor_main_menu_glow, ArtSizes.PIECE),
        ArtSpec(R.drawable.portrait_protagonist, ArtSizes.PORTRAIT),
        ArtSpec(R.drawable.frame_player_avatar, ArtSizes.PIECE),
        ArtSpec(R.drawable.bar_player_xp_empty, ArtSizes.BUTTON),
        ArtSpec(R.drawable.bar_player_xp_fill, ArtSizes.BUTTON),
        ArtSpec(R.drawable.panel_currency_counter, ArtSizes.BUTTON),
        ArtSpec(R.drawable.icon_currency_gold, ArtSizes.ICON),
        ArtSpec(R.drawable.icon_currency_crystal, ArtSizes.ICON),
        ArtSpec(R.drawable.button_currency_add, ArtSizes.ICON),
        ArtSpec(R.drawable.frame_side_button, ArtSizes.ICON),
        ArtSpec(R.drawable.icon_settings_gear, ArtSizes.ICON),
        ArtSpec(R.drawable.icon_journal, ArtSizes.ICON),
        ArtSpec(R.drawable.icon_inventory, ArtSizes.ICON),
        ArtSpec(R.drawable.icon_achievements, ArtSizes.ICON),
        ArtSpec(R.drawable.panel_mode_card_red, ArtSizes.BUTTON),
        ArtSpec(R.drawable.panel_mode_card_blue, ArtSizes.BUTTON),
        ArtSpec(R.drawable.panel_mode_card_small, ArtSizes.BUTTON),
        ArtSpec(R.drawable.decor_mode_card_corners, ArtSizes.BUTTON),
        ArtSpec(R.drawable.icon_story_mode, ArtSizes.ICON),
        ArtSpec(R.drawable.icon_ai_battle, ArtSizes.ICON),
        ArtSpec(R.drawable.icon_local_multiplayer, ArtSizes.ICON),
        ArtSpec(R.drawable.icon_lan_multiplayer, ArtSizes.ICON),
        ArtSpec(R.drawable.icon_ai_vs_ai, ArtSizes.ICON),
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
    )
}
