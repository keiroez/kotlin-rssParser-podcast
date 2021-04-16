package br.ufpe.cin.android.podcast

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import br.ufpe.cin.android.podcast.adapters.EpisodioAdapter
import br.ufpe.cin.android.podcast.data.FeedDB
import br.ufpe.cin.android.podcast.databinding.ActivityFeedBinding
import br.ufpe.cin.android.podcast.repository.EpisodioRepository
import br.ufpe.cin.android.podcast.services.DownloadService
import br.ufpe.cin.android.podcast.services.MusicPlayerBindingService
import br.ufpe.cin.android.podcast.viewModel.EpisodioViewModel
import br.ufpe.cin.android.podcast.viewModel.EpisodioViewModelFactory

class FeedActivity() : AppCompatActivity() {
    private lateinit var binding : ActivityFeedBinding
    private lateinit var episodioAdapter: EpisodioAdapter
    //[ITEM 6] - VIEWMODEL UTILIZADO PARA RECYCLEVIEW UTILIZAR BD
    private val episodioViewModel : EpisodioViewModel by viewModels(){
        EpisodioViewModelFactory(
            EpisodioRepository(
                FeedDB.getInstance(this).episodioDao()))
    }
    companion object {
        private lateinit var serviceIntent: Intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Service do MusicPlayer
        serviceIntent =
            Intent(binding.root.context, MusicPlayerBindingService::class.java)
        binding.root.context.startService(serviceIntent)

        //[ITEM 1] - UTILIZAÇÃO DE RECYCLEVIEW PARA LISTAR ELEMENTOS
        val rvEpisodios = binding.listaEpisodios
        episodioAdapter = EpisodioAdapter(layoutInflater, serviceIntent)
        rvEpisodios.apply {
            layoutManager = LinearLayoutManager(this@FeedActivity)
            adapter = episodioAdapter
        }
        //URL FEED ENVIADO DA ACTIVITY ANTERIOR, PARA UMA CONSULTA DE EPISODIOS POR FEED
        var linkUrl = intent.getStringExtra("url")
        episodioViewModel.getEpisodiosPorFeed(linkUrl.toString()).observe(
            this,
            Observer {
                episodioAdapter.submitList(it.toList())
            }
        )

    }

//  [ITEM 8] AGUARDANDO O RETORNO DO DOWNLOAD COMPLETO ATRAVÉS DE BROADCAST
    private val onDownloadComplete = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Toast.makeText(binding.root.context, "Download finalizado.", Toast.LENGTH_SHORT).show()
            episodioAdapter.notifyDataSetChanged()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(onDownloadComplete, IntentFilter(DownloadService.DOWNLOAD_COMPLETE))
    }
    override fun onPause() {
        unregisterReceiver(onDownloadComplete)
        super.onPause()
    }
}