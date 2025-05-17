package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import zgoly.meteorist.Meteorist;

public class AutoQuaNether extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> slotToClick = sgGeneral.add(new IntSetting.Builder()
            .name("slot-click")
            .description("Slot để click (0 = góc trên bên trái).")
            .defaultValue(11)
            .min(0)
            .sliderMax(54)
            .build()
    );

    private final Setting<Integer> delaySeconds = sgGeneral.add(new IntSetting.Builder()
            .name("thoi-gian-cho")
            .description("Thời gian chờ sau khi hồi sinh để gửi /mine (giây).")
            .defaultValue(3)
            .min(1)
            .sliderMax(10)
            .build()
    );

    private boolean hasClicked = false;
    private boolean commandSent = false;
    private boolean inMineGui = false;
    private long respawnTime = 0;

    public AutoQuaNether() {
        super(Meteorist.CATEGORY, "auto-qua-nether", "Tự động /mine, click slot và hồi sinh.");
    }

    @Override
    public void onActivate() {
        commandSent = false;
        respawnTime = 0;
        hasClicked = false;
        inMineGui = false;

        if (mc.player != null) {
            ChatUtils.sendPlayerMsg("/mine");
            commandSent = true;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        if (mc.currentScreen instanceof DeathScreen) {
            mc.player.requestRespawn();
            commandSent = false;
            respawnTime = System.currentTimeMillis();
            return;
        }

        if (!commandSent && respawnTime > 0) {
            long elapsed = System.currentTimeMillis() - respawnTime;
            if (elapsed >= delaySeconds.get() * 1000L) {
                ChatUtils.sendPlayerMsg("/mine");
                commandSent = true;
            }
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof GenericContainerScreen) {
            String title = event.screen.getTitle().getString();
            if (title.toLowerCase().contains("mine")) {
                inMineGui = true;
                hasClicked = false;
            }
        }
    }

    @EventHandler
    private void onRender(Render2DEvent event) {
        if (!inMineGui || !(mc.currentScreen instanceof GenericContainerScreen)) return;
        if (hasClicked || mc.player == null || mc.interactionManager == null) return;

        GenericContainerScreen screen = (GenericContainerScreen) mc.currentScreen;
        ScreenHandler handler = screen.getScreenHandler();

        int slotIndex = slotToClick.get();
        if (slotIndex >= 0 && slotIndex < handler.slots.size()) {
            Slot slot = handler.getSlot(slotIndex);
            if (slot != null && slot.hasStack()) {
                mc.interactionManager.clickSlot(handler.syncId, slotIndex, 0, SlotActionType.PICKUP, mc.player);
                hasClicked = true;
                commandSent = false;
                inMineGui = false;
            }
        }
    }
}
