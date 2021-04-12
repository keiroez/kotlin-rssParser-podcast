package br.ufpe.cin.android.podcast

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import br.ufpe.cin.android.podcast.adapters.EpisodioAdapter
import br.ufpe.cin.android.podcast.data.FeedDB
import br.ufpe.cin.android.podcast.data.vo.Episodio
import br.ufpe.cin.android.podcast.data.vo.Feed
import br.ufpe.cin.android.podcast.databinding.ActivityFeedBinding
import br.ufpe.cin.android.podcast.repository.EpisodioRepository
import br.ufpe.cin.android.podcast.repository.FeedRepository
import br.ufpe.cin.android.podcast.viewModel.EpisodioViewModel
import br.ufpe.cin.android.podcast.viewModel.EpisodioViewModelFactory
import br.ufpe.cin.android.podcast.viewModel.FeedViewModel
import br.ufpe.cin.android.podcast.viewModel.FeedViewModelFactory
import com.prof.rssparser.Parser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeedActivity() : AppCompatActivity() {
    private lateinit var binding : ActivityFeedBinding
    private lateinit var episodioAdapter: EpisodioAdapter
    private val episodioViewModel : EpisodioViewModel by viewModels(){
        EpisodioViewModelFactory(
            EpisodioRepository(
                FeedDB.getInstance(this).episodioDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rvEpisodios = binding.listaEpisodios
        rvEpisodios.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        episodioAdapter = EpisodioAdapter(layoutInflater)

        rvEpisodios.apply {
            layoutManager = LinearLayoutManager(this@FeedActivity)
            adapter = episodioAdapter
        }

        var linkUrl = intent.getStringExtra("url")

        episodioViewModel.getEpisodiosPorFeed(linkUrl.toString()).observe(
            this,
            Observer {
                episodioAdapter.submitList(it.toList())
            }
        )

    }

    override fun onStart() {
        super.onStart()
    }
}