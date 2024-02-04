package tomokao.utilitymod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.client.options.KeyBinding;
import net.modificationstation.stationapi.api.client.event.keyboard.KeyStateChangedEvent;
import net.modificationstation.stationapi.api.client.event.option.KeyBindingRegisterEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class UtilityModules {
    private static List<Module> modules = new ArrayList();

    public static List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public static class Module {
        public final String id;
        private boolean enabled = false;
        private KeyBinding keyBinding;
        private final Consumer<Boolean> callback;

        public Module(String id, Consumer<Boolean> callback) {
            this.id = id;
            this.callback = callback;

            modules.add(this);
        }

        public Module(String id) {
            this(id, null);
        }

        public KeyBinding getKeyBinding() {
            return keyBinding;
        }

        private Module enabled() {
            setEnabled(true);
            return this;
        }

        private Module withKeyBinding(int key) {
            keyBinding = new KeyBinding(id, key);
            return this;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
            try {
                UtilityUtils.getMinecraft().overlay.addChatMessage(id + (enabled ? " enabled." : " disabled."));
            } catch (Exception e) {}
            if (callback != null) {
                callback.accept(enabled);
            }
        }

        public void toggle() {
            setEnabled(!isEnabled());
        }
    }

    public static Module moduleList = new Module("Module List").enabled().withKeyBinding(Keyboard.KEY_RSHIFT);
    public static Module autoWalk = new Module("Auto Walk");
    public static Module xray = new Module("X-ray");
    public static Module tracer = new Module("Tracer");

    @EventListener
    public void registerKeyBindings(KeyBindingRegisterEvent event) {
        List<KeyBinding> list = event.keyBindings;
        for (var module : getModules()) {
            if (module.keyBinding == null) {
                module.keyBinding = new KeyBinding(module.id, Keyboard.KEY_NONE);
            }
            list.add(module.keyBinding);
        }
    }

    @EventListener
    public void keyPressed(KeyStateChangedEvent event) {
        for (var module : UtilityModules.getModules()) {
            if (Keyboard.getEventKeyState() && Keyboard.isKeyDown(module.getKeyBinding().key)) {
                module.toggle();
            }
        }
    }
}
