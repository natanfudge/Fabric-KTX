package fabricktx.api

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos


private const val RerenderFlag = 8

fun MinecraftClient.drawCenteredStringWithoutShadow(textRenderer: TextRenderer, string: String, x: Int, y: Int, color: Int){
    textRenderer.draw(string, (x - textRenderer.getStringWidth(string) / 2).toFloat(), y.toFloat(), color)
}

fun MinecraftClient.playButtonClickSound() = soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))

fun MinecraftClient.scheduleRenderUpdate(pos: BlockPos) = worldRenderer.updateBlock(
    null, pos, null, null, RerenderFlag
)


fun getMinecraftClient(): MinecraftClient = MinecraftClient.getInstance()




