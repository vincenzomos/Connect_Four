package connectfour

val EDGE = '\u2551'
val LEFT_CORNER = '\u255A'
val RIGHT_CORNER = '\u255D'
val BOTTOM = '\u2550'
val BOTTOM_WITH_STUT = '\u2569'
val SYMBOL_PLAYER_ONE = 'o'
val SYMBOL_PLAYER_TWO = '*'


fun main() {
    var builder = ConnectFourGame.Builder()
    builder = builder.setupPlayBoard()
    val game = builder.build()


    for (i in 1..game.numberOfGames) {
        if (game.numberOfGames > 1) {
            println("Game #$i")
        }
        game.drawWithDiscs()
        playRound(game)
        printScores(game)
    }
    println("Game over!")
}

private fun playRound(board: ConnectFourGame) {
    while (board.ended == false) {
        println(board.currentPlayer.name + "'s turn: ")
        val instruction = readln()
        val status = board.processMove(instruction)
//        board.drawCheckLinesWithDiscs()
        when(status) {
            Status.COLUMN_FULL -> println("Column $instruction is full")
            Status.COLUMN_OUT_OF_RANGE -> println("The column number is out of range (1 - ${board.columns})")
            Status.INSTRUCTION_ILLEGAL -> println("Incorrect column number")
            Status.GAME_CONTINUE -> { board.togglePlayer()
                board.drawWithDiscs() }
            Status.END_STATE -> {
                endGame(board)
            }
            Status.WIN_PLAYER1, Status.WIN_PLAYER2 -> {
                board.drawWithDiscs()
                println("Player ${board.currentPlayer.name} won")
                endGame(board)
            }
            Status.TIE -> {
                board.drawWithDiscs()
                println("It is a draw")
                endGame(board)
            }
        }
    }
}

private fun endGame(board: ConnectFourGame) {
    board.ended = true
}

private fun printScores(board: ConnectFourGame) {
    if (board.numberOfGames > 1) {
        when (board.lastEndStatus) {
            Status.WIN_PLAYER1 -> board.player1.addWinToScore()
            Status.WIN_PLAYER2 -> board.player2.addWinToScore()
            Status.TIE -> {
                board.player1.addDrawToScore()
                board.player2.addDrawToScore()
            }
        }
        board.printScore()
        board.resetGame()
    }
}

fun receiveAndValidateDimensionInput(): Pair<Int, Int> {
    val regex = Regex("\\s*(\\d+)\\s*[xX]\\s*(\\d+)\\s*")
    var result: Pair<Int, Int>? = null

    var validDimension = false
    var reshowDimensionQuestions = false
    while (!validDimension) {
        if (reshowDimensionQuestions) {
            println("Set the board dimensions (Rows x Columns)")
            println("Press Enter for default (6 x 7)")
            reshowDimensionQuestions = false
        }
        val dimension = readln()
        if (dimension == "") return Pair(6, 7)
        if (!regex.matches(dimension)) {
            println("Invalid input")
            reshowDimensionQuestions = true
            continue
        }

        val (rawRows, rawColumns) = regex.find(dimension)!!.destructured
        val rowsInt = rawRows.trim().toIntOrNull()
        val columnsInt = rawColumns.trim().toIntOrNull();

        if (rowsInt == null || rowsInt !in 5..9) {
            println("Board rows should be from 5 to 9")
            reshowDimensionQuestions = true
            continue
        }
        if (columnsInt == null || columnsInt !in 5..9) {
            println("Board columns should be from 5 to 9")
            reshowDimensionQuestions = true
            continue
        }
        validDimension = true
        result = Pair(rowsInt!!, columnsInt!!)
    }
    return result!!
}

class Player(val name: String, val symbol: Char) {

    val playerOne: Boolean
    var score = 0

    init {
        if (symbol.equals(SYMBOL_PLAYER_ONE)) this.playerOne = true else this.playerOne = false
    }
    fun addWinToScore() {
        score += 2
    }
    fun addDrawToScore() {
        score += 1
    }
}

data class Pos(val row: Int, val column: Int)
enum class PosType {
    LAST_POS, HORIZONTAL_CHECK, VERTICAL_CHECK, FORWARD_DIAGONAL, BACKWARD_DIAGONAL
}
class ConnectFourGame private constructor(builder: ConnectFourGame.Builder) {

    val SPACE = ' '
    var player1 = Player("", SYMBOL_PLAYER_ONE)
    var player2 = Player("", SYMBOL_PLAYER_ONE)
    val rows: Int
    val columns: Int
    val numberOfGames: Int
    var playerDiscs = emptyList<MutableList<Char>>()
    var currentPlayer = Player("", SYMBOL_PLAYER_ONE)
    var ended = false
    var lastCol = -1
    var lastRow = -1
    var turns = 0
    val MAX_TURNS: Int
    val debug = false
    val debugHelper: DebugHelper
    var lastEndStatus = Status.GAME_CONTINUE

