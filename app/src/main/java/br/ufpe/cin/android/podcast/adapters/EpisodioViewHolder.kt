package br.ufpe.cin.android.podcast.adapters

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.data.vo.Episodio
import br.ufpe.cin.android.podcast.databinding.ItemepisodioBinding
import br.ufpe.cin.android.podcast.services.DownloadService


class EpisodioViewHolder (private val binding: ItemepisodioBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bintTo(episodio : Episodio){
        binding.itemTitleEp.text = episodio.titulo
        binding.itemDateEp.text = episodio.dataPublicacao

        binding.itemActionEp.setOnClickListener {
            val link = episodio.linkEpisodio+".mp3"
            val i = Intent(binding.root.context, DownloadService::class.java)
            Log.i("LINK_DOWNLOADO", link)
            i.data = Uri.parse(link)
            DownloadService.enqueueWork(binding.root.context, i)
        }
    }
}