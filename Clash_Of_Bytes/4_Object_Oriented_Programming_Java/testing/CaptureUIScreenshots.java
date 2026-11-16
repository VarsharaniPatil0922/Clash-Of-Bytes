package testing;

import database.DatabaseInitializer;
import model.User;
import ui.CharacterSelectionFrame;
import ui.MatchHistoryFrame;
import ui.PlayerDashboardFrame;
import ui.TwoPlayerLoginFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Headless & Automated Capture Utility to generate high-definition PNG screenshots
 * of all four Java Swing Database Authentication & Match History windows.
 */
public class CaptureUIScreenshots {

    public static void main(String[] args) {
        System.out.println("[*] Initializing Database & Swing Screenshot Capturer...");

        try {
            DatabaseInitializer.initializeDatabase();

            // Set Cross-Platform or System Look and Feel
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}

            User p1 = new User(1, "shadow_master", "shadow@clashofbytes.edu", "hash1", "salt1", null);
            User p2 = new User(2, "blaze_warrior", "blaze@clashofbytes.edu", "hash2", "salt2", null);

            // 1. Capture Two-Player Dual Login Frame
            System.out.println("[*] Rendering TwoPlayerLoginFrame...");
            TwoPlayerLoginFrame loginFrame = new TwoPlayerLoginFrame();
            captureFrame(loginFrame, 950, 680, "screenshot_ui_login.png");
            loginFrame.dispose();

            // 2. Capture Character Selection Frame
            System.out.println("[*] Rendering CharacterSelectionFrame...");
            CharacterSelectionFrame charFrame = new CharacterSelectionFrame(p1, p2);
            captureFrame(charFrame, 1050, 720, "screenshot_ui_character_select.png");
            charFrame.dispose();

            // 3. Capture Player Dashboard Frame
            System.out.println("[*] Rendering PlayerDashboardFrame...");
            PlayerDashboardFrame dashFrame = new PlayerDashboardFrame(p1.getUserId());
            captureFrame(dashFrame, 780, 560, "screenshot_ui_player_dashboard.png");
            dashFrame.dispose();

            // 4. Capture Match History Frame
            System.out.println("[*] Rendering MatchHistoryFrame...");
            MatchHistoryFrame historyFrame = new MatchHistoryFrame(p1.getUserId(), p1.getUsername());
            captureFrame(historyFrame, 980, 580, "screenshot_ui_match_history.png");
            historyFrame.dispose();

            System.out.println("[SUCCESS] All 4 GUI screenshots captured successfully!");

        } catch (Exception e) {
            System.err.println("[CaptureUIScreenshots] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void captureFrame(JFrame frame, int width, int height, String outputFileName) throws Exception {
        frame.addNotify();
        frame.setSize(width, height);
        frame.validate();
        frame.setVisible(true);
        Thread.sleep(400); // Allow Swing to layout and render components on EDT

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        frame.printAll(g2);
        g2.dispose();
        frame.setVisible(false);

        File out = new File(outputFileName);
        ImageIO.write(img, "png", out);
        System.out.println("  -> Saved: " + out.getAbsolutePath() + " (" + out.length() + " bytes)");

        // Also copy to artifact directory
        File artifactDir = new File("C:/Users/Varsharani/.gemini/antigravity/brain/1a3f9240-5ae9-47fe-af89-23a4dfd6eded");
        if (artifactDir.exists()) {
            File artifactFile = new File(artifactDir, outputFileName);
            ImageIO.write(img, "png", artifactFile);
        }
    }
}
