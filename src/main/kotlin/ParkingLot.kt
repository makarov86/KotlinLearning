package parking

fun main() {
    val terminal = ConsoleParkTerminal()
    val controller = ParkingController(terminal)

    controller.start()
}

class ParkingController(parkingTerminal: TerminalScreen) {

    private val _terminal: TerminalScreen = parkingTerminal
    private var _parking: ParkingLot? = null

    enum class CommandType(val typeName: String) {
        Create("create"),
        Park("park"),
        Leave("leave"),
        Status("status"),
        RegByColor("reg_by_color"),
        SpotByColor("spot_by_color"),
        SpotByReg("spot_by_reg"),
        Exit("exit")
    }

    open class Command(val type: CommandType)
    class ExitCommand : Command(CommandType.Exit)
    class StatusCommand : Command(CommandType.Status)
    class CreateCommand(val spotCount: Int) : Command(CommandType.Create)
    class ParkCommand(val carNumber: String, val carColor: String) : Command(CommandType.Park)
    class LeaveCommand(val spotNumber: Int) : Command(CommandType.Leave)
    class RegByColor(val color: String) : Command(CommandType.RegByColor)
    class SpotByReg(val reg: String) : Command(CommandType.SpotByReg)
    class SpotByColor(val color: String) : Command(CommandType.SpotByColor)

    public fun start() {
        while (true) {
            val cmd: Command
            try {
                cmd = parseCmd(_terminal.readLine())
            } catch (e: IllegalArgumentException) {
                _terminal.print("Error")
                continue
            }

            if (_parking == null && !(cmd is ExitCommand || cmd is CreateCommand)) {
                _terminal.print("Sorry, a parking lot has not been created.")
                continue
            }

            when (cmd) {
                is ParkCommand -> park(cmd.carNumber, cmd.carColor)
                is LeaveCommand -> leave(cmd.spotNumber)
                is CreateCommand -> create(cmd.spotCount)
                is StatusCommand -> showStatus()
                is RegByColor -> regByColor(cmd.color)
                is SpotByColor -> spotByColor(cmd.color)
                is SpotByReg -> spotByReg(cmd.reg)
                is ExitCommand -> return
            }
        }
    }

    private fun spotByReg(reg: String) {
        val numbers = _parking!!.getSpotsByCarRegNumber(reg).map { it.number }.toList()
        _terminal.print(if (numbers.isEmpty()) "No cars with registration number $reg were found." else numbers.joinToString(", "))
    }

    private fun spotByColor(color: String) {
        val numbers = _parking!!.getSpotsByCarColor(color).map { it.number }.toList()
        _terminal.print(if (numbers.isEmpty()) "No cars with color $color were found." else numbers.joinToString(", "))
    }

    private fun regByColor(color: String) {
        val carNumbers = _parking!!.getSpotsByCarColor(color).map { it.car!!.number }.toList()
        _terminal.print(if (carNumbers.isEmpty()) "No cars with color $color were found." else carNumbers.joinToString(", "))
    }

    private fun leave(spotNumber: Int) {
        try {
            if (_parking!!.tryLeave(spotNumber))
            {
                _terminal.print("Spot $spotNumber is free.")
            } else {
                _terminal.print("There is no car in spot $spotNumber.")
            }
        } catch (e: Exception) {
            _terminal.print("Spot number is invalid")
        }
    }

    private fun park(carNumber: String, carColor: String) {
        val spotIndex = _parking!!.tryPark(carNumber, carColor)
        if (spotIndex == null) {
            _terminal.print("Sorry, the parking lot is full.")
        } else {
            _terminal.print("$carColor car parked in spot $spotIndex.")
        }
    }

    private fun showStatus() {
        if (!_parking!!.hasAnyCars()) {
            _terminal.print("Parking lot is empty.")
            return
        }
        for (spot in _parking!!.getBusySpots()) {
            _terminal.print("${spot.number} ${spot.car?.number} ${spot.car?.color}")
        }
    }

    private fun create(spotCount: Int) {
        _parking = ParkingLot(spotCount)
        _terminal.print("Created a parking lot with ${_parking?.spotCount} spots.")
    }

