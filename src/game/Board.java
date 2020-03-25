/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

/**
 *
 * @author 10214291
 */
public class Board extends JPanel {

    class MyKeyAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (canMove(currentShape, currentRow, currentCol - 1)) {
                        makeMove(currentRow, currentCol - 1);
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (canMove(currentShape, currentRow, currentCol + 1)) {
                        makeMove(currentRow, currentCol + 1);
                    }
                    break;
                case KeyEvent.VK_UP:
                    Shape s = currentShape.rotateLeft();
                    if (canMove(s, currentRow, currentCol)) {
                        currentShape = s;
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (canMove(currentShape, currentRow + 1, currentCol)) {
                        makeMove(currentRow + 1, currentCol);
                    }
                    break;
                case KeyEvent.VK_SPACE:
                    while (canMove(currentShape, currentRow + 1, currentCol)) {
                        currentRow++;
                    }
                    break;
                case KeyEvent.VK_P:
                    pauseGame();
                    break;
                default:
                    break;
            }
            repaint();
        }

    }

    public static final int NUM_ROWS = 22;
    public static final int NUM_COLS = 10;
    public static final int INITIAL_DELTA_TIME = 500;
    public static final int PIECE_VALUE = 4;
    public static final int LINE_VALUE = 10;
    private Tetrominoes[][] playBoard;
    private Shape currentShape;
    private int currentRow;
    private int currentCol;
    private Timer timer;
    private int deltaTime;
    private ScoreBoardIncrementer scoreBoard;
    int reachPoints = 300;
    private StartGame startGame;
    private PauseGame pauseGame;
    private String playerName;

    public Board(){

        super();
        setFocusable(true);
        playBoard = new Tetrominoes[NUM_ROWS][NUM_COLS];
        deltaTime = INITIAL_DELTA_TIME;
        timer = new Timer(deltaTime, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (canMove(currentShape, currentRow + 1, currentCol)) {
                    currentRow++;
                    repaint();
                } else if (currentRow == 0) {
                    try {
                        timer.stop();
                        updateScores();
                    } catch (IOException ex) {
                        
                    }

                } else {
                    moveCurrentShapeToBoard();
                    resetCurrenShape();
                    checkLineCompleted();
                    scoreBoard.incrementScore(PIECE_VALUE);
                }
                changeDeltaTime();
                Toolkit.getDefaultToolkit().sync();

            }

        });

        MyKeyAdapter keyAdepter = new MyKeyAdapter();
        addKeyListener(keyAdepter);
        resetGame();
    }

    private void updateScores() throws IOException {
        startGame.makeList(scoreBoard.getScore());
        startGame.orderList();
        startGame.saveList();
        startGame.printList();
    }

    

    public Board(ScoreBoardIncrementer scb, JFrame parent) {
        this();
        scoreBoard = scb;
        startGame = new StartGame(parent, true, this);
        pauseGame = new PauseGame(parent, true, this);
    }

    private void pauseGame() {
        timer.stop();
        pauseGame.setVisible(true);
    }

    public void startTimer() {
        timer.start();
    }


    private void changeDeltaTime() {
        if (scoreBoard.getScore() > reachPoints) {
            deltaTime -= 50;
            reachPoints *= 2;
            timer.setDelay(deltaTime);
        }
    }

    private void checkLineCompleted() {
        int countLines = 0;
        int countCols;
        for (int row = 0; row < NUM_ROWS; row++) {
            countCols = 0;
            for (int col = 0; col < NUM_COLS; col++) {
                if (playBoard[row][col] == Tetrominoes.NoShape) {
                    break;
                }
                countCols++;
            }
            if (countCols == NUM_COLS) {
                removeLineAndAdjustBoard(row);
                countLines++;
            }
        }
        int totalScore = LINE_VALUE * countLines * countLines;
        scoreBoard.incrementScore(totalScore);
    }

    private void removeLineAndAdjustBoard(int numRow) {
        for (int col = 0; col < NUM_COLS; col++) {
            playBoard[numRow][col] = Tetrominoes.NoShape;
        }
        for (int row = numRow; row >= 0; row--) {
            for (int col = 0; col < NUM_COLS; col++) {
                if (row != 0) {
                    playBoard[row][col] = playBoard[row - 1][col];
                } else {
                    playBoard[row][col] = Tetrominoes.NoShape;
                }
            }
        }
    }

    private void moveCurrentShapeToBoard() {
        for (int i = 0; i < 4; i++) {
            playBoard[currentRow + currentShape.getY(i)][currentCol + currentShape.getX(i)] = currentShape.getShape();
        }
    }

    private void resetCurrenShape() {
        currentShape = new Shape();
        currentRow = 0;
        currentCol = NUM_COLS / 2;
    }

    public void makeMove(int row, int col) {
        currentRow = row;
        currentCol = col;
    }

    public boolean canMove(Shape shape, int newRow, int newCol) {
        int leftBorder = newCol + shape.minX();
        int rightBorder = newCol + shape.maxX();
        int botBorder = newRow + shape.maxY();
        if (leftBorder < 0 || rightBorder >= NUM_COLS || botBorder == NUM_ROWS) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            if (currentRow + shape.getY(i) >= 0) {
                if (playBoard[newRow + shape.getY(i)][newCol + shape.getX(i)] != Tetrominoes.NoShape) {
                    return false;
                }
            }
        }

        return true;
    }

    public void initGame() {
        resetGame();
        resetCurrenShape();
        timer.start();
        scoreBoard.setScore(0);
    }

    private void resetGame() {
        for (int row = 0; row < playBoard.length; row++) {
            for (int col = 0; col < playBoard[0].length; col++) {
                playBoard[row][col] = Tetrominoes.NoShape;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        paintPlayBoard(g2d);
        if (currentShape != null) {
            paintShape(g2d);
        }
    }

    private int squareWidth() {
        return getWidth() / NUM_COLS;
    }

    private int squareHeight() {
        return getHeight() / NUM_ROWS;
    }

    private void paintPlayBoard(Graphics2D g2d) {
        for (int row = 0; row < playBoard.length; row++) {
            for (int col = 0; col < playBoard[0].length; col++) {
                drawSquare(g2d, row, col, playBoard[row][col]);
            }
        }
    }

    private void paintShape(Graphics2D g) {
        for (int i = 0; i < 4; i++) {
            int x = currentCol + currentShape.getX(i);
            int y = currentRow + currentShape.getY(i);

            drawSquare(g, y, x, currentShape.getShape());

        }
    }

    private void drawSquare(Graphics2D g, int row, int col, Tetrominoes shape) {
        Color colors[] = {new Color(0, 0, 0), new Color(204, 102, 102),
            new Color(102, 204, 102), new Color(102, 102, 204),
            new Color(204, 204, 102), new Color(204, 102, 204),
            new Color(102, 204, 204), new Color(218, 170, 0)};
        int x = col * squareWidth();
        int y = row * squareHeight();
        Color color = colors[shape.ordinal()];
        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);
        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
    }

}
