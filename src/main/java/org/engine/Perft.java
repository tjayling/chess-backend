package org.engine;

import org.gui.GuiController;
import org.logic.Mediator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Perft {
    private static Mediator mediator;

    private static List<String> output;

    public static List<String> runPerftFromFen(String fen, int depth, GuiController controller) {
        output = new ArrayList<>();
        mediator = new Mediator(fen, false);
        int totalPermutations = search(depth, depth, "", controller);

        EventQueue.invokeLater(() -> controller.addStringToPerftPane("\n"));
        String nodesSearched = String.format("Nodes searched: %s", totalPermutations);
        EventQueue.invokeLater(() -> controller.addStringToPerftPane("\n"));

        output(nodesSearched, controller);
        return output;
    }

    public static int search(int depth, int targetDepth, String previousMove, GuiController controller) {
        List<String> moves = mediator.getMoves();
        if (depth == 1) {
            int possibleMoves = moves.size();
            if (targetDepth == 2) {
                String moveString = String.format("%s: %s", previousMove, possibleMoves);
                output(moveString, controller);
                return possibleMoves;
            }
            if (targetDepth == 1) {
                for (String move : moves) {
                    String moveString = String.format("%s: %s", move, 1);
                    output(moveString, controller);
                }
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
            String moveString = String.format("%s: %s", previousMove, possibleMoves);
            output(moveString, controller);
        }

        return possibleMoves;
    }

    private static void output(String str, GuiController controller) {
        EventQueue.invokeLater(() -> controller.addStringToPerftPane(str +"\n"));
        output.add(str);
    }
}
