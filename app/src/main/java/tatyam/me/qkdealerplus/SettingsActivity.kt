package tatyam.me.qkdealerplus

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.*
import android.view.MenuItem
import android.view.View

/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
    }
    fun startTimer(view: View){
        val intent = Intent(this, TimerActivity::class.java)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        intent.putExtra("timerMode",sharedPreferences.getString("setTimerMode","1").toInt())
        intent.putExtra("moveTime",sharedPreferences.getString("setMoveTime","60").toInt())
        intent.putExtra("playerTime",sharedPreferences.getString("setPlayerTime","60").toInt())
        intent.putExtra("thinkingTime",sharedPreferences.getString("setThinkingTime","60").toInt())
        intent.putExtra("playersNumber",sharedPreferences.getString("setPlayersNumber","2").toInt())
        intent.putExtra("vibration",sharedPreferences.getBoolean("setVibration",true))
        intent.putExtra("startThinkingTime",sharedPreferences.getString("setThinkingTime","60") != "0")
        startActivity(intent)
        finish()
    }

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
                || TimerPreferenceFragment::class.java.name == fragmentName
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class TimerPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_timer)
            setHasOptionsMenu(true)
            val setTimerMode = findPreference("setTimerMode") as ListPreference
            val setMoveTime = findPreference("setMoveTime") as EditTextPreference
            val setPlayerTime = findPreference("setPlayerTime") as EditTextPreference
            val setThinkingTime = findPreference("setThinkingTime") as EditTextPreference
            val setPlayersNumber = findPreference("setPlayersNumber") as ListPreference
            val setVibration = findPreference("setVibration") as SwitchPreference
            val startTimer = findPreference("startTimer")
            setMoveTime.summary = setMoveTime.text + "秒"
            setPlayerTime.summary = setPlayerTime.text + "秒"
            setThinkingTime.summary = setThinkingTime.text + "秒"

            setMoveTime.isEnabled = setTimerMode.value.toInt() > 0
            setPlayerTime.isEnabled = setTimerMode.value.toInt() > 1
            setThinkingTime.isEnabled = setTimerMode.value.toInt() > 0
            setPlayersNumber.isEnabled = setTimerMode.value.toInt() > 1
            setVibration.isEnabled = setTimerMode.value.toInt() > 0
            startTimer.isEnabled = setTimerMode.value.toInt() > 0
            
            setTimerMode.setOnPreferenceChangeListener { _, newValue ->
                setMoveTime.isEnabled = newValue.toString().toInt() > 0
                setPlayerTime.isEnabled = newValue.toString().toInt() > 1
                setThinkingTime.isEnabled = newValue.toString().toInt() > 0
                setPlayersNumber.isEnabled = newValue.toString().toInt() > 1
                setVibration.isEnabled = newValue.toString().toInt() > 0
                startTimer.isEnabled = newValue.toString().toInt() > 0
                true
            }

            setMoveTime.setOnPreferenceChangeListener { preference, newValue ->
                if (newValue == "0") false else {
                    preference.summary = newValue.toString() + "秒"
                    true
                }
            }

            setPlayerTime.setOnPreferenceChangeListener { preference, newValue ->
                if (newValue == "0") false else {
                    preference.summary = newValue.toString() + "秒"
                    true
                }
            }

            setThinkingTime.setOnPreferenceChangeListener { preference, newValue ->
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
