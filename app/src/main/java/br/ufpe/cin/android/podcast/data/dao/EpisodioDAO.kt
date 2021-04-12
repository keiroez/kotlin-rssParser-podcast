package br.ufpe.cin.android.podcast.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import br.ufpe.cin.android.podcast.data.vo.Episodio

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

    @Query("SELECT * FROM episodios WHERE urlFeed = :arg")
    fun getEpisodiosPorFeed(arg:String) : LiveData<List<Episodio>>

}