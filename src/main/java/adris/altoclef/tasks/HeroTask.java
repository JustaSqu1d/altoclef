package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.resources.KillAndLootTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SlimeEntity;

import java.util.Optional;

public class HeroTask extends Task {
    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        assert MinecraftClient.getInstance().world != null;
        Iterable<Entity> hostiles = MinecraftClient.getInstance().world.getEntities();
        for (Entity hostile : hostiles) {
            double playerCurrentY = mod.getPlayer().getY();
            double hostileCurrentY = hostile.getY();
            double hostileDistanceY = playerCurrentY - hostileCurrentY;
            Optional<Entity> closestHostile = mod.getEntityTracker().getClosestEntity(entity -> hostileDistanceY > -5 &&
                    hostileDistanceY < 5, hostile.getClass());
            if (closestHostile.isPresent()) {
                if (hostile instanceof HostileEntity || hostile instanceof SlimeEntity) {
                    setDebugState("Killing hostile mob.");
                    return new KillAndLootTask(hostile.getClass());
                }
            }
        }
        setDebugState("Searching for hostile mob.");
        return new TimeoutWanderTask();
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof HeroTask;
    }

    @Override
    protected String toDebugString() {
        return "Killing all hostile mobs.";
    }
}