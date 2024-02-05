package tomokao.utilitymod.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.menu.ControlsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ControlsScreen.class)
public abstract class ControlsScreenMixin extends Screen {
    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        // Remove "Done" button, since it gets in the way and the menu can be closed with Esc
        this.buttons.remove(this.buttons.size() - 1);
    }
}
