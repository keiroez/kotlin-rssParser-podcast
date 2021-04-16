package br.ufpe.cin.android.podcast.services

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.JobIntentService
import br.ufpe.cin.android.podcast.adapters.FeedAdapter
import br.ufpe.cin.android.podcast.data.FeedDB
import br.ufpe.cin.android.podcast.data.vo.Episodio
import br.ufpe.cin.android.podcast.data.vo.Feed
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
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class DownloadService : JobIntentService() {
    companion object {
        private val JOB_ID = 1234
        val DOWNLOAD_COMPLETE = "br.ufpe.android.podcast.DOWNLOAD_COMPLETE"

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, DownloadService::class.java, JOB_ID, intent)
        }
    }
    //[ITEM 7] - UTILIZAÇÃO DE JOBINTENT PARA DOWNLOAD E PROCESSAMENTO DO XML
    override fun onHandleWork(intent: Intent) {
        //AQUI O JOBINTENT É UTULIZADO PARA DOWNLOAD
        if (intent.getStringExtra("type") == "download") {
            try {
                val root =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                root.mkdirs()
                //pegando o campo data do intent passado
                val intentData = intent.data
                val output = File(root, intentData!!.lastPathSegment)
                if (output.exists()) {
                    output.delete()
                }
                val url = URL(intentData.toString())
                val c = url.openConnection() as HttpURLConnection
                val fos = FileOutputStream(output.path)
                val out = BufferedOutputStream(fos)
                try {
                    val `in` = c.inputStream
                    val buffer = ByteArray(8192)
                    var len = 0
                    while (`in`.read(buffer).also { len = it } >= 0) {
                        out.write(buffer, 0, len)
                    }
                    out.flush()
                } finally {
                    fos.fd.sync()
                    out.close()
                    c.disconnect()
                }

                //[ITEM 8] - INFORMA ATRAVÉS DE BROADCAST QUE O DOWNLOAD FOI CONCLUÍDO
                sendBroadcast(Intent(DOWNLOAD_COMPLETE))

            } catch (e: IOException) {
                Log.e(javaClass.name, "Exception durante download", e)
            }
        }
        //ABAIXO O JOBINTENT É UTILIZADO PARA BAIXAR, PROCESSAR O XML E ARMAZENAR
        //FEEDS E EPISÓDIOS NO BANCO DE DADOS
        else if (intent.getStringExtra("type") == "rss") {
            try {
                //LISTA DE URLS DE FEEDS ENVIADOS
                val urlFeeds = intent.getStringArrayListExtra("urlFeeds")
                var parser: Parser = Parser.Builder()
                    .context(this)
                    .cacheExpirationMillis(24L * 60L * 60L * 100L)
                    .build()
                val scope = CoroutineScope(Dispatchers.Main.immediate)
                var episodios: ArrayList<Episodio>
                val feedRepo: FeedRepository = FeedRepository(FeedDB.getInstance(this).feedDao())
                val epRepo: EpisodioRepository =
                    EpisodioRepository(FeedDB.getInstance(this).episodioDao())
                scope.launch {
                    for (url in urlFeeds!!) {
                        val channel = withContext(Dispatchers.IO) {
                            parser.getChannel(url.toString())
                        }
                        //PEGANDO OS DADOS DOS EPISODIOS
                        channel.articles.forEach { c ->
                            val ep = Episodio(
                                c.link.toString(),
                                c.title.toString(),
                                c.description.toString(),
                                "",
                                c.audio.toString(),
                                c.pubDate.toString(),
                                //URL DO FEED UTILIZADO COMO CHAVE ESTRANGEIRA PARA CONSULTAS
                                //POR FEED
                                url.toString(),
                                0,
                                c.image.toString()
                            )
                            //INSERINDO OS EPISODIOS NO BANCO
                            epRepo.inserir(ep)
                        }
                        //PEGANDO OS DADOS DO FEED
                        var feed = Feed(
                            url.toString(),
                            channel.title.toString(),
                            channel.description.toString(),
                            channel.link.toString(),
                            channel.image?.url.toString(), 10, 10
                        )
                        //ADICIONANDO FEED NO BANCO
                        feedRepo.inserir(feed)
                    }
                }
            } catch (e: IOException) {
                Log.e(javaClass.name, "Exception durante processamento RSS", e)
            }
        }
    }
}