package voiidstudios.vct.api;

/**
 * Represents the behaviour of a {@link Timer}: traditional countdown or ascending stopwatch.
 */
public enum TimerMode {
    COUNTDOWN(-1) {
        @Override
        public boolean shouldFinish(int currentSeconds, Integer targetSeconds) {
            return currentSeconds <= 0;
        }
    },
    STOPWATCH(1) {
        @Override
        public boolean shouldFinish(int currentSeconds, Integer targetSeconds) {
            return targetSeconds != null && currentSeconds >= targetSeconds;
        }
    };

    private final int direction;

    TimerMode(int direction) {
        this.direction = direction;
    }

    /**
     * The amount of seconds to add every time the timer ticks.
     * @return -1 for countdowns, 1 for stopwatches.
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Evaluates whether the timer should stop after a tick.
     *
     * @param currentSeconds the value after applying {@link #getDirection()}
     * @param targetSeconds  optional target boundary (used by {@link #STOPWATCH})
     * @return true if the timer should finish.
     */
    public abstract boolean shouldFinish(int currentSeconds, Integer targetSeconds);
}
