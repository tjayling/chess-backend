package org.gui;

import static org.gui.Constants.SCREEN_SIZE;
import static org.gui.GuiPage.PERFT_GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class LandingGui extends JPanel {
  private final GuiController controller;

  public LandingGui(GuiController controller) {
    this.controller = controller;

    setLayout(new BorderLayout());
    setBackground(Color.lightGray);
    setPreferredSize(SCREEN_SIZE);
    add(new ButtonPanel(), BorderLayout.CENTER);
    setVisible(true);
  }

  private class ButtonPanel extends JPanel {
    JButton perftGuiButton;

    public ButtonPanel() {
      setLayout(new FlowLayout(FlowLayout.CENTER));
      setBackground(Color.lightGray);
      perftGuiButton = new JButton("Perft");

      addButtons();
      addActionListeners();

      setVisible(true);
    }

    private void addButtons() {
      add(perftGuiButton);
    }

    private void addActionListeners() {
      perftGuiButton.addActionListener(e -> controller.setActiveGui(PERFT_GUI));
    }
  }
}
