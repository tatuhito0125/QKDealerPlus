package tatyam.me.qkdealerplus

import android.content.Context
import android.content.Intent
import android.os.*
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_timer.*


class TimerActivity : AppCompatActivity() {

    private var timerMode = 1
    private var moveTime = 60
    private var playerTime = 60
    private var thinkingTime = 60
    private var vibration = true
    private var startThinkingTime = false
    private var player = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        timerMode = sharedPreferences.getString("timerMode", "1").toInt()
        moveTime = sharedPreferences.getString("moveTime", "60").toInt()
        player = sharedPreferences.getInt("player", 0)
        playerTime = sharedPreferences.getInt("timePlayer$player", sharedPreferences.getString("playerTime", "60").toInt())
        thinkingTime = sharedPreferences.getString("thinkingTime", "60").toInt()
        vibration = intent.getBooleanExtra("vibration", true)
        startThinkingTime = sharedPreferences.getBoolean("startThinkingTime", false)
        val string = moveTime.toString() + ".0"
        timeA.text = string
        val holder = playerTime.toString() + ".0"
        timeB.text = holder
        if (startThinkingTime) {
            val countDown = object: CountDownTimer(thinkingTime * 1000L, 100){
                override fun onTick(l: Long){
                    timeA.text = String.format("%.1f", l / 1000.0)
                    if(l < 6000 && l / 100 % 10 == 0L && vibration) vibrator.vibrate(150)
                }
                override fun onFinish() {
                    vibrator.vibrate(300)
                    timeA.text = "0.0"
                }
            }
            textPlayer.text = "シンキングタイムです"
            timeA.textSize = 120F
            timeB.textSize = 0F
            timerMode = 1
            countDown.start()
        } else {
            val string = "プレイヤー${player + 1} の手番です"
            textPlayer.text = string
            when(timerMode){
                1 -> {
                    val countDown = object: CountDownTimer(moveTime * 1000L, 100){
                        override fun onTick(l: Long){
                            timeA.text = String.format("%.1f", l / 1000.0)
                            if(l < 6000 && l / 100 % 10 == 0L && vibration) vibrator.vibrate(150)
                        }
                        override fun onFinish() {
                            vibrator.vibrate(300)
                            timeA.text = "0.0"
                        }
                    }
                    textPlayer.text = ""
                    timeA.textSize = 120F
                    timeB.textSize = 0F
                    countDown.start()
                }
                2 -> {
                    val countDown = object: CountDownTimer(moveTime * 1000L, 100){
                        override fun onTick(l: Long){
                            timeA.text = String.format("%.1f", l / 1000.0)
                            if(l < 6000 && l / 100 % 10 == 0L && vibration) vibrator.vibrate(150)
                        }
                        override fun onFinish() {
                            vibrator.vibrate(300)
                            val countDown = object: CountDownTimer(playerTime * 1000L, 100){
                                override fun onTick(l: Long){
                                    timeB.text = String.format("%.1f", l / 1000.0)
                                    if(l < 6000 && l / 100 % 10 == 0L && vibration) vibrator.vibrate(150)
                                }
                                override fun onFinish() {
                                    vibrator.vibrate(300)
                                    timeB.text = "0.0"
                                }
                            }
                            timeA.text = "0.0"
                            timeA.textSize = 60F
                            timeB.textSize = 120F
                            countDown.start()
                        }
                    }
                    timeA.textSize = 120F
                    timeB.textSize = 60F
                    countDown.start()
                }
                3 -> {
                    val countDown = object: CountDownTimer(playerTime * 1000L, 100){
                        override fun onTick(l: Long){
                            timeB.text = String.format("%.1f", l / 1000.0)
                            if(l < 6000 && l / 100 % 10 == 0L && vibration) vibrator.vibrate(150)
                        }
                        override fun onFinish() {
                            vibrator.vibrate(300)
                            val countDown = object: CountDownTimer(moveTime * 1000L, 100){
                                override fun onTick(l: Long){
                                    timeA.text = String.format("%.1f", l / 1000.0)
                                    if(l < 6000 && l / 100 % 10 == 0L && vibration) vibrator.vibrate(150)
                                }
                                override fun onFinish() {
                                    vibrator.vibrate(300)
                                    timeA.text = "0.0"
                                }
                            }
                            timeB.text = "0.0"
                            timeA.textSize = 120F
                            timeB.textSize = 60F
                            countDown.start()
                        }
                    }
                    timeA.textSize = 60F
                    timeB.textSize = 120F
                    countDown.start()
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if(!startThinkingTime) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = sharedPreferences.edit()
            editor.putInt("timePlayer$player", if(timeB.text == "0.0") 0 else timeB.text.toString().toDouble().toInt() + 1)
            editor.apply()
            if(timerMode > 1) {
                val intent = Intent("setPlayer")
                intent.putExtra("setText", "プレイヤー${player + 1} の手番です")
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
        }
    }
}
