package tatyam.me.qkdealerplus

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.content.Intent



class TimerActivity : AppCompatActivity() {

    var timerMode = 1
    var moveTime = 60
    var playerTime = 60
    var thinkingTime = 60
    var playersNumber = 2
    var vibration = true
    var startThinkingTime = false
    var player = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        val intent = intent
        timerMode = intent.getIntExtra("timerMode",1)
        moveTime = intent.getIntExtra("moveTime",60)
        playerTime = intent.getIntExtra("playerTime",60)
        thinkingTime = intent.getIntExtra("thinkingTime",60)
        playersNumber = intent.getIntExtra("playersNumber",60)
        vibration = intent.getBooleanExtra("vibration",true)
        startThinkingTime = intent.getBooleanExtra("startThinkingTime",false)
        player = intent.getIntExtra("player", 0)
    }
}
