package br.ufpe.cin.android.podcast.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.ufpe.cin.android.podcast.data.vo.Episodio
import br.ufpe.cin.android.podcast.repository.EpisodioRepository
import java.lang.IllegalArgumentException

class EpisodioViewModel(val repo: EpisodioRepository) : ViewModel() {
    val episodios = repo.episodios

    suspend fun inserir(episodio : Episodio){
        repo.inserir(episodio)
    }

    suspend fun atualizar(episodio : Episodio){
        repo.atualizar(episodio)
    }

    suspend fun remover(episodio : Episodio){
        repo.remover(episodio)
    }

    fun getEpisodiosPorFeed(urlFeed: String): LiveData<List<Episodio>> {
        return repo.getEpisodiosPorFeed(urlFeed)
    }

    suspend fun removeByFeed(urlFeed: String){
        repo.removeByFeed(urlFeed)
    }

    suspend fun getEpisodio(linkEpisodio: String): Episodio{
       return repo.getEpisodio(linkEpisodio)
    }
}

class EpisodioViewModelFactory( private val repo: EpisodioRepository) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //Checar se está usando Episodio view model
        if(modelClass.isAssignableFrom(EpisodioViewModel::class.java)){
            return EpisodioViewModel (repo) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}