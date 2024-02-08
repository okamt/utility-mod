package tomokao.utilitymod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widgets.Textbox;
import net.minecraft.client.options.KeyBinding;
import net.modificationstation.stationapi.api.client.event.keyboard.KeyStateChangedEvent;
import net.modificationstation.stationapi.api.client.event.option.KeyBindingRegisterEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import tomokao.utilitymod.mixin.TextboxAccessor;

import java.util.ArrayList;
import java.util.List;

import static tomokao.utilitymod.UtilityUtils.getMinecraft;

@Environment(EnvType.CLIENT)
public class UtilityScreen extends Screen {
    private static final KeyBinding keyBinding = new KeyBinding("Open Utility GUI", Keyboard.KEY_RSHIFT);
    private static final ArrayList<ModuleList> moduleLists = new ArrayList<>();

    static {
        // TODO: Split into categories
        moduleLists.add(new ModuleList("All", UtilityModules.getModules().stream().filter(m -> !(m instanceof UtilityModules.TriggerModule)).toList()));
        moduleLists.add(new ModuleList("Trigger", UtilityModules.getModules().stream().filter(m -> m instanceof UtilityModules.TriggerModule).toList()));

        int xOffset = 0;
        for (var moduleList : moduleLists) {
            moduleList.x = 4 + xOffset;
            moduleList.y = 4;
            xOffset += moduleList.width + 4;
        }
    }

    public static int gradientColor1 = 0xFFB8B8B8;
    public static int gradientColor2 = 0xFF676767;
    private final Textbox gradientColor1Textbox;
    private final Textbox gradientColor2Textbox;
    private ModuleList dragging = null;
    private int draggingOffsetX = 0;
    private int draggingOffsetY = 0;

    private static String colorToString(int color) {
        // Remove alpha channel, expose only RGB
        return String.format("%06X", color & 0xFFFFFF).toUpperCase();
    }

    private static int stringToColorOr(String string, int or) {
        try {
            // Set alpha of color to FF (fully opaque)
            return Integer.parseUnsignedInt(string, 16) | 0xFF000000;
        } catch (NumberFormatException e) {
            return or;
        }
    }

    // Empty constructor for StationAPI event bus only, should never be actually used
    public UtilityScreen() {
        width = 0;
        height = 0;
        gradientColor1Textbox = null;
        gradientColor2Textbox = null;
    }

    public UtilityScreen(int width, int height) {
        this.width = width;
        this.height = height;
        // x and y values don't matter here, they are set every time before drawing the text boxes
        gradientColor1Textbox = new Textbox(this, getMinecraft().textRenderer, 0, 0, 64, 20, colorToString(gradientColor1));
        gradientColor2Textbox = new Textbox(this, getMinecraft().textRenderer, 0, 0, 64, 20, colorToString(gradientColor2));
        gradientColor1Textbox.setMaxLength(6);
        gradientColor2Textbox.setMaxLength(6);
    }

    private static class ModuleList extends DrawableHelper {
        public final String name;
        public int x;
        public int y;
        public final int width = 64 + 32;
        public final int nameBarHeight = 14;
        private final int listHeight;
        private final List<UtilityModules.Module> modules;
        public boolean folded = false;

        public ModuleList(String name, List<UtilityModules.Module> modules) {
            this.name = name;
            this.modules = modules;
            listHeight = modules.size() * nameBarHeight;
        }

        public int getTotalHeight() {
            return nameBarHeight + listHeight;
        }

        private void drawRectangleLine(int x, int y, int width, int height, int color) {
            fill(x, y, x + width + 1, y + 1, color);
            fill(x, y, x + 1, y + height + 1, color);
            fill(x + width, y, x + width + 1, y + height + 1, color);
            fill(x, y + height, x + width + 1, y + height + 1, color);
        }

        public void render() {
            var textRenderer = getMinecraft().textRenderer;

            // Draw dark transparent background for module list
            if (!folded) {
                // Color values taken from Screen.renderBackground
                fillGradient(x, y, x + width, y + getTotalHeight(), -1072689136, -804253680);
            }

            // Draw name bar
            fillGradient(x, y, x + width, y + nameBarHeight, gradientColor1, gradientColor2);
            drawTextWithShadow(textRenderer, name, x + 4, y + 4, 0xFFFFFFFF);
            var foldIcon = folded ? ">" : "V";
            drawTextWithShadow(textRenderer, foldIcon, x + width - textRenderer.getTextWidth(foldIcon) - 4, y + 4, 0xFFFFFFFF);
            drawRectangleLine(x, y, width, nameBarHeight, 0xFFFFFFFF);

            // Draw modules
            if (!folded) {
                int yOffset = nameBarHeight;
                for (var module : modules) {
                    int color = (module.isEnabled() || module instanceof UtilityModules.TriggerModule) ? 0xFFFFFFFF : 0xFF808080;
                    drawTextWithShadow(textRenderer, module.id, x + 4, y + 4 + yOffset, color);
                    yOffset += nameBarHeight;
                }
                drawRectangleLine(x, y + nameBarHeight, width, listHeight, 0xFFFFFFFF);
            }
        }

