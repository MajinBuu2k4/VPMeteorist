package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import zgoly.meteorist.Meteorist;

public class AutoLoginCum extends Module {
    public static AutoLoginCum INSTANCE;

    private final Setting<String> password = settings.getDefaultGroup()
            .add(new StringSetting.Builder()
                    .name("password")
                    .description("Lệnh login sẽ được gửi tự động.")
                    .defaultValue("/login Phuc2005")
                    .build());

    private boolean loggedIn = false;
    private long loginTime = -1;
    private long lastCheckTime = 0;

    private final BlockPos LOGIN_POS = new BlockPos(0, 65, 0);

    public AutoLoginCum() {
        super(Meteorist.CATEGORY, "AutoLoginCum", "Tự động gửi lệnh /login khi đúng vị trí.");
        INSTANCE = this;
    }

    @Override
    public void onActivate() {
        loggedIn = false;
        loginTime = -1;
        lastCheckTime = System.currentTimeMillis();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        // Nếu chưa login và đang ở đúng vị trí
        if (!loggedIn && isInLoginArea()) {
            ChatUtils.sendPlayerMsg(password.get());
            loggedIn = true;
            loginTime = System.currentTimeMillis();
            return;
        }

        // Sau khi login xong 1s thì bật AutoClickLoginCum
        if (loggedIn && loginTime > 0 && System.currentTimeMillis() - loginTime >= 1000) {
            Module clickModule = Modules.get().get(AutoClickLoginCum.class);
            if (clickModule != null && !clickModule.isActive()) {
                clickModule.toggle();
                info("✅ On AutoClickLoginCum.");
            }
            loginTime = -1;
        }

        // Kiểm tra điều kiện mỗi 10 giây
        if (System.currentTimeMillis() - lastCheckTime >= 10_000) {
            lastCheckTime = System.currentTimeMillis();

            if (checkConditionMet()) {
                info("✅ Okayyy.");
            } else {
                warning("❌ Nooooooo.");
            }

            // Luôn bật AutoClickLoginCum sau khi kiểm tra xong
            Module clickModule = Modules.get().get(AutoClickLoginCum.class);
            if (clickModule != null && !clickModule.isActive()) {
                clickModule.toggle();
                info("✅ On AutoClickLoginCum sau check.");
            }
        }
    }

    @EventHandler
    private void onReceiveMsg(ReceiveMessageEvent event) {
        Text msg = event.getMessage();
        if (msg != null && msg.getString().contains("Vui lòng đăng nhập")) {
            ChatUtils.sendPlayerMsg(password.get());
            loggedIn = true;
            loginTime = System.currentTimeMillis();
        }
    }

    private boolean isInLoginArea() {
        BlockPos pos = mc.player.getBlockPos();
        return pos.equals(LOGIN_POS);
    }

    private boolean checkConditionMet() {
        // Điều kiện giả lập, bạn có thể thay bằng điều kiện khác
        return loggedIn;
    }
}
