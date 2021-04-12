package br.ufpe.cin.android.podcast.adapters

import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.data.vo.Episodio
import br.ufpe.cin.android.podcast.databinding.ItemepisodioBinding


class EpisodioViewHolder (private val binding: ItemepisodioBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bintTo(episodio : Episodio){
        binding.itemTitleEp.text = episodio.titulo
        binding.itemDateEp.text = episodio.dataPublicacao
    }
}