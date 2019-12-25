package fabricktx.impl

import fabricktx.api.initCommon
import fabricktx.api.registerS2CPackets

internal const val ModId = "fabric-ktx"

@Suppress("unused")
fun init() = initCommon(ModId) {
    registerS2CPackets(PlaySoundPacket.serializer(), StopSoundPacket.serializer())
}