    private fun parseCmd(str: String) : Command {
        var cmd = str
        if (cmd.isBlank() || cmd.isEmpty()) throw IllegalArgumentException()

        val cmdParts = cmd.split(' ')
        if (cmdParts.isEmpty()) throw IllegalArgumentException()

        val commandName = cmdParts[0]

        if (cmdParts.size == 1) {
            if (commandName.equals(CommandType.Exit.typeName, ignoreCase = true))
                return ExitCommand()
            if (commandName.equals(CommandType.Status.typeName, ignoreCase = true))
                return StatusCommand()
        }

        if (cmdParts.size < 2) throw IllegalArgumentException()

        when (commandName.lowercase()) {
            CommandType.Create.typeName.lowercase() -> {
                if (cmdParts.size != 2) throw IllegalArgumentException()
                val spotCount = cmdParts[1].toIntOrNull() ?: throw IllegalArgumentException()
                return CreateCommand(spotCount)
            }
            CommandType.Park.typeName.lowercase() -> {
                if (cmdParts.size != 3) throw IllegalArgumentException()
                val number = cmdParts[1]
                var color = formatColor(cmdParts.last())
                return ParkCommand(number, color)
            }
            CommandType.Leave.typeName.lowercase() -> {
                if (cmdParts.size != 2) throw IllegalArgumentException()
                val spotNumber = cmdParts[1].toIntOrNull() ?: throw IllegalArgumentException()
                return LeaveCommand(spotNumber)
            }
            CommandType.RegByColor.typeName.lowercase() -> {
                if (cmdParts.size != 2) throw IllegalArgumentException()
                var color = formatColor(cmdParts.last())
                return RegByColor(color)
            }
            CommandType.SpotByColor.typeName.lowercase() -> {
                if (cmdParts.size != 2) throw IllegalArgumentException()
                var color = formatColor(cmdParts.last())
                return SpotByColor(color)
            }
            CommandType.SpotByReg.typeName.lowercase() -> {
                if (cmdParts.size != 2) throw IllegalArgumentException()
                return SpotByReg(reg = cmdParts[1])
            }
            else -> throw IllegalArgumentException()
        }
    }

    private fun formatColor(color: String) : String = color.first().uppercase() + color.substring(1).lowercase()
}

data class Car(val number: String, val color: String)

class Spot(number: Int) {
    var car: Car? = null
    val number: Int = number
    public fun isFree(): Boolean = car == null
}

class ParkingLot(val spotCount: Int) {

    private val _spots: List<Spot> = List<Spot>(spotCount) { i -> Spot(i + 1) }

    fun hasAnyCars(): Boolean = _spots.any { spot -> spot.car != null }

    fun tryPark(carNumber: String, carColor: String): Int? {
        val freeSpotIndex = _spots.indices.firstOrNull() { i -> _spots[i].car == null }

        if (freeSpotIndex != null) {
            val spot = _spots[freeSpotIndex]
            spot.car = Car(carNumber, carColor)
            return spot.number
        }

        return null
    }

    fun tryLeave(spotNumber: Int): Boolean {
        if (spotNumber - 1 > _spots.lastIndex) throw IllegalArgumentException()
        val spot = _spots[spotNumber - 1]

        if (spot.isFree()) {
            return false
        }
        spot.car = null

        return true
    }

    fun getBusySpots(): List<Spot> = _spots.filter { spot -> spot.car != null }.toList()

    fun getSpotsByCarColor(color: String): List<Spot> {
        return _spots.filter { spot -> spot.car != null && spot.car!!.color.equals(color, ignoreCase = true) }
            .toList()
    }

    fun getSpotsByCarRegNumber(number: String): List<Spot> {
        return _spots.filter { spot -> spot.car != null && spot.car!!.number == number }.toList()
    }
}

interface TerminalScreen {
    fun print(str: String)
    fun readInt() : Int
    fun readLine() : String
}

class ConsoleParkTerminal : TerminalScreen {
    public override fun print(str: String) = println(str)

    public override fun readLine() : String {
        return readln().replace(">", "").trim()
    }

    public override fun readInt(): Int {
        var value: Int?
        do {
            value = readln().replace(">", "").trim().toIntOrNull()
        } while (value == null)

        return value
    }
}