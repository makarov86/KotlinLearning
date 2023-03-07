package parking

fun main() {
    val terminal = ConsoleParkTerminal()
    var spots = mutableListOf<Spot>()

    while (true) {
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
            CommandType.Create -> create(terminal, spots, cmd.spotCount!!)
            CommandType.Status -> showStatus(terminal, spots)
            CommandType.Exit -> return
        }
    }
}

fun showStatus(terminal: TerminalScreen, spots: List<Spot>) {
    if (spots.isEmpty()) {
        terminal.print("Sorry, a parking lot has not been created.")
        return
    }

    var hasAnyCar = false
    for (spot in spots) {
        if (spot.car != null) {
            hasAnyCar = true
            break
        }
    }

    if (!hasAnyCar) {
        terminal.print("Parking lot is empty.")
        return
    }

    for (spotIndex in spots.indices) {
        val spotNumber = spotIndex + 1
        val spot = spots[spotIndex]
        if (spot.car != null)
        {
            terminal.print("$spotNumber ${spot.car?.number} ${spot.car?.color}")
        }
    }
}

fun create(terminal: TerminalScreen, spots: MutableList<Spot>, spotCount: Int) {
    spots.clear()
    spots.addAll(List<Spot>(spotCount) { Spot() })
    terminal.print("Created a parking lot with $spotCount spots.")
}

fun leave(terminal: TerminalScreen, spots: List<Spot>, spotNumber: Int) {
    if (spots.isEmpty()) {
        terminal.print("Sorry, a parking lot has not been created.")
        return
    }

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
    if (spots.isEmpty()) {
        terminal.print("Sorry, a parking lot has not been created.")
        return
    }

    var freeSpotIndex: Int? = null
    for (i in spots.indices) {
        if (spots[i].isFree()) {
            freeSpotIndex = i
            break
        }
    }

    if (freeSpotIndex == null) {
        terminal.print("Sorry, the parking lot is full.")
        return
    }

    val car = Car(carNumber, carColor)
    spots[freeSpotIndex].car = car

    terminal.print("${car.color} car parked in spot ${freeSpotIndex + 1}.")
}

fun parseCmd(str: String) : Command {
    var cmd = str
    if (cmd.isBlank() || cmd.isEmpty()) throw IllegalArgumentException()
    val cmdParts = cmd.replace(">", "").trim().split(' ')

    if (cmdParts.size == 1) {
        if (cmdParts[0] == CommandType.Exit.cmdTypeName) return Command(CommandType.Exit)
        if (cmdParts[0] == CommandType.Status.cmdTypeName) return Command(CommandType.Status)
    }

    if (cmdParts.size < 2) throw IllegalArgumentException()

    when (cmdParts[0]) {
        CommandType.Create.cmdTypeName -> {
            if (cmdParts.size != 2) throw IllegalArgumentException()
            val spotCount = cmdParts[1].toIntOrNull() ?: throw IllegalArgumentException()
            return Command(type = CommandType.Create, spotCount = spotCount!!)
        }
        CommandType.Park.cmdTypeName -> {
            if (cmdParts.size != 3) throw IllegalArgumentException()
            val number = cmdParts[1]
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

enum class CommandType(val cmdTypeName: String) {
    Create("create"),
    Park("park"),
    Leave("leave"),
    Status("status"),
    Exit("exit")
}

data class Command(
    val type: CommandType,
    val carNumber: String? = null,
    val carColor: String? = null,
    val spotNumber: Int? = null,
    val spotCount: Int? = null
)

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