import java.awt.Color;
import java.awt.Graphics;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Dimension;

public class SnakeQLearning {

    static class Point implements Serializable {
        int x, y;
        Point(int x, int y){this.x=x; this.y=y;}
        public boolean equals(Object o){
            if(!(o instanceof Point)) return false;
            Point p=(Point)o; return p.x==x && p.y==y;
        }
        public int hashCode(){ return Objects.hash(x,y); }
    }

    enum Dir { UP, RIGHT, DOWN, LEFT }
    enum Action { STRAIGHT, RIGHT, LEFT }

    static class StepResult {
        double reward;
        boolean done;
        int score;
        StepResult(double reward, boolean done, int score){
            this.reward=reward; this.done=done; this.score=score;
        }
    }

    static class SnakeEnv {
        final int width, height;
        LinkedList<Point> snake;
        Dir dir;
        Point food;
        boolean alive;
        Random rnd;
        int score;
        int maxStepsWithoutFood;
        int stepsSinceFood;

        public SnakeEnv(int width, int height) {
            this.width = width;
            this.height = height;
            rnd = new Random();
            reset();
            maxStepsWithoutFood = width * height * 30;
        }

        public void reset() {
            snake = new LinkedList<>();
            int cx = width/2, cy = height/2;
            snake.add(new Point(cx, cy));
            snake.add(new Point(cx-1, cy));
            snake.add(new Point(cx-2, cy));
            dir = Dir.RIGHT;
            placeFood();
            alive = true;
            score = 0;
            stepsSinceFood = 0;
        }

        void placeFood() {
            while(true){
                int fx = rnd.nextInt(width);
                int fy = rnd.nextInt(height);
                Point p = new Point(fx, fy);
                boolean onSnake = false;
                for(Point s: snake) if(s.equals(p)){ onSnake = true; break; }
                if(!onSnake){ food = p; return; }
            }
        }

        private Dir turnLeft(Dir d){
            switch(d){
                case UP: return Dir.LEFT;
                case LEFT: return Dir.DOWN;
                case DOWN: return Dir.RIGHT;
                case RIGHT: return Dir.UP;
            }
            return d;
        }
        private Dir turnRight(Dir d){
            switch(d){
                case UP: return Dir.RIGHT;
                case RIGHT: return Dir.DOWN;
                case DOWN: return Dir.LEFT;
                case LEFT: return Dir.UP;
            }
            return d;
        }

        private Point nextPoint(Point head, Dir d){
            switch(d){
                case UP: return new Point(head.x, head.y-1);
                case DOWN: return new Point(head.x, head.y+1);
                case LEFT: return new Point(head.x-1, head.y);
                case RIGHT: return new Point(head.x+1, head.y);
            }
            return head;
        }

        private boolean isCollision(Point p){
            if(p.x < 0 || p.y < 0 || p.x >= width || p.y >= height) return true;
            for(int i=0;i<snake.size();i++){
                Point s = snake.get(i);
                if(p.equals(s)) return true;
            }
            return false;
        }

        public StepResult step(Action action){
            if(!alive) return new StepResult(0, true, score);

            Dir intendedDir = dir;
            switch(action){
                case STRAIGHT: break;
                case LEFT: intendedDir = turnLeft(dir); break;
                case RIGHT: intendedDir = turnRight(dir); break;
            }

            Point head = snake.getFirst();
            Point np = nextPoint(head, intendedDir);

            boolean willEat = np.equals(food);
            boolean collision = false;
            if(np.x < 0 || np.y < 0 || np.x >= width || np.y >= height) collision = true;
            else{
                int checkUntil = snake.size() - (willEat ? 0 : 1);
                for(int i=0;i<checkUntil;i++){
                    Point s = snake.get(i);
                    if(np.equals(s)){ collision = true; break;}
                }
            }

            double reward = -0.1;

            if(willCollide(nextPoint(head, dir), willEat)){
                reward -= 1.0;
            }

            if(willCollide(np, willEat)){
                reward -= 2.0;
            }

            // >>> aqui reduzi o impacto do "will hit tail"
            if(willHitTailInDirection(head, intendedDir)){
                reward -= 0.3;
            }

            if(collision){
                alive = false;
                reward = -12.0;
                return new StepResult(reward, true, score);
            }

            dir = intendedDir;
            snake.addFirst(np);
            if(willEat){
                score += 1;
                reward = 2.0;
                placeFood();
                stepsSinceFood = 0;
            } else {
                snake.removeLast();
                stepsSinceFood++;
            }

            if(stepsSinceFood > maxStepsWithoutFood){
                alive = false;
                reward = -8.0;
                return new StepResult(reward, true, score);
            }

            return new StepResult(reward, false, score);
        }

