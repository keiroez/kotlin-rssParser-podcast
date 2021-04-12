package br.ufpe.cin.android.podcast

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import br.ufpe.cin.android.podcast.adapters.FeedAdapter
import br.ufpe.cin.android.podcast.data.FeedDB
import br.ufpe.cin.android.podcast.data.vo.Episodio
import br.ufpe.cin.android.podcast.data.vo.Feed
import br.ufpe.cin.android.podcast.databinding.ActivityMainBinding
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

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var parser : Parser
    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private lateinit var episodios: ArrayList<Episodio>
    private val feedViewModel : FeedViewModel by viewModels(){
        FeedViewModelFactory(
            FeedRepository(
                FeedDB.getInstance(this).feedDao()))
    }
    private val episodioViewModel : EpisodioViewModel by viewModels(){
        EpisodioViewModelFactory(
            EpisodioRepository(
                FeedDB.getInstance(this).episodioDao())
        )
    }

    companion object {
        val PODCAST_FEED_INICIAL = "https://jovemnerd.com.br/feed-nerdcast/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val preference : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val e = preference.edit()
        e.putString("rssfeed", PODCAST_FEED_INICIAL)
        e.apply()

        binding.addFeeds.setOnClickListener {
//            startActivity(Intent(this, PreferenciasActivity::class.java))
        }

        val rvFeeds = binding.rvFeeds
        rvFeeds.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        feedAdapter = FeedAdapter(layoutInflater)
        rvFeeds.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = feedAdapter
        }
        feedViewModel.feeds.observe(
            this,
            Observer {
                feedAdapter.submitList(it.toList())
            }
        )

        parser = Parser.Builder()
            .context(this)
            .cacheExpirationMillis(24L * 60L * 60L * 100L)
            .build()
    }

    override fun onStart() {
        super.onStart()
        val preference : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val pod_inicial = preference.getString("inicial", PODCAST_FEED_INICIAL)
        scope.launch {
            val channel = withContext(Dispatchers.IO) {
                parser.getChannel(pod_inicial.toString())
            }
            episodios = ArrayList<Episodio>()
            channel.articles.forEach { c ->
                var ep = Episodio(
                    c.link.toString(),
                    c.title.toString(),
                    c.description.toString(),
                    c.sourceUrl.toString(),
                    c.pubDate.toString(),
                    channel.link.toString()
                )
//                episodios.add(ep)
                episodioViewModel.inserir(ep)
            }

            var feed = Feed(
                PODCAST_FEED_INICIAL,
                channel.title.toString(),
                channel.description.toString(),
                channel.link.toString(),
                channel.image?.link.toString(), 10, 10
            )
            feedViewModel.inserir(feed)

//            feeds.add(feed)
//            feedAdapter.notifyDataSetChanged()
        }
    }
}