package br.ufpe.cin.android.podcast.data.vo

import androidx.room.Embedded
import androidx.room.Relation

data class FeedComEpisodios(
    @Embedded val feed: Feed,
    @Relation(
        parentColumn = "urlFeed",
        entityColumn = "urlFeed"
    ) val episodios: Episodio)