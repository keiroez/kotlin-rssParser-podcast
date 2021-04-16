package br.ufpe.cin.android.podcast

import android.content.*
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import br.ufpe.cin.android.podcast.adapters.FeedAdapter
import br.ufpe.cin.android.podcast.data.FeedDB
import br.ufpe.cin.android.podcast.databinding.ActivityMainBinding
import br.ufpe.cin.android.podcast.repository.EpisodioRepository
import br.ufpe.cin.android.podcast.repository.FeedRepository
import br.ufpe.cin.android.podcast.services.DownloadService
import br.ufpe.cin.android.podcast.viewModel.EpisodioViewModel
import br.ufpe.cin.android.podcast.viewModel.EpisodioViewModelFactory
import br.ufpe.cin.android.podcast.viewModel.FeedViewModel
import br.ufpe.cin.android.podcast.viewModel.FeedViewModelFactory
import kotlinx.coroutines.*

/*
* AQUI SÃO LISTADOS NUM RECYCLEVIEW TODOS OS FEEDS ADICIONADOS
* TENDO A POSSIBILIDADE DE SEREM ADICIONADOS NOVOS ATRAVÉS DE PreferenceFragmentCompat
* ADICIONANDO A URL DO RSS NUMA SHARED PREFERENCES
* */
class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var feedAdapter: FeedAdapter
    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    //[ITEM 6] - VIEWMODEL UTILIZADO PARA RECYCLEVIEW UTILIZAR BD
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
                FeedDB.getInstance(this).episodioDao())
        )
    }
    private lateinit var PODCAST_DEFAULT_FEED : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //[ITEM 2] - URL PADRÃO, CASO NÃO TENHA NENHUM FEED ADICIONADO
        PODCAST_DEFAULT_FEED = getString(R.string.link_inicial)

        //[ITEM 4] - ACTIVITY PARA ALTERAR SHARED PREFERENCE
        binding.addFeeds.setOnClickListener {
            startActivity(Intent(this, PreferenciasActivity::class.java))
        }

        //[ITEM 1] - UTILIZAÇÃO DE RECYCLEVIEW PARA LISTAR ELEMENTOS
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

        //[EXTRA] - REMOVE FEED ARRASTANDO CARD PARA ESQUERDA
        //https://stackoverflow.com/questions/49827752/how-to-implement-drag-and-drop-and-swipe-to-delete-in-recyclerview/59015928
        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: ViewHolder, target: ViewHolder
                ): Boolean {
                    return true
                }
                override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                    scope.launch(Dispatchers.IO) {
                        //REMOVE EPISODIOS DO BD
                        episodioViewModel.repo.removeByFeed(
                            feedAdapter.currentList.get(
                                viewHolder.adapterPosition).urlFeed
                        )
                        //REMOVE FEED DO BD
                        feedViewModel.remover(feedAdapter.currentList.get(viewHolder.adapterPosition))
                    }
                    Toast.makeText(
                        this@MainActivity,
                        "Feed removido!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        itemTouchHelper.attachToRecyclerView(rvFeeds)
    }

    override fun onStart() {
        super.onStart()
        //[ITEM 4] - UTILIZAÇÃO DE SHARED PREFERENCES PARA ARMAZENAR URLS DOS FEEDS ADICIONADOS
        val preference: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        //CASO NENHUM FEED TENHA SIDO ADICIONADO, É UTILIZADO O FEED DEFAULT
        val podcast_default = preference.getString("rssfeed", PODCAST_DEFAULT_FEED)

            scope.launch {
                var listaFeeds = ArrayList<String>()
                //LISTA DE FEEDS JÁ ARMAZENADOS NO BD
                for( feed in feedViewModel.feeds.value.orEmpty()){
                    listaFeeds.add(feed.urlFeed.toString())
                }
                //FEEDS RECÉM-ADICIONADO OU FEED DEFAULT
                if (podcast_default != "") {
                    if((podcast_default.toString()== PODCAST_DEFAULT_FEED
                                && listaFeeds.isEmpty())
                        || podcast_default.toString()!= PODCAST_DEFAULT_FEED)
                        listaFeeds.add(podcast_default.toString())
                }

                //[ITEM 7] - UTILIZAÇÃO DE JOBINTENT PARA PROCESSAMENTO DO XML E ARMAZENAMENTO NO BD
                val i = Intent(binding.root.context, DownloadService::class.java)
                //ENVIA A LISTA DE FEED PARA SER ADICIONADO OU ATUALIZAR OS JÁ EXISTENTES
                i.putStringArrayListExtra("urlFeeds", listaFeeds)
                //INTENT MSG PARA UTILIZAR O SERVIÇO DE PROCESSAR XML
                i.putExtra("type", "rss")
                DownloadService.enqueueWork(binding.root.context, i)

                //APÓS A ADIÇÃO PELA SHARED PREFERENCE, ESTA É LIMPA PARA MELHOR CONTROLE E NOVAS ADIÇÕES
                preference.edit().clear().apply()
            }
    }
}