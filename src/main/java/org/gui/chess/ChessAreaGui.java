package org.gui.chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.border.EmptyBorder;

public class ChessAreaGui extends JPanel implements ActionListener {
  private final ChessBoardGui chessBoardGui;
  private final ChessOptionsGui chessOptionsGui;


  public ChessAreaGui() {
    this.chessBoardGui = new ChessBoardGui();
    this.chessOptionsGui = new ChessOptionsGui();

    setLayout(new BorderLayout());
    setFocusable(true);
    setBackground(Color.lightGray);
    setBorder(new EmptyBorder(25, 25, 25, 25));
    add(chessBoardGui, BorderLayout.NORTH);
    add(chessOptionsGui, BorderLayout.SOUTH);
  }

  public void setSquares(int[] squares) {
    chessBoardGui.setSquares(squares);
  }

  public void setMoves(List<String> moves) {
    chessBoardGui.setMoves(moves);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    repaint();
  }
}
