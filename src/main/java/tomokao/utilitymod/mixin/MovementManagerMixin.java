package tomokao.utilitymod.mixin;

import net.minecraft.client.MovementManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tomokao.utilitymod.UtilityModules;

@Mixin(MovementManager.class)
public abstract class MovementManagerMixin {
    @Shadow
    private boolean[] keys = new boolean[10];

    @Inject(at = @At("HEAD"), method = "updatePlayer")
    private void updatePlayer(CallbackInfo ci) {
        if (UtilityModules.autoWalk.isEnabled()) {
            keys[0] = true;
        }
    }
}
