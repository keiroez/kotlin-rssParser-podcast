package br.ufpe.cin.android.podcast

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.ufpe.cin.android.podcast.adapters.EpisodioViewHolder
import br.ufpe.cin.android.podcast.data.FeedDB
import br.ufpe.cin.android.podcast.data.vo.Episodio
import br.ufpe.cin.android.podcast.databinding.ActivityEpisodeDetailBinding
import br.ufpe.cin.android.podcast.repository.EpisodioRepository
import br.ufpe.cin.android.podcast.services.DownloadService
import br.ufpe.cin.android.podcast.services.MusicPlayerBindingService
import br.ufpe.cin.android.podcast.viewModel.EpisodioViewModel
import br.ufpe.cin.android.podcast.viewModel.EpisodioViewModelFactory
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class EpisodeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEpisodeDetailBinding
    private val scope = CoroutineScope(Dispatchers.IO)
    private val episodioViewModel: EpisodioViewModel by viewModels() {
        EpisodioViewModelFactory(
            EpisodioRepository(
                FeedDB.getInstance(this).episodioDao()
            )
        )
    }
    private lateinit var episodio: Episodio
    private var musicPlayerService: MusicPlayerBindingService? = null
    private var isBound = false
    internal var TAG = "MusicPlayerBinding"
    private lateinit var repo: EpisodioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = EpisodioRepository(
            FeedDB.getInstance(
                binding.root.context
            ).episodioDao()
        )

        var link = intent.getStringExtra("urlEpisodio").toString()

        scope.launch {
            episodio = episodioViewModel.getEpisodio(link)
            binding.epTitileDetail.text = episodio.titulo
            binding.epDescricaoDetail.text = episodio.descricao
            binding.epLinkDetail.text = episodio.linkEpisodio
            //[ITEM 2] DETALHES DO EPISÓDIO COM IMAGEM
            if (episodio.imagemURL != null) {
                var img = Uri.parse(episodio.imagemURL)
                runOnUiThread(Runnable {
                    Picasso.get().load(img).into(binding.imageView2)
                })
            }
            verificarBotao()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isBound) {
            bindService(
                Intent(this, MusicPlayerBindingService::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
        verificarBotao()
    }

    override fun onStop() {
        if(isBound){
            unbindService(serviceConnection)
            isBound = false
        }
        super.onStop()
    }

    fun verificarBotao() {
        if (episodio.linkArquivo.isEmpty()) {
            //BOTÃO COM ICONE DE DOWNLOAD
            binding.imageButton.setImageResource(R.drawable.ic_baseline_get_app_24)
            binding.imageButton.setOnClickListener {
                //QUANDO O DOWNLOAD É INICIADO, O BOTÃO É BLOQUEADO
                binding.imageButton.isEnabled = false
                val link = episodio.linkDownload
                val i = Intent(binding.root.context, DownloadService::class.java)
                var arquivo = Uri.parse(link).lastPathSegment
                //[ITEM 8] - CAMINHO DO ARQUIVO BAIXADO ARMAZENADO NO BD
                episodio.linkArquivo = arquivo.toString()
                scope.launch(Dispatchers.IO) {
                    repo.atualizar(episodio)
                }
                i.data = Uri.parse(link)
                //INTENT MSG PARA UTILIZAR O SERVIÇO DE DOWNLOAD
                i.putExtra("type", "download")
                //[ITEM 8] - SERVIÇO DE DOWNLOAD INICIADO COM O LINK SENDO ENVIADO POR INTENT
                DownloadService.enqueueWork(binding.root.context, i)
            }
        } else {
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                val root =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val audioFile = File(root, episodio.linkArquivo)

                //Verifica se o arquivo existe
                if (audioFile.exists()) {

                    //[ITEM 9] - ICONE DE PLAY ADICIONADO AO BOTÃO

                    //CONTROLAR SE O EPISÓDIO ESTÁ SENDO EXECUTADO
                    musicPlayerService?.let { Log.i("AUDIO_SERVICE", it.getAudio()) }
                    Log.i("AUDIO", episodio.linkArquivo)

                    //Verifica se o episódio já esta tocando e insere o botao de pause
                    if (musicPlayerService?.getAudio() == episodio.linkArquivo && musicPlayerService?.isPlaying() == true)
                        binding.imageButton.setImageResource(R.drawable.ic_baseline_pause_24)
                    else {
                        //Caso não esteja tocando, o botão será o play
                        binding.imageButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    }
                    //BOTÃO DE PLAY DESBLOQUEADO
                    binding.imageButton.isEnabled = true
                    //BOTÃO COM LISTENER MODIFICADO PARA PLAY
                    binding.imageButton.setOnClickListener {
                        //Se não tiver tocando dá o play enviando o caminho do arquivo
                        if (musicPlayerService?.isPlaying() == false) {
                            musicPlayerService?.setAudio(episodio.linkArquivo)
                            musicPlayerService?.playMusic(episodio.currentPosition, episodio.linkEpisodio)
                            binding.imageButton.setImageResource(R.drawable.ic_baseline_pause_24)
                            //Caso esteja tocando, pausa a execução
                        } else {
                            binding.imageButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                            var currentPosition = musicPlayerService?.pauseMusic()
                            if (currentPosition != null && currentPosition > 0) {
                                //Armazena posição atual quando há uma pausa
                                episodio.currentPosition = currentPosition
                                scope.launch(Dispatchers.IO) {
                                    repo.atualizar(episodio)
                                }
                            }
                        }
                    }
                } else {
                    //CASO O ARQUIVO NÃO EXISTA MAIS, É MOSTRADO UMA MENSAGEM INFORMANDO
                    //E O CAMINHO DO ARQUIVO NO BD É ATUALIZADO
                    Toast.makeText(
                        binding.root.context,
                        "Arquivo nao existe",
                        Toast.LENGTH_SHORT
                    ).show()
                    episodio.linkArquivo = ""
                    scope.launch(Dispatchers.IO) {
                        repo.atualizar(episodio)
                    }
                }
            } else {
                Toast.makeText(
                    binding.root.context,
                    "Armazenamento externo nao esta montado...",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            musicPlayerService = null
        }

        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
            isBound = true
            val musicBinder = iBinder as MusicPlayerBindingService.MusicBinder
            musicPlayerService = musicBinder.service
        }

    }
}