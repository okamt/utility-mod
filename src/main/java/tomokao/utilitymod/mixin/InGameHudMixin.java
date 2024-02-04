package tomokao.utilitymod.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tomokao.utilitymod.UtilityModules;
import tomokao.utilitymod.UtilityUtils;

import java.util.stream.Collectors;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawContext {
	@Inject(at = @At("RETURN"), method = "render", remap = false)
	private void render(CallbackInfo ci) {
		if (UtilityModules.moduleList.isEnabled()) {
			final int[] y = {2};
			UtilityModules.getModules().stream().filter(m -> m.isEnabled()).forEach(m -> {
				UtilityUtils.getMinecraft().textRenderer.drawWithShadow(m.id, 2, y[0], 0xffffff);
				y[0] += 10;
			});
		}
	}
}
