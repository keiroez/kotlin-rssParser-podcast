package br.ufpe.cin.android.podcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.preference.PreferenceFragmentCompat

class PreferenciasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferencias)

        val pref_settings = findViewById<FrameLayout>(R.id.pref_settings)
        //Após criar o fragmento, use o código abaixo para exibir
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.pref_settings,RssPrefsFragment())
                .commit()
    }

    class RssPrefsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.tela_preferencias)
        }

    }
}