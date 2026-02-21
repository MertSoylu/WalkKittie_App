package com.mert.paticat.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.mert.paticat.R

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private var purrSoundId = -1
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
        
        // Load sounds (Assuming purr.ogg is added to res/raw/purr.ogg)
        try {
            purrSoundId = soundPool.load(context, R.raw.purr, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun playPurr() {
        if (isLoaded[purrSoundId] == true) {
            soundPool.play(purrSoundId, 1f, 1f, 1, 0, 1f)
        }
    }
    
    fun release() {
        soundPool.release()
    }
}
