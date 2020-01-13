package tatyam.me.qkdealerplus

import android.content.*
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigInteger
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import kotlin.math.min
import java.security.SecureRandom
import java.util.*


class MainActivity : AppCompatActivity() {
    private var mode = 0
    private var inputFormat = 1
    private var numberOfX = 0
    val times = listOf(5000, 500, 50, 5)
    private var judged = true
    private var joker = false
    private var maxFactorX = 2
    var number = "0"
    private val resetText = arrayOf("Press \"●\" to Judge")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    "setPlayer" -> resultText.text = intent.getStringExtra("setText")
                    "copyBox" -> {
                        (context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip = ClipData.newPlainText("QK++Copy", resultBox.text)
                        val toast = Toast.makeText(this@MainActivity, "copied!", Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.BOTTOM, 0, 16)
                        toast.show()
                    }
                    "clearBox" -> {
                        resultBox.text = ""
                        val toast = Toast.makeText(this@MainActivity, "deleted!", Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.BOTTOM, 0, 16)
                        toast.show()
                    }
                    "showResultBox" -> resultBox.visibility = if (intent.getBooleanExtra("showResultBox", true)) View.VISIBLE else View.INVISIBLE
                }
            }
        }
        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(broadcastReceiver, IntentFilter("setPlayer"))
        localBroadcastManager.registerReceiver(broadcastReceiver, IntentFilter("copyBox"))
        localBroadcastManager.registerReceiver(broadcastReceiver, IntentFilter("clearBox"))
        localBroadcastManager.registerReceiver(broadcastReceiver, IntentFilter("showResultBox"))

        findViewById<Button>(R.id.buttonDelete).setOnLongClickListener {
            judged = false
            number = "0"
            numberOfX = 0
            judgeNumber.text = number
            resultText.text = resetText[mode]
            true
        }

        findViewById<Button>(R.id.buttonJudge).setOnLongClickListener {
            if ("[×=^]".toRegex() in number || numberOfX > 1 || mode > 0) return@setOnLongClickListener false
            judged = true
            val cards = mutableListOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            for (char in number) cards[ATJQKX(char)]++
            var searchResult = 0.toBigInteger()
            if (numberOfX > 0) {
                cards[14] = 0
                val three = (cards[1] + cards[2] * 2 + cards[4] + cards[5] * 2 + cards[7] + cards[8] * 2 + cards[10] + cards[11] * 2 + cards[13]) % 3
                val eleven = if (cards[0] + cards[1] + cards[2] + cards[3] + cards[4] + cards[5] + cards[6] + cards[7] + cards[8] + cards[9] != 0)
                    null else cards[13] * 2 + cards[12] - cards[10]
                for (i in 13 downTo 10) {
                    if (number.length > 1 && (three + i) % 3 == 0) {
                        continue
                    }
                    if (number.length > 1 && eleven != null && (eleven + i) % 11 == 0) {
                        continue
                    }
                    cards[i]++
                    val copy = mutableListOf<Int>()
                    copy.addAll(cards)
                    searchResult = max(permSearch(number.length, copy), searchResult)
                    cards[i]--
                }
                if (searchResult == 0.toBigInteger()) for (i in 9 downTo 0) {
                    cards[i]++
                    if (number.length > 1 && (three + i) % 3 == 0) {
                        cards[i]--
                        continue
                    }
                    val copy = mutableListOf<Int>()
                    copy.addAll(cards)
                    searchResult = max(permSearch(number.length, copy), searchResult)
                    cards[i]--
                }
            } else {
                if (number != "2" && number != "5" && cards[1] + cards[3] + cards[7] + cards[9] + cards[11] + cards[13] == 0) {
                    printResult(number, "は2または5の倍数です")
                    return@setOnLongClickListener true
                }
                if (number != "3" && (cards[1] + cards[2] * 2 + cards[4] + cards[5] * 2 + cards[7] + cards[8] * 2 + cards[10] + cards[11] * 2 + cards[13]) % 3 == 0) {
                    printResult(number, "は3の倍数です")
                    return@setOnLongClickListener true
                }
                if (number != "11" && cards[0] + cards[1] + cards[2] + cards[3] + cards[4] + cards[5] + cards[6] + cards[7] + cards[8] + cards[9] == 0
                        && cards[13] * 2 + cards[12] - cards[10] % 11 == 0) {
                    printResult(number, "は11の倍数です")
                    return@setOnLongClickListener true
                }
                searchResult = permSearch(number.length, cards)
            }
            if (searchResult == 0.toBigInteger()) printResult(number, "は素数になりません")
            else printResult(searchResult.toString(), "は素数です")
            true
        }

        findViewById<Button>(R.id.button1).setOnLongClickListener {
            if (mode > 0) false
            else {
                if (inputFormat < 1) press("1")
                else {
                    press("A")
                }
                true
            }
        }

        findViewById<Button>(R.id.button10).setOnLongClickListener {
            if (mode > 0) false
            else {
                if (inputFormat < 2) press("10")
                else {
                    press("T")
                }
                true
            }
        }

        findViewById<Button>(R.id.button11).setOnLongClickListener {
            if (mode > 0) false
            else {
                if (inputFormat < 3) press("11")
                else {
                    press("J")
                }
                true
            }
        }

        findViewById<Button>(R.id.button12).setOnLongClickListener {
            if (mode > 0) false
            else {
                if (inputFormat < 3) press("12")
                else {
                    press("Q")
                }
                true
            }
        }

        findViewById<Button>(R.id.button13).setOnLongClickListener {
            if (mode > 0) false
            else {
                if (inputFormat < 3) press("13")
                else {
                    press("K")
                }
                true
            }
        }
    }

    fun pressSetting(view: View) = startActivity(Intent(this, SettingsActivity::class.java))

    fun pressTimer(view: View) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getString("timerMode", "0") == "0") {
            startActivity(Intent(this, SettingsActivity::class.java))
            return
        }
        val editor = sharedPreferences.edit()
        if (sharedPreferences.getBoolean("startThinkingTime", false)) {
            editor.putBoolean("startThinkingTime", false)
        } else {
            editor.putInt("player", (sharedPreferences.getInt("player", 0) + 1) % sharedPreferences.getString("playersNumber", "2").toInt())
        }
        editor.apply()
        startActivity(Intent(this, TimerActivity::class.java))
    }

    fun pressPass(view: View) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getString("timerMode", "0") == "0") return
        val editor = sharedPreferences.edit()
        editor.putInt("player", (sharedPreferences.getInt("player", 0) + 1) % sharedPreferences.getString("playersNumber", "2").toInt())
        editor.apply()
        val string = "プレイヤー${(sharedPreferences.getInt("player", 0) + 65).toChar()} の手番です"
        resultText.text = string
    }

    private fun press(string: String) {
        if (judged) {
            judged = false
            number = "0"
            numberOfX = 0
        }
        if (joker) number += '('
        number += string
        while ("^0[0-9ATJQKX]|^[×^]".toRegex() in number) number = number.removeRange(0..0)
        if (joker) {
            number += ')'
            joker = false
        }
        if (number.isEmpty()) number = "0"
        judgeNumber.text = number
        resultText.text = resetText[mode]
    }

    fun press0(view: View) {
        if (number.lastOrNull() in listOf('×', '^')) pressDelete(view)
        press("0")
    }

    fun press1(view: View) {
        if (inputFormat < 1 || mode > 0) press("A")
        else {
            press("1")
        }
    }

    fun press2(view: View) {
        press("2")
    }

    fun press3(view: View) {
        press("3")
    }

    fun press4(view: View) {
        press("4")
    }

    fun press5(view: View) {
        press("5")
    }

    fun press6(view: View) {
        press("6")
    }

    fun press7(view: View) {
        press("7")
    }

    fun press8(view: View) {
        press("8")
    }

    fun press9(view: View) {
        press("9")
    }

    fun press10(view: View) {
        if (inputFormat < 2 || mode > 0) press("T")
        else press("10")
    }

    fun press11(view: View) {
        if (inputFormat < 3 || mode > 0) press("J")
        else press("11")
    }

    fun press12(view: View) {
        if (inputFormat < 3 || mode > 0) press("Q")
        else press("12")
    }

    fun press13(view: View) {
        if (inputFormat < 3 || mode > 0) press("K")
        else press("13")
    }

    fun pressMulti(view: View) {
        if (numberOfX > 1) {
            val toast = Toast.makeText(this, "cannot use X, X and × together", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.BOTTOM, 0, 16)
            toast.show()
            return
        }
        judged = false
        if (number.lastOrNull() in listOf('×', '^', '=')) pressDelete(view)
        press("×")
    }

    fun pressPow(view: View) {
        if (numberOfX > 1) {
            val toast = Toast.makeText(this, "cannot use X, X and ^ together", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.BOTTOM, 0, 16)
            toast.show()
            return
        }
        judged = false
        if (number.lastOrNull() in listOf('×', '^', '=')) pressDelete(view)
        press("^")
    }

    fun pressAny(view: View) {
        if (judged) {
            judged = false
            number = "0"
            numberOfX = 0
        }
        if (mode > 0) {
            if (number.lastOrNull() != '(') {
                joker = true
                press("X")
            }
        } else if (numberOfX < 3) {
            if ('=' in number) {
                val toast = Toast.makeText(this, "cannot use X in factorization mode", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.BOTTOM, 0, 16)
                toast.show()
                return
            }
            if (numberOfX > 0 && '×' in number) {
                val toast = Toast.makeText(this, "cannot use X, X and × together", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.BOTTOM, 0, 16)
                toast.show()
                return
            }
            if (numberOfX > 0 && '^' in number) {
                val toast = Toast.makeText(this, "cannot use X, X and ^ together", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.BOTTOM, 0, 16)
                toast.show()
                return
            }
            press("X")
            numberOfX++
        }
    }

    fun pressDelete(view: View) {
        if (number.lastOrNull() == 'X') numberOfX--
        else if (number.lastOrNull() in listOf('(', ')')) number = number.dropLast(1)
        number = number.dropLast(1)
        if (number.isEmpty()) number = "0"
        judged = false
        judgeNumber.text = number
        resultText.text = resetText[mode]
    }

    fun pressFactoring(view: View) {
        if ('=' in number) return
        if ("[×^X]".toRegex() in number) {
            val toast = Toast.makeText(this, "left side must not contain X, × or ^", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.BOTTOM, 0, 16)
            toast.show()
            return
        }
        judged = false
        press("=")
    }


    fun pressJudge(view: View) {
        if (number.lastOrNull() in listOf('×', '^', '=')) {
            val toast = Toast.makeText(this, "invalid calculation", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.BOTTOM, 0, 16)
            toast.show()
            return
        }
        judged = true
        val cal = "[×^]".toRegex() in number
        if (!cal && '=' in number) {
            val toast = Toast.makeText(this, "the number of factor must be more than 1", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.BOTTOM, 0, 16)
            toast.show()
            return
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (number in listOf("57", "X") && sharedPreferences.getString("timerMode", "0") != "0") {

            val editor = sharedPreferences.edit()
            editor.putInt("player", (sharedPreferences.getInt("player", 0) + sharedPreferences.getString("playersNumber", "2").toInt() - 1) % sharedPreferences.getString("playersNumber", "2").toInt())
            editor.apply()
        }
        if (!cal && numberOfX > 0) resultText.text = "は素数になりません"
        var lastNumberOfX = 0
        if (number.isNotEmpty() && number[number.length - 1] == 'X') {
            lastNumberOfX = 1
            if (number.length > 1 && number[number.length - 2] == 'X') {
                lastNumberOfX = 2
                if (number.length > 2 && number[number.length - 3] == 'X') {
                    lastNumberOfX = 2
                }
            }
        }
        val string = number.replace('A', '1').replace("T", "10")
                .replace("J", "11").replace("Q", "12").replace("K", "13")
        val sortX = sharedPreferences.getString("sortX", "0") == "0"
        when (numberOfX) {
            1 -> {
                for (i in 0..13) {
                    var rpString = string.replaceFirst("X", i.toString())
                    while ("[×^]0[^×^]".toRegex() in rpString) {
                        rpString = rpString.replaceFirst("^0", "^")
                        rpString = rpString.replaceFirst("×0", "×")
                    }
                    if (cal) calculate(rpString)
                    else {
                        if (lastNumberOfX > 0 && i == 0) rpString = rpString.dropLast(1)
                        judge(rpString)
                    }
                }
            }
            2 -> {
                val primes = mutableListOf<BigInteger>()
                for (i in 0..13) for (j in 0..13) {
                    var rpString = string.replaceFirst("X", i.toString()).replaceFirst("X", j.toString())
                    if (lastNumberOfX > 0 && j == 0) {
                        rpString = rpString.dropLast(1)
                        if (lastNumberOfX > 1 && i == 0) rpString = rpString.dropLast(1)
                    }
                    if (isPrime(rpString)) {
                        if (sortX) primes.add(rpString.toBigInteger())
                        else printResult(rpString, "は素数です")
                    }
                }
                if (sortX) for (i in primes.sorted()) printResult(i.toString(), "は素数です")
            }
            3 -> {
                val primes = mutableListOf<BigInteger>()
                for (i in 0..13) for (j in 0..13) for (k in 0..13) {
                    var rpString = string.replaceFirst("X", i.toString()).replaceFirst("X", j.toString()).replaceFirst("X", k.toString())
                    if (lastNumberOfX > 0 && k == 0) {
                        rpString = rpString.dropLast(1)
                        if (lastNumberOfX > 1 && j == 0) {
                            rpString = rpString.dropLast(1)
                            if (lastNumberOfX > 2 && i == 0) rpString = rpString.dropLast(1)
                        }
                    }
                    if (isPrime(rpString)) {
                        if (sortX) primes.add(rpString.toBigInteger())
                        else printResult(rpString, "は素数です")
                    }
                }
                if (sortX) for (i in primes.sorted()) printResult(i.toString(), "は素数です")
            }
            else -> {
                if (cal) calculate(string)
                else judge(string)
            }
        }
    }

    private fun check(n: Int, list: MutableList<Int>, cnt: String): Boolean {
        if (n == 0) return true
        if (list[1] + list[3] + list[7] + list[9] + list[11] + list[13] == 0) return false
        var eleven = true
        for (i in 0..9) if (list[i] != 0) {
            eleven = false
            break
        }
        if (eleven) {
            var eleven = 0
            var i = cnt.length and 1
            for (char in cnt) {
                if (i == 1) eleven += char - '0'
                else eleven -= char - '0'
                i = i xor 1
            }
            if ((eleven - list[10] + list[12] + list[13] * 2) % 11 == 0) return false
        }
        return true
    }

    private fun permSearch(n: Int, list: MutableList<Int>, cnt: BigInteger = 0.toBigInteger(), one: Boolean = false): BigInteger {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val priorityQueue = PriorityQueue<Triple<Int, MutableList<Int>, String>> { a, b ->
                for (i in 0 until min(a.third.length, b.third.length)) if (a.third[i] != b.third[i]) return@PriorityQueue b.third[i].toInt() - a.third[i].toInt()
                return@PriorityQueue a.third.length - b.third.length
            }
            priorityQueue.add(Triple(n, list, ""))
            while (!priorityQueue.isEmpty()) {
                val (n, list, cnt) = priorityQueue.poll()
                if (n == 0) {
                    if (isPrime(cnt)) return cnt.toBigInteger()
                    continue
                }
                if (list[0] != 0) {
                    list[0]--
                    if (check(n - 1, list, cnt + "0")) {
                        val copy = mutableListOf<Int>()
                        copy.addAll(list)
                        priorityQueue.add(Triple(n - 1, copy, cnt + "0"))
                    }
                    list[0]++
                }
                for (i in 2..10) if (list[i] != 0) {
                    list[i]--
                    if (check(n - 1, list, cnt + i.toString())) {
                        val copy = mutableListOf<Int>()
                        copy.addAll(list)
                        priorityQueue.add(Triple(n - 1, copy, cnt + i.toString()))
                    }
                    list[i]++
                }
                for (i in 12..13) if (list[i] != 0) {
                    list[i]--
                    if (check(n - 1, list, cnt + i.toString())) {
                        val copy = mutableListOf<Int>()
                        copy.addAll(list)
                        priorityQueue.add(Triple(n - 1, copy, cnt + i.toString()))
                    }
                    list[i]++
                }
                if (cnt.lastOrNull() != '1') {
                    if (list[1] == 0) {
                        var cnt = cnt
                        var i = 0
                        if (list[11] != 0) while (list[11] > 0) {
                            list[11]--
                            i++
                            cnt += "11"
                            if (check(n - i, list, cnt)) {
                                val copy = mutableListOf<Int>()
                                copy.addAll(list)
                                priorityQueue.add(Triple(n - i, copy, cnt))
                            }
                        }
                    } else {
                        var i = 0
                        var n = n
                        var cnt = cnt
                        while (list[1] + list[11] != 0) {
                            if (i == 0) {
                                list[1]--
                                n--
                            } else {
                                if (list[11] != 0) {
                                    list[11]--
                                    list[1]++
                                } else {
                                    list[1]--
                                    n--
                                }
                            }
                            cnt += "1"
                            if (check(n, list, cnt)) {
                                val copy = mutableListOf<Int>()
                                copy.addAll(list)
                                priorityQueue.add(Triple(n, copy, cnt))
                            }
                            i = i xor 1
                        }
                    }
                }
            }
        } else {
            if (n == 0) return if (isPrimeB(cnt)) cnt else 0.toBigInteger()
            if (n == 1) for (i in 0..13) if (list[i] != 0 && (i !in listOf(1, 11) || !one)) return permSearch(0, mutableListOf(), connect(cnt, i))
            if (list[1] + list[3] + list[7] + list[9] + list[11] + list[13] == 0) return 0.toBigInteger()
            var eleven = true
            for (i in 0..9) if (list[i] != 0) {
                eleven = false
                break
            }
            if (eleven && ((cnt % 11.toBigInteger()).toInt() - list[10] + list[12] + list[13] * 2) % 11 == 0) return 0.toBigInteger()
            var a = 0.toBigInteger()
            for (i in listOf(9, 8, 7, 6, 5, 4, 3, 2, 19, 18, 17, 16, 15, 14, 13, 12, 1, 10, 0)) {
                if (i == 1) {
                    if (one) continue
                    if (list[11] == 0) {
                        if (list[1] != 0) {
                            list[1]--
                            a = permSearch(n - 1, list, connect(cnt, 1))
                            list[1]++
                        }
                    } else if (list[1] == 0) {
                        list[11]--
                        a = permSearch(n - 1, list, connect(cnt, 11))
                        list[11]++
                    } else {
                        val one = list[1]
                        val eleven = list[11]
                        var i = 0
                        var cnt = cnt
                        var n = n
                        while (list[1] + list[11] != 0) {
                            if (i == 0) {
                                list[1]--
                                n--
                            } else {
                                if (list[11] != 0) {
                                    list[11]--
                                    list[1]++
                                } else {
                                    list[1]--
                                    n--
                                }
                            }
                            cnt = connect(cnt, 1)
                            a = max(permSearch(n, list, cnt, true), a)
                            i = i xor 1
                        }
                        list[1] = one
                        list[11] = eleven
                    }
                    if (a != 0.toBigInteger()) return a
                } else if (i < 14 && list[i] > 0) {
                    list[i]--
                    a = permSearch(n - 1, list, connect(cnt, i))
                    if (a != 0.toBigInteger()) return a
                    list[i]++
                } else if (i > 9 && list[1] > 0 && list[i - 10] > 0) {
                    list[1]--
                    list[i - 10]--
                    a = permSearch(n - 2, list, connect(cnt, i))
                    if (a != 0.toBigInteger()) return a
                    list[1]++
                    list[i - 10]++
                }
            }
        }
        return 0.toBigInteger()
    }

    private fun connect(a: BigInteger, b: Int): BigInteger {
        return if (b < 10) a * 10.toBigInteger() + b.toBigInteger()
        else a * 100.toBigInteger() + b.toBigInteger()
    }

    private fun max(a: BigInteger, b: BigInteger) = if (a > b) a else b

    private fun calculate(string: String) {
        val factorList: MutableList<BigInteger> = string.split("[=×^]".toRegex()).asSequence().map { it.toBigInteger() }.toMutableList()
        val multiSign = mutableListOf(true)
        for (i in number) when (i) {
            '=' -> multiSign.add(true)
            '×' -> multiSign.add(true)
            '^' -> multiSign.add(false)
        }
        if ('=' in number) {
            for (i in 1 until factorList.size) {
                if (multiSign[i]) {
                    if (!isPrimeB(factorList[i])) {
                        printResult(string, "は正しくないです")
                        val result = factorList[i].toString() + " は素因数ではありません"
                        resultText.text = result
                        return
                    }
                } else {
                    if (factorList[i] == 1.toBigInteger()) {
                        printResult(string, "は正しくないです")
                        resultText.text = "指数に1は使えません"
                        return
                    }
                    if (factorList[i] == 0.toBigInteger()) {
                        printResult(string, "は正しくないです")
                        resultText.text = "指数に0は使えません"
                        return
                    }
                }
            }
            for (i in multiSign.size - 1 downTo 2) if (!multiSign[i]) {
                if (log10(factorList[i - 1]) * factorList[i].toDouble() > 400) {
                    printResult(string, "は正しくないです")
                    resultText.text = "等式が成立していません"
                    return
                }
                factorList[i - 1] = factorList[i - 1].pow(factorList[i].toInt())
                factorList.removeAt(i)
                multiSign.removeAt(i)
            }
            for (i in multiSign.size - 1 downTo 2) {
                factorList[i - 1] *= factorList[i]
            }
            if (factorList[0] == factorList[1]) {
                printResult(string, "は正しいです")
            } else {
                printResult(string, "は正しくないです")
                resultText.text = "等式が成立していません"
            }
        } else {
            for (i in multiSign.size - 1 downTo 1) if (!multiSign[i]) {
                if (log10(factorList[i - 1]) * factorList[i].toDouble() > 100) {
                    printResult(string, "= TOO BIG")
                    return
                }
                factorList[i - 1] = factorList[i - 1].pow(factorList[i].toInt())
                factorList.removeAt(i)
                multiSign.removeAt(i)
            }
            for (i in multiSign.size - 1 downTo 1) {
                factorList[i - 1] *= factorList[i]
            }
            printResult(string, "= " + factorList[0].toString())
        }
    }

    private fun judge(string: String) {
        var string = string
        while (string.firstOrNull() == '0') string = string.replaceFirst("0", "")
        if (string.isEmpty()) string = "0"
        printResult(string,
                when {
                    string == "0" -> "は加法単位元です"
                    string == "1" -> "は乗法単位元です"
                    string == "57" -> "はグロタンディーク素数です"
                    string == "1729" -> "はラマヌジャンのタクシー数です"
                    isPrime(string) -> "は素数です"
                    numberOfX == 0 && maxFactorX == 0 -> "は素数ではありません"
                    maxFactorX <= numberOfX -> return
                    else -> convertString(factor(string.toBigInteger()))
                }
        )
    }

    private fun convertString(factor: Pair<SortedMap<BigInteger, Int>, Boolean>): String {
        var ans = "= "
        factor.first.forEach {
            ans += it.key
            if (it.value > 1) ans += "^" + it.value.toString()
            ans += " × "
        }
        ans = ans.dropLast(3)
        if (factor.second) ans += "(TO)"
        return ans
    }

    private fun factor(bigint: BigInteger): Pair<SortedMap<BigInteger, Int>, Boolean> {
        val ans = sortedMapOf<BigInteger, Int>()
        var bigint = bigint
        val start = System.currentTimeMillis()
        val primes = listOf(2.toBigInteger(), 3.toBigInteger(), 5.toBigInteger(), 7.toBigInteger(), 11.toBigInteger(), 13.toBigInteger(), 17.toBigInteger(), 19.toBigInteger())
        var timeout = false
        for (i in primes) {
            while (bigint % i == 0.toBigInteger()) {
                bigint /= i
                ans[i] = (ans[i] ?: 0) + 1
            }
        }
        while (!timeout && bigint > 1.toBigInteger() && !bigint.isProbablePrime(100)) {
            val rho = rho(bigint, start)
            if (rho < 2.toBigInteger()) timeout = true
            else {
                bigint /= rho
                if (rho.isProbablePrime(100)) ans[rho] = (ans[rho] ?: 0) + 1
                else factor(rho).first.forEach { ans[it.key] = (ans[it.key] ?: 0) + it.value }
            }
        }
        if (bigint != 1.toBigInteger()) ans[bigint] = (ans[bigint] ?: 0) + 1
        return Pair(ans, timeout)
    }

    private fun printResult(num: String, string: String) {
        resultText.text = string
        var boxText = num + " " + string + "\n" + resultBox.text
        if (boxText.lastOrNull() == '\n') boxText = boxText.dropLast(1)
        resultBox.text = boxText
    }

    private fun isPrime(string: String) = isPrimeB(string.toBigInteger())

    private fun isPrimeB(bigint: BigInteger): Boolean {
        if (bigint < 2.toBigInteger()) return false
        val primes = listOf(2, 3, 5, 7, 11, 13)
        for (i in primes) {
            if (bigint < (i * i).toBigInteger()) return true
            if (bigint % i.toBigInteger() == 0.toBigInteger()) return false
        }
        return bigint.isProbablePrime(100)
    }

    private fun log10(b: BigInteger): Double {
        if (b == 0.toBigInteger()) return 0.0
        var a = b.toBigDecimal()
        var cnt = 0
        while (a >= 10.toBigDecimal()) {
            a /= 10.toBigDecimal()
            cnt++
        }
        return kotlin.math.log10(a.toDouble()) + cnt
    }

    @Suppress("FunctionName")
    private fun ATJQKX(char: Char): Int = when (char) {
        'A' -> 1
        'T' -> 10
        'J' -> 11
        'Q' -> 12
        'K' -> 13
        'X' -> 14
        else -> char - '0'
    }

    private fun rho(N: BigInteger, start: Long): BigInteger {
        val random = SecureRandom()
        var divisor: BigInteger
        var c = BigInteger(N.bitLength(), random) % N
        var x = 2.toBigInteger()
        var xx = 2.toBigInteger()
        do {
            x = (x * x + c) % N
            xx = (xx * xx + c) % N
            xx = (xx * xx + c) % N
            divisor = (x - xx).abs().gcd(N)
            if (x == xx) {
                c = BigInteger(N.bitLength(), random)
                x = 2.toBigInteger()
                xx = 2.toBigInteger()
            }
            if (System.currentTimeMillis() - start > times[numberOfX]) return 0.toBigInteger()
        } while (divisor == 1.toBigInteger() || divisor == N)
        return divisor
    }
}
