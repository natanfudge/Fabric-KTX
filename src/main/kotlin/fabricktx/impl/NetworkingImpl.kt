package fabricktx.impl

import drawer.readFrom
import fabricktx.api.C2SPacket
import fabricktx.api.CommonModInitializationContext
import fabricktx.api.KotlinPacket
import fabricktx.api.S2CPacket
import io.netty.buffer.Unpooled
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerialModule
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.fabricmc.loader.api.FabricLoader
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

internal fun CommonModInitializationContext.registerServerToClientPacket(
        packetId: String, packetConsumer: (PacketContext, PacketByteBuf) -> Unit
) {
    require(FabricLoader.getInstance().environmentType == EnvType.CLIENT) { "C2S packets must only be registered on the client!" }
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

internal fun CommonModInitializationContext.registerS2C(serializer: KSerializer<out S2CPacket<*>>, modId: String,
                                                        context: SerialModule) {
    // We put the modId into the packetModIds list for BOTH the client and the server, so they can both access it.
    NetworkingImpl.packetModIds[serializer] = modId
    if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
        // We register the packet itself ONLY to the client, because only the client needs to know about incoming packets.
        registerServerToClientPacket(serializer.packetId) { packetContext, packetByteBuf ->
            serializer.readFrom(packetByteBuf, context = context).apply {
                packetContext.taskQueue.execute {
                    use(packetContext)
                }
            }
        }
    }

}

internal fun CommonModInitializationContext.registerC2S(serializer: KSerializer<out C2SPacket<*>>, modId: String,
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

