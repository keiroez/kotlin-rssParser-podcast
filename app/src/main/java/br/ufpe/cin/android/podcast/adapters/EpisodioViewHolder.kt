package br.ufpe.cin.android.podcast.adapters

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.R
import br.ufpe.cin.android.podcast.data.FeedDB
import br.ufpe.cin.android.podcast.data.vo.Episodio
import br.ufpe.cin.android.podcast.databinding.ItemepisodioBinding
import br.ufpe.cin.android.podcast.repository.EpisodioRepository
import br.ufpe.cin.android.podcast.services.DownloadService
import br.ufpe.cin.android.podcast.services.MusicPlayerBindingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class EpisodioViewHolder(
    private val binding: ItemepisodioBinding,
    private val adapter: EpisodioAdapter
) :
    RecyclerView.ViewHolder(binding.root) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val repo: EpisodioRepository = EpisodioRepository(
        FeedDB.getInstance(
            binding.root.context
        ).episodioDao()
    )
    private var isBound = false
    internal var TAG = "MusicPLayerBindig"
    private var musicPlayerService: MusicPlayerBindingService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isBound = true
            val musicBinder = service as MusicPlayerBindingService.MusicBinder
            musicPlayerService = musicBinder.service
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            musicPlayerService = null
        }
    }
    private var serviceOn = false
    private val serviceIntent =
        Intent(binding.root.context, MusicPlayerBindingService::class.java)


    fun bintTo(episodio: Episodio) {
        binding.itemTitleEp.text = episodio.titulo
        binding.itemDateEp.text = episodio.dataPublicacao


        //[ITEM 8] - AO CLICAR NO BOTÃO DE DOWNLOAD É INICIADO O SERVIÇO PARA BAIXAR
        //           O ÁUDIO DO EPISÓDIO
        if (episodio.linkArquivo.isEmpty()) {
            //BOTÃO COM ICONE DE DOWNLOAD
            binding.btActionEp.setImageResource(R.drawable.ic_baseline_get_app_24)
            binding.btActionEp.setOnClickListener {
                //QUANDO O DOWNLOAD É INICIADO, O BOTÃO É BLOQUEADO
                binding.btActionEp.isEnabled = false
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
                if (audioFile.exists()) {
                    //[ITEM 9] - ICONE DE PLAY ADICIONADO AO BOTÃO
                    binding.btActionEp.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    //VARIÁVEL PARA CONTROLAR SE O EPISÓDIO ESTÁ SENDO EXECUTADO
                    var play = false
                    //BOTÃO DE PLAY DESBLOQUEADO
                    binding.btActionEp.isEnabled = true
                    //BOTÃO COM LISTENER MODIFICADO PARA PLAY
                    binding.btActionEp.setOnClickListener {
                        serviceIntent.putExtra("audio", episodio.linkArquivo)
                        if (!isBound) {
                            binding.root.context.bindService(
                                serviceIntent,
                                serviceConnection,
                                Context.BIND_AUTO_CREATE
                            )
                        }
                        if (!play) {
                            if(!serviceOn) {
                                binding.root.context.startService(serviceIntent)
                                serviceOn = true
                            }
                            binding.btActionEp.setImageResource(R.drawable.ic_baseline_pause_24)
                            if (isBound) {
                                musicPlayerService?.playMusic()
                            }
                            play = true
                        } else {
                            binding.btActionEp.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                            if (isBound) {
                                musicPlayerService?.pauseMusic()
                            }
                            play = false
                            adapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    Toast.makeText(
                        binding.root.context,
                        "Arquivo nao existe",
                        Toast.LENGTH_SHORT
                    ).show()
                    //CASO O ARQUIVO NÃO EXISTA MAIS, É MOSTRADO UMA MENSAGEM INFORMANDO
                    //E O CAMINHO DO ARQUIVO NO BD É ATUALIZADO
                    episodio.linkArquivo = ""
                    scope.launch(Dispatchers.IO) {
                        repo.atualizar(episodio)
                        adapter.notifyDataSetChanged()
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
}