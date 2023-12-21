package org.gui;

import static org.gui.GuiPage.LANDING_PAGE;
import static org.gui.GuiPage.PERFT_GUI;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import org.gui.perft.PerftGui;

public class GuiController {
  private static GuiController INSTANCE;
  private final Gui gui;
  private GuiPage activeGui = LANDING_PAGE;
  private final Map<GuiPage, JPanel> pageMap;

  private GuiController(Gui gui) {
    this.gui = gui;
    pageMap = new HashMap<>();
  }

  public static GuiController instantiate(Gui gui) {
    if (INSTANCE != null) {
      throw new RuntimeException("GuiController already initialized");
    }
    INSTANCE = new GuiController(gui);
    INSTANCE.registerPages();
    return INSTANCE;
  }

  public static GuiController getInstance() {
    if (INSTANCE == null) {
      throw new RuntimeException("GuiController not initialized");
    }
    return INSTANCE;
  }

  public void showActiveGui() {
    gui.showActiveGui(pageMap.get(activeGui));
  }

  public void setActiveGui(GuiPage guiPage) {
    activeGui = guiPage;
    showActiveGui();
  }

  private void registerPages() {
    pageMap.put(LANDING_PAGE, new LandingGui(this));
    pageMap.put(PERFT_GUI, new PerftGui());
  }
}
