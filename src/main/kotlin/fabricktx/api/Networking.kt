package fabricktx.api

import drawer.write
import fabricktx.impl.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.world.World
import java.util.stream.Stream


interface KotlinPacket<T : KotlinPacket<T>> {
    val serializer: KSerializer<T>
    fun use(context: PacketContext)
    val serializationContext: SerialModule get() = EmptyModule
}

interface C2SPacket<T : KotlinPacket<T>> : KotlinPacket<T>

interface S2CPacket<T : KotlinPacket<T>> : KotlinPacket<T>

interface TwoSidedPacket<T : KotlinPacket<T>> : S2CPacket<T>, C2SPacket<T>


/******************************
 * Automatic Serializer Wrappers
 ******************************/

fun CommonModInitializationContext.registerS2CPackets(vararg serializers: KSerializer<out S2CPacket<*>>,
                                                      context: SerialModule = EmptyModule) {
    for (serializer in serializers) registerS2C(serializer, modId, context)
}

fun CommonModInitializationContext.registerC2SPackets(vararg serializers: KSerializer<out C2SPacket<*>>,
                                                      context: SerialModule = EmptyModule) {
    for (serializer in serializers) registerC2S(serializer, modId, context)
}


/**
 * Sends a packet from the server to the client for all the players in the stream.
 */
fun <T : S2CPacket<T>> Stream<PlayerEntity>.sendPacket(packet: T) {
    for (player in this) player.sendPacket(packet)
}

/**
 * Sends a packet from the server to the client for all the players in the stream.
 */
fun <T : S2CPacket<T>> PlayerEntity.sendPacket(packet: T) {
    val serializer = packet.serializer
    val modId = NetworkingImpl.packetModIds[serializer]
    require(modId != null) { "Packet with serializer $serializer was not registered!" }

    sendPacket(packetId = Identifier(modId, serializer.packetId)) {
        packet.serializer.write(packet, this, context = packet.serializationContext)
    }
}

/**
 * Sends a packet from the server to the client for all the players in the stream.
 */
fun <T : C2SPacket<T>> sendPacketToServer(packet: T) {
    val serializer = packet.serializer
    val modId = NetworkingImpl.packetModIds[serializer]
    require(modId != null) { "Packet with serializer $serializer was not registered!" }

    sendPacketToServer(Identifier(modId, serializer.packetId)) {
        packet.serializer.write(packet, this, context = packet.serializationContext)
    }
}

val PacketContext.world: World get() = player.world