        private boolean mouseClicked(UtilityScreen screen, int clickX, int clickY, int button) {
            // Return value:
            // true = event was utilized
            // false = event was not utilized, propagate to the next GUI elements

            boolean inNameBar = clickX >= x && clickX < x + width && clickY >= y && clickY < y + nameBarHeight;
            boolean inModuleList = clickX >= x && clickX < x + width && clickY >= y + nameBarHeight && clickY < y + getTotalHeight();
            if (button == 0) { // Left click
                if (screen.dragging == null && inNameBar) {
                    screen.dragging = this;
                    screen.draggingOffsetX = clickX - x;
                    screen.draggingOffsetY = clickY - y;
                    return true;
                } else if (inModuleList) {
                    var index = (clickY - y - nameBarHeight) / nameBarHeight;
                    modules.get(index).toggle();
                    return true;
                }
            } else if (button == 1) { // Right click
                if (inNameBar) {
                    folded = !folded;
                } else if (inModuleList) {
                    // TODO: Module config GUI
                }
            }
            return false;
        }

        private void snapToBorder(Screen screen) {
            // Snap name bar of the module list back inside the window
            if (x < 0) x = 0;
            if (y < 0) y = 0;
            if (x + width >= screen.width) x = screen.width - width - 1;
            if (y + nameBarHeight >= screen.height) y = screen.height - nameBarHeight - 1;
        }
    }

    @Override
    public void render(int i, int j, float f) {
        // Keep text boxes anchored to bottom right of the screen
        ((TextboxAccessor) gradientColor1Textbox).setX(width - 64 - 4);
        ((TextboxAccessor) gradientColor1Textbox).setY(height - 20 - 4 - 20 - 4);
        ((TextboxAccessor) gradientColor2Textbox).setX(width - 64 - 4);
        ((TextboxAccessor) gradientColor2Textbox).setY(height - 20 - 4);

        gradientColor1 = stringToColorOr(gradientColor1Textbox.getText().trim(), gradientColor1);
        gradientColor2 = stringToColorOr(gradientColor2Textbox.getText().trim(), gradientColor2);

        gradientColor1Textbox.draw();
        gradientColor2Textbox.draw();

        // Reverse iteration on module lists, since mouse events get passed in forward order
        // This makes it so the lists rendered on top are the ones that receive the mouse events first
        for (int m = moduleLists.size() - 1; m >= 0; m--) {
            var moduleList = moduleLists.get(m);
            moduleList.snapToBorder(this);
            moduleList.render();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void keyPressed(char c, int i) {
        if (gradientColor1Textbox.selected) {
            gradientColor1Textbox.keyPressed(c, i);
        } else if (gradientColor2Textbox.selected) {
            gradientColor2Textbox.keyPressed(c, i);
        }

        super.keyPressed(c, i);
    }

    @Override
    protected void mouseClicked(int clickX, int clickY, int button) {
        // 0 = left button, 1 = right button, 2 = wheel button
        if (button < 0) {
            return;
        }

        for (var moduleList : moduleLists) {
            // Module lists can be on top of each other, so the first one to actually utilize the event stops it from
            // propagating further
            if (moduleList.mouseClicked(this, clickX, clickY, button)) {
                return;
            }
        }

        gradientColor1Textbox.mouseClicked(clickX, clickY, button);
        gradientColor2Textbox.mouseClicked(clickX, clickY, button);
    }

    @Override
    public void mouseReleased(int clickX, int clickY, int button) {
        // For some reason, this function is sometimes called with button -1
        if (button < 0) {
            return;
        }

        if (dragging != null) {
            dragging.snapToBorder(this);
            dragging = null;
        }
    }

    @Override
    public void onMouseEvent() {
        // Copied from super, we need to keep the x and y calculated there
        int x;
        int y;
        if (Mouse.getEventButtonState()) {
            x = Mouse.getEventX() * width / minecraft.actualWidth;
            y = height - Mouse.getEventY() * height / minecraft.actualHeight - 1;
            mouseClicked(x, y, Mouse.getEventButton());
        } else {
            x = Mouse.getEventX() * width / minecraft.actualWidth;
            y = height - Mouse.getEventY() * height / minecraft.actualHeight - 1;
            mouseReleased(x, y, Mouse.getEventButton());
        }

        if (dragging != null) {
            dragging.x = x - draggingOffsetX;
            dragging.y = y - draggingOffsetY;
        }
    }

    @EventListener
    public void registerKeyBindings(KeyBindingRegisterEvent event) {
        event.keyBindings.add(keyBinding);
    }

    @EventListener
    public void keyPressed(KeyStateChangedEvent ignoredEvent) {
        if (Keyboard.getEventKey() == keyBinding.key && Keyboard.getEventKeyState()) {
            var currentScreen = getMinecraft().currentScreen;
            if (currentScreen instanceof UtilityScreen) {
                getMinecraft().openScreen(null);
                getMinecraft().lockCursor();
            } else if (currentScreen == null) {
                getMinecraft().openScreen(new UtilityScreen(getMinecraft().actualWidth, getMinecraft().actualHeight));
            }
        }
    }
}
