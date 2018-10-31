package tatyam.me.qkdealerplus

import android.content.*
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


class MainActivity : AppCompatActivity() {
    private var mode = 0
    private var inputFormat = 1
    private var numberOfX = 0
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
                    "showResultBox" -> resultBox.visibility = if(intent.getBooleanExtra( "showResultBox", true)) View.VISIBLE else View.INVISIBLE
                }
            }
        }
        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(broadcastReceiver, IntentFilter("setPlayer"))
        localBroadcastManager.registerReceiver(broadcastReceiver, IntentFilter("copyBox"))
        localBroadcastManager.registerReceiver(broadcastReceiver, IntentFilter("clearBox"))

        findViewById<Button>(R.id.buttonDelete).setOnLongClickListener {
            judged = false
            number = "0"
            numberOfX = 0
            judgeNumber.text = number
            resultText.text = resetText[mode]
            true
        }

        findViewById<Button>(R.id.buttonJudge).setOnLongClickListener {
            if ("[×=^]".toRegex() in number || numberOfX > 1) return@setOnLongClickListener false
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
                    cards[i]++
                    if (number.length > 1 && (three + i) % 3 == 0) {
                        cards[i]--
                        continue
                    }
                    if (number.length > 1 && eleven != null && (eleven + i) % 11 == 0) {
                        cards[i]--
                        continue
                    }
                    searchResult = max(permSearch(number.length, cards, 0.toBigInteger()), searchResult)
                    cards[i]--
                }
                if (searchResult == 0.toBigInteger()) for (i in 9 downTo 0) {
                    cards[i]++
                    if (number.length > 1 && (three + i) % 3 == 0) {
                        cards[i]--
                        continue
                    }
                    searchResult = max(permSearch(number.length, cards, 0.toBigInteger()), searchResult)
                    cards[i]--
                }
            } else {
                if (number != "3" && (cards[1] + cards[2] * 2 + cards[4] + cards[5] * 2 + cards[7] + cards[8] * 2 + cards[10] + cards[11] * 2 + cards[13]) % 3 == 0) {
                    printResult(number, "は3の倍数です")
                    return@setOnLongClickListener true
                }
                if (number != "11" && cards[0] + cards[1] + cards[2] + cards[3] + cards[4] + cards[5] + cards[6] + cards[7] + cards[8] + cards[9] == 0
                        && cards[13] * 2 + cards[12] - cards[10] % 11 == 0) {
                    printResult(number, "は11の倍数です")
                    return@setOnLongClickListener true
                }
                searchResult = permSearch(number.length, cards, 0.toBigInteger())
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

    fun pressTimer(view: View){
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getString("timerMode", "0") == "0") {
            startActivity(Intent(this, SettingsActivity::class.java))
            return
        }
        val editor = sharedPreferences.edit()
        if (sharedPreferences.getBoolean("startThinkingTime", false)) {
            editor.putBoolean("startThinkingTime", false)
        }
        else {
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
        val string = "プレイヤー${sharedPreferences.getInt("player", 0) + 1} の手番です"
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
        if (number in listOf("57", "X") && sharedPreferences.getString("timerMode", "0") != "0"){

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

    private fun permSearch(n: Int, list: MutableList<Int>, cnt: BigInteger): BigInteger {
        if (n == 0) return if (isPrimeB(cnt)) cnt else 0.toBigInteger()
        if (n == 1) for (i in 0..13) if (list[i] > 0) return permSearch(0, mutableListOf(), connect(cnt, i))
        if (list[1] + list[3] + list[7] + list[9] + list[11] + list[13] == 0) return 0.toBigInteger()
        var eleven = true
        for (i in 0..9) if (list[i] != 0) {
            eleven = false
            break
        }
        if (eleven && ((cnt % 11.toBigInteger()).toInt() - list[10] + list[12] + list[13]) % 11 == 0) return 0.toBigInteger()
        var a = 0.toBigInteger()
        for (i in listOf(9, 8, 7, 6, 5, 4, 3, 2, 19, 18, 17, 16, 15, 14, 13, 12, 1, 10, 0)) {
            if (i == 1) {
                if (list[1] > 0) {
                    list[1]--
                    a = permSearch(n - 1, list, connect(cnt, 1))
                    list[1]++
                }
                if (list[11] > 0) {
                    list[11]--
                    a = max(permSearch(n - 1, list, connect(cnt, 11)), a)
                    list[11]++
                }
                if (a != 0.toBigInteger()) return a
            }
            if (i < 14 && list[i] > 0) {
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
                    else -> factor(string)
                }
        )
    }

    private fun factor(string: String): String {
        var ans = ""
        val times = listOf(5000, 500, 50, 5)
        var bigint = string.toBigInteger()
        val start = System.currentTimeMillis()
        var cnt = 0
        var i = 2.toBigInteger()
        while (bigint % i == 0.toBigInteger()) {
            bigint /= i
            cnt++
        }
        if (cnt > 0) {
            ans += " × " + i.toString()
            if (cnt > 1) ans += "^" + cnt.toString()
        }
        if (bigint.isProbablePrime(100)) {
            ans += " × " + bigint.toString()
            return ans.replaceFirst(" ×", "=")
        }
        if (bigint == 1.toBigInteger()) return ans.replaceFirst(" ×", "=")
        cnt = 0
        i = 3.toBigInteger()
        while (bigint % i == 0.toBigInteger()) {
            bigint /= i
            cnt++
        }
        if (cnt > 0) {
            ans += " × " + i.toString()
            if (cnt > 1) ans += "^" + cnt.toString()
        }
        if (bigint.isProbablePrime(100)) {
            ans += " × " + bigint.toString()
            return ans.replaceFirst(" ×", "=")
        }
        if (bigint == 1.toBigInteger()) return ans.replaceFirst(" ×", "=")
        i = 5.toBigInteger()
        while (!bigint.isProbablePrime(100)) {
            cnt = 0
            while (bigint % i == 0.toBigInteger()) {
                bigint /= i
                cnt++
            }
            if (cnt > 0) {
                ans += " × " + i.toString()
                if (cnt > 1) ans += "^" + cnt.toString()
            }
            if (bigint == 1.toBigInteger()) return ans.replaceFirst(" ×", "=")
            val end = System.currentTimeMillis()
            if (end - start > times[numberOfX]) {
                ans += " × " + bigint.toString() + "(TO)"
                return ans.replaceFirst(" ×", "=")
            }
            i += 4.toBigInteger() - (i % 6.toBigInteger()).shr(1)
        }
        ans += " × " + bigint.toString()
        return ans.replaceFirst(" ×", "=")
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
        val primes = listOf(2, 3, 5, 7, 11, 13 )
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

    private fun ATJQKX(char: Char): Int = when (char) {
        'A' -> 1
        'T' -> 10
        'J' -> 11
        'Q' -> 12
        'K' -> 13
        'X' -> 14
        else -> char - '0'
    }
}
