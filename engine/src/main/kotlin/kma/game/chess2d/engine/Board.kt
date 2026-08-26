package kma.game.chess2d.engine

/**
 * Trạng thái một thế cờ, và là nơi mọi nước đi được thực hiện.
 *
 * Bàn cờ là ByteArray(64) duy nhất, sửa trực tiếp bằng makeMove/unmakeMove chứ
 * không bao giờ tạo bản sao (mục 2.3). Mọi thông tin không suy ra được từ bàn cờ
 * sau khi hoàn nguyên một nước — quân bị ăn, quyền nhập thành, ô bắt tốt qua
 * đường, đồng hồ 50 nước — được đóng gói vào một Int trên undo stack.
 *
 * Lớp này KHÔNG tự kiểm tra tính hợp lệ của nước đi. Sinh nước hợp lệ là việc của
 * MoveGenerator; tách như vậy để search không phải trả giá cho kiểm tra trùng lặp.
 */
class Board {

    val squares = ByteArray(Squares.COUNT)

    var whiteToMove = true
    var castlingRights = 0
    var epSquare = Squares.NONE
    var halfmoveClock = 0
    var fullmoveNumber = 1

    /** Vị trí vua được theo dõi sẵn: kiểm tra chiếu là việc làm ở mọi node. */
    private val kingSquares = intArrayOf(Squares.NONE, Squares.NONE)

    private val historyMoves = IntArray(MAX_HISTORY)
    private val historyState = IntArray(MAX_HISTORY)

    /**
     * Khóa Zobrist của từng thế cờ đã đi qua. Khóa không suy ra được từ bàn cờ sau
     * khi hoàn nguyên, nên phải nằm trên undo stack giống các thông tin khác. Đây
     * cũng chính là dữ liệu để đếm lặp ba lần thế.
     */
    private val historyHash = LongArray(MAX_HISTORY)
    private var ply = 0

    /** Khóa Zobrist của thế cờ hiện tại, được cập nhật tăng dần trong makeMove. */
    var hashKey = 0L
        private set

    /** Số nước đang nằm trên undo stack — UI dùng để bật/tắt nút Undo. */
    val movesPlayed: Int get() = ply

    fun canUndo(): Boolean = ply > 0

    /**
     * Tính lại khóa Zobrist từ đầu. Chỉ gọi sau khi dựng thế bằng setPiece — trong
     * lúc đi thì khóa được cập nhật tăng dần, rẻ hơn rất nhiều.
     */
    fun refreshHash() {
        hashKey = Zobrist.compute(this)
    }

    /**
     * Số lần thế cờ hiện tại đã xuất hiện, tính cả lần này.
     *
     * Chỉ quét lùi tới nước không hoàn nguyên được gần nhất (ăn quân hoặc đi tốt):
     * trước mốc đó thế cờ vĩnh viễn không thể trùng lại, nên quét thêm là vô ích.
     * Bước nhảy 2 vì chỉ thế cờ cùng bên đến lượt mới có thể trùng nhau.
     */
    fun repetitionCount(): Int {
        var count = 1
        val earliest = maxOf(0, ply - halfmoveClock)
        var index = ply - 2
        while (index >= earliest) {
            if (historyHash[index] == hashKey) count++
            index -= 2
        }
        return count
    }

    fun clear() {
        squares.fill(Piece.NONE)
        whiteToMove = true
        castlingRights = 0
        epSquare = Squares.NONE
        halfmoveClock = 0
        fullmoveNumber = 1
        kingSquares[0] = Squares.NONE
        kingSquares[1] = Squares.NONE
        ply = 0
        hashKey = 0L
    }

    fun pieceAt(square: Int): Byte = squares[square]

    /** Đặt quân và cập nhật luôn vị trí vua. Chỉ dùng khi dựng thế, không dùng khi đi. */
    fun setPiece(square: Int, piece: Byte) {
        squares[square] = piece
        if (piece != Piece.NONE && Piece.typeOf(piece) == Piece.KING) {
            kingSquares[colorIndex(Piece.isWhite(piece))] = square
        }
    }

    fun kingSquare(white: Boolean): Int = kingSquares[colorIndex(white)]

    fun isInCheck(white: Boolean = whiteToMove): Boolean {
        val king = kingSquare(white)
        return king != Squares.NONE && isSquareAttacked(king, byWhite = !white)
    }

