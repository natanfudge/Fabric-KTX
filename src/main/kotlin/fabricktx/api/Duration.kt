@file:Suppress("NOTHING_TO_INLINE")

package fabricktx.api

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.World
import kotlin.time.Duration
import kotlin.time.seconds

inline val World.durationTime get() = time.ticks

const val TicksPerSecond = 20
const val TicksPerSecondD = 20.0

fun CompoundTag.putDuration(key: String, duration: Duration) = putLong(key, duration.inTicks.toLong())
fun CompoundTag.getDuration(key: String) = getLong(key).ticks

inline val Int.ticks get() = (this / TicksPerSecondD).seconds
inline val Double.ticks get() = (this / TicksPerSecondD).seconds
inline val Long.ticks get() = (this / TicksPerSecondD).seconds
inline val Duration.inTicks get() = inSeconds * TicksPerSecond

inline infix fun Duration.with(speed: Speed): Double = this.inTicks * speed.blocksPerTick

inline fun distancePassedIn(calc: () -> Double) = calc()
inline val Int.bps get() = Speed(this.d / TicksPerSecond)
inline val Double.bps get() = Speed(this / TicksPerSecond)


inline class Speed(val blocksPerTick: Double) {
    inline operator fun plus(otherSpeed: Speed) =
            Speed(this.blocksPerTick + otherSpeed.blocksPerTick)
}