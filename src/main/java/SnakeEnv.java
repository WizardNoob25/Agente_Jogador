import Enums.Action;
import Enums.Dir;

import java.util.*;

public class SnakeEnv {
    private final int width, height;
    private LinkedList<Point> snake;
    private Dir dir;
    private Point food;
    private boolean alive;
    private final Random rnd;
    private int score;
    private final int maxStepsWithoutFood;
    private int stepsSinceFood;

    private static final double COLLISION_PENALTY = -12.0;
    private static final double FOOD_REWARD = 2.0;
    private static final double STEP_PENALTY = -0.25;
    private static final double TAIL_PENALTY = -0.3;
    private static final double STARVATION_PENALTY = -15.0;

    public SnakeEnv(int width, int height) {
        this.width = width;
        this.height = height;
        this.rnd = new Random();
        this.maxStepsWithoutFood = width * height * 30;
        reset();
    }

    public void reset() {
        snake = new LinkedList<>();
        int cx = width / 2, cy = height / 2;
        snake.add(new Point(cx, cy));
        dir = Dir.RIGHT;
        placeFood();
        alive = true;
        score = 0;
        stepsSinceFood = 0;
    }

    private void placeFood() {
        while (true) {
            int fx = rnd.nextInt(width);
            int fy = rnd.nextInt(height);
            Point p = new Point(fx, fy);
            if (!snake.contains(p)) {
                food = p;
                return;
            }
        }
    }

    public StepResult step(Action action) {
        if (!alive) return new StepResult(0, true, score);

        Dir intendedDir = getIntendedDir(action);
        Point head = snake.getFirst();
        Point np = nextPoint(head, intendedDir);
        boolean willEat = np.equals(food);

        double reward = STEP_PENALTY;
        if (willCollide(np, willEat)) reward += COLLISION_PENALTY;
        if (willHitTailInDirection(head, intendedDir)) reward += TAIL_PENALTY;

        if (isCollision(np)) {
            alive = false;
            return new StepResult(COLLISION_PENALTY, true, score);
        }

        dir = intendedDir;
        snake.addFirst(np);

        if (willEat) {
            score++;
            reward = FOOD_REWARD;
            placeFood();
            stepsSinceFood = 0;
        } else {
            snake.removeLast();
            stepsSinceFood++;
        }

        if (stepsSinceFood > maxStepsWithoutFood) {
            alive = false;
            return new StepResult(STARVATION_PENALTY, true, score);
        }

        return new StepResult(reward, false, score);
    }

    private Dir getIntendedDir(Action action) {
        switch (action) {
            case LEFT:
                return turnLeft(dir);
            case RIGHT:
                return turnRight(dir);
            default:
                return dir;
        }
    }

    public int getState() {
        Point head = snake.getFirst();
        boolean dangerStraight = willCollide(nextPoint(head, dir), false);
        boolean dangerRight = willCollide(nextPoint(head, turnRight(dir)), false);
        boolean dangerLeft = willCollide(nextPoint(head, turnLeft(dir)), false);

        boolean foodLeft = food.getX() < head.getX();
        boolean foodRight = food.getX() > head.getX();
        boolean foodUp = food.getY() < head.getY();
        boolean foodDown = food.getY() > head.getY();

        boolean willHitTailStraight = willHitTailInDirection(head, dir);
        boolean willHitTailRight = willHitTailInDirection(head, turnRight(dir));
        boolean willHitTailLeft = willHitTailInDirection(head, turnLeft(dir));

        int state = 0;
        state |= (dangerStraight ? 1 : 0) << 0;
        state |= (dangerRight ? 1 : 0) << 1;
        state |= (dangerLeft ? 1 : 0) << 2;
        state |= (dir == Dir.LEFT ? 1 : 0) << 3;
        state |= (dir == Dir.RIGHT ? 1 : 0) << 4;
        state |= (dir == Dir.UP ? 1 : 0) << 5;
        state |= (dir == Dir.DOWN ? 1 : 0) << 6;
        state |= (foodLeft ? 1 : 0) << 7;
        state |= (foodRight ? 1 : 0) << 8;
        state |= (foodUp ? 1 : 0) << 9;
        state |= (foodDown ? 1 : 0) << 10;
        state |= (willHitTailStraight ? 1 : 0) << 11;
        state |= (willHitTailRight ? 1 : 0) << 12;
        state |= (willHitTailLeft ? 1 : 0) << 13;
        return state;
    }

    private boolean isCollision(Point p) {
        return p.getX() < 0 || p.getY() < 0 || p.getX() >= width || p.getY() >= height || snake.contains(p);
    }

    private boolean willCollide(Point p, boolean willEat) {
        if (isCollision(p)) return true;
        int checkUntil = snake.size() - (willEat ? 0 : 1);
        for (int i = 0; i < checkUntil; i++) if (p.equals(snake.get(i))) return true;
        return false;
    }

    private boolean willHitTailInDirection(Point head, Dir d) {
        Point p = nextPoint(head, d);
        while (p.getX() >= 0 && p.getY() >= 0 && p.getX() < width && p.getY() < height) {
            if (snake.contains(p)) return true;
            p = nextPoint(p, d);
        }
        return false;
    }

    public Point nextPoint(Point head, Dir d) {
        switch (d) {
            case UP: return new Point(head.getX(), head.getY() - 1);
            case DOWN: return new Point(head.getX(), head.getY() + 1);
            case LEFT: return new Point(head.getX() - 1, head.getY());
            case RIGHT: return new Point(head.getX() + 1, head.getY());
        }
        return head;
    }

    private Dir turnLeft(Dir d) {
        switch (d) {
            case UP: return Dir.LEFT;
            case LEFT: return Dir.DOWN;
            case DOWN: return Dir.RIGHT;
            case RIGHT: return Dir.UP;
        }
        return d;
    }

    private Dir turnRight(Dir d) {
        switch (d) {
            case UP: return Dir.RIGHT;
            case RIGHT: return Dir.DOWN;
            case DOWN: return Dir.LEFT;
            case LEFT: return Dir.UP;
        }
        return d;
    }

    public int getScore() { return score; }
    public Dir getDir() { return dir; }
    public Point getFood() { return food; }
    public List<Point> getSnakeBody() { return Collections.unmodifiableList(snake); }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public LinkedList<Point> getSnake() {
        return snake;
    }

    public boolean isAlive() {
        return alive;
    }

    public Random getRnd() {
        return rnd;
    }

    public int getMaxStepsWithoutFood() {
        return maxStepsWithoutFood;
    }

    public int getStepsSinceFood() {
        return stepsSinceFood;
    }
}
