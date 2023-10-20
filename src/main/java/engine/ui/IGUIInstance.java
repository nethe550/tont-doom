package engine.ui;

import engine.Window;
import engine.scene.Scene;

public interface IGUIInstance {

    void drawGUI();

    boolean handleGUIInput(Scene scene, Window window);

}
