package net.sn0wix_.gui;

import net.sn0wix_.Main;

import javax.swing.*;
import java.awt.*;

public class ConsoleJFrame extends JFrame {

    public ConsoleJFrame() throws HeadlessException {
        this.setResizable(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Freemcserver Backupper v1.0");

        if (Main.CONFIG.darkMode) {
            this.getContentPane().setBackground(new Color(45, 45, 45));
        }
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.pack();

        this.setVisible(true);
        //centerFrameOnScreen(this);
    }

    private void centerFrameOnScreen(JFrame frame) {
        // Get the screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Calculate the new location for the JFrame
        int x = screenSize.width;
        int y = screenSize.height;

        System.out.println("X: " + x + " ,y: " + y);
        // Set the location of the JFrame
        frame.setLocation(x / 2, y / 2);
    }
}
