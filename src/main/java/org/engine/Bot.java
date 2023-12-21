package org.engine;

import java.util.List;
import java.util.Random;

public class Bot {
  public String makeMove(String boardFen, List<String> moves) {
    Random rand = new Random();
    return moves.get(rand.nextInt(moves.size()));
  }
}
