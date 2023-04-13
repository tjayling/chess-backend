package org.gui;

import org.engine.Perft;
import org.logic.Mediator;

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
        Perft.runPerftFromFen(currentFen, depth, this);
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
}
