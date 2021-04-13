package br.ufpe.cin.android.podcast

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
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
                FeedDB.getInstance(this).feedDao()
            )
        )
    }
    private val episodioViewModel : EpisodioViewModel by viewModels(){
        EpisodioViewModelFactory(
            EpisodioRepository(
                FeedDB.getInstance(this).episodioDao()
            )
        )
    }

    companion object {
        val PODCAST_FEED_INICIAL = "https://jovemnerd.com.br/feed-nerdcast/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val preference : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//        val e = preference.edit()
//        if(preference.getString("rssfeed","")=="") {
//            e.putString("rssfeed", PODCAST_FEED_INICIAL)
//            e.apply()
//        }

        binding.addFeeds.setOnClickListener {
            startActivity(Intent(this, PreferenciasActivity::class.java))
        }

        val rvFeeds = binding.rvFeeds
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

        //Remove feed arrastando o card pro lado
        //https://stackoverflow.com/questions/49827752/how-to-implement-drag-and-drop-and-swipe-to-delete-in-recyclerview/59015928
        val mIth = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: ViewHolder, target: ViewHolder
                ): Boolean {
                    return true // true if moved, false otherwise
                }
                override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                    scope.launch(Dispatchers.IO) {
                        feedViewModel.remover(feedAdapter.currentList.get(viewHolder.adapterPosition))
                    }
                    Toast.makeText(
                        this@MainActivity,
                        "Feed removido!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        mIth.attachToRecyclerView(rvFeeds)

        parser = Parser.Builder()
            .context(this)
            .cacheExpirationMillis(24L * 60L * 60L * 100L)
            .build()
    }

    override fun onStart() {
        super.onStart()
        val preference: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val pod_inicial = preference.getString("rssfeed", PODCAST_FEED_INICIAL)
        if (pod_inicial != "") {
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
                        pod_inicial.toString()
                    )
                    episodioViewModel.inserir(ep)
                }
                var feed = Feed(
                    pod_inicial.toString(),
                    channel.title.toString(),
                    channel.description.toString(),
                    channel.link.toString(),
                    channel.image?.link.toString(), 10, 10
                )
                feedViewModel.inserir(feed)
                preference.edit().clear().apply()
            }
        }
    }
}