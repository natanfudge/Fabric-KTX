package fabricktx.impl

import drawer.readFrom
import fabricktx.api.*
import io.netty.buffer.Unpooled
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerialModule
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf


/*******
 * Fabric Api Wrappers
 ********/

internal fun CommonModInitializationContext.registerClientToServerPacket(
        packetId: String, packetConsumer: (PacketContext, PacketByteBuf) -> Unit
) {
    ServerSidePacketRegistry.INSTANCE.register(Identifier(modId, packetId), packetConsumer)
}

internal fun ClientModInitializationContext.registerServerToClientPacket(
        packetId: String, packetConsumer: (PacketContext, PacketByteBuf) -> Unit
) {
    ClientSidePacketRegistry.INSTANCE.register(Identifier(modId, packetId), packetConsumer)
}

internal fun PlayerEntity.sendPacket(packetId: Identifier, packetBuilder: PacketByteBuf.() -> Unit) {
    val packet = PacketByteBuf(Unpooled.buffer()).apply(packetBuilder)
    ServerSidePacketRegistry.INSTANCE.sendToPlayer(this, packetId, packet)

}

internal fun sendPacketToServer(packetId: Identifier, packetBuilder: PacketByteBuf.() -> Unit) {
    val packet = PacketByteBuf(Unpooled.buffer()).apply(packetBuilder)
    ClientSidePacketRegistry.INSTANCE.sendToServer(packetId, packet)
}

internal fun ClientModInitializationContext.registerS2C(serializer: KSerializer<out S2CPacket<*>>, modId : String,
                                                        context: SerialModule) {
    NetworkingImpl.packetModIds[serializer] = modId
    registerServerToClientPacket(serializer.packetId) { packetContext, packetByteBuf ->
        serializer.readFrom(packetByteBuf, context = context).apply {
            packetContext.taskQueue.execute {
                use(packetContext)
            }
        }
    }
}

internal fun CommonModInitializationContext.registerC2S(serializer: KSerializer<out C2SPacket<*>>, modId : String,
                                                        context: SerialModule) {
    NetworkingImpl.packetModIds[serializer] = modId
    registerClientToServerPacket(serializer.packetId) { packetContext, packetByteBuf ->
        serializer.readFrom(packetByteBuf, context = context).apply {
            packetContext.taskQueue.execute {
                use(packetContext)
            }
        }
    }
}

internal val <T : KotlinPacket<out T>> KSerializer<out T>.packetId get() = descriptor.name.toLowerCase()

internal object NetworkingImpl {
    val packetModIds = mutableMapOf<KSerializer<*>, String>()
}

