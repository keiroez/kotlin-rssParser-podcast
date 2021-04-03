package br.ufpe.cin.android.podcast

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.data.Feed
import br.ufpe.cin.android.podcast.databinding.ItemfeedBinding

class FeedAdapter (
    private val feed: ArrayList<Feed>,
    private val inflater: LayoutInflater):
    RecyclerView.Adapter<FeedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = ItemfeedBinding.inflate(inflater, parent, false)
        binding.card.setOnClickListener {
            inflater.context.startActivity(Intent(inflater.context, FeedActivity::class.java))
        }
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bintTo(feed[position])
    }

    override fun getItemCount(): Int = feed.size

    private object FeedDiffer : DiffUtil.ItemCallback<Feed>() {
        override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }

    }

}