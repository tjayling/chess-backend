package org.gui.shared;

import static org.gui.Constants.SCREEN_SIZE;
import static org.gui.GuiPage.LANDING_PAGE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.gui.GuiController;

public class HeaderPanel extends JPanel {
  private final GuiController controller;

  public HeaderPanel() {
    this.controller = GuiController.getInstance();

    setLayout(new BorderLayout());
    setBackground(Color.lightGray);
    setPreferredSize(SCREEN_SIZE);
    add(new ButtonPanel(), BorderLayout.CENTER);
    setVisible(true);
  }

  private class ButtonPanel extends JPanel {
    JButton homeButton;

    public ButtonPanel() {
      setLayout(new FlowLayout(FlowLayout.CENTER));
      setBackground(Color.lightGray);
      homeButton = new JButton("\uD83C\uDFE0");

      addButtons();
      addActionListeners();

      setVisible(true);
    }

    private void addButtons() {
      add(homeButton);
    }

    private void addActionListeners() {
      homeButton.addActionListener(e -> controller.setActiveGui(LANDING_PAGE));
    }
  }
}
