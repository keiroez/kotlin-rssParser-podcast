package br.ufpe.cin.android.podcast.services

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import br.ufpe.cin.android.podcast.EpisodeDetailActivity
import br.ufpe.cin.android.services.CHANNEL_ID
import br.ufpe.cin.android.services.NOTIFICATION_ID
import br.ufpe.cin.android.services.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
import br.ufpe.cin.android.services.VERBOSE_NOTIFICATION_CHANNEL_NAME
import java.io.FileInputStream

class MusicPlayerBindingService : Service() {
    private lateinit var mPlayer: MediaPlayer
    private var numStart = 0
    private var audio: String = ""
    private var urlEpisodio: String = ""

    override fun onCreate() {
        super.onCreate()
        Log.d("MusicPlayerService", "MusicPlayerBindingService sendo criado na memória agora.")
//        mPlayer = MediaPlayer.create(this, R.raw.moonlightsonata)
        //A música não vai ficar tocando em loop
//        mPlayer.isLooping = true
        mPlayer = MediaPlayer();
        createChannel()
        val intent = Intent(this,EpisodeDetailActivity::class.java)
        intent.putExtra("urlEpisodio", urlEpisodio)
        //como se fosse o startActivity(intent) pra ser executado mais tarde (ao clicar na notificacao)
        val pendingIntent = PendingIntent.getActivity(this,0,intent,0)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setContentTitle("Podcast tocando!")
            .setContentText("clique para acessar o player")
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        Log.d("MusicPlayerService", "MusicPlayerService sendo destruído agora.")
        mPlayer.release()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //Sinaliza o que fazer caso o service seja interrompido pelo sistema
        //START_NOT_STICKY - não é reiniciado automaticamente
        //START_STICKY - vai ser reiniciado automaticamente assim que possível, com intent nulo
        //START_REDELIVER_INTENT - vai ser reiniciado automaticamente assim que possível, com último intent usado para comando start
        return START_NOT_STICKY
    }

    //Seta o caminho do audio
    fun setAudio(audioEnv: String) {
        //verifica se é um novo audio ou o mesmo que já está em execução
        if (audio != audioEnv) {
            //Se já estiver tocando, ele para a execução e recria o mediaPlayer
            if (mPlayer.isPlaying)
                mPlayer.stop()
            mPlayer = MediaPlayer()
            audio = audioEnv
            var filePath = FileInputStream(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                ).path + "/" + audio
            )
            Log.d("MusicPlayerService", filePath.toString())
            mPlayer.setDataSource(filePath.fd);
            mPlayer.prepare();
        }
    }

    fun getAudio() :String{
        return audio
    }

    fun isPlaying(): Boolean{
        return mPlayer.isPlaying
    }

    fun playMusic(current :Int, urlEp: String) {
        if (!mPlayer.isPlaying) {
            //Seta posição atual se for maior que 0
            setCurrent(current)
            mPlayer.start()
            urlEpisodio = urlEp
        }
    }

    fun setCurrent(current: Int){
        if(current>0) {
            mPlayer.seekTo(current)
        }
    }

    fun pauseMusic(): Int {
        if (mPlayer.isPlaying) {
            mPlayer.pause()
        }
        return mPlayer.currentPosition
    }

    fun rewind() {
        if (mPlayer.isPlaying) {
            mPlayer.seekTo(0)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return musicBinder
    }

    private val musicBinder: IBinder = MusicBinder()

    inner class MusicBinder : Binder() {
        val service: MusicPlayerBindingService
            get() = this@MusicPlayerBindingService
    }

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                VERBOSE_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}