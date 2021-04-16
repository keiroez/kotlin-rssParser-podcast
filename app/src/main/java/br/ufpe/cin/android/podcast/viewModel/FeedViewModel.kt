package br.ufpe.cin.android.podcast.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.ufpe.cin.android.podcast.data.vo.Feed
import br.ufpe.cin.android.podcast.repository.FeedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class FeedViewModel(val repo: FeedRepository) : ViewModel() {
    val feeds: LiveData<List<Feed>> = repo.feeds

    suspend fun inserir(feed : Feed){
        viewModelScope.launch(Dispatchers.IO) {
            repo.inserir(feed)
        }
    }

    suspend fun atualizar(feed: Feed){
        viewModelScope.launch(Dispatchers.IO) {
            repo.atualizar(feed)
        }
    }

    suspend fun remover(feed: Feed){
        viewModelScope.launch(Dispatchers.IO) {
            repo.remover(feed)
        }
    }

}

class FeedViewModelFactory( private val repo: FeedRepository) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //Checar se está usando feed view model
        if(modelClass.isAssignableFrom(FeedViewModel::class.java)){
            return FeedViewModel (repo) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}