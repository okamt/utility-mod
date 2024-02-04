package tomokao.utilitymod.mixin;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.InGame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tomokao.utilitymod.UtilityModules;
import tomokao.utilitymod.UtilityUtils;

@Mixin(InGame.class)
public abstract class InGameMixin extends DrawableHelper {
	@Inject(at = @At("RETURN"), method = "renderHud", remap = false)
	private void renderHud(CallbackInfo ci) {
		if (UtilityModules.moduleList.isEnabled()) {
			final int[] y = {2};
			UtilityModules.getModules().stream().filter(m -> m.isEnabled()).forEach(m -> {
				UtilityUtils.getMinecraft().textRenderer.drawTextWithShadow(m.id, 2, y[0], 0xffffff);
				y[0] += 10;
			});
		}
	}
}
