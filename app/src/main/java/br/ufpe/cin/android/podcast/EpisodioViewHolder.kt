package br.ufpe.cin.android.podcast

import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.data.Episodio
import br.ufpe.cin.android.podcast.databinding.ItemfeedBinding

class EpisodioViewHolder (private val binding: ItemfeedBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bintTo(episodio : Episodio){
        binding.itemTitle.text = episodio.titulo
        binding.itemDate.text = episodio.dataPublicacao
    }
}