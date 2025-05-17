package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.network.Http;
import zgoly.meteorist.Meteorist;

import java.util.Timer;
import java.util.TimerTask;

public class BossCs5 extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Thời gian hẹn giờ (phút)
    private final Setting<Integer> interval = sgGeneral.add(new IntSetting.Builder()
            .name("interval-minutes")
            .description("Khoảng thời gian giữa mỗi thông báo (phút).")
            .defaultValue(15)
            .min(1)
            .sliderMax(60)
            .build()
    );

    // Webhook Discord URL
    private final Setting<String> webhookUrl = sgGeneral.add(new StringSetting.Builder()
            .name("webhook-url")
            .description("URL Webhook Discord để gửi thông báo.")
            .defaultValue("https://discord.com/api/webhooks/1372778023228801054/oeJ5Z_5gVBRhFUd3xvy_axRD4Q5hfcshCvG9u73G0DM3y9yecnB_kBWx34DBetMJIenh")
            .build()
    );

    private Timer timer;

    public BossCs5() {
        super(Meteorist.CATEGORY, "boss-cs5", "Thông báo Boss Cs5 theo hẹn giờ qua Discord Webhook.");
    }

    @Override
    public void onActivate() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendWebhook("Boss Cs5 xuất hiện rồi!!!");
            }
        }, 0, interval.get() * 60 * 1000L);
        info("BossCs5 đã bật, thông báo mỗi " + interval.get() + " phút.");
    }

    @Override
    public void onDeactivate() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        info("BossCs5 đã tắt.");
    }

    private void sendWebhook(String content) {
        String url = webhookUrl.get();
        if (url == null || url.isEmpty()) {
            error("Webhook URL chưa được cấu hình.");
            return;
        }

        String json = "{\"content\": \"" + content + "\"}";

        try {
            Http.post(url)
                    .bodyJson(json)
                    .send();
            info("Đã gửi thông báo tới Discord.");
        } catch (Exception e) {
            error("Gửi webhook thất bại: " + e.getMessage());
        }
    }
}