        public int getState() {
            Point head = snake.getFirst();
            Dir dStraight = dir;
            Dir dRight = turnRight(dir);
            Dir dLeft = turnLeft(dir);

            boolean dangerStraight = willCollide(nextPoint(head, dStraight), false);
            boolean dangerRight = willCollide(nextPoint(head, dRight), false);
            boolean dangerLeft = willCollide(nextPoint(head, dLeft), false);

            boolean dirLeft = (dir == Dir.LEFT);
            boolean dirRight = (dir == Dir.RIGHT);
            boolean dirUp = (dir == Dir.UP);
            boolean dirDown = (dir == Dir.DOWN);

            boolean foodLeft = food.x < head.x;
            boolean foodRight = food.x > head.x;
            boolean foodUp = food.y < head.y;
            boolean foodDown = food.y > head.y;

            boolean willHitTailStraight = willHitTailInDirection(head, dStraight);
            boolean willHitTailRight = willHitTailInDirection(head, dRight);
            boolean willHitTailLeft = willHitTailInDirection(head, dLeft);

            int state = 0;
            state |= (dangerStraight?1:0) << 0;
            state |= (dangerRight?1:0) << 1;
            state |= (dangerLeft?1:0) << 2;
            state |= (dirLeft?1:0) << 3;
            state |= (dirRight?1:0) << 4;
            state |= (dirUp?1:0) << 5;
            state |= (dirDown?1:0) << 6;
            state |= (foodLeft?1:0) << 7;
            state |= (foodRight?1:0) << 8;
            state |= (foodUp?1:0) << 9;
            state |= (foodDown?1:0) << 10;
            state |= (willHitTailStraight?1:0) << 11;
            state |= (willHitTailRight?1:0) << 12;
            state |= (willHitTailLeft?1:0) << 13;
            return state;
        }

        private boolean willCollide(Point p, boolean willEat) {
            if(p.x < 0 || p.y < 0 || p.x >= width || p.y >= height) return true;
            int checkUntil = snake.size() - (willEat ? 0 : 1);
            for(int i=0;i<checkUntil;i++){
                Point s = snake.get(i);
                if(p.equals(s)) return true;
            }
            return false;
        }

        private boolean willHitTailInDirection(Point head, Dir d) {
            Point p = nextPoint(head, d);
            while(p.x >= 0 && p.y >= 0 && p.x < width && p.y < height){
                for(Point s : snake){
                    if(p.equals(s)) return true;
                }
                p = nextPoint(p, d);
            }
            return false;
        }

        public int getScore() { return score; }
        public Dir getDir() { return dir; }
        public Point getFood() { return food; }
        public List<Point> getSnakeBody() { return snake; }
    }

    static class QLearningAgent implements Serializable {
        private static final long serialVersionUID = 1L;
        Map<Integer, double[]> qtable;
        int nActions = Action.values().length;
        Random rnd = new Random();

        double alpha = 0.1;
        double gamma = 0.8;

        public QLearningAgent(){
            qtable = new HashMap<>();
        }

        private double[] getQRow(int state){
            return qtable.computeIfAbsent(state, k -> new double[nActions]);
        }

        public int chooseAction(int state, double epsilon){
            if(rnd.nextDouble() < epsilon){
                return rnd.nextInt(nActions);
            } else {
                double[] q = getQRow(state);
                double best = q[0];
                List<Integer> bestIdx = new ArrayList<>();
                bestIdx.add(0);
                for(int i=1;i<q.length;i++){
                    if(q[i] > best){
                        best = q[i];
                        bestIdx.clear();
                        bestIdx.add(i);
                    } else if(q[i] == best) {
                        bestIdx.add(i);
                    }
                }
                return bestIdx.get(rnd.nextInt(bestIdx.size()));
            }
        }

        public void update(int state, int action, double reward, int nextState, boolean done){
            double[] q = getQRow(state);
            double qsa = q[action];
            double target;
            if(done){
                target = reward;
            } else {
                double[] qNext = getQRow(nextState);
                double maxNext = qNext[0];
                for(int i=1;i<qNext.length;i++) if(qNext[i] > maxNext) maxNext = qNext[i];
                target = reward + gamma * maxNext;
            }
            q[action] = qsa + alpha * (target - qsa);
        }

