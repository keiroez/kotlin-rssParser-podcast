package br.ufpe.cin.android.podcast.adapters

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.FeedActivity
import br.ufpe.cin.android.podcast.PreferenciasActivity
import br.ufpe.cin.android.podcast.data.vo.Feed
import br.ufpe.cin.android.podcast.databinding.ItemfeedBinding

class FeedViewHolder(private val binding: ItemfeedBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bintTo(feed: Feed) {
        binding.itemTitle.text = feed.titulo
        binding.itemDescricao.text = feed.descricao

        //AO CLICAR NO CARD DO FEED, ABRE A FEED ACTIVITY COM SEUS EPISÓDIOS
        binding.card.setOnClickListener {
            var it = Intent(binding.root.context, FeedActivity::class.java)
            it.putExtra("url", feed.urlFeed)
            binding.root.context.startActivity(it)
        }

    }
}