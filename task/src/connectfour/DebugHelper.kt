package connectfour

// Everything after this is in red
val RED = "\u001b[31m"
val GREEN = "\u001b[32m"

// Resets previous color codes
val RESET = "\u001b[0m"

class DebugHelper(val rows: Int, val columns: Int) {

    val debugMap = mutableMapOf<Pos,PosType>()
    var lastPos = Pos(1,1)

    fun resetMap() {
        debugMap.clear()
    }

    fun colorPrintPos(pos: Pos) {
        if (pos.equals(lastPos)) {
            colorPrint('x', GREEN)
        } else {
            when (debugMap.get(pos)) {
                PosType.FORWARD_DIAGONAL, PosType.BACKWARD_DIAGONAL, PosType.VERTICAL_CHECK, PosType.HORIZONTAL_CHECK -> colorPrint(
                    'x',
                    RED
                )
                PosType.LAST_POS -> colorPrint('x', GREEN)
            }
        }
    }

    fun colorPrint(char: Char, color: String): Unit {
        print(color + char + RESET)
    }
}