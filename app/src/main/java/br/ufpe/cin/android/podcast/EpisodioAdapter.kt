package br.ufpe.cin.android.podcast

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.data.Episodio
import br.ufpe.cin.android.podcast.databinding.ItemfeedBinding

class EpisodioAdapter(
    private val episodios: ArrayList<Episodio>,
    private val inflater: LayoutInflater):
    RecyclerView.Adapter<EpisodioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodioViewHolder {
        val binding = ItemfeedBinding.inflate(inflater, parent, false)
        return EpisodioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpisodioViewHolder, position: Int) {
        holder.bintTo(episodios[position])
    }

    override fun getItemCount(): Int = episodios.size

}