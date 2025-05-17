package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.text.Text;
import zgoly.meteorist.Meteorist;

public class AutoTraLoi extends Module {
    private final Setting<String> triggerName = settings.getDefaultGroup()
            .add(new StringSetting.Builder()
                    .name("Noi dung dieu kien")
                    .description("Noi dung phat hien")
                    .defaultValue("@MajinBuu2k4")
                    .build()
            );

    private final Setting<String> replyMessage = settings.getDefaultGroup()
            .add(new StringSetting.Builder()
                    .name("Tin nhan tra loi")
                    .description("Tin auto tra loi.")
                    .defaultValue("Ê có gì hong 😎")
                    .build()
            );

    public AutoTraLoi() {
        super(Meteorist.CATEGORY, "AutoTraLoi", "Auto tra loi tin nhan khi du du kien.");
    }

    @EventHandler
    private void onReceiveMsg(ReceiveMessageEvent event) {
        if (mc.player == null || mc.world == null) return;

        Text message = event.getMessage();
        if (message == null) return;

        String content = message.getString();
        if (content.contains(triggerName.get())) {
            mc.player.networkHandler.sendChatMessage(replyMessage.get());
        }
    }
}
