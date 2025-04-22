package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import zgoly.meteorist.Meteorist;

import static org.lwjgl.glfw.GLFW.*;

public class SaveTP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Keybind> saveKey = sgGeneral.add(new KeybindSetting.Builder()
            .name("save-key")
            .description("Phím để lưu vị trí hiện tại.")
            .defaultValue(Keybind.fromKey(GLFW_KEY_EQUAL))
            .build()
    );

    private final Setting<Keybind> tpKey = sgGeneral.add(new KeybindSetting.Builder()
            .name("tp-key")
            .description("Phím để teleport tới vị trí đã lưu.")
            .defaultValue(Keybind.fromKey(GLFW_KEY_R))
            .build()
    );

    public static BlockPos savedPos = null;

    public SaveTP() {
        super(Meteorist.CATEGORY, "save-tp", "Lưu vị trí hiện tại và teleport về đó khi bấm phím.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // Lưu vị trí hiện tại khi bấm phím save
        if (saveKey.get().isPressed()) {
            savedPos = mc.player.getBlockPos();
            info("Đã lưu tọa độ: " + savedPos.getX() + ", " + savedPos.getY() + ", " + savedPos.getZ());
        }

        // Teleport lại khi bấm phím tp
        if (tpKey.get().isPressed()) {
            if (savedPos != null) {
                double x = savedPos.getX() + 0.5;
                double y = savedPos.getY();
                double z = savedPos.getZ() + 0.5;
                mc.player.setPosition(x, y, z);
                info("Đã teleport đến vị trí đã lưu.");
            } else {
                warning("Chưa có vị trí nào được lưu.");
            }
        }
    }
}
