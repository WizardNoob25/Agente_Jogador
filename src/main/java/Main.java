public class Main {

    public static void main(String[] args) {
        final int boardSize = 12;
        final String qTableFile = "qtable.ser";
        final int trainingEpisodes = 50_000;
        final int playGames = 1000;
        final int guiDelayMs = 90;

        try {
            SnakeEnv env = new SnakeEnv(boardSize, boardSize);
            QLearningAgent agent;

            java.io.File file = new java.io.File(qTableFile);
            if (file.exists()) {
                System.out.println("Carregando Q-table existente...");
                agent = QLearningAgent.load(qTableFile);
            } else {
                System.out.println("Treinando novo agente...");
                agent = new QLearningAgent();
                Trainer trainer = new Trainer(env, agent);
                trainer.setEpisodes(trainingEpisodes);
                trainer.trainAndSave(qTableFile);
            }

            Trainer trainer = new Trainer(env, agent);
            System.out.println("Iniciando partidas com GUI...");
            trainer.playWithGui(agent, guiDelayMs, playGames);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
