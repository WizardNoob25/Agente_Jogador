import javax.swing.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;

public class App {
    public static void main(String[] args) throws Exception {
        int boardWidth = 350;
        int boardHeight = boardWidth;
        
        int count = 0;

        JFrame frame = new JFrame("Snake");
        frame.setLocation(700,350);
        //frame.setLocationRelativeTo(null);
        frame.setVisible(true);
      
        frame.setSize(boardWidth, boardHeight);
        
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		System.out.println(count);
		
		SnakeGame snakeGame = new SnakeGame(boardWidth, boardHeight);
		frame.add(snakeGame);
		
		frame.pack();
		
		
		snakeGame.requestFocus();
    }
}
