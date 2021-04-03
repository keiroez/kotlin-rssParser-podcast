package br.ufpe.cin.android.podcast

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceManager
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


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var feeds: ArrayList<Feed>
    private lateinit var adapter: FeedAdapter
    private lateinit var parser : Parser
    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private lateinit var episodios: ArrayList<Episodio>

    companion object {
        val PODCAST_FEED_INICIAL = "https://jovemnerd.com.br/feed-nerdcast/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        feeds = ArrayList<Feed>()

        val preference : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val e = preference.edit()
        e.putString("inicial", PODCAST_FEED_INICIAL)
        e.apply()
        binding.addFeeds.setOnClickListener {
            startActivity(Intent(this, PreferenciasActivity::class.java))
        }

        val rvFeeds = binding.rvFeeds

        rvFeeds.layoutManager = LinearLayoutManager(this)

        rvFeeds.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        adapter = FeedAdapter(feeds, layoutInflater)

        rvFeeds.adapter = adapter

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
                    c.pubDate.toString()
                )
                episodios.add(ep)
            }

            var feed = Feed(
                channel.link.toString(), channel.title.toString(), channel.description.toString(), channel.link.toString(),
                channel.image?.link.toString(), 10, 10, episodios
            )

            feeds.add(feed)

            adapter.notifyDataSetChanged()
        }
    }
}