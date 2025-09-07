import java.io.*;
import java.util.*;
import Enums.*;

public class QLearningAgent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<Integer, double[]> qTable;
    private final int nActions = Action.values().length;
    private final Random rnd = new Random();

    private final double alpha; // taxa de aprendizado
    private final double gamma; // fator de desconto

    public QLearningAgent(double alpha, double gamma) {
        this.alpha = alpha;
        this.gamma = gamma;
        this.qTable = new HashMap<>();
    }

    public QLearningAgent() {
        this(0.1, 0.8);
    }

    private double[] getQRow(int state) {
        return qTable.computeIfAbsent(state, k -> new double[nActions]);
    }

    public int chooseAction(int state, double epsilon) {
        if (rnd.nextDouble() < epsilon) {
            return rnd.nextInt(nActions);
        } else {
            double[] q = getQRow(state);
            double maxQ = q[0];
            List<Integer> bestIndices = new ArrayList<>();
            bestIndices.add(0);
            for (int i = 1; i < q.length; i++) {
                if (q[i] > maxQ) {
                    maxQ = q[i];
                    bestIndices.clear();
                    bestIndices.add(i);
                } else if (q[i] == maxQ) {
                    bestIndices.add(i);
                }
            }
            return bestIndices.get(rnd.nextInt(bestIndices.size()));
        }
    }

    public void update(int state, int action, double reward, int nextState, boolean done) {
        double[] q = getQRow(state);
        double qsa = q[action];
        double target = done ? reward : reward + gamma * Arrays.stream(getQRow(nextState)).max().orElse(0.0);
        q[action] = qsa + alpha * (target - qsa);
    }

    public void save(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
        }
    }

    public static QLearningAgent load(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (QLearningAgent) ois.readObject();
        }
    }
}
