package machine

const val BUY_ACTION_CODE = "buy"
const val FILL_ACTION_CODE = "fill"
const val TAKE_ACTION_CODE = "take"

const val ESPRESSO_NAME = "espresso"
const val LATTE_NAME = "latte"
const val CAPPUCCINO_NAME = "cappuccino"

fun main() {
    val display = ConsoleCoffeeMachineDisplay()

    // Инициализация типов кофе - это будет настройкой для создаваемой кофе машины
    val espressoType = CoffeeType(ESPRESSO_NAME, Ingredients(250, 0, 16), 4)
    val latteType = CoffeeType(LATTE_NAME, Ingredients(350, 75, 20), 7)
    val cappuccinoType = CoffeeType(CAPPUCCINO_NAME, Ingredients(200, 100, 12), 6)

    // Начальные запасы кофе машины:
    val state = CoffeeMachineInitState(Ingredients(400, 540, 120), 9, 550)

    // Создаем экземпляр самой кофе машины
    val machine = CoffeeMachine(display, listOf(espressoType, latteType, cappuccinoType), state)
    machine.start()

    // На этом всё, весь алгоритм работы в самой кофе машине
}

/**
 * Класс кофе машины
 * @param display экран кофе машины
 * @param coffeeTypes настройки, а именно список возможных типов напитков, их состав ингридиентов и цена
 * @param initState начальное наполнение кофе машины
 */
class CoffeeMachine(display: Display, coffeeTypes: List<CoffeeType>, initState: CoffeeMachineInitState) {
    private val _coffeeTypes: List<CoffeeType>
    private val _display: Display
    private val _storage: CoffeeMachineStorage
    private val _availableActions = listOf(BUY_ACTION_CODE, FILL_ACTION_CODE, TAKE_ACTION_CODE)

    init {
        _coffeeTypes = coffeeTypes
        _display = display
        _storage = CoffeeMachineStorage(initState)
    }

    public fun start() {
        _display.print(_storage.getStorageInfo())
        _display.print("Write action (${_availableActions.joinToString(", ")}):")
        val action = _display.readOneOfValues(_availableActions)

        when (action) {
            BUY_ACTION_CODE -> buyAction()
            FILL_ACTION_CODE -> fillAction()
            TAKE_ACTION_CODE -> takeAction()
        }

        _display.print("")
        _display.print(_storage.getStorageInfo())
    }

    public fun buyAction() {
        val sb = StringBuilder()
        val typeNumbers = mutableListOf<String>()
        for (typeInd in _coffeeTypes.indices) {
            sb.append("${typeInd + 1} - ${_coffeeTypes[typeInd].name}")
            if (typeInd != _coffeeTypes.size - 1) sb.append(", ")
            typeNumbers.add((typeInd + 1).toString())
        }
        val allTypesWithNumber = sb.toString()

        _display.print("What do you want to buy? ${allTypesWithNumber}:")
        val selectedNumber = _display.readOneOfValues(typeNumbers).toInt()
        val selectedCoffeeType = _coffeeTypes[selectedNumber - 1]

        // Кофе выбрано, готовим, продаём
        _storage.cups--
        _storage.incomeAmount += selectedCoffeeType.price
        _storage.subIngredients(selectedCoffeeType.ingredients)
    }

    public fun fillAction() {
        _display.print("Write how many ml of water you want to add:")
        val water = _display.readInt()

        _display.print("Write how many ml of milk you want to add:")
        val milk = _display.readInt()

        _display.print("Write how many grams of coffee beans you want to add:")
        val beans = _display.readInt()

        _display.print("Write how many disposable cups you want to add:")
        val cups = _display.readInt()

        _storage.cups += cups
        _storage.addIngredients(Ingredients(water, milk, beans))
    }

    public fun takeAction() {
        _display.print("I gave you \$${_storage.incomeAmount}")
        _storage.incomeAmount = 0
    }
}

/**
 * Ингридиенты, экземпляры класса immutable, неизменяемые
 */
data class Ingredients(val water: Int, val milk: Int, val coffeeBeans: Int)

/**
 * Тип напитка, состав ингридиентов и цена
 */
data class CoffeeType(val name: String, val ingredients: Ingredients, val price: Int)

/**
 * Начальное состояние кофе-машины, кол-во ингридиентов в запасе, чистых кружек, денег
 */
data class CoffeeMachineInitState(val ingredients: Ingredients, val cups: Int, val incomeAmount: Int)

/**
 * Внутренне хранилище кофе машины
 */
class CoffeeMachineStorage(initState: CoffeeMachineInitState) {
    var ingredients: Ingredients
    var cups: Int
    var incomeAmount: Int

    init {
        ingredients = initState.ingredients
        cups = initState.cups
        incomeAmount = initState.incomeAmount
    }

    /**
     * Добавить ингридиенты в хранилище
     */
    public fun addIngredients(addIngredients: Ingredients) {
        ingredients = Ingredients(
            ingredients.water + addIngredients.water,
            ingredients.milk + addIngredients.milk,
            ingredients.coffeeBeans + addIngredients.coffeeBeans)
    }

    /**
     * Истратить игридиенты
     */
    public fun subIngredients(subIngredients: Ingredients) {
        ingredients = Ingredients(
            ingredients.water - subIngredients.water,
            ingredients.milk - subIngredients.milk,
            ingredients.coffeeBeans - subIngredients.coffeeBeans)
    }

    public fun getStorageInfo(): String
    {
        val sb = StringBuilder()
        sb.appendLine("The coffee machine has:")
        sb.appendLine("${ingredients.water} ml of water")
        sb.appendLine("${ingredients.milk} ml of milk")
        sb.appendLine("${ingredients.coffeeBeans} g of coffee beans")
        sb.appendLine("$cups disposable cups")
        sb.appendLine("\$$incomeAmount of money")
        return  sb.toString();
    }
}

/**
 * Интерфейс экрана (абстракция)
 */
interface Display {
    fun print(str: String)
    fun readInt() : Int
    fun readOneOfValues(values: List<String>) : String
}

/**
 * Реализация терминала - ввода вывода экрана кофе машины на консоль приложения
 */
class ConsoleCoffeeMachineDisplay : Display {
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

    override fun readOneOfValues(values: List<String>): String {
        var value: String? = null
        do {
            System.out.print("> ")
            value = readln()
        } while (!values.contains(value))

        return value!!
    }
}