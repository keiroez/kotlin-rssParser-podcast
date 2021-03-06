package br.ufpe.cin.android.podcast.data.vo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episodios")
data class Episodio(
    @PrimaryKey
    val linkEpisodio: String,
    val titulo: String,
    val descricao: String,
    var linkArquivo: String,
    val linkDownload: String,
    val dataPublicacao: String,
    val urlFeed: String,
    var currentPosition: Int,
    var imagemURL: String
) {
    override fun toString(): String {
        return "$titulo ($dataPublicacao) => $descricao"
    }
}