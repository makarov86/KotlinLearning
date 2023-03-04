package cinema

import java.lang.Exception

const val TOTAL_SEATS_EQUAL_PRICE_LIMIT = 60
const val HIGH_PRICE = 10
const val LOW_PRICE = 8

fun main() {
    println("Enter the number of rows:\n> ")
    print("> ")
    val rowsCount = readln().toInt()

    println("Enter the number of seats in each row:\n> ")
    print("> ")
    val seatsCount = readln().toInt()

    // Создаем экземпляр объекта кинотеатра
    val cinema = Cinema(rowsCount, seatsCount)

    val ticketOffice = TicketOffice(cinema)

    val income: Int

    if (rowsCount * seatsCount <= TOTAL_SEATS_EQUAL_PRICE_LIMIT) {
        income = rowsCount * seatsCount * HIGH_PRICE
    } else {
        val highPriceRowsCount = rowsCount / 2
        val lowPriceRowCount = rowsCount - highPriceRowsCount
        income = highPriceRowsCount * seatsCount * HIGH_PRICE + lowPriceRowCount * seatsCount * LOW_PRICE
    }

    println("Total income:")
    println("\$$income")
}

/**
 * Класс кинотеатр
 * @param rowsCount Кол-во рядов
 * @param seatsCount Кол-во мест в одном ряду
 * @constructor Создать объект кинотеатр
 */
class Cinema (rowsCount: Int, seatsCount: Int) {

    private val rowsCount: Int
    private val seatsCount: Int

    /**
     * Признак того что кинотеатр маленький
     */
    private val isSmallCinema: Boolean

    /**
     * Схема зала с местами (MutableList нам избыточен, схема не будет менять в процессе работы)
     */
    private val _seatsScheme: List<List<Seat>>

    init {
        if (rowsCount == 0 || seatsCount == 0) {
            throw Exception("Error!")
        }

        this.rowsCount = rowsCount
        this.seatsCount = seatsCount

        // Инициализация схемы зала, список списков мест, и элементы списка - объекты класса Место
        _seatsScheme = List<List<Seat>>(rowsCount) {
            List<Seat>(seatsCount) { Seat() }
        }

        isSmallCinema = rowsCount * seatsCount <= TOTAL_SEATS_EQUAL_PRICE_LIMIT

        // Теперь для схемы зала нужно просчитать и заполнить цены на места
        for (rowInd in _seatsScheme.indices) {
            for (seat in _seatsScheme[rowInd]) {
                seat.price = calcPrice(rowInd + 1)
            }
        }
    }

    public fun getTicketPrice(rowNumber: Int, seatNumber: Int) : Int {
        // Работает со схемой зала, в ней уже заранее просчитаны все цены
        return _seatsScheme[rowNumber - 1][seatNumber - 1].price!!
    }

    /**
     * Вернет схему зала в виде строки
     */
    override fun toString(): String {
        return super.toString()
    }

    /**
     * Рассчёт цены на место
     * @param rowNumber номер ряда
     */
    private fun calcPrice(rowNumber: Int): Int {
        if (isSmallCinema) {
            return HIGH_PRICE
        }
        val highPriceRowsCount = rowsCount / 2
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
 * Класс кассы кинотеатров
 */
class TicketOffice(cinema: Cinema){
    val cinema: Cinema

    init {
        this.cinema = cinema
    }
}