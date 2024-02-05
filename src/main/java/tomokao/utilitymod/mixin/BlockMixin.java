package tomokao.utilitymod.mixin;

import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tomokao.utilitymod.UtilityModules;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    private void getBrightness(CallbackInfoReturnable<Float> cir) {
        if (UtilityModules.fullBright.isEnabled() || UtilityModules.xray.isEnabled()) {
            cir.setReturnValue(1f);
            cir.cancel();
        }
    }

    @Inject(method = "isSideRendered", at = @At("HEAD"), cancellable = true)
    private void isSideRendered(CallbackInfoReturnable<Boolean> cir) {
        if (UtilityModules.xray.isEnabled()) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
