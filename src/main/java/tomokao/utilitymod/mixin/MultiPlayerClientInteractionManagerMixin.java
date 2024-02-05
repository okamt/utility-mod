package tomokao.utilitymod.mixin;

import net.minecraft.client.MultiPlayerClientInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tomokao.utilitymod.UtilityModules;

@Mixin(MultiPlayerClientInteractionManager.class)
public abstract class MultiPlayerClientInteractionManagerMixin {
    @Shadow
    private int field_2614;

    @Inject(method = "digBlock", at = @At("HEAD"))
    private void digBlock(CallbackInfo ci) {
        if (UtilityModules.noBreakDelay.isEnabled()) {
            field_2614 = 0;
        }
    }
}
