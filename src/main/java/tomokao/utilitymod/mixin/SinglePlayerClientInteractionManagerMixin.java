package tomokao.utilitymod.mixin;

import net.minecraft.client.SinglePlayerClientInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tomokao.utilitymod.UtilityModules;

@Mixin(SinglePlayerClientInteractionManager.class)
public abstract class SinglePlayerClientInteractionManagerMixin {
    @Shadow
    private int hitDelay;

    @Inject(method = "digBlock", at = @At("HEAD"))
    private void digBlock(CallbackInfo ci) {
        if (UtilityModules.noBreakDelay.isEnabled()) {
            hitDelay = 0;
        }
    }
}
