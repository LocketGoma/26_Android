package kr.ac.lecture.mobilegame.foundation.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * SoundPool 하나만 사용합니다. 긴 BGM용 Streaming Backend가 아닙니다.
 * SoundPool은 디코딩된 Sound 하나당 약 1 MB 제한이 있으므로 짧은 효과음을 사용하세요.
 */
internal class SoundPoolBackend(private val context: Context, channelCount: Int) {
    init { require(channelCount in 1..8) }
    @Volatile var onLoadComplete: ((Int, Int) -> Unit)? = null
    private val pool = SoundPool.Builder()
        .setMaxStreams(channelCount)
        .setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
        .build().apply { setOnLoadCompleteListener { _, id, status -> onLoadComplete?.invoke(id, status) } }

    fun resourceName(resourceId: Int): String = context.resources.getResourceEntryName(resourceId)
    fun load(resourceId: Int): Int = pool.load(context, resourceId, 1)
    fun play(sampleId: Int, volume: Float, loop: Boolean): Int =
        pool.play(sampleId, volume, volume, 1, if (loop) -1 else 0, 1f)
    fun pause(streamId: Int) = pool.pause(streamId)
    fun resume(streamId: Int) = pool.resume(streamId)
    fun stop(streamId: Int) = pool.stop(streamId)
    fun setVolume(streamId: Int, volume: Float) = pool.setVolume(streamId, volume, volume)
    fun release() { pool.setOnLoadCompleteListener(null); pool.release() }
}