    init {
        this.rows = builder.rows
        this.columns = builder.columns
        this.player1 = builder.player1!!
        this.player2 = builder.player2!!
        this.playerDiscs = List(builder.rows) { MutableList<Char>(builder.columns) { ' ' } }
        this.currentPlayer = builder.player1!!
        this.MAX_TURNS = builder.rows * builder.columns
        this.debugHelper = DebugHelper(this.rows, this.columns)
        this.numberOfGames = builder.numberOfGames
    }

    class Builder {
        var player1: Player? = null
        var player2: Player? = null
        var rows = 6
        var columns = 7
        var numberOfGames = 1

        fun setupPlayBoard(): Builder {
            println("Connect Four")
            println("First player's name:")
            this.player1 = Player(readln(), SYMBOL_PLAYER_ONE)
            println("Second player's name:")
            this.player2 = Player(readln(), SYMBOL_PLAYER_TWO)
            println("Set the board dimensions (Rows x Columns)")
            println("Press Enter for default (6 x 7)")

            val dimensions: Pair<Int, Int> = receiveAndValidateDimensionInput()
            this.numberOfGames = iterativeProcessForgettingNumberOfGames()

            println("${this.player1!!.name} VS ${this.player2!!.name}")
            println("${dimensions.first} X ${dimensions.second} board")
            if (this.numberOfGames == 1) {
                println("Single game")
            } else {
                println("Total $numberOfGames games")
            }
            rows = dimensions.first
            columns = dimensions.second
            return this
        }

        private fun iterativeProcessForgettingNumberOfGames(): Int {
            while(true) {
                println("Do you want to play single or multiple games?")
                println("For a single game, input 1 or press Enter")
                println("Input a number of games:")
                val initialValue = readln()
                if (initialValue == null || initialValue == "") {
                   return 1
                } else {
                    val positiveInt = initialValue.toIntOrNull()
                    if (positiveInt != null && positiveInt > 0) return positiveInt
                }
                println("Invalid input")
            }
        }

        fun build() = ConnectFourGame(this)
    }

    fun processMove(instruction: String): Status {

        if (debug) {
            debugHelper.resetMap()
        }

        var status: Status
        if (isNumeric(instruction)) {
            val columnIns = instruction.toInt()
            if (columnIns in 1..columns) {
                status = this.addDiscToColumn(columnIns)
            } else {
                status =  Status.COLUMN_OUT_OF_RANGE
            }
        } else {
            if (!instruction.isNullOrEmpty() && instruction.equals("End", ignoreCase = true)) {
                return Status.END_STATE
            }
            status = Status.INSTRUCTION_ILLEGAL
        }
        if (debug) println("lastPosition [${this.lastRow}][${this.lastCol}]")
        if (status != Status.GAME_CONTINUE) {
            return status
        }
        status = checkWinner()
        if ((status != Status.WIN_PLAYER1 || status != Status.WIN_PLAYER2) && ++turns == MAX_TURNS) {
            this.lastEndStatus = Status.TIE
            return Status.TIE
        }

        return status
    }

    private fun checkWinner() :Status {
        if (this.lastCol == -1) {
            return Status.INSTRUCTION_ILLEGAL
        }

        val streak = currentPlayer.symbol.toString().repeat(4)
       if (horizontalStreak(streak) ||
           verticalStreak(streak) ||
           forwardDiagonal(streak) ||
           backwardDiagonal(streak)) {
           val status = if (currentPlayer.playerOne) {
               lastEndStatus = Status.WIN_PLAYER1
               Status.WIN_PLAYER1
           } else {
               lastEndStatus = Status.WIN_PLAYER2
               Status.WIN_PLAYER2
           }
           return status
       }
        return Status.GAME_CONTINUE

    }

    private fun forwardDiagonal(streak: String): Boolean {
        val sb: StringBuilder = StringBuilder(this.rows)
        for (h in 0 until this.rows) {
            val w = lastCol + this.lastRow - h
            if (0 <= w && w < this.columns) {
                if (debug) {
                    println(" FD check lastpos[${this.lastRow}][${this.lastCol}]w=$w rows=${this.rows}, added index [$h][$w]")
                }
                sb.append(this.playerDiscs.get(h).get(w))
                if (debug) {
                    println("New string buffer : ${sb.toString()}")
                    if (debug) debugHelper.debugMap[Pos(h, w)] = PosType.FORWARD_DIAGONAL
                }
            }
        }
        return sb.toString().contains(streak)

    }

