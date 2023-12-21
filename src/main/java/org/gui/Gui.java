package org.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Gui extends JFrame {

  public Gui() {
    GuiController controller = GuiController.instantiate(this);

    setLayout(new BorderLayout());
    setTitle("Chess");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setResizable(false);

    controller.showActiveGui();

    pack();
    setLocationRelativeTo(null);
    setVisible(true); // Set visible to true on the Swing event dispatch thread
  }

  public void showActiveGui(JPanel gui) {
    getContentPane().removeAll();
    getContentPane().add(gui, BorderLayout.CENTER);
    pack();
    setLocationRelativeTo(null);
    revalidate();
    repaint();
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(Gui::new);
  }
}
