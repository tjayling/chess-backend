package org.gui;

import org.engine.Perft;
import org.engine.StockfishEngine;
import org.gui.perft.PerftGui;
import org.logic.Mediator;

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

    public void runPerftFromCurrentState(int depth) {
        String currentFen = mediator.getCurrentFen();

        long startTime = System.nanoTime();
        List<String> localPerft = Perft.runPerftFromFen(currentFen, depth, this);
        long endTime = System.nanoTime();
        double timeTakenInMs = (endTime - startTime) / 1000000.0;

        System.out.println("Time taken: " + timeTakenInMs + " ms");

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
