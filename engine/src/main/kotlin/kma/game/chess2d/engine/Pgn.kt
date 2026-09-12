package kma.game.chess2d.engine

/**
 * Xuất và nhập PGN (Portable Game Notation) — mục 7.3.
 *
 * Đặt trong `:engine` vì PGN viết bằng SAN, mà SAN chỉ sinh đúng khi biết danh sách
 * nước hợp lệ của từng thế cờ. Nhờ nằm cùng chỗ với engine, mọi nước nhận vào đều
 * đi qua bộ sinh nước để duyệt, và toàn bộ việc này test được bằng JVM thuần.
 *
 * Chủ ý thiết kế: **không tin một dòng nào trong file nhận vào.** Một nước chỉ được
 * chấp nhận khi engine sinh ra đúng ký hiệu đó ở thế cờ hiện tại; sai cú pháp hay sai
 * luật đều báo lỗi kèm số nước để người dùng biết phải sửa ở đâu.
 */
object Pgn {

    /** Bảy tag bắt buộc theo chuẩn PGN, đúng thứ tự phải ghi ra file. */
    val REQUIRED_TAGS = listOf("Event", "Site", "Date", "Round", "White", "Black", "Result")

    /** Kết quả "chưa xong", dùng khi xuất một ván đang chơi dở. */
    const val RESULT_UNKNOWN = "*"

    /** Độ dài tối đa một dòng movetext, theo thói quen của định dạng PGN. */
    private const val LINE_WIDTH = 80

    /**
     * Một ván đầy đủ dưới dạng dựng lại được.
     *
     * Lưu nước đi ở dạng UCI chứ không lưu SAN: UCI không phụ thuộc thế cờ nên dựng
     * lại bàn cờ chỉ cần đọc tuần tự, còn SAN luôn sinh lại được từ UCI khi cần.
     */
    data class Game(
        val event: String = "Chess 2D",
        val site: String = "LAN",
        /** Định dạng PGN là `YYYY.MM.DD`, phần không biết thì ghi dấu `?`. */
        val date: String = "????.??.??",
        val round: String = "-",
        val white: String = "White",
        val black: String = "Black",
        val result: String = RESULT_UNKNOWN,
        val startFen: String = Fen.START,
        val uciMoves: List<String> = emptyList(),
    )

    /**
     * Lỗi cú pháp hoặc lỗi luật khi đọc PGN.
     *
     * @param moveNumber số nước đi (tính theo nước đôi như trong PGN) nơi phát hiện lỗi,
     *        `null` khi lỗi nằm ở phần tag chứ không ở phần nước đi.
     */
    class PgnException(
        message: String,
        val moveNumber: Int? = null,
        val token: String = "",
    ) : IllegalArgumentException(message)

    // ------------------------------------------------------------------- xuất

    /**
     * Viết một ván thành PGN.
     *
     * Ván bắt đầu từ thế khác thế đầu chuẩn thì phải ghi thêm cặp tag `SetUp`/`FEN`,
     * không thì bên đọc sẽ dựng từ thế đầu và mọi nước sau đó đều sai luật.
     */
    fun export(game: Game): String {
        val builder = StringBuilder()
        val values = listOf(
            game.event,
            game.site,
            game.date,
            game.round,
            game.white,
            game.black,
            game.result,
        )
        for ((name, value) in REQUIRED_TAGS.zip(values)) {
            builder.append('[').append(name).append(" \"").append(escape(value)).append("\"]\n")
        }
        if (game.startFen != Fen.START) {
            builder.append("[SetUp \"1\"]\n")
            builder.append("[FEN \"").append(escape(game.startFen)).append("\"]\n")
        }
        builder.append('\n')
        builder.append(movetextOf(game))
        builder.append('\n')
        return builder.toString()
    }

    /**
     * Phần nước đi: `1. e4 e5 2. Nf3 ...` rồi đến kết quả.
     *
     * SAN được sinh lại bằng cách đi lại cả ván trên một bàn cờ sạch: ký hiệu SAN phụ
     * thuộc thế cờ tại đúng lúc đi nước đó, không suy ra được từ riêng chuỗi UCI.
     */
    private fun movetextOf(game: Game): String {
        val board = Fen.parse(game.startFen)
        val tokens = mutableListOf<String>()
        var moveNumber = board.fullmoveNumber
        var whiteToMove = board.whiteToMove

        for ((index, uci) in game.uciMoves.withIndex()) {
            val legal = Engine.legalMoves(board)
            val move = legal.firstOrNull { it.toUci() == uci }
                ?: throw PgnException(
                    "illegal move at ply ${index + 1}: $uci",
                    moveNumber,
                    uci,
                )
            if (whiteToMove) tokens.add("$moveNumber.")
            // Đen đi nước đầu tiên (ván bắt đầu từ thế giữa ván) thì phải ghi `12...`,
            // không thì người đọc tưởng đó là nước của Trắng.
            else if (index == 0) tokens.add("$moveNumber...")
            tokens.add(San.of(board, move, legal))
            board.makeMove(move)
            if (!whiteToMove) moveNumber += 1
            whiteToMove = !whiteToMove
        }
        tokens.add(game.result)
        return wrap(tokens)
    }

    /** Gấp dòng theo từng token, không bao giờ cắt giữa một ký hiệu SAN. */
    private fun wrap(tokens: List<String>): String {
        val builder = StringBuilder()
        var lineLength = 0
        for (token in tokens) {
            if (lineLength == 0) {
                builder.append(token)
                lineLength = token.length
            } else if (lineLength + 1 + token.length > LINE_WIDTH) {
                builder.append('\n').append(token)
                lineLength = token.length
            } else {
                builder.append(' ').append(token)
                lineLength += 1 + token.length
            }
        }
        return builder.toString()
    }

