package brachy.modularui.integration.recipeviewer.util;

import brachy.modularui.screen.ClientScreenHandler;
import brachy.modularui.screen.ModularScreen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;

import org.jetbrains.annotations.ApiStatus;

@Deprecated
@ApiStatus.Experimental
public class RecipeScreenRenderingUtil {

    public static final IItemHandlerModifiable EMPTY_ITEM_HANDLER = new EmptyItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }
    };

    @ApiStatus.Internal
    public static void drawScreenBackground(GuiGraphicsExtractor guiGraphics, ModularScreen screen,
                                            int mouseX, int mouseY, float partialTick) {
        screen.getContext().setGraphics(guiGraphics);
        screen.getContext().updateState(mouseX, mouseY, partialTick);
        // copied from ClientScreenHandler#drawScreenInternal to
        // let us draw foreground elements separately after everything else.
        //Stencil.reset();
        //screen.getContext().getStencil().push(screen.getScreenArea());

        screen.render(guiGraphics, mouseX, mouseY, partialTick);

        ClientScreenHandler.drawVanillaElements(guiGraphics, screen.getScreenWrapper().wrappedScreen(),
                mouseX, mouseY, partialTick);

        //screen.getContext().getStencil().pop();
    }

    @ApiStatus.Internal
    public static void drawScreenForeground(GuiGraphicsExtractor guiGraphics, ModularScreen screen,
                                            int mouseX, int mouseY, float partialTick) {
        screen.getContext().setGraphics(guiGraphics);
        screen.getContext().updateState(mouseX, mouseY, partialTick);
        //screen.getContext().graphicsPose().pushPose();

        // copied from ClientScreenHandler#drawScreenInternal to
        // let us draw foreground elements separately after everything else.
        //screen.getContext().getStencil().push(screen.getScreenArea());
        screen.drawForeground(guiGraphics);

        //screen.getContext().getStencil().pop();
        //screen.getContext().graphicsPose().popPose();
    }
}
