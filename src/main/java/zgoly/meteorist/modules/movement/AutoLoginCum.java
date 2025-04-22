package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import zgoly.meteorist.Meteorist;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.SafeWalk;
import zgoly.meteorist.modules.movement.AutoTreo;

public class AutoLoginCum extends Module {
    // Settings group
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> useSlotDelay = sgGeneral.add(new IntSetting.Builder()
            .name("use-slot-delay")
            .description("Delay giữa mỗi lần sử dụng Slot 5 (ms)")
            .defaultValue(30000) // Mặc định là 30 giây
            .min(0)
            .sliderMax(60000)
            .build()
    );

    private final Setting<String> menuTitle = sgGeneral.add(new StringSetting.Builder()
            .name("menu-title")
            .description("Tên GUI Menu")
            .defaultValue("MENU LUCKYVN NETWORK")
            .build()
    );

    private final Setting<String> serverTitle = sgGeneral.add(new StringSetting.Builder()
            .name("server-title")
            .description("Tên GUI Server")
            .defaultValue("LUCKYVN SURVIVAL")
            .build()
    );

    private final Setting<Integer> menuSlot = sgGeneral.add(new IntSetting.Builder()
            .name("menu-slot")
            .description("Slot click trong menu")
            .defaultValue(23)
            .min(0)
            .max(53)
            .build()
    );

    private final Setting<Integer> serverSlot = sgGeneral.add(new IntSetting.Builder()
            .name("server-slot")
            .description("Slot click trong server GUI")
            .defaultValue(35)
            .min(0)
            .max(53)
            .build()
    );

    private final Setting<Integer> joinDelay = sgGeneral.add(new IntSetting.Builder()
            .name("join-delay")
            .description("Delay trước khi vào cụm (ms)")
            .defaultValue(3000)
            .min(0)
            .sliderMax(10000)
            .build()
    );

    private final Setting<Boolean> enableSafeWalk = sgGeneral.add(new BoolSetting.Builder()
            .name("enable-safe-walk")
            .description("Bật Safe Walk sau khi vào cụm")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> enableAutoTreo = sgGeneral.add(new BoolSetting.Builder()
            .name("enable-auto-treo")
            .description("Bật Auto Treo sau khi vào cụm")
            .defaultValue(true)
            .build()
    );

    private long lastUseSlotTime = 0; // Thời gian sử dụng Slot 5 cuối cùng
    private enum State {
        WAITING, MENU, SERVER, JOINED
    }

    private State state = State.WAITING;
    private long joinStart = 0;

    public AutoLoginCum() {
        super(Meteorist.CATEGORY, "AutoLoginCum", "Tự động vào cụm");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        long currentTime = System.currentTimeMillis();

        switch (state) {
            case WAITING -> {
                // Kiểm tra thời gian giữa mỗi lần sử dụng slot 5
                if (currentTime - lastUseSlotTime >= useSlotDelay.get()) {
                    lastUseSlotTime = currentTime;
                    // Sử dụng Slot 5 để mở menu
                    mc.player.getInventory().selectedSlot = 4; // slot 5 (index bắt đầu từ 0)
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND); // Click chuột phải
                    info("Đã sử dụng Slot 5 và click chuột phải để mở menu.");
                    state = State.MENU;
                }
            }

            case MENU -> {
                if (!(mc.currentScreen instanceof GenericContainerScreen)) return;
                Text title = mc.currentScreen.getTitle();
                if (title != null && title.getString().equalsIgnoreCase(menuTitle.get())) {
                    clickSlot(menuSlot.get());
                    info("Đã click vào slot trong menu.");
                    state = State.SERVER;
                }
            }

            case SERVER -> {
                if (!(mc.currentScreen instanceof GenericContainerScreen)) return;
                Text title = mc.currentScreen.getTitle();
                if (title != null && title.getString().equalsIgnoreCase(serverTitle.get())) {
                    clickSlot(serverSlot.get());
                    joinStart = currentTime;
                    info("Đã click vào slot trong server GUI.");
                    state = State.JOINED;
                }
            }

            case JOINED -> {
                if (currentTime - joinStart >= joinDelay.get()) {
                    if (enableSafeWalk.get()) {
                        Module safeWalk = Modules.get().get(SafeWalk.class);
                        if (!safeWalk.isActive()) safeWalk.toggle();
                        info("Đã bật Safe Walk.");
                    }

                    if (enableAutoTreo.get()) {
                        Module autoTreo = Modules.get().get(AutoTreo.class);
                        if (!autoTreo.isActive()) autoTreo.toggle();
                        info("Đã bật Auto Treo.");
                    }

                    state = State.WAITING;
                }
            }
        }
    }

    // Click vào slot trong GUI
    private void clickSlot(int slotIndex) {
        if (mc.player == null || mc.currentScreen == null) return;
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) return;

        mc.interactionManager.clickSlot(
                screen.getScreenHandler().syncId,
                slotIndex,
                0,
                SlotActionType.PICKUP,
                mc.player
        );
        info("Đã click vào slot: " + slotIndex);
    }
}
