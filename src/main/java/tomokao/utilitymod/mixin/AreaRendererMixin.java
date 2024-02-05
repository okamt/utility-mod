package tomokao.utilitymod.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.render.AreaRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tomokao.utilitymod.UtilityModules;

@Mixin(AreaRenderer.class)
public abstract class AreaRendererMixin {
    @Inject(method = "update()V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/level/BlockView;getBlockId(III)I"))
    private void update(CallbackInfo ci, @Local(index = 18) LocalIntRef blockId) {
        if (UtilityModules.xray.isEnabled() && !UtilityModules.xray.shouldRender(blockId.get())) {
            blockId.set(0);
        }
    }
}
