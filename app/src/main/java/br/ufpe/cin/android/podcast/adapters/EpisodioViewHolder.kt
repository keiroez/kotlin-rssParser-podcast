package br.ufpe.cin.android.podcast.adapters

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.R
import br.ufpe.cin.android.podcast.data.FeedDB
import br.ufpe.cin.android.podcast.data.vo.Episodio
import br.ufpe.cin.android.podcast.databinding.ItemepisodioBinding
import br.ufpe.cin.android.podcast.repository.EpisodioRepository
import br.ufpe.cin.android.podcast.services.DownloadService
import br.ufpe.cin.android.podcast.services.MusicPlayerBidingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class EpisodioViewHolder(private val binding: ItemepisodioBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val repo: EpisodioRepository = EpisodioRepository(
        FeedDB.getInstance(
            binding.root.context
        ).episodioDao()
    )


    fun bintTo(episodio: Episodio) {
        binding.itemTitleEp.text = episodio.titulo
        binding.itemDateEp.text = episodio.dataPublicacao

        if (episodio.linkArquivo.isEmpty()) {
            binding.btActionEp.setImageResource(R.drawable.ic_baseline_get_app_24)
            binding.btActionEp.setOnClickListener {
                binding.btActionEp.isEnabled = false
                val link = episodio.linkDownload
                val i = Intent(binding.root.context, DownloadService::class.java)
                var arquivo = Uri.parse(link).lastPathSegment
                episodio.linkArquivo = arquivo.toString()
                scope.launch(Dispatchers.IO) {
                    repo.atualizar(episodio)
                }
                i.data = Uri.parse(link)
                DownloadService.enqueueWork(binding.root.context, i)
            }
        } else {
            var play = false
            binding.btActionEp.isEnabled = true
            binding.btActionEp.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            binding.btActionEp.setOnClickListener {
                if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                    val root =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val audioFile = File(root, episodio.linkArquivo)
                    if (audioFile.exists()) {
//                        Toast.makeText(
//                            binding.root.context,
//                            "Arquivo ta pronto!!",
//                            Toast.LENGTH_SHORT
//                        ).show()
                        if (!play) {
                            val i = Intent(
                                binding.root.context,
                                MusicPlayerBidingService::class.java
                            )
                            i.putExtra("audio", episodio.linkArquivo)
                            binding.root.context.startService(i)
                            play = true
                        } else {
                            binding.root.context.stopService(
                                Intent(
                                    binding.root.context,
                                    MusicPlayerBidingService::class.java
                                )
                            )
                            play = false
                        }
                    } else {
                        Toast.makeText(
                            binding.root.context,
                            "Arquivo nao existe",
                            Toast.LENGTH_SHORT
                        ).show()
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
}