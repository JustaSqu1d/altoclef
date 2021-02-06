package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.misc.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.baritone.InteractWithBlockPositionProcess;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class InteractItemWithBlockTask extends Task {

    private final ItemTarget _toUse;

    private final Direction _direction;
    private final BlockPos _target;

    private boolean _trying;

    private final MovementProgressChecker _moveChecker = new MovementProgressChecker(4, 0.1, 4, 0.01);
    private final Task _wanderTask = new TimeoutWanderTask(5);

    public InteractItemWithBlockTask(ItemTarget toUse, Direction direction, BlockPos target) {
        _toUse = toUse;
        _direction = direction;
        _target = target;
    }
    public InteractItemWithBlockTask(ItemTarget toUse, BlockPos target) {
        // null means any side is OK
        this(toUse, null, target);
    }

    @Override
    protected void onStart(AltoClef mod) {
        _trying = false;
        _moveChecker.reset();
    }

    @Override
    protected Task onTick(AltoClef mod) {

        if (!mod.getInventoryTracker().targetMet(_toUse)) {
            _moveChecker.reset();
            return TaskCatalogue.getItemTask(_toUse);
        }

        // Wander and check
        if (_wanderTask.isActive() && !_wanderTask.isFinished(mod)) {
            _moveChecker.reset();
            return _wanderTask;
        }
        if (!_moveChecker.check(mod)) {
            Debug.logMessage("Failed, wandering.");
            return _wanderTask;
        }

        if (!proc(mod).isActive()) {
            Debug.logMessage("Interact with block process restarting");
            _trying = true;
            proc(mod).getToBlock(_target, _direction, true, true, false);
            proc(mod).setInteractEquipItem(_toUse);
        }

        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        proc(mod).onLostControl();
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return _trying && !proc(mod).isActive();
    }

    @Override
    protected boolean isEqual(Task obj) {
        if (obj instanceof InteractItemWithBlockTask) {
            InteractItemWithBlockTask task = (InteractItemWithBlockTask) obj;
            if ((task._direction == null) != (_direction == null)) return false;
            if (task._direction != null && !task._direction.equals(_direction)) return false;
            if (!task._toUse.equals(_toUse)) return false;
            if (!task._target.equals(_target)) return false;
            return true;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Interact using " + _toUse + " at " + _target + " dir " + _direction;
    }

    private InteractWithBlockPositionProcess proc(AltoClef mod) {
        return mod.getCustomBaritone().getInteractWithBlockPositionProcess();
    }
}