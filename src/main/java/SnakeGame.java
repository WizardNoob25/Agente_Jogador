import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    
    int recompensa = 0;
    double alpha = 0.1;
    double gamma = 0.9;
    int boardWidth;
    int boardHeight;
    int tileSize = 25;

    //snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;
    
    Map<Integer,double[]> qtable;

    //food
    Tile food;
    Random random;
    enum Dir { UP, RIGHT, DOWN, LEFT }

    Dir dir =  Dir.UP;

    //game logic
    int velocityX;
    int velocityY;
    Timer gameLoop;

    boolean gameOver = false;



    SnakeGame(int boardWidth, int boardHeight) {
        qtable = new HashMap<>();
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<Tile>();

        food = new Tile(10, 10);
        random = new Random();
        placeFood();

        velocityX = 1;
        velocityY = 0;

        //game timer
        gameLoop = new Timer(100, this); //how long it takes to start timer, milliseconds gone between frames
        gameLoop.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Toolkit.getDefaultToolkit().sync();
        draw(g);
    }

    public void draw(Graphics g) {
        //Grid Lines
        for(int i = 0; i < boardWidth/tileSize; i++) {
            //(x1, y1, x2, y2)
            g.drawLine(i*tileSize, 0, i*tileSize, boardHeight);
            g.drawLine(0, i*tileSize, boardWidth, i*tileSize);
        }

        //Food
        g.setColor(Color.red);
        // g.fillRect(food.x*tileSize, food.y*tileSize, tileSize, tileSize);
        g.fill3DRect(food.x*tileSize, food.y*tileSize, tileSize, tileSize, true);

        //Snake Head
        g.setColor(Color.green);
        // g.fillRect(snakeHead.x, snakeHead.y, tileSize, tileSize);
        // g.fillRect(snakeHead.x*tileSize, snakeHead.y*tileSize, tileSize, tileSize);
        g.fill3DRect(snakeHead.x*tileSize, snakeHead.y*tileSize, tileSize, tileSize, true);

        //Snake Body
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);
            // g.fillRect(snakePart.x*tileSize, snakePart.y*tileSize, tileSize, tileSize);
            g.fill3DRect(snakePart.x*tileSize, snakePart.y*tileSize, tileSize, tileSize, true);
        }

        //Score
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        if (gameOver) {
            g.setColor(Color.red);
            g.drawString("Game Over: " + String.valueOf(snakeBody.size()), tileSize - 16, tileSize);
        }
        else {
            g.drawString("Score: " + String.valueOf(snakeBody.size()), tileSize - 16, tileSize);
        }
        
        Toolkit.getDefaultToolkit().sync();
    }

    public void placeFood(){
        food.x = random.nextInt(boardWidth/tileSize);
        food.y = random.nextInt(boardHeight/tileSize);
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

    public void move() {
		recompensa = 0;

        int state = getState();

        update(state,,recompensa,);
		
        //eat food
        if (collision(snakeHead, food)) {
            snakeBody.add(new Tile(food.x, food.y));
            recompensa = 1;
            placeFood();
        }

        //move snake body
        for (int i = snakeBody.size()-1; i >= 0; i--) {
            Tile snakePart = snakeBody.get(i);
            if (i == 0) { //right before the head
                snakePart.x = snakeHead.x;
                snakePart.y = snakeHead.y;
            }
            else {
                Tile prevSnakePart = snakeBody.get(i-1);
                snakePart.x = prevSnakePart.x;
                snakePart.y = prevSnakePart.y;
            }
        }

        //game over conditions
        for (int i = 1; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);

            //collide with snake head
            if (collision(snakeHead, snakePart)) {
                gameOver = true;
                recompensa = -1;
            }
        }

        if (snakeHead.x*tileSize < 0 || snakeHead.x*tileSize > (boardWidth - 1) || //passed left border or right border
                snakeHead.y*tileSize < 0 || snakeHead.y*tileSize > (boardHeight - 1)) { //passed top border or bottom border
            gameOver = true;
            recompensa = -1;
        }
        
        //move snake head
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;
        
        //System.out.println(recompensa);
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) { //called every x milliseconds by gameLoop timer
        repaint();
        if (gameOver) {
			//gameLoop.stop();
			
			snakeBody.clear();
			snakeHead.x = random.nextInt(boardWidth/tileSize);
			snakeHead.y = random.nextInt(boardHeight/tileSize);
			
			gameLoop.restart();
			gameOver = false;
        }
        move();
    }
    
    public void go_up() {
		velocityX = 0;
		velocityY = -1;
        dir = Dir.UP;
	}
	
	public void go_down() {
		velocityX = 0;
		velocityY = 1;
        dir = Dir.DOWN;
	}
	
	public void go_left() {
		velocityX = -1;
		velocityY = 0;
        dir = Dir.LEFT;
	}
	
	public void go_right() {
		velocityX = 1;
		velocityY = 0;
        dir = Dir.RIGHT;
	}

    private double[] getQRow(int state){
        return qtable.computeIfAbsent(state, k -> new double[3]);
    }

    public int getState() {
        Dir dStraight = dir;
        Dir dRight = turnRight(dir);
        Dir dLeft = turnLeft(dir);
        boolean dangerStraight = willCollide(nextTile(snakeHead, dStraight), false);
        boolean dangerRight = willCollide(nextTile(snakeHead, dRight), false);
        boolean dangerLeft = willCollide(nextTile(snakeHead, dLeft), false);

        boolean dirLeft = (dir == Dir.LEFT);
        boolean dirRight = (dir == Dir.RIGHT);
        boolean dirUp = (dir == Dir.UP);
        boolean dirDown = (dir == Dir.DOWN);

        boolean foodLeft = food.x < snakeHead.x;
        boolean foodRight = food.x > snakeHead.x;
        boolean foodUp = food.y < snakeHead.y;
        boolean foodDown = food.y > snakeHead.y;

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
        return state;
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

    private Tile nextTile(Tile head, Dir d){
        switch(d){
            case UP: return new Tile(head.x, head.y-1);
            case DOWN: return new Tile(head.x, head.y+1);
            case LEFT: return new Tile(head.x-1, head.y);
            case RIGHT: return new Tile(head.x+1, head.y);
        }
        return head;
    }

    private boolean willCollide(Tile p, boolean willEat) {
        if(p.x < 0 || p.y < 0 || p.x >= boardWidth || p.y >= boardHeight) return true;
        int checkUntil = snakeBody.size() - (willEat ? 0 : 1);
        for(int i=0;i<checkUntil;i++){
            Tile s = snakeBody.get(i);
            if(p.equals(s)) return true;
        }
        return false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // System.out.println("KeyEvent: " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) {
            velocityX = 0;
            velocityY = -1;
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) {
            velocityX = 0;
            velocityY = 1;
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) {
            velocityX = -1;
            velocityY = 0;
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) {
            velocityX = 1;
            velocityY = 0;
        }
    }

    //not needed
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
