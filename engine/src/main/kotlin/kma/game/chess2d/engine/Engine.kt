package kma.game.chess2d.engine

/**
 * Cạnh ngoài của module :engine — nơi :app và :ai gọi vào.
 *
 * Giữ mọi thứ ở đây thật mỏng: để UI phụ thuộc vào một điểm duy nhất thay vì
 * gọi tản mát vào từng lớp bên trong.
 */
object Engine {

    const val BOARD_SIZE = Squares.COUNT

    const val START_FEN = Fen.START

    /** Tạo thế cờ đầu ván. */
    fun newGame(): Board = Board.startPosition()

    fun fromFen(fen: String): Board = Fen.parse(fen)

    fun legalMoves(board: Board): List<Move> = MoveGenerator.legalMoves(board)

    fun status(board: Board): GameStatus = Rules.status(board)

    /** Tìm nước hợp lệ khọp với ô đi và ô đến mà người chơi vừa chọn trên UI.
     *
     * Trả về danh sách vì phong cấp có tới bốn nước cùng ô đi và ô đến — lúc đó UI
     * phải hỏi người chơi muốn phong quân gì.
     */
    fun movesBetween(board: Board, from: Int, to: Int): List<Move> =
        legalMoves(board).filter { it.from == from && it.to == to }
}
