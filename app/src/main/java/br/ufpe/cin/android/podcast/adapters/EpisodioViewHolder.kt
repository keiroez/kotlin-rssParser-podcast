package br.ufpe.cin.android.podcast.adapters

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.icu.util.ULocale
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.EpisodeDetailActivity
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
import java.text.SimpleDateFormat
import java.util.*


class EpisodioViewHolder(
    private val binding: ItemepisodioBinding,
    private val adapter: EpisodioAdapter,
    private val serviceIntent: Intent
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

    companion object {
        private var musicPlayerService: MusicPlayerBindingService? = null
    }

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

    init {
        binding.root.context.bindService(
            serviceIntent,
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    fun bintTo(episodio: Episodio) {
        binding.itemTitleEp.text = episodio.titulo

        //Formata????o de data
        val cal: Calendar = Calendar.getInstance()
        cal.time = Date(episodio.dataPublicacao)
        val formato = "dd/MM/yyyy"
        val format = SimpleDateFormat(formato)
        val dataFormatada = format.format(cal.time)
        binding.itemDateEp.text = dataFormatada
        binding.txtEpDesc.text = episodio.descricao

        binding.cardEp.setOnClickListener {
            val i = Intent(binding.root.context, EpisodeDetailActivity::class.java)
            i.putExtra("urlEpisodio", episodio.linkEpisodio)
            binding.root.context.startActivity(i)
        }

        //[ITEM 8] - AO CLICAR NO BOT??O DE DOWNLOAD ?? INICIADO O SERVI??O PARA BAIXAR
        //           O ??UDIO DO EPIS??DIO
        if (episodio.linkArquivo.isEmpty()) {
            //BOT??O COM ICONE DE DOWNLOAD
            binding.btActionEp.setImageResource(R.drawable.ic_baseline_get_app_24)
            binding.btActionEp.setOnClickListener {
                //QUANDO O DOWNLOAD ?? INICIADO, O BOT??O ?? BLOQUEADO
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
                //INTENT MSG PARA UTILIZAR O SERVI??O DE DOWNLOAD
                i.putExtra("type", "download")
                //[ITEM 8] - SERVI??O DE DOWNLOAD INICIADO COM O LINK SENDO ENVIADO POR INTENT
                DownloadService.enqueueWork(binding.root.context, i)
            }
        } else {
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                val root =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val audioFile = File(root, episodio.linkArquivo)

                //Verifica se o arquivo existe
                if (audioFile.exists()) {

                    //[ITEM 9] - ICONE DE PLAY ADICIONADO AO BOT??O

                    //CONTROLAR SE O EPIS??DIO EST?? SENDO EXECUTADO
                    musicPlayerService?.let { Log.i("AUDIO_SERVICE", it.getAudio()) }
                    Log.i("AUDIO", episodio.linkArquivo)

                    //Verifica se o epis??dio j?? esta tocando e insere o botao de pause
                    if (musicPlayerService?.getAudio() == episodio.linkArquivo && musicPlayerService?.isPlaying() == true)
                        binding.btActionEp.setImageResource(R.drawable.ic_baseline_pause_24)
                    else {
                        //Caso n??o esteja tocando, o bot??o ser?? o play
                        binding.btActionEp.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    }
                    //BOT??O DE PLAY DESBLOQUEADO
                    binding.btActionEp.isEnabled = true
                    //BOT??O COM LISTENER MODIFICADO PARA PLAY
                    binding.btActionEp.setOnClickListener {
                        //Se n??o tiver tocando d?? o play enviando o caminho do arquivo
                        if (musicPlayerService?.isPlaying() == false) {
                            musicPlayerService?.setAudio(episodio.linkArquivo)
                            musicPlayerService?.playMusic(episodio.currentPosition, episodio.linkEpisodio)
                            binding.btActionEp.setImageResource(R.drawable.ic_baseline_pause_24)
                            //Caso esteja tocando, pausa a execu????o
                        } else {
                            binding.btActionEp.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                            var currentPosition = musicPlayerService?.pauseMusic()
                            if (currentPosition != null && currentPosition > 0) {
                                //Armazena posi????o atual quando h?? uma pausa
                                episodio.currentPosition = currentPosition
                                scope.launch(Dispatchers.IO) {
                                    repo.atualizar(episodio)
                                }
                            }
                            //Notifica adaoter sobre a mudan??a
                            adapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    //CASO O ARQUIVO N??O EXISTA MAIS, ?? MOSTRADO UMA MENSAGEM INFORMANDO
                    //E O CAMINHO DO ARQUIVO NO BD ?? ATUALIZADO
                    Toast.makeText(
                        binding.root.context,
                        "Arquivo nao existe",
                        Toast.LENGTH_SHORT
                    ).show()
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