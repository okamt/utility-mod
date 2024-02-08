package tomokao.utilitymod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.block.Block;
import net.minecraft.client.options.KeyBinding;
import net.modificationstation.stationapi.api.client.event.keyboard.KeyStateChangedEvent;
import net.modificationstation.stationapi.api.client.event.option.KeyBindingRegisterEvent;
import net.modificationstation.stationapi.api.event.registry.BlockRegistryEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import static tomokao.utilitymod.UtilityUtils.getMinecraft;

@Environment(EnvType.CLIENT)
public class UtilityModules {
    private static final ArrayList<Module> modules = new ArrayList<>();

    public static List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public static class Module {
        public final String id;
        protected boolean enabled = false;
        protected KeyBinding keyBinding;
        protected final Consumer<Boolean> callback;

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

        protected Module enabled() {
            setEnabled(true);
            return this;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
            try {
                getMinecraft().overlay.addChatMessage(id + (enabled ? " §aenabled" : " §4disabled"));
                getMinecraft().soundHelper.playSound("random.click", 1.0F, enabled ? 1.0F : 0.5F);
            } catch (Exception ignored) {
            }
            if (callback != null) {
                callback.accept(enabled);
            }
        }

        public void toggle() {
            setEnabled(!isEnabled());
        }

        public Object getProperties() {
            return null;
        }
    }

    public static class TriggerModule extends Module {
        protected final Runnable callback;

        public TriggerModule(String id, Runnable callback) {
            super(id);
            this.callback = callback;
        }

        @Override
        protected Module enabled() {
            throw new IllegalStateException("can't call enabled() on a trigger module, there is no enabled state");
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public void setEnabled(boolean enabled) {
            if (!enabled) return;
            trigger();
        }

        @Override
        public void toggle() {
            trigger();
        }

        public void trigger() {
            try {
                getMinecraft().overlay.addChatMessage(id + " §etriggered");
                getMinecraft().soundHelper.playSound("random.click", 1.0F, 1.0F);
            } catch (Exception ignored) {
            }
            callback.run();
        }
    }

    public static class XrayModule extends Module {
        public final HashSet<Integer> blockIdSet = new HashSet<>();
        private boolean isWhitelist = true;

        public XrayModule(String id, Consumer<Boolean> callback) {
            super(id, callback);
        }

        private void registerDefaultSet() {
            Collections.addAll(
                    blockIdSet,
                    Block.DIAMOND_ORE.id,
                    Block.COAL_ORE.id,
                    Block.GOLD_ORE.id,
                    Block.IRON_ORE.id,
                    Block.REDSTONE_ORE.id,
                    Block.REDSTONE_ORE_LIT.id,
                    Block.LAPIS_LAZULI_ORE.id,

                    Block.STILL_LAVA.id,
                    Block.FLOWING_LAVA.id,
                    Block.STILL_WATER.id,
                    Block.FLOWING_WATER.id,

                    Block.CHEST.id,
                    Block.LOCKED_CHEST.id,
                    Block.MOB_SPAWNER.id,
                    Block.FURNACE.id,
                    Block.WORKBENCH.id,
                    Block.WOOD_DOOR.id,
                    Block.IRON_DOOR.id
            );
        }

        public boolean isWhitelist() {
            return isWhitelist;
        }

        public void setIsWhitelist(boolean isWhitelist) {
            this.isWhitelist = isWhitelist;
        }

        public boolean shouldRender(int blockId) {
            return isWhitelist == blockIdSet.contains(blockId);
        }
    }

    public static final Module moduleList = new Module("Module List").enabled();
    public static final Module autoWalk = new Module("Auto Walk", enabled -> {
        if (!enabled) {
            var minecraft = getMinecraft();
            minecraft.player.playerKeypressManager.onKeyPressed(minecraft.options.forwardKey.key, false);
        }
    });
    public static final XrayModule xray = new XrayModule("X-Ray", enabled -> getMinecraft().levelRenderer.updateFromOptions());
    public static final Module fullBright = new Module("Full Bright", enabled -> getMinecraft().levelRenderer.updateFromOptions());
    public static final Module noBreakDelay = new Module("No Break Delay");
    public static final Module getSeed = new TriggerModule("Get Seed", () -> {
        var seed = Long.toString(getMinecraft().level.getSeed());
        getMinecraft().player.sendMessage(seed + " (copied to clipboard)");
        try {
            StringSelection selection = new StringSelection(seed);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        } catch (Exception ignore) {
        }
    });

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
    public void keyPressed(KeyStateChangedEvent ignoredEvent) {
        if (getMinecraft().currentScreen != null) {
            return;
        }

        for (var module : UtilityModules.getModules()) {
            if (module.getKeyBinding().key == Keyboard.KEY_NONE) continue;

            if (Keyboard.getEventKey() == module.getKeyBinding().key && Keyboard.getEventKeyState()) {
                module.toggle();
            }
        }
    }

    @EventListener
    public void xrayRegisterDefaultSet(BlockRegistryEvent event) {
        xray.registerDefaultSet();
    }
}
