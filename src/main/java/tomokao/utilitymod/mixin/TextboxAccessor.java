package tomokao.utilitymod.mixin;

import net.minecraft.client.gui.widgets.Textbox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Textbox.class)
public interface TextboxAccessor {
    @Accessor("x")
    int getX();

    @Accessor("x")
    @Mutable
    void setX(int x);

    @Accessor("y")
    int getY();

    @Accessor("y")
    @Mutable
    void setY(int y);

    @Accessor
    int getWidth();

    @Accessor
    @Mutable
    void setWidth(int width);

    @Accessor
    int getHeight();

    @Accessor
    @Mutable
    void setHeight(int height);
}
