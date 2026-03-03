package com.mathpuzzlegame.theme;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;

public class ThemeManager {

    private boolean darkMode = false;

    public void applyInitialTheme() {
        FlatMacLightLaf.setup();
        darkMode = false;
    }

    public void toggleTheme(JFrame frame) {
        try {
            if (darkMode) {
                UIManager.setLookAndFeel(new FlatMacLightLaf());
            } else {
                UIManager.setLookAndFeel(new FlatMacDarkLaf());
            }
            darkMode = !darkMode;
            SwingUtilities.updateComponentTreeUI(frame);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isDarkMode() {
        return darkMode;
    }
}