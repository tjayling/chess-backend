package org.logic;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import static org.util.FenUtil.generateFenFromMoveData;
import static org.util.LoadUtil.loadFenFromFile;

public class Mediator {
    private static final Queue<String> squareHistory = Collections.asLifoQueue(new ArrayDeque<>());
    private String currentFen;
    private Board board;

    public Mediator() {
        this.currentFen = loadFenFromFile();
        this.board = new Board(currentFen);
    }


    public Mediator(String str, boolean isUrl) {
        this.currentFen = isUrl ? loadFenFromFile(str) : str;
        this.board = new Board(currentFen);
    }

    public String getCurrentFen() {
        return currentFen;
    }

    public List<String> getMoves() {
        return board.getMoves();
    }

    public String makeMove(String move) {
        squareHistory.add(currentFen);
        MoveData moveData = board.move(move);
        moveData.flipColourToPlay();

        currentFen = generateFenFromMoveData(moveData);
        board = new Board(currentFen);
        return currentFen;
    }

    public void unmakeMove() {
        currentFen = squareHistory.remove();
        board = new Board(currentFen);
    }
}
