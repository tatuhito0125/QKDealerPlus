package tatyam.me.qkdealerplus

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.*
import android.support.v4.content.LocalBroadcastManager
import android.view.MenuItem
import android.view.View


/**
 * A [PreferenceActivity] that presents a set of application tings. On
 * handset devices, tings are presented as a single list. On tablets,
 * tings are split by category, with category headers shown to the left of
 * the list of tings.
 *
 * See [Android Design: tings](http://developer.android.com/design/patterns/tings.html)
 * for design guidelines and the [tings API Guide](http://developer.android.com/guide/topics/ui/tings.html)
 * for more information on developing a tings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity() {
    private lateinit var localBroadcastManager: LocalBroadcastManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
    }

    fun startTimer(view: View) {
        val intent = Intent(this, TimerActivity::class.java)
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferenceManager.edit()
        if (preferenceManager.getString("thinkingTime", "60") != "0") editor.putBoolean("startThinkingTime", true)
        for (i in 0..6) editor.putInt("timePlayer$i", preferenceManager.getString("playerTime", "60").toInt())
        editor.putInt("player", 0)
        editor.apply()
        startActivity(intent)
        finish()
    }

    fun copyBox(view: View) = LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("copyBox"))
    fun clearBox(view: View) = LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("clearBox"))

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * {@inheritDoc}
     */
    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
                || TimerPreferenceFragment::class.java.name == fragmentName || SettingsPreferenceFragment::class.java.name == fragmentName
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class TimerPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_timer)
            setHasOptionsMenu(true)
            val timerMode = findPreference("timerMode") as ListPreference
            val moveTime = findPreference("moveTime") as EditTextPreference
            val playerTime = findPreference("playerTime") as EditTextPreference
            val thinkingTime = findPreference("thinkingTime") as EditTextPreference
            val playersNumber = findPreference("playersNumber") as ListPreference
            val vibration = findPreference("vibration") as SwitchPreference
            val startTimer = findPreference("startTimer")
            moveTime.summary = moveTime.text + "秒"
            playerTime.summary = playerTime.text + "秒"
            thinkingTime.summary = thinkingTime.text + "秒"

            moveTime.isEnabled = timerMode.value.toInt() > 0
            playerTime.isEnabled = timerMode.value.toInt() > 1
            thinkingTime.isEnabled = timerMode.value.toInt() > 0
            playersNumber.isEnabled = timerMode.value.toInt() > 1
            vibration.isEnabled = timerMode.value.toInt() > 0
            startTimer.isEnabled = timerMode.value.toInt() > 0
            
            timerMode.setOnPreferenceChangeListener { _, newValue ->
                moveTime.isEnabled = newValue.toString().toInt() > 0
                playerTime.isEnabled = newValue.toString().toInt() > 1
                thinkingTime.isEnabled = newValue.toString().toInt() > 0
                playersNumber.isEnabled = newValue.toString().toInt() > 1
                vibration.isEnabled = newValue.toString().toInt() > 0
                startTimer.isEnabled = newValue.toString().toInt() > 0
                true
            }

            moveTime.setOnPreferenceChangeListener { preference, newValue ->
                if (newValue == "0") false else {
                    preference.summary = newValue.toString() + "秒"
                    true
                }
            }

            playerTime.setOnPreferenceChangeListener { preference, newValue ->
                if (newValue == "0") false else {
                    preference.summary = newValue.toString() + "秒"
                    true
                }
            }

            thinkingTime.setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue.toString() + "秒"
                true
            }
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class SettingsPreferenceFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_settings)
            setHasOptionsMenu(true)
            findPreference("showResultBox").setOnPreferenceChangeListener { _, newValue ->
                val intent = Intent("showResultBox")
                intent.putExtra("showResultBox", newValue as Boolean)
                LocalBroadcastManager.getInstance(activity).sendBroadcast(intent)
                true
            }
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()

            if (preference is ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                val index = preference.findIndexOfValue(stringValue)

                // Set the summary to reflect the new value.
                preference.setSummary(
                        if (index >= 0)
                            preference.entries[index]
                        else
                            null)

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.summary = stringValue
            }
            true
        }

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

    }
}
