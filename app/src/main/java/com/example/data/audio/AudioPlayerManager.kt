package com.example.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.example.data.model.EqualizerPreset
import com.example.data.model.Song
import com.example.service.AudioPlayerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.sin
import kotlin.random.Random

enum class RepeatMode {
    OFF, ALL, QUEUE, ONE
}

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 1000L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.ALL,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val pitchSemitones: Float = 0f,
    val volumeBoost: Float = 1.0f, // 100% to 500%
    val crossfadeSeconds: Int = 0,
    val isGaplessEnabled: Boolean = true,
    val sleepTimerRemainingSeconds: Int? = null,
    val isOnlineMode: Boolean = true
)

data class EqualizerState(
    val isEnabled: Boolean = true,
    val currentPreset: String = "Rock",
    val bandGains: List<Float> = listOf(5f, 3f, -1f, -3f, 1f, 4f, 6f, 7f, 5f, 3f),
    val bassBoost: Float = 7f,
    val surroundSound: Float = 6f,
    val loudnessEnhancer: Boolean = true,
    val bassSlider: Float = 7f,
    val virtualizerSlider: Float = 6f,
    val stereoBalance: Float = 0f
)

class AudioPlayerManager(private val context: Context? = null) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _equalizerState = MutableStateFlow(EqualizerState())
    val equalizerState: StateFlow<EqualizerState> = _equalizerState.asStateFlow()

    private val _visualizerFrequencies = MutableStateFlow(List(32) { 0.2f })
    val visualizerFrequencies: StateFlow<List<Float>> = _visualizerFrequencies.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var equalizerEffect: android.media.audiofx.Equalizer? = null
    private var bassBoostEffect: android.media.audiofx.BassBoost? = null
    private var loudnessEnhancerEffect: android.media.audiofx.LoudnessEnhancer? = null

    private var playbackJob: Job? = null
    private var visualizerJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var isRealMediaActive = false

    init {
        AudioPlayerService.activeAudioPlayerManager = this
        startVisualizerEngine()
    }

    fun setQueue(songs: List<Song>, startIndex: Int = 0, autoPlay: Boolean = true) {
        if (songs.isEmpty()) return
        val clampedIndex = startIndex.coerceIn(0, songs.lastIndex)
        val song = songs[clampedIndex]
        _playerState.value = _playerState.value.copy(
            queue = songs,
            currentIndex = clampedIndex,
            currentSong = song,
            durationMs = song.durationMs,
            currentPositionMs = 0L,
            isPlaying = autoPlay
        )
        if (autoPlay) {
            playSong(song)
        } else {
            prepareSongPaused(song)
        }
    }

    private fun prepareSongPaused(song: Song) {
        releaseMediaPlayer()
        if (context != null && (song.filePath.startsWith("content://") || File(song.filePath).exists())) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    if (song.filePath.startsWith("content://")) {
                        setDataSource(context, Uri.parse(song.filePath))
                    } else {
                        setDataSource(song.filePath)
                    }
                    prepare()
                    val dur = duration.toLong()
                    _playerState.value = _playerState.value.copy(durationMs = if (dur > 0) dur else song.durationMs)
                }
                isRealMediaActive = true
                setupAudioEffects(mediaPlayer?.audioSessionId ?: 0)
            } catch (e: Exception) {
                e.printStackTrace()
                isRealMediaActive = false
            }
        } else {
            isRealMediaActive = false
        }
    }

    private fun playSong(song: Song) {
        releaseMediaPlayer()
        if (context != null && (song.filePath.startsWith("content://") || File(song.filePath).exists())) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    if (song.filePath.startsWith("content://")) {
                        setDataSource(context, Uri.parse(song.filePath))
                    } else {
                        setDataSource(song.filePath)
                    }
                    setOnCompletionListener {
                        onSongCompleted()
                    }
                    setOnErrorListener { _, _, _ ->
                        isRealMediaActive = false
                        startPlaybackProgress()
                        true
                    }
                    prepare()
                    start()
                    val dur = duration.toLong()
                    _playerState.value = _playerState.value.copy(
                        durationMs = if (dur > 0) dur else song.durationMs,
                        isPlaying = true
                    )
                }
                isRealMediaActive = true
                setupAudioEffects(mediaPlayer?.audioSessionId ?: 0)
                applyVolumeAndSpeed()
                startPlaybackProgress()

                AudioPlayerService.startService(context)
                return
            } catch (e: Exception) {
                e.printStackTrace()
                isRealMediaActive = false
            }
        } else {
            isRealMediaActive = false
        }

        _playerState.value = _playerState.value.copy(isPlaying = true)
        startPlaybackProgress()
        if (context != null) {
            AudioPlayerService.startService(context)
        }
    }

    fun playPause() {
        val currentState = _playerState.value
        if (currentState.currentSong == null && currentState.queue.isNotEmpty()) {
            setQueue(currentState.queue, 0, autoPlay = true)
            return
        }

        val newPlaying = !currentState.isPlaying
        _playerState.value = currentState.copy(isPlaying = newPlaying)

        if (isRealMediaActive && mediaPlayer != null) {
            try {
                if (newPlaying) {
                    mediaPlayer?.start()
                } else {
                    mediaPlayer?.pause()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (newPlaying) {
            startPlaybackProgress()
            if (context != null) AudioPlayerService.startService(context)
        } else {
            playbackJob?.cancel()
        }
    }

    fun nextSong() {
        val state = _playerState.value
        if (state.queue.isEmpty()) return

        val nextIdx = if (state.isShuffle) {
            if (state.queue.size > 1) {
                var r = Random.nextInt(state.queue.size)
                while (r == state.currentIndex) {
                    r = Random.nextInt(state.queue.size)
                }
                r
            } else 0
        } else {
            (state.currentIndex + 1) % state.queue.size
        }

        val nextSong = state.queue[nextIdx]
        _playerState.value = state.copy(
            currentIndex = nextIdx,
            currentSong = nextSong,
            durationMs = nextSong.durationMs,
            currentPositionMs = 0L,
            isPlaying = true
        )
        playSong(nextSong)
    }

    fun previousSong() {
        val state = _playerState.value
        if (state.queue.isEmpty()) return

        if (state.currentPositionMs > 3000L) {
            seekTo(0L)
            return
        }

        val prevIdx = if (state.currentIndex - 1 < 0) state.queue.lastIndex else state.currentIndex - 1
        val prevSong = state.queue[prevIdx]
        _playerState.value = state.copy(
            currentIndex = prevIdx,
            currentSong = prevSong,
            durationMs = prevSong.durationMs,
            currentPositionMs = 0L,
            isPlaying = true
        )
        playSong(prevSong)
    }

    fun seekTo(positionMs: Long) {
        val state = _playerState.value
        val clamped = positionMs.coerceIn(0L, state.durationMs)
        _playerState.value = state.copy(currentPositionMs = clamped)

        if (isRealMediaActive && mediaPlayer != null) {
            try {
                mediaPlayer?.seekTo(clamped.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleShuffle() {
        val current = _playerState.value.isShuffle
        _playerState.value = _playerState.value.copy(isShuffle = !current)
    }

    fun cycleRepeatMode() {
        val nextMode = when (_playerState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.QUEUE
            RepeatMode.QUEUE -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playerState.value = _playerState.value.copy(repeatMode = nextMode)
    }

    fun setSpeed(speed: Float) {
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
        applyVolumeAndSpeed()
    }

    fun setPitch(pitch: Float) {
        _playerState.value = _playerState.value.copy(pitchSemitones = pitch)
        applyVolumeAndSpeed()
    }

    fun setVolumeBoost(boost: Float) {
        _playerState.value = _playerState.value.copy(volumeBoost = boost)
        applyVolumeAndSpeed()
    }

    fun toggleOnlineMode() {
        val current = _playerState.value.isOnlineMode
        _playerState.value = _playerState.value.copy(isOnlineMode = !current)
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val totalSeconds = minutes * 60
        _playerState.value = _playerState.value.copy(sleepTimerRemainingSeconds = totalSeconds)

        sleepTimerJob = scope.launch {
            var rem = totalSeconds
            while (rem > 0) {
                delay(1000L)
                rem--
                _playerState.value = _playerState.value.copy(sleepTimerRemainingSeconds = rem)
            }
            _playerState.value = _playerState.value.copy(isPlaying = false, sleepTimerRemainingSeconds = null)
            mediaPlayer?.pause()
            playbackJob?.cancel()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _playerState.value = _playerState.value.copy(sleepTimerRemainingSeconds = null)
    }

    // Equalizer controls
    fun toggleEqualizer(enabled: Boolean) {
        _equalizerState.value = _equalizerState.value.copy(isEnabled = enabled)
        applyEqualizerEffects()
    }

    fun selectEqualizerPreset(preset: EqualizerPreset) {
        _equalizerState.value = _equalizerState.value.copy(
            currentPreset = preset.name,
            bandGains = preset.bandGains
        )
        applyEqualizerEffects()
    }

    fun updateBandGain(bandIndex: Int, gainDb: Float) {
        val gains = _equalizerState.value.bandGains.toMutableList()
        if (bandIndex in gains.indices) {
            gains[bandIndex] = gainDb.coerceIn(-12f, 12f)
            _equalizerState.value = _equalizerState.value.copy(
                bandGains = gains,
                currentPreset = "Custom"
            )
            applyEqualizerEffects()
        }
    }

    fun setBassBoost(value: Float) {
        _equalizerState.value = _equalizerState.value.copy(bassBoost = value, bassSlider = value)
        applyEqualizerEffects()
    }

    fun setSurroundSound(value: Float) {
        _equalizerState.value = _equalizerState.value.copy(surroundSound = value, virtualizerSlider = value)
        applyEqualizerEffects()
    }

    fun toggleLoudnessEnhancer(enabled: Boolean) {
        _equalizerState.value = _equalizerState.value.copy(loudnessEnhancer = enabled)
        applyVolumeAndSpeed()
    }

    fun resetEqualizer() {
        _equalizerState.value = EqualizerState()
        applyEqualizerEffects()
    }

    private fun onSongCompleted() {
        val state = _playerState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0L)
                if (isRealMediaActive && mediaPlayer != null) {
                    try {
                        mediaPlayer?.start()
                    } catch (e: Exception) {
                        playSong(state.currentSong ?: return)
                    }
                } else {
                    playSong(state.currentSong ?: return)
                }
            }
            else -> {
                nextSong()
            }
        }
    }

    private fun setupAudioEffects(audioSessionId: Int) {
        releaseAudioEffects()
        if (audioSessionId == 0) return

        try {
            equalizerEffect = android.media.audiofx.Equalizer(0, audioSessionId).apply {
                enabled = _equalizerState.value.isEnabled
            }
            applyEqualizerEffects()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            bassBoostEffect = android.media.audiofx.BassBoost(0, audioSessionId).apply {
                val bass = _equalizerState.value.bassBoost
                setStrength((bass * 100f).toInt().coerceIn(0, 1000).toShort())
                enabled = bass > 0f
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                loudnessEnhancerEffect = android.media.audiofx.LoudnessEnhancer(audioSessionId).apply {
                    val boost = _playerState.value.volumeBoost
                    if (boost > 1.0f) {
                        val gainMb = ((boost - 1.0f) * 2000f).toInt().coerceIn(0, 8000)
                        setTargetGain(gainMb)
                        enabled = true
                    } else {
                        enabled = false
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyEqualizerEffects() {
        val eq = equalizerEffect ?: return
        val eqState = _equalizerState.value
        try {
            eq.enabled = eqState.isEnabled
            if (eqState.isEnabled) {
                val bands = eq.numberOfBands
                val gains = eqState.bandGains
                for (i in 0 until bands) {
                    val gainDb = if (i in gains.indices) gains[i] else 0f
                    val mB = (gainDb * 100f).toInt().toShort()
                    eq.setBandLevel(i.toShort(), mB)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            bassBoostEffect?.let { bb ->
                val bass = eqState.bassBoost
                bb.setStrength((bass * 100f).toInt().coerceIn(0, 1000).toShort())
                bb.enabled = eqState.isEnabled && bass > 0f
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyVolumeAndSpeed() {
        val player = mediaPlayer ?: return
        try {
            val boost = _playerState.value.volumeBoost
            val baseVol = 1.0f
            player.setVolume(baseVol, baseVol)

            try {
                loudnessEnhancerEffect?.let { enhancer ->
                    if (boost > 1.0f && _equalizerState.value.loudnessEnhancer) {
                        val gainMb = ((boost - 1.0f) * 2000f).toInt().coerceIn(0, 8000)
                        enhancer.setTargetGain(gainMb)
                        enhancer.enabled = true
                    } else {
                        enhancer.enabled = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val speed = _playerState.value.playbackSpeed
                val pitchSemitones = _playerState.value.pitchSemitones
                val pitchFactor = Math.pow(2.0, (pitchSemitones / 12.0).toDouble()).toFloat().coerceIn(0.25f, 3.0f)

                val params = player.playbackParams
                params.speed = speed
                params.pitch = pitchFactor
                player.playbackParams = params
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startPlaybackProgress() {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (_playerState.value.isPlaying) {
                delay(200L)
                val state = _playerState.value
                if (isRealMediaActive && mediaPlayer != null) {
                    try {
                        val player = mediaPlayer ?: break
                        if (player.isPlaying) {
                            val currentPos = player.currentPosition.toLong()
                            val dur = player.duration.toLong()
                            _playerState.value = state.copy(
                                currentPositionMs = currentPos,
                                durationMs = if (dur > 0) dur else state.durationMs
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    val speed = state.playbackSpeed
                    val nextPos = state.currentPositionMs + (200L * speed).toLong()

                    if (nextPos >= state.durationMs) {
                        onSongCompleted()
                        break
                    } else {
                        _playerState.value = state.copy(currentPositionMs = nextPos)
                    }
                }
            }
        }
    }

    private fun releaseAudioEffects() {
        try {
            equalizerEffect?.release()
            bassBoostEffect?.release()
            loudnessEnhancerEffect?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            equalizerEffect = null
            bassBoostEffect = null
            loudnessEnhancerEffect = null
        }
    }

    private fun releaseMediaPlayer() {
        releaseAudioEffects()
        try {
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            isRealMediaActive = false
        }
    }

    private fun startVisualizerEngine() {
        visualizerJob = scope.launch {
            var step = 0f
            while (true) {
                delay(60L)
                val isPlaying = _playerState.value.isPlaying
                val bass = _equalizerState.value.bassBoost / 10f

                step += 0.2f
                val newFreqs = List(32) { index ->
                    if (!isPlaying) {
                        0.08f + sin((index + step) * 0.3f) * 0.04f
                    } else {
                        val base = sin((index * 0.4f) + step) * 0.35f + 0.45f
                        val boost = if (index < 8) bass * 0.25f else 0f
                        val noise = Random.nextFloat() * 0.15f
                        (base + boost + noise).coerceIn(0.1f, 1.0f)
                    }
                }
                _visualizerFrequencies.value = newFreqs
            }
        }
    }

    fun release() {
        releaseMediaPlayer()
        playbackJob?.cancel()
        visualizerJob?.cancel()
        sleepTimerJob?.cancel()
    }
}
