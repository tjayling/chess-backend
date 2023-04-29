package org.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import static org.gui.Constants.*;
import static org.util.FenUtil.getSquaresFromFen;
import static org.util.MoveUtil.getStartSquare;
import static org.util.MoveUtil.getTargetSquare;

public class ChessGui extends JPanel implements ActionListener {
    private final GuiController controller;
    private List<String> moves;
    private int[] squares;
    private int selectedStartSquare;
    private boolean pieceIsSelected = false;
    private List<String> selectedMoves = new ArrayList<>();

    public ChessGui(GuiController controller) {
        this.controller = controller;

        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setFocusable(true);
        setBackground(Color.lightGray);
        addMouseListener(new MouseAdapterImpl());

        selectedStartSquare = -1;

        Timer timer = new Timer(30, this);
        timer.start();
    }

    public void drawBoard(Graphics2D g2, int[] squares) {
        if (squares == null) {
            return;
        }


        int offset = 12;
        int size = 50;

        // Draw board
        g2.setStroke(new java.awt.BasicStroke(3));
        g2.setColor(Color.black);
        for (int file = 0; file < CELLS; file++) {
            for (int rank = 0; rank < CELLS; rank++) {
                if ((file + rank) % 2 == 0) {
                    g2.fillRect(BOARD_HEIGHT - CELL_SIZE - file * CELL_SIZE, BOARD_HEIGHT - CELL_SIZE - (rank * CELL_SIZE), CELL_SIZE, CELL_SIZE);
                }
//                g2.setColor(((8 * rank) + file) % 2 == 0 ? Color.white : Color.black);

//                g2.drawString(Integer.toString((8 * rank) + file), CELL_SIZE + file * CELL_SIZE - 20, CELL_SIZE + (rank * CELL_SIZE));

            }
        }

        // Draw selected square and pieces
        g2.setColor(Color.RED);
        for (int rank = 0; rank < CELLS; rank++) {
            for (int file = 0; file < CELLS; file++) {
                int currentPosition = file + (rank * 8);
                if (selectedStartSquare == currentPosition && squares[currentPosition] != 0) {
                    g2.drawRect(file * CELL_SIZE, rank * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
                if (squares[currentPosition] == 0) {
                    continue;
                }
                g2.drawImage(PIECE_MAP.get(squares[currentPosition]), file * CELL_SIZE + offset, rank * CELL_SIZE + offset, size, size, null);
            }
        }

        // Draw possible moves
        for (int rank = 0; rank < CELLS; rank++) {
            for (int file = 0; file < CELLS; file++) {
                int currentPosition = file + (rank * 8);
                g2.setColor(new Color(20, 167, 204, 200));
                for (String move : selectedMoves) {
                    if (getTargetSquare(move) == currentPosition) {
                        g2.fillOval(file * CELL_SIZE + offset + 10, rank * CELL_SIZE + offset + 10, 30, 30);
                    }
                }
            }
        }
    }



    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.translate(BOARD_OFFSET, BOARD_OFFSET);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g2.setColor(Color.black);
        drawBoard(g2, squares);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        repaint();
    }

    public void setSquares(int[] squares) {
        this.squares = squares;
    }

    public void setMoves(List<String> moves) {
        this.moves = moves;
    }

    private class MouseAdapterImpl extends MouseAdapter {
        @Override
        public void mouseReleased(MouseEvent e) {
            int mouseX = e.getX();
            int mouseY = e.getY();
            if (mouseX > BOARD_OFFSET && mouseX < BOARD_WIDTH + BOARD_OFFSET && mouseY > BOARD_OFFSET && mouseY < BOARD_HEIGHT + BOARD_OFFSET) {
                int file = Math.floorDiv(mouseX - BOARD_OFFSET, CELL_SIZE);
                int rank = Math.floorDiv(mouseY - BOARD_OFFSET, CELL_SIZE);
                if (pieceIsSelected) {
                    int selectedTargetSquare = (rank * 8) + file;
                    for (String move : moves) {
                        int startSquare = getStartSquare(move);
                        int targetSquare = getTargetSquare(move);
                        if (startSquare == selectedStartSquare && targetSquare == selectedTargetSquare) {
                            controller.makeMove(move);

                            pieceIsSelected = false;
                            selectedStartSquare = -1;
                            selectedMoves = new ArrayList<>();
                            return;
                        }
                    }
                }

                selectedStartSquare = (rank * 8) + file;
                selectedMoves = new ArrayList<>();
                for (String move : moves) {
                    if (getStartSquare(move) == selectedStartSquare) {
                        selectedMoves.add(move);
                    }
                }
                pieceIsSelected = true;
                return;

            }
            pieceIsSelected = false;
        }
    }
}