    private fun escape(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"")

    // ------------------------------------------------------------------ nhập

    /**
     * Đọc một ván từ PGN.
     *
     * Chấp nhận những thứ file thật hay có: chú thích `{...}` và `;`, biến `(...)`, NAG
     * `$1`, và dấu `!?` sau nước đi. Những thứ đó bị bỏ qua chứ không gây lỗi, vì chúng
     * không ảnh hưởng tới thế cờ.
     *
     * @throws PgnException khi thiếu tag bắt buộc, khi FEN không đọc được, hoặc khi một
     *         ký hiệu nước đi không phải nước hợp lệ ở thế cờ đó.
     */
    fun parse(text: String): Game {
        val tags = parseTags(text)
        val missing = REQUIRED_TAGS.filter { it !in tags }
        if (missing.isNotEmpty()) {
            throw PgnException("missing required tag(s): ${missing.joinToString(", ")}")
        }

        val startFen = tags["FEN"] ?: Fen.START
        val board = runCatching { Fen.parse(startFen) }.getOrElse {
            throw PgnException("bad FEN tag: $startFen")
        }

        val movetext = stripTags(text)
        val uciMoves = parseMoves(movetext, board)
        return Game(
            event = tags.getValue("Event"),
            site = tags.getValue("Site"),
            date = tags.getValue("Date"),
            round = tags.getValue("Round"),
            white = tags.getValue("White"),
            black = tags.getValue("Black"),
            result = tags.getValue("Result"),
            startFen = startFen,
            uciMoves = uciMoves,
        )
    }

    private val TAG_PATTERN = Regex("""\[\s*(\w+)\s*"((?:[^"\\]|\\.)*)"\s*]""")

    private fun parseTags(text: String): Map<String, String> =
        TAG_PATTERN.findAll(text).associate { match ->
            match.groupValues[1] to unescape(match.groupValues[2])
        }

    private fun unescape(value: String): String =
        value.replace("\\\"", "\"").replace("\\\\", "\\")

    private fun stripTags(text: String): String = TAG_PATTERN.replace(text, " ")

    /**
     * Đọc từng ký hiệu nước đi và duyệt bằng engine.
     *
     * Một token được coi là hợp lệ khi trùng với SAN của đúng một nước trong danh sách
     * nước hợp lệ ở thế hiện tại. Cách này đọc được cả những file viết thiếu hoặc
     * viết thừa dấu `+`/`#`, mà vẫn không cho lọt một nước sai luật nào.
     */
    private fun parseMoves(movetext: String, board: Board): List<String> {
        val cleaned = removeAnnotations(movetext)
        val uciMoves = mutableListOf<String>()

        for (rawToken in cleaned.split(Regex("\\s+"))) {
            val token = rawToken.trim()
            if (token.isEmpty()) continue
            if (token in RESULT_TOKENS) break
            // Số nước (`12.` hay `12...`) chỉ để người đọc; thứ tự thật nằm ở chính bàn cờ.
            if (token.all { it.isDigit() || it == '.' }) continue

            val san = normalize(token)
            if (san.isEmpty()) continue
            val legal = Engine.legalMoves(board)
            val matches = legal.filter { normalize(San.of(board, it, legal)) == san }
            if (matches.size != 1) {
                throw PgnException(
                    if (matches.isEmpty()) {
                        "illegal or unreadable move \"$token\""
                    } else {
                        "ambiguous move \"$token\""
                    },
                    board.fullmoveNumber,
                    token,
                )
            }
            val move = matches.first()
            uciMoves.add(move.toUci())
            board.makeMove(move)
        }
        return uciMoves
    }

    private val RESULT_TOKENS = setOf("1-0", "0-1", "1/2-1/2", RESULT_UNKNOWN)

    /**
     * Bỏ chú thích, biến, và NAG.
     *
     * Biến có thể lồng nhau nên phải đếm độ sâu ngoặc; chú thích `{...}` thì không
     * lồng được theo chuẩn.
     */
    private fun removeAnnotations(movetext: String): String {
        val builder = StringBuilder()
        var depth = 0
        var inComment = false
        var inLineComment = false

        for (character in movetext) {
            when {
                inLineComment -> if (character == '\n') inLineComment = false
                inComment -> if (character == '}') inComment = false
                character == '{' -> inComment = true
                character == ';' -> inLineComment = true
                character == '(' -> depth += 1
                character == ')' -> if (depth > 0) depth -= 1
                depth > 0 -> Unit
                else -> builder.append(character)
            }
        }
        // NAG (`$1`) không nói gì về thế cờ, bỏ cả cụm.
        return Regex("""\$\d+""").replace(builder.toString(), " ")
    }

    /**
     * Đưa một ký hiệu SAN về dạng so sánh được.
     *
     * Bỏ `+`, `#`, `!`, `?`, dấu `x` và `-` của kiểu viết đầy đủ, đổi `0-0` thành `OO`,
     * và bỏ `=` trước quân phong cấp — những kiểu viết này đều gặp ngoài thực tế và
     * đều chỉ một nước đi duy nhất.
     */
    private fun normalize(token: String): String {
        val builder = StringBuilder()
        for (character in token) {
            when (character) {
                '+', '#', '!', '?', 'x', '=', '-' -> Unit
                '0' -> builder.append('O')
                else -> builder.append(character)
            }
        }
        return builder.toString()
    }
}
