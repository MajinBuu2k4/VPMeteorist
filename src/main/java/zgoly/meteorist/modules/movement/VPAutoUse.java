package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.TimerUtils;
import meteordevelopment.meteorclient.MeteorClient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class VPAutoUse extends Module {
    public static final Category VP_CATEGORY = new Category("VP-Movement");

    public static final MinecraftClient mc = MinecraftClient.getInstance();

    // Slot to switch to
    public static final Setting<Integer> slot = MeteorClient.SETTINGS.create(new IntSetting.Builder()
        .name("slot")
        .description("Slot to switch to (0-8).")
        .defaultValue(5)
        .min(0)
        .max(8)
        .build()
    );

    // Delay time
    public static final Setting<Integer> delay = MeteorClient.SETTINGS.create(new IntSetting.Builder()
        .name("delay-ms")
        .description("Delay after switching slot before using.")
        .defaultValue(1000)
        .min(0)
        .sliderMax(5000)
        .build()
    );

    // Whether to use the item
    public static final Setting<Boolean> doUse = MeteorClient.SETTINGS.create(new BoolSetting.Builder()
        .name("use-item")
        .description("Whether to right click the item after delay.")
        .defaultValue(true)
        .build()
    );

    // Internal timer
    private final TimerUtils timer = new TimerUtils();

    private boolean didSwitch = false;

    public VPAutoUse() {
        super(VP_CATEGORY, "vp-auto-use", "Tự động chuyển slot và sử dụng item.");
    }

    @Override
    public void onActivate() {
        didSwitch = false;
        timer.reset();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;

        if (!didSwitch) {
            mc.player.getInventory().selectedSlot = slot.get();
            didSwitch = true;
            timer.reset();
        } else if (doUse.get() && timer.hasReached(delay.get())) {
            ItemStack stack = mc.player.getMainHandStack();
            if (!stack.isEmpty()) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
            toggle(); // Tắt module sau khi dùng
        }
    }
}
