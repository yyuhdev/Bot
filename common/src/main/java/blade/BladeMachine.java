package blade;

import blade.debug.BladeDebug;
import blade.debug.DebugFrame;
import blade.debug.ErrorOccurrence;
import blade.debug.ReportError;
import blade.impl.BladeImpl;
import blade.planner.score.ScoreAction;
import blade.planner.score.ScorePlanner;
import blade.state.BladeState;
import blade.util.blade.BladeGoal;
import blade.util.blade.BladePlannedAction;
import blade.util.blade.ConfigKey;

import java.util.HashMap;
import java.util.Map;

public class BladeMachine {
    protected final Bot bot;
    protected final BladeState state = new BladeState();
    protected final ScorePlanner planner = new ScorePlanner();
    protected final BladeDebug report = new BladeDebug();
    private final Map<ConfigKey<?>, Object> config = new HashMap<>();

    protected BladeGoal goal = null;
    protected boolean enabled = false;
    protected ScoreAction previousAction;
    protected DebugFrame frame;

    public BladeMachine(Bot bot) {
        this.bot = bot;
        registerDefault();
    }

    public void tick() {
        if (!enabled) return;
        if (goal == null) return;
        frame = report.newFrame();
        ReportError.wrap(() -> {
            planner.refreshAll(bot, state);
            goal.setBot(bot);
            goal.tick();
            produceState();
            frame.setState(state.copy());
            BladePlannedAction<ScoreAction> plan = planner.planInternal(goal, state, frame.getPlanner());
            if (plan == null) return;
            previousAction = plan.tick(previousAction, frame);
        }, frame, ErrorOccurrence.OTHER);
    }

    public void produceState() {
        state.produce(bot);
    }

    public ScorePlanner getPlanner() {
        return planner;
    }

    public void setGoal(BladeGoal goal) {
        setEnabled(true);
        this.goal = goal;
    }

    public void addAction(ScoreAction action) {
        planner.addAction(action);
    }

    public BladeState getState() {
        return state;
    }

    public BladeDebug getReport() {
        return report;
    }

    public DebugFrame getLastFrame() {
        return frame;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void causePanic(String detailedReason) {
        // bot.sendError("Blade has panicked.");
        bot.destroy();
        DebugFrame frame = report.newFrame();
        frame.addError(ReportError.from(new Exception("Panic: " + detailedReason), ErrorOccurrence.PANIC));
    }

    public void registerDefault() {
        BladeImpl.register(this);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ConfigKey<T> key) {
        return (T) config.computeIfAbsent(key, ConfigKey::getDefaultValue);
    }

    public <T> void set(ConfigKey<T> key, T value) {
        config.put(key, value);
    }
}