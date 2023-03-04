package machine

const val WATER_PER_CUP = 200
const val MILK_PER_CUP = 50
const val COOFFEE_BEANS_FOR_CUP = 15

fun main() {
    val display = ConsoleCoffeeMachineDisplay()

    display.print("Write how many ml of water the coffee machine has:")
    val waterInStock = display.readInt()

    display.print("Write how many ml of milk the coffee machine has:")
    val milkInStock = display.readInt()

    display.print("Write how many grams of coffee beans the coffee machine has:")
    val cooffeeBeansInStock = display.readInt()

    display.print("Write how many cups of coffee you will need:")
    val orderedCups = display.readInt()

    // На сколько кружек хватит воды, молока, порошка
    val maxCupsByWater = waterInStock / WATER_PER_CUP
    val maxCupsByMilk = milkInStock / MILK_PER_CUP
    val maxCupsByCoffeeBeans = cooffeeBeansInStock / COOFFEE_BEANS_FOR_CUP

    val maxCups = Math.min(Math.min(maxCupsByWater, maxCupsByMilk), maxCupsByCoffeeBeans)

    var answer: String = when {
        orderedCups == maxCups -> "Yes, I can make that amount of coffee"
        orderedCups < maxCups -> "Yes, I can make that amount of coffee (and even ${maxCups - orderedCups} more than that)"
        else -> "No, I can make only ${maxCups} cups of coffee"
    }

    display.print(answer)
}

interface Display {
    fun print(str: String)
    fun readInt() : Int
}

class ConsoleCoffeeMachineDisplay : Display{
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