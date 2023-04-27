package org.gui;

import org.engine.Perft;
import org.engine.StockfishEngine;
import org.gui.perft.PerftGui;
import org.logic.Mediator;

import java.awt.*;
import java.util.Collections;
import java.util.List;

import static org.util.FenUtil.getSquaresFromFen;

public class GuiController {
    private PerftGui perftGui;
    private ChessGui chessGui;
    private Mediator mediator;

    protected GuiController(Mediator mediator) {
        this.mediator = mediator;
    }

    protected void registerViews(PerftGui perftGui, ChessGui chessGui) {
        this.perftGui = perftGui;
        this.chessGui = chessGui;

        this.chessGui.setVisible(true);
        this.perftGui.setVisible(true);

        this.chessGui.setSquares(getSquaresFromFen(mediator.getCurrentFen()));
        this.chessGui.setMoves(mediator.getMoves());
    }

    public void makeMove(String move) {
        mediator.makeMove(move);
        chessGui.setMoves(mediator.getMoves());
        chessGui.setSquares(getSquaresFromFen(mediator.getCurrentFen()));
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
            EventQueue.invokeLater(() -> perftGui.addStringToPerftDiffPane(String.format("Time taken: %s ms\n", totalTime)));
            times[i - 1] = totalTime;
        }

        double sum = 0;
        for (double time : times) {
            sum += time;
        }
        double averageTime = sum / (iterations - 1);

        EventQueue.invokeLater(() -> perftGui.addStringToPerftDiffPane(String.format("\nAverage time taken for depth of %s:\n %s ms\n", depth, averageTime)));
    }

    public void runPerftFromCurrentState(int depth) {
        String currentFen = mediator.getCurrentFen();

        List<String> localPerft = Perft.runPerftFromFen(currentFen, depth, this);

        List<String> stockfishPerft = StockfishEngine.runPerftFromFen(currentFen, depth, this);

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
        chessGui.setSquares(getSquaresFromFen(mediator.getCurrentFen()));
        chessGui.setMoves(mediator.getMoves());
    }

    public void addStringToPerftPane(String string) {
        perftGui.addStringToPerftPane(string);
    }

    public void addStringToStockfishPane(String string) {
        perftGui.addStringToStockfishPane(string);
    }

    public void addStringToPerftDiffPane(String string) {
        perftGui.addStringToPerftDiffPane(string);
    }

    public void addStringToStockfishDiffPane(String string) {
        perftGui.addStringToStockfishDiffPane(string);
    }
}
