package org.gui.perft;

import org.engine.Bot;
import org.engine.Perft;
import org.engine.StockfishEngineConn;
import org.gui.chess.ChessAreaGui;
import org.logic.Mediator;

import java.awt.*;
import java.util.Collections;
import java.util.List;

import static org.util.FenUtil.getSquaresFromFen;

public class PerftGuiController {
  private static PerftGuiController INSTANCE;
  private PerftPanel perftPanel;
  private ChessAreaGui chessAreaGui;
  private Mediator mediator;
  private Bot bertie;
  private boolean botMode = true;

  private PerftGuiController(Mediator mediator) {
    this.mediator = mediator;
    this.bertie = new Bot();
  }

  public static PerftGuiController instantiate(Mediator mediator) {
    if (INSTANCE != null) {
      throw new RuntimeException("PerftGuiController already initialized");
    }
    INSTANCE = new PerftGuiController(mediator);
    return INSTANCE;
  }

  public static PerftGuiController getInstance() {
    if (INSTANCE == null) {
      throw new RuntimeException("PerftGuiController not initialized");
    }
    return INSTANCE;
  }

  public void registerViews(PerftPanel perftPanel, ChessAreaGui chessAreaGui) {
    this.perftPanel = perftPanel;
    this.chessAreaGui = chessAreaGui;

    this.chessAreaGui.setVisible(true);
    this.perftPanel.setVisible(true);

    this.chessAreaGui.setSquares(getSquaresFromFen(mediator.getCurrentFen()));
    this.chessAreaGui.setMoves(mediator.getMoves());
  }

  public void makeMove(String move) {
    mediator.makeMove(move);
    chessAreaGui.setMoves(mediator.getMoves());
    chessAreaGui.setSquares(getSquaresFromFen(mediator.getCurrentFen()));
    mediator.makeMove(bertie.makeMove(mediator.getCurrentFen(), mediator.getMoves()));
    chessAreaGui.setMoves(mediator.getMoves());
    chessAreaGui.setSquares(getSquaresFromFen(mediator.getCurrentFen()));
  }

  public void getPerftTiming(int depth) {
    String currentFen = mediator.getCurrentFen();

    int iterations = 5;

    double[] times = new double[iterations - 1];

    for (int i = 0; i < iterations; i++) {
      long startTime = System.nanoTime();
      Perft.runPerftFromFen(currentFen, depth, this);
      long endTime = System.nanoTime();
      if (i == 0) {
        continue;
      }
      double totalTime = (endTime - startTime) / 1000000.0;
      EventQueue.invokeLater(() -> perftPanel.addStringToPerftDiffPane(String.format("Time taken: %s ms\n", totalTime)));
      times[i - 1] = totalTime;
    }

    double sum = 0;
    for (double time : times) {
      sum += time;
    }
    double averageTime = sum / (iterations - 1);

    EventQueue.invokeLater(() -> perftPanel.addStringToPerftDiffPane(String.format("\nAverage time taken for depth of %s:\n %s ms\n", depth, averageTime)));
  }

  public void runPerftFromCurrentState(int depth) {
    String currentFen = mediator.getCurrentFen();

    List<String> localPerft = Perft.runPerftFromFen(currentFen, depth, this);

    List<String> stockfishPerft = StockfishEngineConn.runPerftFromFen(currentFen, depth, this);

    List<String> tempLocalPerft = List.copyOf(localPerft);

    localPerft.removeAll(stockfishPerft);
    stockfishPerft.removeAll(tempLocalPerft);
    stockfishPerft.remove("");

    Collections.sort(localPerft);
    Collections.sort(stockfishPerft);

    for (String s : localPerft) {
      addStringToPerftDiffPane(s + "\n");
    }
    for (String s : stockfishPerft) {
      addStringToStockfishDiffPane(s + "\n");
    }
  }

  public String getCurrentFen() {
    return mediator.getCurrentFen();
  }

  public void loadFen(String fenString) {
    mediator = new Mediator(fenString, false);
    chessAreaGui.setSquares(getSquaresFromFen(mediator.getCurrentFen()));
    chessAreaGui.setMoves(mediator.getMoves());
  }

  public void addStringToPerftPane(String string) {
    perftPanel.addStringToPerftPane(string);
  }

  public void addStringToStockfishPane(String string) {
    perftPanel.addStringToStockfishPane(string);
  }

  public void addStringToPerftDiffPane(String string) {
    perftPanel.addStringToPerftDiffPane(string);
  }

  public void addStringToStockfishDiffPane(String string) {
    perftPanel.addStringToStockfishDiffPane(string);
  }

  public void setBotMode(boolean botMode) {
    this.botMode = botMode;
  }
}
