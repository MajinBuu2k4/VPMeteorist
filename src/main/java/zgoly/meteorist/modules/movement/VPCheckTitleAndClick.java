package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import zgoly.meteorist.Meteorist;

public class VPCheckTitleAndClick extends Module {

    // ========== Settings ==========
    public static final Setting<String> guiTitle = new StringSetting.Builder()
        .name("gui-title")
        .description("Tiêu đề GUI cần check.")
        .defaultValue("LUCKYVN")
        .build();

    public static final Setting<Integer> slotToClick = new IntSetting.Builder()
        .name("slot")
        .description("Slot để click (1 là slot đầu tiên).")
        .defaultValue(1)
        .min(1)
        .sliderMax(54)
        .build();

    public static final Setting<Boolean> autoDisable = new BoolSetting.Builder()
        .name("auto-disable")
        .description("Tự động tắt module sau khi click.")
        .defaultValue(true)
        .build();

    // ========== Trạng thái ==========
    public static boolean hasClicked = false;

    // ========== Constructor ==========
    public VPCheckTitleAndClick() {
        super(Meteorist.CATEGORY, "vp-check-title-and-click", "Kiểm tra GUI theo tiêu đề và click slot nếu khớp.");
    }

    // ========== Sự kiện mở GUI ==========
    @EventHandler
    public static void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof GenericContainerScreen) {
            String title = event.screen.getTitle().getString();
            if (title.contains(guiTitle.get())) {
                hasClicked = false;
            }
        }
    }

    // ========== Tick render để xử lý click ==========
    @EventHandler
    public static void onRender(Render2DEvent event) {
        if (!(mc.currentScreen instanceof GenericContainerScreen)) return;

        String title = mc.currentScreen.getTitle().getString();
        if (!title.contains(guiTitle.get())) return;
        if (hasClicked) return;

        GenericContainerScreen screen = (GenericContainerScreen) mc.currentScreen;
        ScreenHandler handler = screen.getScreenHandler();

        int slotIndex = slotToClick.get() - 1; // vì người dùng nhập 1 là slot 0
        if (slotIndex >= 0 && slotIndex < handler.slots.size()) {
            Slot slot = handler.getSlot(slotIndex);

            if (slot != null && slot.hasStack()) {
                mc.interactionManager.clickSlot(handler.syncId, slotIndex, 0, SlotActionType.PICKUP, mc.player);
                hasClicked = true;

                if (autoDisable.get()) {
                    toggle(); // tắt module
                }
            }
        }
    }
}
