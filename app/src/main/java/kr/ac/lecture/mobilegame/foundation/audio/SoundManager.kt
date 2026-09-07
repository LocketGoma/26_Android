package kr.ac.lecture.mobilegame.foundation.audio

import android.content.Context
import android.util.Log

/** Manager가 로드한 Sound의 이름과 기본 설정입니다. Playback 상태는 Channel이 소유합니다. */
class SoundAsset internal constructor(
    val name: String,
    val resourceId: Int,
    val defaultVolume: Float,
    val loop: Boolean,
    internal val sampleId: Int,
)

/**
 * [강사 제공 기반 시스템] 짧은 효과음용 Logical Channel입니다. 기본 4개, 최대 8개입니다.
 * UI/GL Thread와 비동기 Load Callback의 상태 변경은 같은 lock으로 직렬화합니다.
 * SoundPool의 자연 종료 시각은 추적하지 않습니다. Play는 재시작, Pause 뒤 Play는 Resume입니다.
 * 재생 시점·채널 배치는 학생이 game 패키지에서 결정합니다.
 */
class SoundManager(context: Context, val channelCount: Int = 4) {
    private class Channel {
        var sound: SoundAsset? = null
        var volume = 1f
        var streamId = 0
        var paused = false
        var wantsPlay = false
    }
    init { require(channelCount in 1..8) { "channelCount must be 1..8" } }
    private val backend = SoundPoolBackend(context.applicationContext, channelCount)
    private val channels = Array(channelCount) { Channel() }
    private val assets = mutableSetOf<SoundAsset>()
    private val samples = mutableMapOf<Int, Int>() // Resource ID → SoundPool sample ID
    private val loadStatus = mutableMapOf<Int, Int>() // 없음: 로딩 중, 0: 성공, 그 외: 실패
    private var masterVolume = 1f
    private var hostPaused = true
    private var released = false

    init {
        backend.onLoadComplete = { id, status ->
            synchronized(this) {
                if (!released) {
                    loadStatus[id] = status
                    if (status != 0) warning("Sound load failed: sample=$id status=$status")
                    channels.filter { it.sound?.sampleId == id && it.wantsPlay }.forEach { start(it) }
                }
            }
        }
    }

    @Synchronized
    fun loadSound(resourceId: Int, name: String? = null, volume: Float = 1f, loop: Boolean = false): SoundAsset? {
        if (!available()) return null
        return try {
            val assetName = name ?: backend.resourceName(resourceId)
            if (assetName.isBlank()) { warning("Sound name is blank"); return null }
            val sample = samples[resourceId] ?: backend.load(resourceId).also {
                if (it != 0) samples[resourceId] = it
            }
            if (sample == 0) { warning("Sound load rejected: resource=$resourceId"); return null }
            SoundAsset(assetName, resourceId, clampVolume(volume), loop, sample).also { assets += it }
        } catch (error: RuntimeException) {
            warning("Cannot load sound resource=$resourceId: ${error.message}")
            null
        }
    }

    @Synchronized
    fun register(channelId: Int, sound: SoundAsset?): Boolean {
        val channel = channel(channelId) ?: return false
        if (channel.sound != null) { warning("Channel $channelId is occupied; unregister first"); return false }
        if (sound == null || sound !in assets) { warning("Sound is missing or belongs to another manager"); return false }
        channel.sound = sound
        return true
    }

    @Synchronized
    fun play(channelId: Int) {
        val channel = registeredChannel(channelId) ?: return
        channel.wantsPlay = true
        start(channel)
    }

    @Synchronized
    fun pause(channelId: Int) {
        val channel = registeredChannel(channelId) ?: return
        channel.wantsPlay = false
        if (channel.streamId != 0) backend.pause(channel.streamId)
        channel.paused = true
    }

    @Synchronized
    fun stop(channelId: Int) { registeredChannel(channelId)?.let { stopStream(it) } }

    @Synchronized
    fun unregister(channelId: Int) {
        val channel = channel(channelId) ?: return
        stopStream(channel)
        channel.sound = null
        // Sample은 다른 Channel/Asset이 공유할 수 있어 Manager.release()까지 캐시합니다.
    }

    @Synchronized
    fun setChannelVolume(channelId: Int, volume: Float) {
        val channel = channel(channelId) ?: return
        channel.volume = clampVolume(volume)
        applyVolume(channel)
    }

    @Synchronized
    fun setMasterVolume(volume: Float) {
        if (!available()) return
        masterVolume = clampVolume(volume)
        channels.forEach { applyVolume(it) }
    }

    @Synchronized
    fun onHostPause() {
        if (!available() || hostPaused) return
        hostPaused = true
        channels.forEach {
            if (it.streamId != 0 && !it.paused) { backend.pause(it.streamId); it.paused = true }
        }
    }

    @Synchronized
    fun onHostResume() {
        if (!available() || !hostPaused) return
        hostPaused = false
        channels.filter { it.wantsPlay }.forEach { start(it) }
    }

    @Synchronized
    fun release() {
        if (released) return
        channels.forEach { stopStream(it); it.sound = null }
        released = true
        backend.onLoadComplete = null
        backend.release()
        assets.clear()
        samples.clear()
        loadStatus.clear()
    }

    private fun start(channel: Channel) {
        val sound = channel.sound ?: return
        if (hostPaused) return
        val status = loadStatus[sound.sampleId] ?: return // 로딩 완료 뒤 한 번 재생합니다.
        if (status != 0) { channel.wantsPlay = false; warning("Sound is not ready: ${sound.name}"); return }
        if (channel.paused && channel.streamId != 0) {
            backend.resume(channel.streamId)
        } else {
            if (channel.streamId != 0) backend.stop(channel.streamId)
            channel.streamId = backend.play(sound.sampleId, effectiveVolume(channel), sound.loop)
            if (channel.streamId == 0) { channel.wantsPlay = false; warning("Playback failed: ${sound.name}") }
        }
        channel.paused = false
    }

    private fun stopStream(channel: Channel) {
        if (channel.streamId != 0) backend.stop(channel.streamId)
        channel.streamId = 0
        channel.paused = false
        channel.wantsPlay = false
    }
    private fun effectiveVolume(channel: Channel) = (channel.sound?.defaultVolume ?: 0f) * channel.volume * masterVolume
    private fun applyVolume(channel: Channel) {
        if (channel.streamId != 0) backend.setVolume(channel.streamId, effectiveVolume(channel))
    }
    private fun available(): Boolean {
        if (released) warning("SoundManager is released")
        return !released
    }
    private fun channel(id: Int): Channel? {
        if (!available()) return null
        if (id !in channels.indices) { warning("Invalid channel=$id; valid=0..${channelCount - 1}"); return null }
        return channels[id]
    }
    private fun registeredChannel(id: Int): Channel? = channel(id)?.let {
        if (it.sound == null) { warning("Channel $id has no sound"); null } else it
    }
    private fun clampVolume(value: Float): Float = if (value.isNaN()) 0f else value.coerceIn(0f, 1f)
    private fun warning(message: String) { Log.w("SoundManager", message) }
}