    /**
     * Ô này có bị bên byWhite tấn công không.
     *
     * Cách làm: từ ô cần hỏi, đi ngược theo từng kiểu di chuyển để xem có đúng loại
     * quân địch ở đầu bên kia hay không. Rẻ hơn nhiều so với sinh toàn bộ nước đi
     * của địch rồi dò xem có nước nào tới ô này.
     */
    fun isSquareAttacked(square: Int, byWhite: Boolean): Boolean {
        val pawn = Piece.of(Piece.PAWN, byWhite)
        for (source in Attacks.pawnAttackers(byWhite)[square]) {
            if (squares[source] == pawn) return true
        }

        val knight = Piece.of(Piece.KNIGHT, byWhite)
        for (source in Attacks.knight[square]) {
            if (squares[source] == knight) return true
        }

        val king = Piece.of(Piece.KING, byWhite)
        for (source in Attacks.king[square]) {
            if (squares[source] == king) return true
        }

        // Quân đi xa: trên mỗi tia chỉ cần xét quân đầu tiên gặp, vì quân đó chặn tầm.
        val rook = Piece.of(Piece.ROOK, byWhite)
        val bishop = Piece.of(Piece.BISHOP, byWhite)
        val queen = Piece.of(Piece.QUEEN, byWhite)
        for (direction in 0..Attacks.BISHOP_DIR_LAST) {
            val slider = if (direction <= Attacks.ROOK_DIR_LAST) rook else bishop
            for (target in Attacks.rays[square][direction]) {
                val piece = squares[target]
                if (piece == Piece.NONE) continue
                if (piece == slider || piece == queen) return true
                break
            }
        }
        return false
    }

    /**
     * Thực hiện nước đi. Nhận nước giả hợp lệ — có thể để vua mình bị chiếu, người
     * gọi phải tự kiểm tra bằng isInCheck rồi unmakeMove nếu không hợp lệ.
     */
    fun makeMove(move: Move) {
        val from = move.from
        val to = move.to
        val flag = move.flag
        val movingWhite = whiteToMove
        val mover = squares[from]
        val moverType = Piece.typeOf(mover)

        // Bắt tốt qua đường là trường hợp duy nhất quân bị ăn không nằm ở ô đến.
        val capturedSquare = if (flag == Move.EN_PASSANT) {
            if (movingWhite) to - 8 else to + 8
        } else {
            to
        }
        val captured = squares[capturedSquare]

        historyMoves[ply] = move.raw
        historyState[ply] = packState(captured)
        historyHash[ply] = hashKey
        ply++

        // Giữ lại để cập nhật khóa Zobrist ở cuối hàm: cả hai sắp bị ghi đè.
        val previousCastling = castlingRights
        val previousEpSquare = epSquare

        squares[capturedSquare] = Piece.NONE
        squares[from] = Piece.NONE
        squares[to] = if (move.isPromotion) Piece.of(move.promotionType, movingWhite) else mover

        // Giữ lại ô xe để cập nhật khóa Zobrist: nhập thành dịch chuyển hai quân.
        var rookFrom = Squares.NONE
        var rookTo = Squares.NONE
        if (moverType == Piece.KING) {
            kingSquares[colorIndex(movingWhite)] = to
            // Vua nhậy hai ô, xe nhảy qua vua. Ô xe suy ra được từ ô đến của vua.
            when (flag) {
                Move.CASTLE_KING -> {
                    rookFrom = to + 1
                    rookTo = to - 1
                }
                Move.CASTLE_QUEEN -> {
                    rookFrom = to - 2
                    rookTo = to + 1
                }
            }
            if (rookFrom != Squares.NONE) moveRook(rookFrom, rookTo)
        }

        // Một phép AND xử lý đủ ba trường hợp mất quyền nhập thành:
        // vua rời ô gốc, xe rời ô gốc, và xe bị ăn ngay tại ô gốc.
        castlingRights = castlingRights and CASTLING_MASK[from] and CASTLING_MASK[to]

        epSquare = if (flag == Move.DOUBLE_PAWN_PUSH) {
            if (movingWhite) from + 8 else from - 8
        } else {
            Squares.NONE
        }

        halfmoveClock = if (moverType == Piece.PAWN || captured != Piece.NONE) 0 else halfmoveClock + 1
        if (!movingWhite) fullmoveNumber++
        whiteToMove = !movingWhite

        // Cập nhật khóa Zobrist tăng dần: chỉ XOR đúng những gì vừa đổi. Tính lại từ
        // đầu sẽ tốn 64 ô ở mọi node của search, còn ở đây chỉ vài phép XOR.
        var key = hashKey
        if (captured != Piece.NONE) key = key xor Zobrist.pieceKey(captured, capturedSquare)
        key = key xor Zobrist.pieceKey(mover, from)
        // Đọc quân ở ô đến từ bàn cờ để phong cấp tự đúng, không phải xét lại flag.
        key = key xor Zobrist.pieceKey(squares[to], to)
        if (rookFrom != Squares.NONE) {
            val rook = Piece.of(Piece.ROOK, movingWhite)
            key = key xor Zobrist.pieceKey(rook, rookFrom) xor Zobrist.pieceKey(rook, rookTo)
        }
        key = key xor Zobrist.castling[previousCastling and 0xF] xor
            Zobrist.castling[castlingRights and 0xF]
        if (previousEpSquare != Squares.NONE) {
            key = key xor Zobrist.epFile[Squares.fileOf(previousEpSquare)]
        }
        if (epSquare != Squares.NONE) key = key xor Zobrist.epFile[Squares.fileOf(epSquare)]
        hashKey = key xor Zobrist.sideToMove
    }

