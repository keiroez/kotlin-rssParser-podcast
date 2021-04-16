package br.ufpe.cin.android.podcast.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.get
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import br.ufpe.cin.android.podcast.data.vo.Episodio
import br.ufpe.cin.android.podcast.databinding.ItemepisodioBinding
import br.ufpe.cin.android.podcast.services.MusicPlayerBindingService
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class EpisodioAdapter(
    private val inflater: LayoutInflater,
    private val serviceIntent: Intent):
    ListAdapter<Episodio, EpisodioViewHolder>(EpisodioAdapter.EpisodioDiffer) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodioViewHolder {
        val binding = ItemepisodioBinding.inflate(inflater, parent, false)
        return EpisodioViewHolder(binding, this, serviceIntent)
    }

    override fun onBindViewHolder(holder: EpisodioViewHolder, position: Int) {
        holder.bintTo(getItem(position))
    }

    private object EpisodioDiffer : DiffUtil.ItemCallback<Episodio>() {
        override fun areItemsTheSame(oldItem: Episodio, newItem: Episodio): Boolean {
            return oldItem == newItem
        }
        override fun areContentsTheSame(oldItem: Episodio, newItem: Episodio): Boolean {
            return oldItem == newItem
        }
    }
}