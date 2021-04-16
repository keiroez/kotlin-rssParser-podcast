package br.ufpe.cin.android.podcast.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import br.ufpe.cin.android.podcast.data.vo.Episodio
import br.ufpe.cin.android.podcast.data.vo.Feed

//[ITEM 5] - UTILIZAÇÃO DE SQLITE POR MEIO DO ROOM PARA ARMAZENAMENTO DE DADOS (DAO EPISODIO)
@Dao
interface EpisodioDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserir(episodio: Episodio)

    @Delete
    suspend fun remover(episodio: Episodio)

    @Update
    suspend fun atualizar(episodio: Episodio)

    @Query("SELECT * FROM episodios")
    fun getEpisodios(): LiveData<List<Episodio>>

    //RETORNA EPISÓDIOS PELA URL DO FEED
    @Query("SELECT * FROM episodios WHERE urlFeed = :arg")
    fun getEpisodiosPorFeed(arg:String) : LiveData<List<Episodio>>

    //Remove todos episodios quando o feed for removido
    @Query("DELETE FROM episodios WHERE urlFeed = :arg")
    suspend fun removeByFeed(arg: String)

}