package br.ufpe.cin.android.podcast

import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.data.Feed
import br.ufpe.cin.android.podcast.databinding.ItemfeedBinding

class FeedViewHolder(private val binding: ItemfeedBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bintTo(feed: Feed) {
        binding.itemTitle.text = feed.titulo
//        binding.itemDate.text = feed.dataPublicacao
    }
}