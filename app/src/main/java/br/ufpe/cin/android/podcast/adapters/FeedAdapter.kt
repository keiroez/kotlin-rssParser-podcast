package br.ufpe.cin.android.podcast.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import br.ufpe.cin.android.podcast.data.vo.Feed
import br.ufpe.cin.android.podcast.databinding.ItemfeedBinding

class FeedAdapter (
    private val inflater: LayoutInflater):
    ListAdapter<Feed, FeedViewHolder>(FeedDiffer) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = ItemfeedBinding.inflate(inflater, parent, false)
        return FeedViewHolder(binding)
    }
    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bintTo(getItem(position))
    }
    private object FeedDiffer : DiffUtil.ItemCallback<Feed>() {
        override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }
        override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }
    }

}