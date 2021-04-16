package br.ufpe.cin.android.podcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //[ITEM 1] - UTILIZAÇÃO DE RECYCLEVIEW PARA LISTAR ELEMENTOS
        val rvEpisodios = binding.listaEpisodios
        episodioAdapter = EpisodioAdapter(layoutInflater)
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

    override fun onResume() {
        super.onResume()
        registerReceiver(onDownloadComplete, IntentFilter(DownloadService.DOWNLOAD_COMPLETE))
    }
    override fun onPause() {
        unregisterReceiver(onDownloadComplete)
        super.onPause()
    }
}