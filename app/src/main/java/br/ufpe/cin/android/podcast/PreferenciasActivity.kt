package br.ufpe.cin.android.podcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class PreferenciasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferencias)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.pref_settings,RssPrefsFragment())
                .commit()
    }
    //[item 4] UTILIZAÇÃO DE PreferenceFragmentCompat PARA ADIÇÃO DE NOVOS PREFERENCES
    class RssPrefsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.tela_preferencias)
        }
    }
}