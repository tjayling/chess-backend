package org.engine;

import org.gui.GuiController;
import org.logic.Mediator;

import java.awt.*;
import java.util.List;

public class Perft {
    private static Mediator mediator;

    public static void runPerftFromFen(String fen, int depth, GuiController controller) {
        mediator = new Mediator(fen, false);
        int totalPermutations = search(depth, depth, "", controller);
        EventQueue.invokeLater(() -> controller.addStringToPerftPane(String.format("Total permutations: %s\n", totalPermutations)));
    }

    public static int search(int depth, int targetDepth, String previousMove, GuiController controller) {
        List<String> moves = mediator.getMoves();
        if (depth == 1) {
            int possibleMoves = moves.size();
            if (targetDepth == 2) {
                EventQueue.invokeLater(() -> controller.addStringToPerftPane(String.format("%s: %s\n", previousMove, possibleMoves)));
                return possibleMoves;
            }
            if (targetDepth == 1) {
                System.out.println(moves);
            }
            return possibleMoves;
        }

        int possibleMoves = 0;
        for (String move : moves) {
            mediator.makeMove(move);
            possibleMoves += search(depth - 1, targetDepth, move, controller);
            mediator.unmakeMove();
        }
        if (depth == targetDepth - 1) {
            int finalPossibleMoves = possibleMoves;
            EventQueue.invokeLater(() -> controller.addStringToPerftPane(String.format("%s: %s\n", previousMove, finalPossibleMoves)));
        }

        return possibleMoves;
    }
}
