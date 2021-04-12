package br.ufpe.cin.android.podcast.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.ufpe.cin.android.podcast.data.dao.EpisodioDAO
import br.ufpe.cin.android.podcast.data.vo.Episodio

class EpisodioRepository (private val episodioDAO: EpisodioDAO) {
    val episodios = episodioDAO.getEpisodios()

    @WorkerThread
    suspend fun inserir(episodio: Episodio){
        episodioDAO.inserir(episodio)
    }

    @WorkerThread
    suspend fun atualizar(episodio: Episodio){
        episodioDAO.atualizar(episodio)
    }

    @WorkerThread
    suspend fun remover(episodio: Episodio){
        episodioDAO.remover(episodio)
    }

    @WorkerThread
    fun getEpisodiosPorFeed(arg: String): LiveData<List<Episodio>>{
        return episodioDAO.getEpisodiosPorFeed(arg)
    }
}