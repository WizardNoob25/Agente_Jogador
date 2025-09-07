import javax.swing.*;
import java.awt.*;
import java.util.List;
import Enums.*;
import Enums.Action;

public class Trainer {
    private final SnakeEnv env;
    private final QLearningAgent agent;

    private double epsilonStart = 1.0;
    private double epsilonEnd = 0.001;
    private double epsilonDecay = 0.99;
    private int episodes = 100_000;
    private int maxStepsPerEpisode = 100_000;
    private int scoreSum = 0;

    public Trainer(int width, int height) {
        env = new SnakeEnv(width, height);
        agent = new QLearningAgent();
    }

    public Trainer(SnakeEnv env, QLearningAgent agent) {
        this.env = env;
        this.agent = agent;
    }

    // Métodos de configuração
    public void setEpisodes(int episodes) { this.episodes = episodes; }
    public void setEpsilonStart(double epsilonStart) { this.epsilonStart = epsilonStart; }
    public void setEpsilonEnd(double epsilonEnd) { this.epsilonEnd = epsilonEnd; }
    public void setEpsilonDecay(double epsilonDecay) { this.epsilonDecay = epsilonDecay; }
    public void setMaxStepsPerEpisode(int maxSteps) { this.maxStepsPerEpisode = maxSteps; }

    public void trainAndSave(String filename) throws Exception {
        double epsilon = epsilonStart;

        for (int ep = 1; ep <= episodes; ep++) {
            env.reset();
            int state = env.getState();
            boolean done = false;
            int steps = 0;

            while (!done && steps < maxStepsPerEpisode) {
                int actionIdx = agent.chooseAction(state, epsilon);
                Action action = Action.values()[actionIdx];

                StepResult sr = env.step(action);
                int nextState = env.getState();
                agent.update(state, actionIdx, sr.getReward(), nextState, sr.isDone());

                state = nextState;
                done = sr.isDone();
                steps++;
            }

            epsilon = Math.max(epsilonEnd, epsilon * epsilonDecay);

            if (ep % 1000 == 0) {
                System.out.printf("Episode %d/%d | eps=%.4f | score=%d | snakeLen=%d%n",
                        ep, episodes, epsilon, env.getScore(), env.getSnakeBody().size());
            }
        }

        agent.save(filename);
        System.out.println("Treino completo. Q-table salva em: " + filename);
    }

    public void playWithGui(QLearningAgent agent, int delayMs, int numGames) {
        for (int game = 1; game <= numGames; game++) {
            env.reset();
            JFrame frame = new JFrame("Snake - QAgent Play (" + game + "/" + numGames + ")");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            int cell = 25;
            JPanel panel = new JPanel() {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, getWidth(), getHeight());

                    Toolkit.getDefaultToolkit().sync();
                    g.setColor(Color.DARK_GRAY);

                    for (int i = 0; i < env.getWidth(); i++)
                        g.drawLine(i * cell, 0, i * cell, env.getHeight() * cell);
                    for (int j = 0; j < env.getHeight(); j++)
                        g.drawLine(0, j * cell, env.getWidth() * cell, j * cell);

                    Point f = env.getFood();
                    g.setColor(Color.RED);
                    g.fill3DRect(f.getX() * cell, f.getY() * cell, cell, cell, true);

                    g.setColor(Color.GREEN);
                    List<Point> body = env.getSnakeBody();
                    if (!body.isEmpty()) {
                        Point head = body.get(0);
                        g.fill3DRect(head.getX() * cell, head.getY() * cell, cell, cell, true);
                        for (int i = 1; i < body.size(); i++) {
                            Point p = body.get(i);
                            g.fill3DRect(p.getX() * cell, p.getY() * cell, cell, cell, true);
                        }
                    }

                    g.setFont(new Font("Arial", Font.PLAIN, 16));
                    g.setColor(env.getSnakeBody().isEmpty() ? Color.RED : Color.WHITE);
                    g.drawString("Score: " + env.getScore(), 10, 20);
                }
            };

            panel.setPreferredSize(new Dimension(env.getWidth() * cell, env.getHeight() * cell));
            frame.add(panel);
            frame.pack();
            frame.setVisible(true);

            boolean done = false;
            while (!done) {
                int state = env.getState();
                int actionIdx = agent.chooseAction(state, 0.0);
                Action action = Action.values()[actionIdx];
                StepResult sr = env.step(action);
                panel.repaint();
                done = sr.isDone();
                try { Thread.sleep(delayMs); } catch (Exception ignored) {}
            }
            scoreSum += env.getScore();

            System.out.println("Fim do jogo " + game + " | Score: " + env.getScore() + " | Media ate entao: " +  scoreSum/game);

            frame.dispose();
            try { Thread.sleep(800); } catch (Exception ignored) {}
        }
    }
}
