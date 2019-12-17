package fabricktx.impl

import fabricktx.api.initClientOnly
import fabricktx.api.registerS2CPackets

internal const val ModId = "fabric-ktx"

@Suppress("unused")
fun initClient() = initClientOnly(ModId) {
    registerS2CPackets(PlaySoundPacket.serializer(), StopSoundPacket.serializer())
}