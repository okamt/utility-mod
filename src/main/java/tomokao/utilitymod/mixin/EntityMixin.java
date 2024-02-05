package tomokao.utilitymod.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tomokao.utilitymod.UtilityModules;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "getBrightnessAtEyes", at = @At("HEAD"), cancellable = true)
    private void getBrightnessAtEyes(CallbackInfoReturnable<Float> cir) {
        if (UtilityModules.fullBright.isEnabled() || UtilityModules.xray.isEnabled()) {
            cir.setReturnValue(1f);
            cir.cancel();
        }
    }
}
