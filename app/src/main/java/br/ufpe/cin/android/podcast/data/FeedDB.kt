package br.ufpe.cin.android.podcast.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.ufpe.cin.android.podcast.data.dao.EpisodioDAO
import br.ufpe.cin.android.podcast.data.dao.FeedDAO
import br.ufpe.cin.android.podcast.data.vo.Episodio
import br.ufpe.cin.android.podcast.data.vo.Feed

//[ITEM 5] - UTILIZAÇÃO DE SQLITE POR MEIO DO ROOM PARA ARMAZENAMENTO DE DADOS
@Database(entities = [Feed::class, Episodio::class],version = 2)
 abstract class FeedDB : RoomDatabase() {
     abstract fun feedDao(): FeedDAO
     abstract fun episodioDao(): EpisodioDAO
     companion object{
         @Volatile
         private var INSTANCE: FeedDB? = null
         fun getInstance(c: Context): FeedDB{
             return INSTANCE ?: synchronized(this){
                 val instance = Room.databaseBuilder(
                     c.applicationContext,
                     FeedDB::class.java,
                     "feeds_db"
                 ).build()
                 INSTANCE = instance
                 instance //return
             }
         }
     }
}