        public void save(String filename) throws IOException {
            try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))){
                oos.writeObject(this);
            }
        }

        public static QLearningAgent load(String filename) throws IOException, ClassNotFoundException {
            try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))){
                return (QLearningAgent) ois.readObject();
            }
        }
    }

    static class Trainer {
        SnakeEnv env;
        QLearningAgent agent;

        double EPS_START = 1.0;
        double EPS_END = 0.001;
        double EPS_DECAY = 0.99;
        int EPISODES = 1000000;
        int MAX_STEPS_PER_EPISODE = 100000;

        public Trainer(int width, int height){
            env = new SnakeEnv(width, height);
            agent = new QLearningAgent();
        }

        public Trainer(SnakeEnv env, QLearningAgent agent){
            this.env = env; this.agent = agent;
        }

        public void trainAndSave(String qFilename) throws IOException {
            double eps = EPS_START;
            for(int ep=1; ep<=EPISODES; ep++){
                env.reset();
                int state = env.getState();
                boolean done = false;
                int steps = 0;
                while(!done && steps < MAX_STEPS_PER_EPISODE){
                    int actionIdx = agent.chooseAction(state, eps);
                    Action action = Action.values()[actionIdx];

                    Point head = env.getSnakeBody().get(0);
                    Dir intendedDir = env.getDir();
                    if(action == Action.LEFT) intendedDir = env.turnLeft(intendedDir);
                    else if(action == Action.RIGHT) intendedDir = env.turnRight(intendedDir);

                    double tailRiskPenalty = env.willHitTailInDirection(head, intendedDir) ? -0.045 : 0.0;

                    StepResult sr = env.step(action);
                    double usedReward = sr.reward + tailRiskPenalty;

                    int nextState = env.getState();
                    agent.update(state, actionIdx, usedReward, nextState, sr.done);
                    state = nextState;
                    done = sr.done;
                    steps++;
                }
                eps = Math.max(EPS_END, eps * EPS_DECAY);

                if(ep % 1000 == 0){
                    System.out.printf("Episode %d/%d, eps=%.4f, score=%d, snakeLen=%d\n",
                            ep, EPISODES, eps, env.getScore(), env.getSnakeBody().size());
                }
            }
            agent.save(qFilename);
            System.out.println("Treino completo. Q-table salva em: " + qFilename);
        }

        public void playWithGui(QLearningAgent agent, int delayMs, int numGames){
            for(int game=1; game<=numGames; game++){
                env.reset();
                JFrame frame = new JFrame("Snake - QAgent Play ("+game+"/"+numGames+")");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                int cell = 25;
                int boardWidth = env.width * cell;
                int boardHeight = env.height * cell;

                JPanel panel = new JPanel(){
                    protected void paintComponent(Graphics g){
                        super.paintComponent(g);
                        g.setColor(Color.BLACK);
                        g.fillRect(0,0,getWidth(),getHeight());

                        Toolkit.getDefaultToolkit().sync();
                        g.setColor(Color.DARK_GRAY);
                        for(int i=0; i<env.width; i++){
                            g.drawLine(i*cell, 0, i*cell, boardHeight);
                        }
                        for(int j=0; j<env.height; j++){
                            g.drawLine(0, j*cell, boardWidth, j*cell);
                        }

                        Point f = env.getFood();
                        g.setColor(Color.RED);
                        g.fill3DRect(f.x*cell, f.y*cell, cell, cell, true);

                        g.setColor(Color.GREEN);
                        List<Point> body = env.getSnakeBody();
                        if(!body.isEmpty()){
                            Point head = body.get(0);
                            g.fill3DRect(head.x*cell, head.y*cell, cell, cell, true);
                            for(int i=1;i<body.size();i++){
                                Point s = body.get(i);
                                g.fill3DRect(s.x*cell, s.y*cell, cell, cell, true);
                            }
                        }

                        g.setFont(new Font("Arial", Font.PLAIN, 16));
                        if(!env.alive){
                            g.setColor(Color.RED);
                            g.drawString("Game Over: " + env.getScore(), 10, 20);
                        } else {
                            g.setColor(Color.WHITE);
                            g.drawString("Score: " + env.getScore(), 10, 20);
                        }
                    }
                };

                panel.setPreferredSize(new Dimension(boardWidth, boardHeight));
                frame.add(panel);
                frame.pack();
                frame.setVisible(true);

                boolean done=false;
                while(!done){
                    int state = env.getState();
                    int actionIdx = agent.chooseAction(state, 0.0);
                    Action action = Action.values()[actionIdx];
                    StepResult sr = env.step(action);
                    panel.repaint();
                    done = sr.done;
                    try{ Thread.sleep(delayMs); } catch(Exception e){}
                }
                System.out.println("Fim do jogo "+game+" Score: " + env.getScore());
                frame.dispose();

                try { Thread.sleep(800); } catch(Exception e){}
            }
        }
    }

    public static void main(String[] args) {
        int width = 10;
        int height = width;
        Trainer trainer = new Trainer(width, height);

        String qfile = "qtable.ser";
        try {
            System.out.println("Iniciando treino...");
            trainer.EPISODES = 50000;
            trainer.trainAndSave(qfile);

            QLearningAgent agent = QLearningAgent.load(qfile);

            trainer.playWithGui(agent, 80, 1000);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