    private fun backwardDiagonal(streak: String): Boolean {
        val sb: StringBuilder = StringBuilder(this.rows)
        for (h in 0 until this.rows) {
            val w = this.lastCol - this.lastRow + h
            if (0 <= w && w < this.columns) {
                if (debug) println(" BD check lastpos[${this.lastCol}][${this.lastRow}]w=$w rows=${this.rows}, added index [$h][$w]")
                sb.append(this.playerDiscs.get(h).get(w))
                if (debug) {
                    println("New string buffer : ${sb.toString()}")
                    debugHelper.debugMap[Pos(h,w)] = PosType.BACKWARD_DIAGONAL
                }
            }
        }
        return sb.toString().contains(streak)
    }

    private fun horizontalStreak(streak: String) : Boolean {

        if (debug) {
            for (col in 0..columns-1) {
                debugHelper.debugMap[Pos(this.lastRow, col)] = PosType.HORIZONTAL_CHECK
            }
        }
        return String(this.playerDiscs[this.lastRow].toCharArray()).contains(streak)
    }

    private fun verticalStreak(streak: String) : Boolean {
        //create verticalString
        val sb = StringBuilder(this.rows)
        for (h in 0 until this.rows) {
            sb.append(this.playerDiscs.get(h).get(lastCol))
            if (debug) {
                debugHelper.debugMap[Pos(h, lastCol)] = PosType.VERTICAL_CHECK
            }
        }
        return sb.toString().contains(streak)
    }

    private fun isColumnFull(columnIns: Int): Boolean {
        if (playerDiscs[0][columnIns-1] != SPACE) {
            return true
        }
        return false
    }

    fun addDiscToColumn(columnIns: Int): Status {
        if (!isColumnFull(columnIns)) {
            for (h in rows -1 downTo 0) {
                if (playerDiscs[h][columnIns-1] == SPACE) {
                    //add the disc above the row where there is a character
                    playerDiscs[h][columnIns-1] = currentPlayer.symbol
                    lastCol = columnIns - 1
                    lastRow = h
                    if (debug) debugHelper.lastPos = Pos(lastRow, lastCol)
                    return Status.GAME_CONTINUE
                }
            }
        }
        return Status.COLUMN_FULL
    }

    fun isNumeric(toCheck: String): Boolean {
        return toCheck.all { char -> char.isDigit() }
    }

    fun drawWithDiscs() {
        for (i in 1..rows) {
            if (i == 1) {
                var columnIndex = 1
                repeat(columns) {
                    print(" " + columnIndex)
                    columnIndex += 1
                }
                println("")
            }
            for (j in 1..columns + 1) {
                if (j <= columns) {
                    print("${EDGE}${playerDiscs[i-1][j-1]}")
                } else {
                    print("${EDGE} ")
                }
            }
            println("")
        }
        print("${LEFT_CORNER}")
        repeat(columns - 1) {
            print("${BOTTOM}${BOTTOM_WITH_STUT}")
        }
        print("${BOTTOM}${RIGHT_CORNER}")
        println()
    }

    fun drawCheckLinesWithDiscs() {
//        val discs = playerDiscs.reversed()
        for (i in 1..rows) {
            if (i == 1) {
                var columnIndex = 1
                repeat(columns) {
                    print(" " + columnIndex)
                    columnIndex += 1
                }
                println("")
            }
            for (j in 1..columns + 1) {
                if (j <= columns) {
                    print("${EDGE}")
                    if (debug && debugHelper.debugMap.containsKey(Pos(i-1, j-1))) {
                            debugHelper.colorPrintPos(Pos(i-1, j-1))
                    } else {
                       print(playerDiscs[i - 1][j - 1])
                    }
                } else {
                    print("${EDGE} ")
                }
            }
            println("")
        }
        print("${LEFT_CORNER}")
        repeat(columns - 1) {
            print("${BOTTOM}${BOTTOM_WITH_STUT}")
        }
        print("${BOTTOM}${RIGHT_CORNER}")
        println()
    }

    fun togglePlayer() {
        if (currentPlayer.equals(player1)) {
            currentPlayer = player2
        } else {
            currentPlayer=  player1
        }
    }

    fun resetGame() {
        this.ended = false
        this.playerDiscs = List(this.rows) { MutableList<Char>(this.columns) { ' ' } }
        this.lastEndStatus = Status.GAME_CONTINUE
        this.turns = 0
        this.togglePlayer()
    }

    fun printScore() {
        println("Score")
        println("${player1.name}: ${player1.score} ${player2.name}: ${player2.score} ")
    }
}

enum class Status {
    GAME_CONTINUE, COLUMN_FULL, COLUMN_OUT_OF_RANGE, INSTRUCTION_ILLEGAL, END_STATE, WIN_PLAYER1, WIN_PLAYER2, TIE
}