    /** Hoàn nguyên nước đi gần nhất. Đây cũng chính là tính năng Undo của UI. */
    fun unmakeMove() {
        require(ply > 0) { "Không còn nước nào để hoàn lại" }
        ply--

        val move = Move(historyMoves[ply])
        val state = historyState[ply]
        // Khóa lấy lại nguyên vẹn từ stack, không cần XOR ngược từng bước.
        hashKey = historyHash[ply]

        whiteToMove = !whiteToMove
        val movingWhite = whiteToMove
        if (!movingWhite) fullmoveNumber--

        val from = move.from
        val to = move.to
        val flag = move.flag

        // Phong cấp thì quân trả về ô cũ là tốt, không phải quân đã được phong.
        squares[from] = if (move.isPromotion) Piece.of(Piece.PAWN, movingWhite) else squares[to]
        squares[to] = Piece.NONE

        val captured = unpackCaptured(state)
        if (captured != Piece.NONE) {
            val capturedSquare = if (flag == Move.EN_PASSANT) {
                if (movingWhite) to - 8 else to + 8
            } else {
                to
            }
            squares[capturedSquare] = captured
        }

        if (Piece.typeOf(squares[from]) == Piece.KING) {
            kingSquares[colorIndex(movingWhite)] = from
            when (flag) {
                Move.CASTLE_KING -> moveRook(to - 1, to + 1)
                Move.CASTLE_QUEEN -> moveRook(to + 1, to - 2)
            }
        }

        castlingRights = unpackCastling(state)
        epSquare = unpackEpSquare(state)
        halfmoveClock = unpackHalfmoveClock(state)
    }

    fun toFen(): String = Fen.format(this)

    override fun toString(): String = toFen()

    private fun moveRook(from: Int, to: Int) {
        squares[to] = squares[from]
        squares[from] = Piece.NONE
    }

    private fun colorIndex(white: Boolean): Int = if (white) 0 else 1

    // Đóng gói trạng thái không thể suy ra từ bàn cờ vào một Int:
    // bit 0-3 quân bị ăn · 4-7 quyền nhập thành · 8-14 ô bắt tốt qua đường · 15-24 đồng hồ 50 nước.
    private fun packState(captured: Byte): Int =
        (captured.toInt() and 0xF) or
            ((castlingRights and 0xF) shl 4) or
            (((epSquare + 1) and 0x7F) shl 8) or
            ((halfmoveClock and 0x3FF) shl 15)

    private fun unpackCaptured(state: Int): Byte = (state and 0xF).toByte()

    private fun unpackCastling(state: Int): Int = (state ushr 4) and 0xF

    private fun unpackEpSquare(state: Int): Int = ((state ushr 8) and 0x7F) - 1

    private fun unpackHalfmoveClock(state: Int): Int = (state ushr 15) and 0x3FF

    companion object {

        const val MAX_HISTORY = 1024

        const val WHITE_KING_SIDE = 1
        const val WHITE_QUEEN_SIDE = 2
        const val BLACK_KING_SIDE = 4
        const val BLACK_QUEEN_SIDE = 8
        const val ALL_CASTLING = 15

        /**
         * Mặt nạ quyền nhập thành theo ô. Mặc định giữ nguyên mọi quyền; riêng sáu ô
         * gốc của vua và xe thì xóa quyền tương ứng.
         */
        private val CASTLING_MASK = IntArray(Squares.COUNT) { ALL_CASTLING }.also { mask ->
            mask[Squares.E1] = ALL_CASTLING and (WHITE_KING_SIDE or WHITE_QUEEN_SIDE).inv()
            mask[Squares.H1] = ALL_CASTLING and WHITE_KING_SIDE.inv()
            mask[Squares.A1] = ALL_CASTLING and WHITE_QUEEN_SIDE.inv()
            mask[Squares.E8] = ALL_CASTLING and (BLACK_KING_SIDE or BLACK_QUEEN_SIDE).inv()
            mask[Squares.H8] = ALL_CASTLING and BLACK_KING_SIDE.inv()
            mask[Squares.A8] = ALL_CASTLING and BLACK_QUEEN_SIDE.inv()
        }

        fun startPosition(): Board = Fen.parse(Fen.START)
    }
}
