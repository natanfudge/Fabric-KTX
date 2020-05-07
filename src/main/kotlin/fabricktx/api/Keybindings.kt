package fabricktx.api

import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier

@PublishedApi
internal const val Misc = "key.categories.misc"

typealias ClientCallback = (MinecraftClient) -> Unit

class KotlinKeyBindingBuilder @PublishedApi internal constructor(private val id: Identifier,
                                                                 private val code: Int,
                                                                 private val type: InputUtil.Type,
                                                                 private val category: String) {
    private var onPressStart: ClientCallback? = null
    private var onReleased: ClientCallback? = null

    @PublishedApi
    internal fun build() =
            KotlinKeyBinding(id, code, type, category, onPressStart, onReleased)

    ///////// API ///////////////

    fun onPressStart(callback: ClientCallback) = apply { onPressStart = callback }
    fun onReleased(callback: ClientCallback) = apply { onReleased = callback }

    //////////////////////////////
}

class KotlinKeyBinding(id: Identifier, code: Int, type: InputUtil.Type, category: String,
                       private val onPressStart: ClientCallback?, private val onReleased: ClientCallback?)
    : FabricKeyBinding(id, type, code, category) {

    //////////////// API ////////////////
    companion object {
        inline fun create(id: Identifier,
                          code: Int,
                          type: InputUtil.Type = InputUtil.Type.KEYSYM,
                          category: String = Misc, init: KotlinKeyBindingBuilder.() -> Unit = {}) = KotlinKeyBindingBuilder(
            id, code, type, category
        ).apply(init).build()
    }
    /////////////////////////////////////////

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        if (onPressStart != null && pressed && !wasPressed()) {
            onPressStart.invoke(getMinecraftClient())
        }
        if (onReleased != null && !pressed && wasPressed()) {
            onReleased.invoke(getMinecraftClient())
        }
    }

}

