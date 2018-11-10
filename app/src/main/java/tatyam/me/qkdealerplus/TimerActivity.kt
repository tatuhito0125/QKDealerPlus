package tatyam.me.qkdealerplus

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.*
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_timer.*
import kotlin.math.ceil


class TimerActivity : AppCompatActivity() {

    private var timerMode = 1
    private var moveTime = 60
    private var playerTime = 60
    private var thinkingTime = 60
    private var vibration = true
    private var startThinkingTime = false
    private var player = 0
    private var bigSize = 120F
    private var smallSize = 60F
    private var timer1: CountDownTimer? = null
    private var timer2: CountDownTimer? = null
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
        vibration = sharedPreferences.getBoolean("vibration", true)
        startThinkingTime = sharedPreferences.getBoolean("startThinkingTime", false)
        if (this.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            bigSize = 160F
            smallSize = 80F
        }
        val string = moveTime.toString() + ".0"
        timeA.text = string
        val holder = playerTime.toString() + ".0"
        timeB.text = holder
        if (startThinkingTime) {
            timer1 = object: CountDownTimer(thinkingTime * 1000L, 100){
                override fun onTick(l: Long){
                    timeA.text = String.format("%.1f", l / 1000.0)
                    if(l < 6000 && l / 100 % 10 == 0L && vibration) vibrator.vibrate(150)
                }
                override fun onFinish() {
                    if (vibration) vibrator.vibrate(300)
                    timeA.text = "0.0"
                }
            }
            textPlayer.text = "シンキングタイムです"
            timeA.textSize = bigSize
            timeB.textSize = 0F
            timerMode = 1
            timer1!!.start()
        } else {
            val string = "プレイヤー${player + 1} の手番です"
            textPlayer.text = string
            when(timerMode){
                1 -> {
                    timer1 = object: CountDownTimer(moveTime * 1000L, 100){
                        override fun onTick(l: Long){
                            timeA.text = String.format("%.1f", l / 1000.0)
                            if(l < 6000 && l / 100 % 10 == 0L && vibration) vibrator.vibrate(150)
                        }
                        override fun onFinish() {
                            if (vibration) vibrator.vibrate(300)
                            timeA.text = "0.0"
                        }
                    }
                    textPlayer.text = ""
                    timeA.textSize = bigSize
                    timeB.textSize = 0F
                    timer1!!.start()
                }
                2 -> {
                    timer1 = object: CountDownTimer(moveTime * 1000L, 100){
                        override fun onTick(l: Long){
                            timeA.text = String.format("%.1f", l / 1000.0)
                            if(l < 6000 && l / 100 % 10 == 0L && vibration) {
                                vibrator.vibrate(150)
                            }
                        }
                        override fun onFinish() {
                            if (vibration) vibrator.vibrate(300)
                            timer2 = object: CountDownTimer(playerTime * 1000L, 100){
                                override fun onTick(l: Long){
                                    timeB.text = String.format("%.1f", l / 1000.0)
                                    if(l < 6000 && l / 100 % 10 == 0L && vibration) {
                                        vibrator.vibrate(150)
                                    }
                                }
                                override fun onFinish() {
                                    if (vibration) vibrator.vibrate(300)
                                    timeB.text = "0.0"
                                }
                            }
                            timeA.text = "0.0"
                            timeA.textSize = smallSize
                            timeB.textSize = bigSize
                            timer2!!.start()
                        }
                    }
                    timeA.textSize = bigSize
                    timeB.textSize = smallSize
                    timer1!!.start()
                }
                3 -> {
                    timer1 = object: CountDownTimer(playerTime * 1000L, 100){
                        override fun onTick(l: Long){
                            timeB.text = String.format("%.1f", l / 1000.0)
                            if(l < 6000 && l / 100 % 10 == 0L && vibration) vibrator.vibrate(150)
                        }
                        override fun onFinish() {
                            if (vibration) vibrator.vibrate(300)
                            timer2 = object: CountDownTimer(moveTime * 1000L, 100){
                                override fun onTick(l: Long){
                                    timeA.text = String.format("%.1f", l / 1000.0)
                                    if(l < 6000 && l / 100 % 10 == 0L && vibration) vibrator.vibrate(150)
                                }
                                override fun onFinish() {
                                    if (vibration) vibrator.vibrate(300)
                                    timeA.text = "0.0"
                                }
                            }
                            timeB.text = "0.0"
                            timeA.textSize = bigSize
                            timeB.textSize = smallSize
                            timer2!!.start()
                        }
                    }
                    timeA.textSize = smallSize
                    timeB.textSize = bigSize
                    timer1!!.start()
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        timer1?.cancel()
        timer2?.cancel()
        if(!startThinkingTime) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = sharedPreferences.edit()
            editor.putInt("timePlayer$player", ceil(timeB.text.toString().toDouble()).toInt())
            editor.apply()
            if(timerMode > 1) {
                val intent = Intent("setPlayer")
                intent.putExtra("setText", "プレイヤー${player + 1} の手番です")
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
        }
    }
}
