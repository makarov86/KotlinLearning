package cinema

import java.lang.Exception

const val TOTAL_SEATS_EQUAL_PRICE_LIMIT = 60
const val HIGH_PRICE = 10
const val LOW_PRICE = 8

fun main() {
    // Любой ввод вывод или пользовательский интерфейс
    // должен быть изолирован и абстрагирован от остальной логики
    val screen = ConsoleTerminalScreen()

    screen.print("Enter the number of rows:")
    val rowsCount = screen.readInt()

    screen.print("Enter the number of seats in each row:")
    val seatsCount = screen.readInt()

    // Создаем экземпляр объекта кинотеатра
    val cinema = VidnoeCinema(rowsCount, seatsCount)

    // Создаем экземпляр объекта кассы кинотеатров
    val ticketOffice = TicketOffice(cinema, screen)

    screen.print("\nCinema:")
    screen.print(cinema.schemeToString())

    screen.print("Enter a row number:")
    val rowNumber = screen.readInt()
    screen.print("Enter a seat number in that row:")
    val seatNumber = screen.readInt()
    screen.print("\nTicket price: \$${cinema.getTicketPrice(rowNumber, seatNumber)}\n")
    cinema.sellSeat(rowNumber, seatNumber)
    screen.print("Cinema:")
    screen.print(cinema.schemeToString())
}

/**
 * Интерфейс для конкретных кинотеатров
 */
interface Cinema {
    /**
     * Получить цену билета на конкретный ряд и место
     */
    fun getTicketPrice(rowNumber: Int, seatNumber: Int) : Int

    /**
     * Продать место
     */
    fun sellSeat(rowNumber: Int, seatNumber: Int)

    /**
     * Вернет схему зала в виде строки
     */
    fun schemeToString() : String
}

/**
 * Класс видновский кинотеатр
 * @param rowsCount Кол-во рядов
 * @param seatsCount Кол-во мест в одном ряду
 * @constructor Создать объект кинотеатр
 */
class VidnoeCinema (rowsCount: Int, seatsCount: Int) : Cinema {

    private val _rowsCount: Int
    private val _seatsCount: Int

    /**
     * Признак того что кинотеатр маленький
     */
    private val _isSmallCinema: Boolean

    /**
     * Схема зала с местами (MutableList нам избыточен, схема не будет меняться в процессе работы)
     */
    private val _seatsScheme: List<List<Seat>>

    init {
        if (rowsCount == 0 || seatsCount == 0) {
            throw Exception("Error!")
        }

        this._rowsCount = rowsCount
        this._seatsCount = seatsCount

        // Инициализация схемы зала, список списков мест, и элементы списка - объекты класса Место
        _seatsScheme = List<List<Seat>>(rowsCount) {
            List<Seat>(seatsCount) { Seat() }
        }

        _isSmallCinema = rowsCount * seatsCount <= TOTAL_SEATS_EQUAL_PRICE_LIMIT

        // Теперь для схемы зала нужно просчитать и заполнить цены на места
        for (rowInd in _seatsScheme.indices) {
            for (seat in _seatsScheme[rowInd]) {
                seat.price = calcPrice(rowInd + 1)
            }
        }
    }

    /**
     * Получить цену билета на конкретный ряд и место
     */
    public override fun getTicketPrice(rowNumber: Int, seatNumber: Int) : Int {
        // Работает со схемой зала, в ней уже заранее просчитаны все цены
        return _seatsScheme[rowNumber - 1][seatNumber - 1].price!!
    }

    /**
     * Продать место
     */
    public override fun sellSeat(rowNumber: Int, seatNumber: Int) {
        // TODO проверить приделы
        val seat = _seatsScheme[rowNumber - 1][seatNumber - 1]

        if (!seat.available) throw Exception("Already sold")
        seat.available = false
    }

    /**
     * Вернет схему зала в виде строки
     */
    public override fun schemeToString(): String {
        val strBuilder = StringBuilder()
        strBuilder.append("  ")
        for (seat in 1.._seatsCount){
            strBuilder.append("$seat ")
        }
        strBuilder.appendLine()
        for (row in 1.._rowsCount){
            strBuilder.append("$row ")
            for (seat in 1.._seatsCount){
                strBuilder.append(_seatsScheme[row - 1][seat - 1].toString() + " ")
            }
            strBuilder.appendLine()
        }

        return strBuilder.toString()
    }

    /**
     * Рассчёт цены на место
     * @param rowNumber номер ряда
     */
    private fun calcPrice(rowNumber: Int): Int {
        if (_isSmallCinema) {
            return HIGH_PRICE
        }
        val highPriceRowsCount = _rowsCount / 2
        return if (rowNumber <= highPriceRowsCount) HIGH_PRICE else LOW_PRICE
    }
}

/**
 * Класс Место в зале
 * @constructor Создать объект место
 * @param available Свободно или нет (по умолчанию свободно)
 */
class Seat(available: Boolean = true) {
    var price: Int? = null
    var available: Boolean

    // В реальной программе ряд и место лучше было бы занести сюда в параметры места.
    // Цена бы зависела не только от ряда, а много от чего.
    init {
        this.available = available
    }

    override fun toString(): String {
        return if (available) "S" else "B"
    }
}

/**
 * Интерфейс экрана терминала
 */
interface TerminalScreen {
    fun print(str: String)
    fun readInt() : Int
}

/**
 * Реализация терминала - ввода вывода пользователя на консоль приложения
 */
class ConsoleTerminalScreen : TerminalScreen {
    public override fun print(str: String) {
        println(str)
    }

    public override fun readInt(): Int {
        var value: Int?
        do {
            System.out.print("> ")
            value = readln().toIntOrNull()
        } while (value == null)

        return value
    }
}

/**
 * Класс кассы кинотеатров.
 * При инициализации объектов класса - используются абстракции кинотеатра и экрана терминала, (через интерфейсы)
 * @param cinema объект реализующий интерфейс Кинотеатр
 * @param terminalScreen объект реализующий интерфейс экрана терминала
 */
class TicketOffice(cinema: Cinema, terminalScreen: TerminalScreen){
    val cinema: Cinema

    init {
        this.cinema = cinema
    }
}