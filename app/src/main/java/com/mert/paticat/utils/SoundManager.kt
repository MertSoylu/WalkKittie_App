package com.mert.paticat.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.SystemClock
import com.mert.paticat.R

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private var purrMediaPlayer: android.media.MediaPlayer? = null
    // Keep soundPool for future short sounds (like meow)
    private var meowSoundId = -1
    private val isLoaded = mutableMapOf<Int, Boolean>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
            
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                isLoaded[sampleId] = true
            }
        }
        
        // Initialize MediaPlayer for purr
        try {
            purrMediaPlayer = android.media.MediaPlayer.create(context, R.raw.purr)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun playPurr() {
        try {
            // Only play if not already playing
            if (purrMediaPlayer?.isPlaying == true) {
                return
            }
            purrMediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun release() {
        try {
            soundPool.release()
            purrMediaPlayer?.release()
            purrMediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
