package testing;

import database.DatabaseInitializer;
import model.User;
import ui.CharacterSelectionFrame;
import ui.LeaderboardFrame;
import ui.MainMenuFrame;
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
 * of all six Java Swing Database Authentication & Combat Hub windows.
 */
public class CaptureUIScreenshots {

    public static void main(String[] args) {
        System.out.println("[*] Initializing Database & Swing Screenshot Capturer...");

        try {
            DatabaseInitializer.initializeDatabase();

            // Set System or Cross-Platform Look and Feel
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}

            model.PlayerSession p1 = new model.PlayerSession(
                "da5fe071-df32-4c3a-8cbd-97ff031e6660",
                "shadow_master",
                "shadow_master@gmail.com",
                "mock_token_1",
                "mock_refresh_1",
                3600
            );
            model.PlayerSession p2 = new model.PlayerSession(
                "ed51dd64-b253-47b7-b17e-c63bf7d8914e",
                "blaze_warrior",
                "blaze_warrior@gmail.com",
                "mock_token_2",
                "mock_refresh_2",
                3600
            );
            service.PlayerSessionManager.getInstance().clearAll();
            service.PlayerSessionManager.getInstance().setPlayer1Session(p1);
            service.PlayerSessionManager.getInstance().setPlayer2Session(p2);

            // 1. Capture Main Menu Frame
            System.out.println("[*] Rendering MainMenuFrame...");
            MainMenuFrame mainFrame = new MainMenuFrame();
            captureFrame(mainFrame, 960, 680, "screenshot_ui_main_menu.png");
            mainFrame.dispose();

            // 2. Capture Two-Player Dual Login Frame
            System.out.println("[*] Rendering TwoPlayerLoginFrame...");
            TwoPlayerLoginFrame loginFrame = new TwoPlayerLoginFrame();
            captureFrame(loginFrame, 980, 720, "screenshot_ui_login.png");
            loginFrame.dispose();

            // 3. Capture Character Selection Frame
            System.out.println("[*] Rendering CharacterSelectionFrame...");
            CharacterSelectionFrame charFrame = new CharacterSelectionFrame(p1, p2);
            captureFrame(charFrame, 1060, 740, "screenshot_ui_character_select.png");
            charFrame.dispose();

            // 4. Capture Player Dashboard Frame
            System.out.println("[*] Rendering PlayerDashboardFrame...");
            PlayerDashboardFrame dashFrame = new PlayerDashboardFrame(p1);
            captureFrame(dashFrame, 860, 640, "screenshot_ui_player_dashboard.png");
            dashFrame.dispose();

            // 5. Capture Match History Frame
            System.out.println("[*] Rendering MatchHistoryFrame...");
            MatchHistoryFrame historyFrame = new MatchHistoryFrame(p1);
            captureFrame(historyFrame, 1020, 620, "screenshot_ui_match_history.png");
            historyFrame.dispose();

            // 6. Capture Tournament Leaderboard Frame
            System.out.println("[*] Rendering LeaderboardFrame...");
            LeaderboardFrame leadFrame = new LeaderboardFrame();
            captureFrame(leadFrame, 960, 620, "screenshot_ui_leaderboard.png");
            leadFrame.dispose();

            System.out.println("[SUCCESS] All 6 Java Swing GUI screenshots captured successfully!");

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
        Thread.sleep(500); // Allow Swing to layout and render components on EDT

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        frame.printAll(g2);
        g2.dispose();
        frame.setVisible(false);

        // Save in current directory
        File out = new File(outputFileName);
        ImageIO.write(img, "png", out);
        System.out.println("  -> Saved: " + out.getAbsolutePath() + " (" + out.length() + " bytes)");

        // Save in root screenshots/ and Clash_Of_Bytes/screenshots/
        File[] targetDirs = {
            new File("screenshots"),
            new File("Clash_Of_Bytes/screenshots"),
            new File("../screenshots"),
            new File("C:/Users/Varsharani/.gemini/antigravity/brain/1a3f9240-5ae9-47fe-af89-23a4dfd6eded")
        };

        for (File dir : targetDirs) {
            if (dir.exists() && dir.isDirectory()) {
                File dest = new File(dir, outputFileName);
                ImageIO.write(img, "png", dest);
            }
        }
    }
}
