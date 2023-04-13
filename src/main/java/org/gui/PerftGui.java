package org.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static org.gui.Constants.*;

public class PerftGui extends JPanel implements ActionListener {
    private final JTextArea textOutput;
    public PerftGui(GuiController controller) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, SCREEN_HEIGHT));
        setFocusable(true);
        setBackground(Color.lightGray);
        addMouseListener(new PerftGui.MouseAdapterImpl());

        int margin = 15;
        setBorder(BorderFactory.createEmptyBorder(margin + 5, margin, margin, margin + 5));

        // Create a JComboBox
        Integer[] items = {1, 2, 3, 4, 5, 6};
        JComboBox<Integer> comboBox = new JComboBox<>(items);

        JButton doPerftButton = new JButton("Do perft");

        textOutput = new JTextArea();
        textOutput.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textOutput);

        int scrollPaneMargin = 10;
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(scrollPaneMargin, scrollPaneMargin, scrollPaneMargin, scrollPaneMargin));

        // Create a JPanel for center alignment
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(Color.lightGray);
        topPanel.add(comboBox);
        topPanel.add(doPerftButton);

        // Add topPanel and textOutput to the PerftGui panel
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Attach an ActionListener to the JButton
        doPerftButton.addActionListener(e -> {
            if (!textOutput.getText().isEmpty()) {
                textOutput.append("---------------------------\n\n");
            }
            Integer selectedItem= (Integer) comboBox.getSelectedItem();
            if (selectedItem != null) {
                int depth = selectedItem;
                String message = String.format("Starting perft test to a depth of %s...\n", depth);
                textOutput.append(message);
                if (depth > 4) {
                    textOutput.append("This may take a while...\n");
                }

                new Thread(() -> controller.runPerftFromCurrentState(depth)).start();
            }
        });

        // Add the bottom panel to the bottom of the panel
        FenPanel fenPanel = new FenPanel(controller);
        add(fenPanel, BorderLayout.SOUTH);
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        repaint();
    }

    public void addStringToPerftPane(String string) {
        textOutput.append(string);
    }

    private class MouseAdapterImpl extends MouseAdapter {
        @Override
        public void mouseReleased(MouseEvent e) {
            int mouseX = e.getX();
            int mouseY = e.getY();

        }
    }
}
