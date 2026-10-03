package kma.game.chess2d.ui.art

import kma.game.chess2d.R

/** Cạnh dài tối đa (px) dùng khi nạp từng nhóm asset. Mọi nơi dùng ảnh phải dùng đúng hằng này để chia sẻ cache. */
object ArtSizes {
    const val SCREEN = 1280
    const val LOGO = 1280
    const val BUTTON = 1024
    const val BOARD = 1024
    const val PIECE = 256
    const val OVERLAY = 256
    const val ICON = 128
}

/** Danh sách ảnh nạp trước ở splash: nền, logo, nút menu, bàn cờ và quân cờ. */
object ArtAssets {
    val preload: List<ArtSpec> = listOf(
        ArtSpec(R.drawable.screen_splash_background, ArtSizes.SCREEN),
        ArtSpec(R.drawable.ui_logo_tower_of_chess, ArtSizes.LOGO),
        ArtSpec(R.drawable.screen_main_menu, ArtSizes.SCREEN),
        ArtSpec(R.drawable.char_protagonist_full_body, ArtSizes.BOARD),
        ArtSpec(R.drawable.ui_avatar_frame, ArtSizes.PIECE),
        ArtSpec(R.drawable.icon_settings, ArtSizes.ICON),
        ArtSpec(R.drawable.ui_menu_button_primary_wide, ArtSizes.BUTTON),
        ArtSpec(R.drawable.ui_menu_button_primary_wide_pressed, ArtSizes.BUTTON),
        ArtSpec(R.drawable.ui_menu_button_secondary_wide, ArtSizes.BUTTON),
        ArtSpec(R.drawable.tile_marble_light, ArtSizes.OVERLAY),
        ArtSpec(R.drawable.tile_marble_dark, ArtSizes.OVERLAY),
        ArtSpec(R.drawable.tile_selected_overlay, ArtSizes.OVERLAY),
        ArtSpec(R.drawable.tile_move_overlay, ArtSizes.OVERLAY),
        ArtSpec(R.drawable.tile_attack_overlay, ArtSizes.OVERLAY),
        ArtSpec(R.drawable.tile_check_overlay, ArtSizes.OVERLAY),
        ArtSpec(R.drawable.piece_white_king, ArtSizes.PIECE),
        ArtSpec(R.drawable.piece_white_queen, ArtSizes.PIECE),
        ArtSpec(R.drawable.piece_white_rook, ArtSizes.PIECE),
        ArtSpec(R.drawable.piece_white_bishop, ArtSizes.PIECE),
        ArtSpec(R.drawable.piece_white_knight, ArtSizes.PIECE),
        ArtSpec(R.drawable.piece_white_pawn, ArtSizes.PIECE),
        ArtSpec(R.drawable.piece_black_king_bright, ArtSizes.PIECE),
        ArtSpec(R.drawable.piece_black_queen_bright, ArtSizes.PIECE),
        ArtSpec(R.drawable.piece_black_rook_bright, ArtSizes.PIECE),
        ArtSpec(R.drawable.piece_black_bishop_bright, ArtSizes.PIECE),
        ArtSpec(R.drawable.piece_black_knight_bright, ArtSizes.PIECE),
        ArtSpec(R.drawable.piece_black_pawn_bright, ArtSizes.PIECE),
    )
}
