package br.ufpe.cin.android.podcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import br.ufpe.cin.android.podcast.data.FeedDB
import br.ufpe.cin.android.podcast.data.vo.Episodio
import br.ufpe.cin.android.podcast.databinding.ActivityEpisodeDetailBinding
import br.ufpe.cin.android.podcast.repository.EpisodioRepository
import br.ufpe.cin.android.podcast.viewModel.EpisodioViewModel
import br.ufpe.cin.android.podcast.viewModel.EpisodioViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EpisodeDetailActivity : AppCompatActivity() {
    private lateinit var binding : ActivityEpisodeDetailBinding
    private val scope = CoroutineScope(Dispatchers.IO)
    private val episodioViewModel : EpisodioViewModel by viewModels(){
        EpisodioViewModelFactory(
            EpisodioRepository(
                FeedDB.getInstance(this).episodioDao())
        )
    }
    private lateinit var episodio :Episodio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var link = intent.getStringExtra("urlEpisodio").toString()

        scope.launch {
            episodio = episodioViewModel.getEpisodio(link)
            binding.epTitileDetail.text = episodio.titulo
            binding.epDescricaoDetail.text = episodio.descricao
            binding.epLinkDetail.text = episodio.linkEpisodio
        }
    }
}