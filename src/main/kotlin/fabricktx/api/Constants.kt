package fabricktx.api

object BlockStateUpdate {
    const val Default = 0b0000000 // 0 - DEFAULT/NONE
    const val PropagateChange = 0b0000001 // 1 - PROPAGATE_CHANGE
    const val UpdateListeners = 0b0000010 // 2 - UPDATE_LISTENERS
    const val NoRedraw = 0b0000100 // 4 - UPDATE_SILENTLY
    const val RedrawOnMainThread = 0b0001000 // 8 - UPDATE_ON_MAIN_THREAD
    const val ForceState = 0b0010000 // 16 - FORCE_STATE*
    const val SkipDrops = 0b0100000 // 32 - SKIP_DROPS
    const val IsMechanicalUpdate = 0b1000000 // 64 - IS_MECHANICAL_UPDATE

}