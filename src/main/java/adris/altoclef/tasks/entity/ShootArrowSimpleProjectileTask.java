package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.speedrun.BeatMinecraft2Task;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ShootArrowSimpleProjectileTask extends Task {

    private final Entity target;
    private boolean shooting = false;
    private boolean shot = false;

    private final TimerGame _shotTimer = new TimerGame(1);

    public ShootArrowSimpleProjectileTask(Entity target) {
        this.target = target;
    }

    @Override
    protected void onStart(AltoClef mod) {
        shooting = false;
    }

    private static Rotation calculateThrowLook(AltoClef mod, Entity target) {
        // Velocity based on bow charge.
        float velocity = (mod.getPlayer().getItemUseTime() - mod.getPlayer().getItemUseTimeLeft()) / 20f;
        velocity = (velocity * velocity + velocity * 2) / 3;
        if (velocity > 1) velocity = 1;

        // Positions
        double posX = target.getPos().getX() + (target.getPos().getX() - target.prevX);
        double posY = target.getPos().getY() + (target.getPos().getY() - target.prevY);
        double posZ = target.getPos().getZ() + (target.getPos().getZ() - target.prevZ);

        // Adjusting for hitbox heights
        posY -= 1.9f - target.getHeight();

        double relativeX = posX - mod.getPlayer().getX();
        double relativeY = posY - mod.getPlayer().getY();
        double relativeZ = posZ - mod.getPlayer().getZ();

        // Calculate the pitch
        double hDistance = Math.sqrt(relativeX * relativeX + relativeZ * relativeZ);
        double hDistanceSq = hDistance * hDistance;
        float g = 0.006f;
        float velocitySq = velocity * velocity;
        float pitch = (float) -Math.toDegrees(Math.atan((velocitySq - Math.sqrt(velocitySq * velocitySq - g * (g * hDistanceSq + 2 * relativeY * velocitySq))) / (g * hDistance)));

//         Set player rotation
        if (Float.isNaN(pitch)) {
            return new Rotation(target.getYaw(), target.getPitch());
        } else {
            return new Rotation(Vec3dToYaw(mod, new Vec3d(posX, posY, posZ)), pitch);
        }
    }

    private static float Vec3dToYaw(AltoClef mod, Vec3d vec) {
        return mod.getPlayer().getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(vec.getZ() - mod.getPlayer().getZ(), vec.getX() - mod.getPlayer().getX())) - 90f - mod.getPlayer().getYaw());
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (mod.getItemStorage().hasItem(Items.BOW) && (mod.getItemStorage().hasItem(Items.ARROW) || mod.getItemStorage().hasItem(Items.SPECTRAL_ARROW))) {
            Rotation lookTarget = calculateThrowLook(mod, target);
            LookHelper.lookAt(mod, lookTarget);

            boolean charged = mod.getPlayer().getItemUseTime() > 20;

            mod.getSlotHandler().forceEquipItem(Items.BOW);

            if (LookHelper.isLookingAt(mod, lookTarget) && !shooting) {
                mod.getInputControls().hold(Input.CLICK_RIGHT);
                shooting = true;
                _shotTimer.reset();
            }
            if (shooting && charged) {
                List<ArrowEntity> arrow = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class);
                // If any of the arrows belong to us and are moving, do not shoot yet
                // Prevents from shooting multiple arrows to the same target
                // TODO: Map each arrow to a target and only shoot if there is no arrow for that target
                for (ArrowEntity a : arrow) {
                    if (a.getOwner() == mod.getPlayer() && a.getVelocity().length() > 0.1) {
                        return null;
                    }
                }
                if (BeatMinecraft2Task.getConfig().renderDistanceManipulation) {
                    // For farther entities, the arrow may get stuck in the air, so we need to increase the simulation distance
                    MinecraftClient.getInstance().options.getSimulationDistance().setValue(32);
                }
                mod.getInputControls().release(Input.CLICK_RIGHT); // Release the arrow
                shot = true;
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getInputControls().release(Input.CLICK_RIGHT);
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return shot;
    }

    @Override
    protected boolean isEqual(Task other) {
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Shooting arrow at " + target.getDisplayName().toString();
    }
}
