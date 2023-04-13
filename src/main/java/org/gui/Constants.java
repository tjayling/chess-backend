package org.gui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static org.util.PieceUtil.*;
import static org.util.PieceUtil.BLACK;

class Constants {
    protected static final int BOARD_OFFSET = 25;
    protected static final int SCREEN_WIDTH = 600 + (BOARD_OFFSET * 2);
    protected static final int SCREEN_HEIGHT = 600 + (BOARD_OFFSET * 2);
    protected static final int BOARD_WIDTH = 600;
    protected static final int BOARD_HEIGHT = 600;
    protected static final int CELLS = 8;
    protected static final int CELL_SIZE = BOARD_WIDTH / CELLS;
    protected static final Map<Integer, Image> PIECE_MAP = new HashMap<>() {{
        put(KING | WHITE, new ImageIcon("src/main/resources/assets/white-king.png").getImage());
        put(PAWN | WHITE, new ImageIcon("src/main/resources/assets/white-pawn.png").getImage());
        put(KNIGHT | WHITE, new ImageIcon("src/main/resources/assets/white-knight.png").getImage());
        put(BISHOP | WHITE, new ImageIcon("src/main/resources/assets/white-bishop.png").getImage());
        put(ROOK | WHITE, new ImageIcon("src/main/resources/assets/white-rook.png").getImage());
        put(QUEEN | WHITE, new ImageIcon("src/main/resources/assets/white-queen.png").getImage());
        put(KING | BLACK, new ImageIcon("src/main/resources/assets/black-king.png").getImage());
        put(PAWN | BLACK, new ImageIcon("src/main/resources/assets/black-pawn.png").getImage());
        put(KNIGHT | BLACK, new ImageIcon("src/main/resources/assets/black-knight.png").getImage());
        put(BISHOP | BLACK, new ImageIcon("src/main/resources/assets/black-bishop.png").getImage());
        put(ROOK | BLACK, new ImageIcon("src/main/resources/assets/black-rook.png").getImage());
        put(QUEEN | BLACK, new ImageIcon("src/main/resources/assets/black-queen.png").getImage());
    }};
}
