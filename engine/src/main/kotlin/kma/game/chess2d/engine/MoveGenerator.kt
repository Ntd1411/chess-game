package kma.game.chess2d.engine

/**
 * Sinh nước đi.
 *
 * Chia hai tầng có chủ ý:
 * - pseudoLegal: đúng luật di chuyển từng loại quân, nhưng có thể để vua mình bị chiếu.
 * - legal: thực hiện thử từng nước rồi loại những nước làm vua mình bị chiếu.
 *
 * Cách lọc bằng make/unmake đắt hơn cách dò quân bị ghìm, nhưng đúng trong mọi
 * trường hợp biên — kể cả cái bắt tốt qua đường làm hở chiếu ngang, lỗi mà hầu hết
 * engine tự viết đều mắc. Đủ nhanh cho depth 6; tối ưu sau khi perft đã xanh.
 */
object MoveGenerator {

    /** Sinh nước hợp lệ vào out. buffer là bộ đệm tạm do người gọi cấp để không cấp phát mới. */
    fun legal(board: Board, out: MoveList, buffer: MoveList) {
        pseudoLegal(board, buffer)
        out.clear()
        val mover = board.whiteToMove
        for (index in 0 until buffer.size) {
            val move = buffer[index]
            board.makeMove(move)
            if (!board.isInCheck(mover)) out.add(move)
            board.unmakeMove()
        }
    }

    /** Phiên bản tiện dụng cho UI và test, có cấp phát. Không dùng trong search. */
    fun legalMoves(board: Board): List<Move> {
        val out = MoveList()
        legal(board, out, MoveList())
        return out.toList()
    }

    fun pseudoLegal(board: Board, out: MoveList) {
        out.clear()
        val white = board.whiteToMove
        for (square in 0 until Squares.COUNT) {
            val piece = board.squares[square]
            if (piece == Piece.NONE || !Piece.isColor(piece, white)) continue
            when (Piece.typeOf(piece)) {
                Piece.PAWN -> pawnMoves(board, square, white, out)
                Piece.KNIGHT -> jumpMoves(board, square, white, Attacks.knight[square], out)
                Piece.BISHOP -> slideMoves(
                    board, square, white, Attacks.BISHOP_DIR_FIRST, Attacks.BISHOP_DIR_LAST, out,
                )
                Piece.ROOK -> slideMoves(
                    board, square, white, Attacks.ROOK_DIR_FIRST, Attacks.ROOK_DIR_LAST, out,
                )
                Piece.QUEEN -> slideMoves(
                    board, square, white, Attacks.ROOK_DIR_FIRST, Attacks.BISHOP_DIR_LAST, out,
                )
                Piece.KING -> {
                    jumpMoves(board, square, white, Attacks.king[square], out)
                    castlingMoves(board, white, out)
                }
            }
        }
    }

    private fun pawnMoves(board: Board, from: Int, white: Boolean, out: MoveList) {
        val forward = if (white) 8 else -8
        val startRank = if (white) 1 else 6
        val promotionRank = if (white) 7 else 0

        val oneStep = from + forward
        if (board.squares[oneStep] == Piece.NONE) {
            if (Squares.rankOf(oneStep) == promotionRank) {
                addPromotions(from, oneStep, capture = false, out = out)
            } else {
                out.add(Move.of(from, oneStep, Move.QUIET))
                // Đi hai ô chỉ từ hàng gốc, và cả hai ô đều phải trống.
                if (Squares.rankOf(from) == startRank) {
                    val twoSteps = oneStep + forward
                    if (board.squares[twoSteps] == Piece.NONE) {
                        out.add(Move.of(from, twoSteps, Move.DOUBLE_PAWN_PUSH))
                    }
                }
            }
        }

        for (target in Attacks.pawnAttacks(white)[from]) {
            val victim = board.squares[target]
            if (victim != Piece.NONE) {
                if (!Piece.isColor(victim, white)) {
                    if (Squares.rankOf(target) == promotionRank) {
                        addPromotions(from, target, capture = true, out = out)
                    } else {
                        out.add(Move.of(from, target, Move.CAPTURE))
                    }
                }
            } else if (target == board.epSquare) {
                out.add(Move.of(from, target, Move.EN_PASSANT))
            }
        }
    }

    /** Phong cấp luôn sinh đủ bốn lựa chọn: có thế phong Mã mới thắng, phong Hậu lại hoà. */
    private fun addPromotions(from: Int, to: Int, capture: Boolean, out: MoveList) {
        val flags = if (capture) Move.PROMOTION_CAPTURE_FLAGS else Move.PROMOTION_FLAGS
        for (flag in flags) out.add(Move.of(from, to, flag))
    }

