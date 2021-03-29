package br.ufpe.cin.android.podcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import br.ufpe.cin.android.podcast.data.Episodio
import br.ufpe.cin.android.podcast.data.Feed
import br.ufpe.cin.android.podcast.databinding.ActivityMainBinding
import com.prof.rssparser.Parser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var parser : Parser
    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private lateinit var feed : Feed
    private lateinit var episodios: ArrayList<Episodio>
    private lateinit var adapter: EpisodioAdapter

    companion object {
        val PODCAST_FEED = "https://jovemnerd.com.br/feed-nerdcast/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        episodios = ArrayList<Episodio>()

        val rvEpisodios = binding.listaEpisodios

        rvEpisodios.layoutManager = LinearLayoutManager(this)

        rvEpisodios.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        adapter = EpisodioAdapter(episodios, layoutInflater)

        rvEpisodios.adapter = adapter

        parser = Parser.Builder()
            .context(this)
            .cacheExpirationMillis(24L * 60L * 60L * 100L)
            .build()
    }

    override fun onStart() {
        super.onStart()
        scope.launch {
            val channel = withContext(Dispatchers.IO) {
                parser.getChannel(PODCAST_FEED)
            }

            channel.articles.forEach { c ->
                var ep = Episodio(
                    c.link.toString(),
                    c.title.toString(),
                    c.description.toString(),
                    c.sourceUrl.toString(),
                    c.pubDate.toString()
                )
                episodios.add(ep)
            }
            adapter.notifyDataSetChanged()

//            feed = Feed(
//                channel.link.toString(),
//                channel.title.toString(),
//                channel.description.toString(),
//                channel.link.toString(),
//                channel.image.url.toString(),
//                10,
//                10,
//                episodios
//            )
        }

    }
}