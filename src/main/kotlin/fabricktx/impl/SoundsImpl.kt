@file:UseSerializers(ForVec3d::class, ForIdentifier::class)

package fabricktx.impl

import drawer.ForIdentifier
import drawer.ForVec3d
import fabricktx.api.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.client.sound.SoundInstance
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.max

@Serializable
internal data class PlaySoundPacket(val soundInstance: CommonPositionedSoundInstance) : S2CPacket<PlaySoundPacket> {
    override val serializer get() = serializer()
    override fun use(context: PacketContext) = context.world.playSound(soundInstance)
}

@Serializable
internal data class StopSoundPacket(val pos: Vec3d, val soundId: Identifier) : S2CPacket<StopSoundPacket> {
    override val serializer get() = serializer()
    override fun use(context: PacketContext) = context.world.stopSound(soundId, pos)
}


private const val MaxSoundDistance = 16.0
internal fun World.playersThatCanHear(pos: Vec3d, volume: Float) = PlayerStream.around(
    this, pos, max(MaxSoundDistance, MaxSoundDistance * volume)
)

internal fun CommonPositionedSoundInstance.toClientOnly() = ClientBuilders.soundInstance(
    soundEvent, category, pos, volume, pitch, repeats, repeatDelay,
    when (attenuationType) {
        CommonAttenuationType.NONE -> SoundInstance.AttenuationType.NONE
        CommonAttenuationType.LINEAR -> SoundInstance.AttenuationType.LINEAR
    },
    relative
)

internal fun CommonPositionedSoundInstance.sendToNearbyClients(world: World) = world.playersThatCanHear(pos, volume)
    .sendPacket(PlaySoundPacket(this))