    private fun jumpMoves(board: Board, from: Int, white: Boolean, targets: IntArray, out: MoveList) {
        for (target in targets) {
            val victim = board.squares[target]
            when {
                victim == Piece.NONE -> out.add(Move.of(from, target, Move.QUIET))
                !Piece.isColor(victim, white) -> out.add(Move.of(from, target, Move.CAPTURE))
            }
        }
    }

    private fun slideMoves(
        board: Board,
        from: Int,
        white: Boolean,
        firstDirection: Int,
        lastDirection: Int,
        out: MoveList,
    ) {
        for (direction in firstDirection..lastDirection) {
            for (target in Attacks.rays[from][direction]) {
                val victim = board.squares[target]
                if (victim == Piece.NONE) {
                    out.add(Move.of(from, target, Move.QUIET))
                    continue
                }
                if (!Piece.isColor(victim, white)) out.add(Move.of(from, target, Move.CAPTURE))
                break
            }
        }
    }

    /**
     * Nhập thành. Quyền nhập thành đã bảo đảm vua và xe chưa từng rời ô gốc, nên chỉ
     * còn phải kiểm tra hai điều kiện động: đường đi trống, và vua không đi qua ô bị
     * tấn công. Ô đích để tầng legal kiểm tra, nên không xét lại ở đây.
     *
     * Lưu ý ô b1/b8 phải trống nhưng ĐƯỢC phép bị tấn công — vua không đi qua ô đó.
     */
    private fun castlingMoves(board: Board, white: Boolean, out: MoveList) {
        val rights = board.castlingRights
        if (white) {
            if (rights and Board.WHITE_KING_SIDE != 0 &&
                isPathClear(board, WHITE_KING_SIDE_EMPTY) &&
                isPathSafe(board, WHITE_KING_SIDE_SAFE, white)
            ) {
                out.add(Move.of(Squares.E1, Squares.G1, Move.CASTLE_KING))
            }
            if (rights and Board.WHITE_QUEEN_SIDE != 0 &&
                isPathClear(board, WHITE_QUEEN_SIDE_EMPTY) &&
                isPathSafe(board, WHITE_QUEEN_SIDE_SAFE, white)
            ) {
                out.add(Move.of(Squares.E1, Squares.C1, Move.CASTLE_QUEEN))
            }
        } else {
            if (rights and Board.BLACK_KING_SIDE != 0 &&
                isPathClear(board, BLACK_KING_SIDE_EMPTY) &&
                isPathSafe(board, BLACK_KING_SIDE_SAFE, white)
            ) {
                out.add(Move.of(Squares.E8, Squares.G8, Move.CASTLE_KING))
            }
            if (rights and Board.BLACK_QUEEN_SIDE != 0 &&
                isPathClear(board, BLACK_QUEEN_SIDE_EMPTY) &&
                isPathSafe(board, BLACK_QUEEN_SIDE_SAFE, white)
            ) {
                out.add(Move.of(Squares.E8, Squares.C8, Move.CASTLE_QUEEN))
            }
        }
    }

    private fun isPathClear(board: Board, squares: IntArray): Boolean {
        for (square in squares) if (board.squares[square] != Piece.NONE) return false
        return true
    }

    private fun isPathSafe(board: Board, squares: IntArray, white: Boolean): Boolean {
        for (square in squares) if (board.isSquareAttacked(square, byWhite = !white)) return false
        return true
    }

    // f1, g1 · vua đi qua e1, f1, g1
    private val WHITE_KING_SIDE_EMPTY = intArrayOf(5, 6)
    private val WHITE_KING_SIDE_SAFE = intArrayOf(4, 5, 6)

    // b1, c1, d1 · vua đi qua e1, d1, c1
    private val WHITE_QUEEN_SIDE_EMPTY = intArrayOf(1, 2, 3)
    private val WHITE_QUEEN_SIDE_SAFE = intArrayOf(4, 3, 2)

    private val BLACK_KING_SIDE_EMPTY = intArrayOf(61, 62)
    private val BLACK_KING_SIDE_SAFE = intArrayOf(60, 61, 62)

    private val BLACK_QUEEN_SIDE_EMPTY = intArrayOf(57, 58, 59)
    private val BLACK_QUEEN_SIDE_SAFE = intArrayOf(60, 59, 58)
}
