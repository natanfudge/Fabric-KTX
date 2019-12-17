@file:UseSerializers(ForSoundEvent::class, ForVec3d::class)

package fabricktx.api

import drawer.ForSoundEvent
import drawer.ForVec3d
import fabricktx.impl.StopSoundPacket
import fabricktx.impl.playersThatCanHear
import fabricktx.impl.sendToNearbyClients
import fabricktx.impl.toClientOnly
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World


fun World.playSound(soundInstance: CommonPositionedSoundInstance) {
    if (world.isClient) {
        val clientSound = soundInstance.toClientOnly()
        getMinecraftClient().soundManager.play(clientSound)
    } else soundInstance.sendToNearbyClients(world/*, keyForStopping*/)

}

//TODO: note, this system is pretty bad. It doesn't work for new people coming in, it doesn't stop when they leave the area,
// it might prevent one sound from stopping on time if sounds overlap, and more. make a lib.
/**
 * @param originalVolume Make sure to pass the same value as in [CommonPositionedSoundInstance] if that's customized
 */
fun World.stopSound(soundKey: Identifier, pos: Vec3d, originalVolume: Float = 1.0f) {
    if (world.isClient) {
        //TODO: use stopSounds instead
        getMinecraftClient().soundManager.stopSounds(soundKey, null)
    } else playersThatCanHear(pos, originalVolume).sendPacket(
        StopSoundPacket(
            pos,
            soundKey
        )
    )

}

enum class CommonAttenuationType {
    NONE,
    LINEAR;
}

@Serializable
data class CommonPositionedSoundInstance(
    val soundEvent: SoundEvent,
    val category: SoundCategory,
    val pos: Vec3d,
    val volume: Float = 1.0f,
    val pitch: Float = 1.0f,
    val repeats: Boolean = false,
    val repeatDelay: Int = 0,
    val attenuationType: CommonAttenuationType = CommonAttenuationType.LINEAR,
    val relative: Boolean = false
)