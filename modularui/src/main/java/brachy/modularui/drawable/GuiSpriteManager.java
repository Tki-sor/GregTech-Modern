package brachy.modularui.drawable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.NotNull;

/**
 * 1.21 has this class in vanilla, it can be used via {@code Minecraft.getGuiSprites()}.<br>
 * Here in 1.20 land, though, we have to implement it ourselves.
 * <p>
 * Note that the atlas JSON <i>should</i> be kept, as MC 1.21 only adds textures in gui/sprites to the atlas. We want all of them.
 */
public class GuiSpriteManager {

    public static final Identifier LOCATION_GUI = Identifier.withDefaultNamespace("textures/atlas/gui.png");

    private static final GuiSpriteManager instance = new GuiSpriteManager();

    public GuiSpriteManager(TextureManager textureManager) {}

    private GuiSpriteManager() {}

    public static GuiSpriteManager getInstance() {
        return instance;
    }

    /**
     * Gets a sprite associated with the passed resource location.
     */
    public @NotNull TextureAtlasSprite getSprite(@NotNull Identifier location) {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI).getSprite(location);
    }

}
