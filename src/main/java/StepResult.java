public class StepResult {
    private final double reward;
    private final boolean done;
    private final int score;

    public StepResult(double reward, boolean done, int score) {
        this.reward = reward;
        this.done = done;
        this.score = score;
    }

    public double getReward() { return reward; }
    public boolean isDone() { return done; }
    public int getScore() { return score; }
}
