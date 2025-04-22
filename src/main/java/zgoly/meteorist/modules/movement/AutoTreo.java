package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.SafeWalk;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.modules.movement.SaveTP;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;

public class AutoTreo extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> loopDelay = sgGeneral.add(new IntSetting.Builder()
            .name("thoi-gian-vong-lap")
            .description("Thoi gian cho giua moi vong lap (ms).")
            .defaultValue(280000)
            .min(1000)
            .sliderMax(600000)
            .build()
    );

    private final Setting<Integer> mobCheckRange = sgGeneral.add(new IntSetting.Builder()
            .name("pham-vi-kiem-tra-mob")
            .description("Pham vi kiem tra mob xung quanh (block).")
            .defaultValue(5)
            .min(1)
            .sliderMax(20)
            .build()
    );

    private final Setting<Integer> mobCheckDelay = sgGeneral.add(new IntSetting.Builder()
            .name("delay-kiem-tra-mob")
            .description("Delay giua moi lan kiem tra mob (ms).")
            .defaultValue(500)
            .min(100)
            .sliderMax(5000)
            .build()
    );

    private final Setting<Integer> forwardTime1 = sgGeneral.add(new IntSetting.Builder()
            .name("di-chuyen-tien-1")
            .description("Thoi gian di chuyen tien lan 1 (ms).")
            .defaultValue(1000)
            .min(100)
            .sliderMax(5000)
            .build()
    );

    private final Setting<Integer> waitAfterForward1 = sgGeneral.add(new IntSetting.Builder()
            .name("cho-sau-tien-1")
            .description("Thoi gian cho sau khi di tien lan 1 (ms).")
            .defaultValue(1000)
            .min(100)
            .sliderMax(5000)
            .build()
    );

    private final Setting<Integer> backwardTime = sgGeneral.add(new IntSetting.Builder()
            .name("di-chuyen-lui")
            .description("Thoi gian di chuyen lui (ms).")
            .defaultValue(1000)
            .min(100)
            .sliderMax(5000)
            .build()
    );

    private final Setting<Integer> forwardTime2 = sgGeneral.add(new IntSetting.Builder()
            .name("di-chuyen-tien-2")
            .description("Thoi gian di chuyen tien lan 2 (ms).")
            .defaultValue(1000)
            .min(100)
            .sliderMax(5000)
            .build()
    );

    private final Setting<Integer> waitAfterForward2 = sgGeneral.add(new IntSetting.Builder()
            .name("cho-sau-tien-2")
            .description("Thoi gian cho sau khi di tien lan 2 (ms).")
            .defaultValue(1000)
            .min(100)
            .sliderMax(5000)
            .build()
    );

    private long lastLoopTime = 0;
    private long waitStart = 0;
    private boolean runningAction = false;
    private int actionStep = 0;

    private int lastCountdownSecond = -1;

    public AutoTreo() {
        super(Meteorist.CATEGORY, "auto-treo", "Tu dong treo lap lai hanh dong neu khong co mob xung quanh.");
    }

    @Override
    public void onActivate() {
        lastLoopTime = System.currentTimeMillis();
        waitStart = 0;
        actionStep = 0;
        runningAction = false;
        lastCountdownSecond = -1;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();

        // Hiển thị đếm ngược từ 10s cuối
        if (!runningAction) {
            long timeSinceLastLoop = now - lastLoopTime;
            long remaining = loopDelay.get() - timeSinceLastLoop;

            if (remaining <= 10_000 && remaining > 0) {
                int sec = (int) (remaining / 1000) + 1;
                if (sec != lastCountdownSecond) {
                    lastCountdownSecond = sec;
                    warning("Còn " + sec + "s trước khi lặp lại hành động.");
                }
            }
        }

        if (!runningAction && now - lastLoopTime >= loopDelay.get()) {
            if (hasMobNearby()) {
                if (now - waitStart >= mobCheckDelay.get()) {
                    waitStart = now;
                }
                return;
            }

            runningAction = true;
            actionStep = 0;
            waitStart = now;
        }

        if (!runningAction) return;

        switch (actionStep) {
            case 0 -> {
                mc.options.forwardKey.setPressed(true);
                if (now - waitStart >= forwardTime1.get()) {
                    mc.options.forwardKey.setPressed(false);
                    waitStart = now;
                    actionStep++;
                }
            }

            case 1 -> {
                if (now - waitStart >= waitAfterForward1.get()) {
                    waitStart = now;
                    actionStep++;
                }
            }

            case 2 -> {
                mc.options.backKey.setPressed(true);
                if (now - waitStart >= backwardTime.get()) {
                    mc.options.backKey.setPressed(false);
                    waitStart = now;
                    actionStep++;
                }
            }

            case 3 -> {
                SaveTP saveTP = Modules.get().get(SaveTP.class);
                BlockPos pos = saveTP.savedPos;

                if (pos != null) {
                    double x = pos.getX() + 0.5;
                    double y = pos.getY();
                    double z = pos.getZ() + 0.5;
                    mc.player.setPosition(x, y, z);
                    info("Đã teleport đến vị trí đã lưu từ SaveTP.");
                } else {
                    warning("Chưa có vị trí nào được lưu trong SaveTP.");
                }

                waitStart = now;
                actionStep++;
            }

            case 4 -> {
                Module sw = Modules.get().get(SafeWalk.class);
                if (!sw.isActive()) sw.toggle();
                waitStart = now;
                actionStep++;
            }

            case 5 -> {
                mc.options.forwardKey.setPressed(true);
                if (now - waitStart >= forwardTime2.get()) {
                    mc.options.forwardKey.setPressed(false);
                    waitStart = now;
                    actionStep++;
                }
            }

            case 6 -> {
                if (now - waitStart >= waitAfterForward2.get()) {
                    waitStart = now;
                    actionStep++;
                }
            }

            case 7 -> {
                Module sw = Modules.get().get(SafeWalk.class);
                if (sw.isActive()) sw.toggle();
                lastLoopTime = now;
                runningAction = false;
                lastCountdownSecond = -1; // Reset countdown
            }
        }
    }

    private boolean hasMobNearby() {
        Iterable<Entity> entities = mc.world.getEntities();
        for (Entity entity : entities) {
            if (entity instanceof MobEntity) {
                double distanceSq = mc.player.getPos().squaredDistanceTo(entity.getPos());
                if (distanceSq <= mobCheckRange.get() * mobCheckRange.get()) {
                    return true;
                }
            }
        }
        return false;
    }
}
