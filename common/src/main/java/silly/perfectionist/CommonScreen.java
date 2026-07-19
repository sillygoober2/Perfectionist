package silly.perfectionist;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static silly.perfectionist.CollectionDataManager.allSurvivalStacks;
import static silly.perfectionist.Constants.*;

public class CommonScreen extends Screen {
    private final CollectionDataManager dataManager;

    private Button prevButton;
    private Button nextButton;

    public CommonScreen(CollectionDataManager dataManager){
        super(Component.literal("Item Collection"));
        this.dataManager = dataManager;
    }

    @Override
    protected void init() {
        int columns = Math.max(1, (this.width - 60) / slotSize);
        int rows = Math.max(1, (this.height - 100) / slotSize);
        itemsPerPage = columns * rows;

        int yOffset = 65;

        this.prevButton = this.addRenderableWidget(Button.builder(Component.literal("< Prev"), b -> changePage(-1))
                .bounds(this.width / 2 - 110, this.height - yOffset, 50, 20).build());
        this.prevButton.active = currentPage != 0;

        this.nextButton = this.addRenderableWidget(Button.builder(Component.literal("Next >"), b -> changePage(1))
                .bounds(this.width / 2 + 60, this.height - yOffset, 50, 20).build());
        this.nextButton.active = currentPage != getMaxPages()-1;

        this.addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - yOffset, 100, 20).build());


        int maxPages = getMaxPages();
        if (currentPage >= maxPages) {
            currentPage = maxPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }

    }

    private int getMaxPages(){
        int maxPages = Math.max(1, (int) Math.ceil((double) allSurvivalStacks.size() / itemsPerPage));
        return maxPages;
    }

    private void changePage(int amount) {
        currentPage += amount;
        currentPage = Math.clamp(currentPage,0, getMaxPages()-1);
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (this.prevButton != null) this.prevButton.active = currentPage > 0;
        if (this.nextButton != null) this.nextButton.active = currentPage < getMaxPages() - 1;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta){

        super.extractRenderState(graphics, mouseX, mouseY, delta);

        Component headerTitle = Component.literal("Item Collection (" + (currentPage + 1) + "/" + getMaxPages() + ")");

            int titleWidth = this.font.width(headerTitle);
        graphics.text(this.font, headerTitle, this.width / 2 - titleWidth / 2, 10, 0xFFFFFFFF, true);

        int barWidth = 400;
        int barHeight = 6;
        int barX = this.width / 2 - (barWidth / 2);
        int barY = this.height - 75;

        int unlockedCount = dataManager.getUnlockedAmount();
        float collectionProgress = !allSurvivalStacks.isEmpty() ? (float) unlockedCount / allSurvivalStacks.size() : 0.0f;
        int progressWidth = (int) (barWidth * collectionProgress);

        String percentageString = String.format("%.2f", collectionProgress * 100);
        String progressString = unlockedCount+"/"+allSurvivalStacks.size()+" [%"+percentageString+"] Items Discovered";

        int progressTextWidth = this.font.width(progressString);

        graphics.fill(barX, barY, barX + barWidth, barY  + barHeight, 0xFF222222);

        graphics.text(this.font, Component.literal(progressString), this.width / 2 - progressTextWidth / 2, barY - 12, 0xFFFFFFFF);

        if (progressWidth > 0) {
            graphics.fill(barX, barY, barX + progressWidth, barY + barHeight, 0xFF00FF00);
        }

        int columns = (this.width - 60) / slotSize;
        int totalGridWidth = columns * slotSize;
        int startX = (this.width- totalGridWidth) / 2;

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allSurvivalStacks.size());

        for (int i = startIndex; i < endIndex; i++) {
            Item item = allSurvivalStacks.get(i);
            String itemRegistryName = BuiltInRegistries.ITEM.getKey(item).toString();

            int relativeIndex = i - startIndex;
            int col = relativeIndex % columns;
            int row = relativeIndex / columns;
            int x = startX + (col * slotSize);
            int y = startY + (row * slotSize);

            if (y > barY - 20) {
                break;
            }

            boolean isUnlocked = dataManager.hasItem(itemRegistryName);
            int borderColor = isUnlocked ? 0xFF76ff76 : 0xFF555555;
            int backgroundColor = isUnlocked ? 0x44000000 : 0x22000000;

            graphics.fill(x, y, x + slotSize - 2, y + slotSize - 2, borderColor);
            graphics.fill(x + 1, y + 1, x + slotSize - 3, y + slotSize - 3, backgroundColor);

            graphics.item(new  ItemStack(item), x + 7, y + 7);

            if (!isUnlocked) {
                graphics.fill(x + 1, y + 1, x + slotSize - 3, y + slotSize - 3, 0xAA111111);
            }

            if (mouseX >= x && mouseX <= x + slotSize - 2 && mouseY >= y && mouseY <= y + slotSize - 2) {
                graphics.setTooltipForNextFrame(new  ItemStack(item).getHoverName(), mouseX - 2, mouseY + 5);
            }
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (Constants.openKey != null && openKey.matches(event)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(event);
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
