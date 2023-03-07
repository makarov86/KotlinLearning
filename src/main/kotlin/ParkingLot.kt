package parking

fun main() {
    val terminal = ConsoleParkTerminal()
    val spots = listOf<Spot>(Spot(Car("t675we750", "white")), Spot())
    val cmd: Command

    try {
        cmd = parseCmd(terminal.readLine())
    } catch (e: IllegalArgumentException) {
        terminal.print("Error")
        return
    }

    when (cmd.type) {
        CommandType.Park -> park(terminal, spots, cmd.carNumber!!, cmd.carColor!!)
        CommandType.Leave -> leave(terminal, spots, cmd.spotNumber!!)
    }
}

fun leave(terminal: TerminalScreen, spots: List<Spot>, spotNumber: Int) {
    if (spotNumber - 1 > spots.lastIndex) {
        terminal.print("Error")
        return
    }

    val spot = spots[spotNumber - 1]
    if (spot.isFree()) {
        terminal.print("There is no car in spot $spotNumber.")
        return
    }

    spot.car = null
    terminal.print("Spot $spotNumber is free.")
}

fun park(terminal: TerminalScreen, spots: List<Spot>, carNumber: String, carColor: String) {
    var freeSpotIndex: Int? = null
    for (i in spots.indices) {
        if (spots[i].isFree()) {
            freeSpotIndex = i
            break
        }
    }

    if (freeSpotIndex == null) {
        terminal.print("No free spots!")
        return
    }

    spots[freeSpotIndex].car = Car(carNumber, carColor)

    terminal.print("$carColor car parked in spot ${freeSpotIndex + 1}.")
}

fun parseCmd(str: String) : Command {
    var cmd = str
    if (cmd.isBlank() || cmd.isEmpty()) throw IllegalArgumentException()
    val cmdParts = cmd.replace(">", "").trim().split(' ')
    if (cmdParts.size < 2) throw IllegalArgumentException()

    when (cmdParts[0]) {
        CommandType.Park.cmdTypeName -> {
            if (cmdParts.size < 3) throw IllegalArgumentException()
            val number = cmdParts.subList(1, cmdParts.lastIndex - 1).joinToString()
            var color = cmdParts.last()
            return Command(type = CommandType.Park, carNumber = number, carColor = color)
        }
        CommandType.Leave.cmdTypeName -> {
            if (cmdParts.size != 2) throw IllegalArgumentException()
            val spotNumber = cmdParts[1].toIntOrNull() ?: throw IllegalArgumentException()
            return Command(type = CommandType.Leave, spotNumber = spotNumber!!)
        }
        else -> throw IllegalArgumentException()
    }
}

enum class CommandType(val cmdTypeName: String) { Park("park"), Leave("leave") }

data class Command(
    val type: CommandType,
    val carNumber: String? = null,
    val carColor: String? = null,
    val spotNumber: Int? = null)

class Spot(var car: Car? = null){
    public fun isFree(): Boolean = car == null
}

data class Car(val number: String, val color: String)

interface TerminalScreen {
    fun print(str: String)
    fun readInt() : Int
    fun readLine() : String
}

class ConsoleParkTerminal : TerminalScreen {
    public override fun print(str: String) = println(str)

    public override fun readLine() : String {
        //System.out.print("> ")
        return readln()
    }

    public override fun readInt(): Int {
        var value: Int?
        do {
            //System.out.print("> ")
            value = readln().toIntOrNull()
        } while (value == null)

        return value
    }
}