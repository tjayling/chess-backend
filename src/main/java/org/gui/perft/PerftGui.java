package org.gui.perft;

import org.gui.GuiController;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static org.gui.Constants.SCREEN_HEIGHT;

public class PerftGui extends JPanel implements ActionListener {
    private final JTextArea localOutput;
    private final JTextArea stockfishOutput;
    private final JTextArea localDiffOutput;
    private final JTextArea stockfishDiffOutput;

    public PerftGui(GuiController controller) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(600, SCREEN_HEIGHT));
        setFocusable(true);
        setBackground(Color.lightGray);
        addMouseListener(new PerftGui.MouseAdapterImpl());

        int margin = 15;
        setBorder(BorderFactory.createEmptyBorder(margin + 5, margin, margin, margin + 5));

        // Create a JComboBox
        Integer[] items = {1, 2, 3, 4, 5, 6, 7};
        JComboBox<Integer> comboBox = new JComboBox<>(items);

        JButton doPerftButton = new JButton("Do perft");
        JButton doEfficiencyTestButton = new JButton("Do efficiency test");

        localOutput = new JTextArea();
        localOutput.setEditable(false);
        JScrollPane localScrollPane = new JScrollPane(localOutput);

        stockfishOutput = new JTextArea();
        stockfishOutput.setEditable(false);
        JScrollPane stockfishScrollPane = new JScrollPane(stockfishOutput);

        localDiffOutput = new JTextArea();
        localDiffOutput.setEditable(false);
        JScrollPane localDiffPane = new JScrollPane(localDiffOutput);

        stockfishDiffOutput = new JTextArea();
        stockfishDiffOutput.setEditable(false);
        JScrollPane stockfishDiffPane = new JScrollPane(stockfishDiffOutput);

        // Add margins to
        int scrollPaneMargin = 10;
        localScrollPane.setViewportBorder(BorderFactory.createEmptyBorder(scrollPaneMargin, scrollPaneMargin, scrollPaneMargin, scrollPaneMargin));
        stockfishScrollPane.setViewportBorder(BorderFactory.createEmptyBorder(scrollPaneMargin, scrollPaneMargin, scrollPaneMargin, scrollPaneMargin));

        // Create top panels
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(Color.lightGray);
        topPanel.add(comboBox);
        topPanel.add(doPerftButton);
        topPanel.add(doEfficiencyTestButton);

        // Create output panels
        JPanel perftOutputPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        JPanel diffOutputPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        diffOutputPanel.setPreferredSize(new Dimension(600, 150));


        //Set backgrounds of panels
        perftOutputPanel.setBackground(Color.lightGray);
        diffOutputPanel.setBackground(Color.lightGray);

        // Add perft outputs to the perft output panel
        perftOutputPanel.add(localScrollPane);
        perftOutputPanel.add(stockfishScrollPane);

        // Add diff outputs to the diff output panel
        diffOutputPanel.add(localDiffPane);
        diffOutputPanel.add(stockfishDiffPane);

        JPanel outputPanel = new JPanel(new BorderLayout());
        Border border = BorderFactory.createEmptyBorder(0, 0, 10, 0);
        perftOutputPanel.setBorder(border);

        outputPanel.add(perftOutputPanel, BorderLayout.CENTER);
        outputPanel.add(diffOutputPanel, BorderLayout.SOUTH);


        add(topPanel, BorderLayout.NORTH);
        add(outputPanel, BorderLayout.CENTER);


        String lineBreak = "---------------------------\n\n";

        // Attach an ActionListener to the doPerftButton
        doPerftButton.addActionListener(e -> {
            if (!localOutput.getText().isEmpty()) {
                localOutput.append(lineBreak);
            }
            if (!stockfishOutput.getText().isEmpty()) {
                stockfishOutput.append(lineBreak);
            }
            if (!localDiffOutput.getText().isEmpty()) {
                localDiffOutput.append(lineBreak);
            }
            if (!stockfishDiffOutput.getText().isEmpty()) {
                stockfishDiffOutput.append(lineBreak);
            }
            Integer depth = (Integer) comboBox.getSelectedItem();
            if (depth != null) {
                String message = String.format("Starting perft test to a depth of %s...\n", depth);
                localOutput.append(message);
                stockfishOutput.append(message);

                new Thread(() -> controller.runPerftFromCurrentState(depth)).start();
            }
        });

        doEfficiencyTestButton.addActionListener(e -> {
            if (!localOutput.getText().isEmpty()) {
                localOutput.append(lineBreak);
            }
            if (!localDiffOutput.getText().isEmpty()) {
                localDiffOutput.append(lineBreak);
            }
            Integer depth = (Integer) comboBox.getSelectedItem();
            if (depth != null) {
                localOutput.append(String.format("Starting efficiency test to a depth of %s...\n", depth));

                new Thread(() -> controller.getPerftTiming(depth)).start();
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
        localOutput.append(string);
    }

    public void addStringToStockfishPane(String string) {
        stockfishOutput.append(string);
    }

    public void addStringToPerftDiffPane(String string) {
        localDiffOutput.append(string);
    }

    public void addStringToStockfishDiffPane(String string) {
        stockfishDiffOutput.append(string);
    }

    private class MouseAdapterImpl extends MouseAdapter {
        @Override
        public void mouseReleased(MouseEvent e) {
            int mouseX = e.getX();
            int mouseY = e.getY();

        }
    }
}
