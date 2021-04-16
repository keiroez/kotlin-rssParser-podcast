package br.ufpe.cin.android.podcast.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import br.ufpe.cin.android.podcast.data.vo.Feed
import br.ufpe.cin.android.podcast.data.vo.FeedComEpisodios

//[ITEM 5] - UTILIZAÇÃO DE SQLITE POR MEIO DO ROOM PARA ARMAZENAMENTO DE DADOS (DAO FEED)
@Dao
interface FeedDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserir(feed: Feed)

    @Delete
    suspend fun remover(feed: Feed)

    @Update
    suspend fun atualizar(feed: Feed)

    @Query("SELECT * FROM feeds")
    fun getFeeds(): LiveData<List<Feed>>

    @Transaction
    @Query("SELECT * FROM feeds WHERE urlFeed = :arg")
    fun getFeedComEpisodios(arg: String): LiveData<List<FeedComEpisodios>>

}