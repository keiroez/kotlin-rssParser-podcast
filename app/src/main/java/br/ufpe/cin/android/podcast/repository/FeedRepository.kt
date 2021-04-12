package br.ufpe.cin.android.podcast.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.ufpe.cin.android.podcast.data.dao.FeedDAO
import br.ufpe.cin.android.podcast.data.vo.Feed

class FeedRepository (private val feedDAO : FeedDAO) {
    val feeds  = feedDAO.getFeeds()

    @WorkerThread
    suspend fun inserir(feed:Feed){
        feedDAO.inserir(feed)
    }

    @WorkerThread
    suspend fun atualizar(feed:Feed){
        feedDAO.atualizar(feed)
    }

    @WorkerThread
    suspend fun remover(feed:Feed){
        feedDAO.remover(feed)
    }

    @WorkerThread
    fun getFeedsComEpisodios(linkFeed: String){
        feedDAO.getFeedComEpisodios(linkFeed)
    }

//    fun getPorLink(link:String){
//        feedDAO.getPorLink(link)
//    }
}