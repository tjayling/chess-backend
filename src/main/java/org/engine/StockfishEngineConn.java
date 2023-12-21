package org.engine;

import org.gui.perft.PerftGuiController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class StockfishEngineConn {

    private static OutputStreamWriter processWriter;

    public static List<String> runPerftFromFen(String fen, int depth, PerftGuiController controller) {
        List<String> output = new ArrayList<>();
        try {
            Process process = new ProcessBuilder("stockfish").start();

            BufferedReader processReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            processWriter = new OutputStreamWriter(process.getOutputStream());

            sendCommand(String.format("position fen %s", fen));
            sendCommand(String.format("go perft %s", depth));

            String line;
            do {
                line = processReader.readLine();
                if (!line.startsWith("Stockfish")) {
                    controller.addStringToStockfishPane(line + "\n");
                    output.add(line);
                }
            } while (!line.startsWith("Nodes"));

            sendCommand("quit");
            processWriter.close();
            processReader.close();

            return output;
        } catch (IOException e) {
            controller.addStringToStockfishPane(String.format("There was an error starting the stockfish process: %s\n", e.getMessage()));
            return new ArrayList<>();
        }
    }

    private static void sendCommand(String command) throws IOException {
        processWriter.write(command + "\n");
        processWriter.flush();
    }
}
