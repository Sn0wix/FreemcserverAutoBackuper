package net.sn0wix_;

import net.sn0wix_.files.FilesUtil;
import net.sn0wix_.files.MaxBackupsChecker;
import net.sn0wix_.files.config.Config;
import net.sn0wix_.files.config.ConfigFile;
import net.sn0wix_.ftp.FtpUtil;
import net.sn0wix_.gui.ConsoleJFrame;
import net.sn0wix_.gui.ConsoleOutputStream;
import net.sn0wix_.gui.MultiplePrintStreams;
import net.sn0wix_.http.HttpUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

public class Main {
    public static final ConfigFile CONFIG_FILE = new ConfigFile("FreemcserverBackups", "config.json", "backups", "latestlog.txt");
    public static Config CONFIG = new Config();
    public static final String CREDITS = "Freemcserver Backupper v1.0 - Made by Sn0wix_: https://linktr.ee/sn0wix_, MIT License, Source code: https://github.com/Sn0wix/FreemcserverAutoBackuper";
    public static ConsoleJFrame frame;

    public static void main(String[] args) {
        CONFIG = CONFIG_FILE.read();

        if (CONFIG != null) {
            try {
                frame = createJFrameAndLogFile(CONFIG.openConsole);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

        } else {
            CONFIG = new Config();
        }

        System.out.println(CREDITS);

        try {
            FilesUtil.createReadMe();

            if (CONFIG_FILE.create()) {
                exit(false, true, false, false);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!CONFIG.lastCheck.isEmpty()) {
            if (!MaxBackupsChecker.hasTimeFromLastCheckPassed()) {
                System.out.println("Cannot backup. Time from the last backup has not ran out yet!");
                exit(false, false, true, false);
            }
        }

        //USER
        if (CONFIG.checkFmcsApiKey) {
            HashMap<String, String> apiKeys = HttpUtil.sendAndRead(HttpUtil.getLoginRequest(), false, false, "LastUserLoginResponse.json", "api_key", "success", "ban_reason");

            if (apiKeys != null && apiKeys.containsKey("api_key") && apiKeys.get("success").equals("true")) {
                CONFIG.fmcsApiKey = apiKeys.get("api_key");
            }

            if (apiKeys != null && apiKeys.containsKey("ban_reason") && !apiKeys.get("ban_reason").isEmpty() && apiKeys.get("ban_reason") != null) {
                System.out.println("!!!WARNING: You have been banned. Reason: " + apiKeys.get("suspension_reason"));
            }

            try {
                //waiting for the request timer to reach zero
                Thread.sleep(CONFIG.delayBetweenRequests);
            } catch (InterruptedException e) {
                System.err.println("Couldn't wait between requests. If this keeps happening, set checkFmcsApiKey to false.");
                throw new RuntimeException(e);
            }
        }


        //SERVER
        HashMap<String, String> domainKeys = null;

        if (CONFIG.checkFtpDns) {
            domainKeys = HttpUtil.sendAndRead(HttpUtil.getServerDetailsRequest(), true, true, "LastServerDetailsResponse.json", "dns_name", "success", "running", "suspension_reason");

            if (domainKeys != null && domainKeys.containsKey("dns_name") && domainKeys.get("success").equals("true")) {
                CONFIG.ftpServer = domainKeys.get("dns_name");
            }

            if (domainKeys != null && domainKeys.containsKey("suspension_reason") && !domainKeys.get("suspension_reason").isEmpty() && domainKeys.get("suspension_reason") != null) {
                System.out.println("!!!WARNING: Your server is suspended for " + domainKeys.get("suspension_reason"));
            }
        }

        CONFIG_FILE.write(CONFIG);

        if (domainKeys != null) {
            boolean isRunning = Boolean.parseBoolean(domainKeys.get("running"));
            boolean backupWhileRunning = CONFIG.backupWhileRunning;

            if (CONFIG.shouldBackup) {
                System.out.println("Backing, server status: " + (isRunning ? "running" : "stopped") + ", should backup while running: " + backupWhileRunning);
                FtpUtil.connectAndDownload(CONFIG.ftpAccount, CONFIG.ftpAccountPassword, CONFIG.ftpServer, CONFIG.ftpPort);
                MaxBackupsChecker.checkMaxBackups();
            } else if (!(isRunning && !backupWhileRunning)) {
                FtpUtil.connectAndDownload(CONFIG.ftpAccount, CONFIG.ftpAccountPassword, CONFIG.ftpServer, CONFIG.ftpPort);
                MaxBackupsChecker.checkMaxBackups();
            } else {
                CONFIG.shouldBackup = true;
                System.out.println("Cannot backup while server is running!");
            }
        } else if (!CONFIG.ftpServer.isEmpty()) {
            FtpUtil.connectAndDownload(CONFIG.ftpAccount, CONFIG.ftpAccountPassword, CONFIG.ftpServer, CONFIG.ftpPort);
            MaxBackupsChecker.checkMaxBackups();
        }

        exit(true, false, false, new File(CONFIG_FILE.currentBackupLocation).exists());
    }

    public static void exit(boolean writeTime, boolean showInfoDialogue, boolean finishWhenChecking, boolean successfulDownload) {
        if (writeTime) {
            CONFIG.lastCheck = CONFIG_FILE.getLocalDateAndTime();
        }

        CONFIG_FILE.write(CONFIG);
        byte b = (byte) (Math.random() * 10);
        System.out.println("Exiting...");
        if (b == 3) {
            System.out.println("Have a nice day :)");
        }


        if (showInfoDialogue) {
            JOptionPane.showMessageDialog(null, "Created new config file located in:\n " + new File(CONFIG_FILE.configFileLocation).getAbsolutePath(), "Freemcserver Backuper", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }

        if (successfulDownload && CONFIG.popupAfterSuccessfulDownload) {
            JOptionPane.showMessageDialog(null, "New backup has been created successfully!", "Freemcserver Backuper", JOptionPane.INFORMATION_MESSAGE);
        } else if (CONFIG.popupAfterFinish) {
            JOptionPane.showMessageDialog(null, "Freemcserver Backuper has finished!", "Freemcserver Backuper", JOptionPane.PLAIN_MESSAGE);
        }

        if (!CONFIG.closeConsoleUponExit) {
            try {
                if (finishWhenChecking && CONFIG.closeConsoleWhenChecking) {
                    System.exit(0);
                }

                if (successfulDownload && CONFIG.closeConsoleWhenDownloaded) {
                    System.exit(0);
                }

                Thread.currentThread().stop();
            } catch (Exception e) {
                System.err.println("Can not stop the main thread!");
                e.printStackTrace();
            }
        }

        System.exit(0);
    }

    private static ConsoleJFrame createJFrameAndLogFile(boolean openWindow) throws FileNotFoundException {
        if (openWindow) {
            ConsoleJFrame frame = new ConsoleJFrame();
            JTextArea textArea = new JTextArea();

            MultiplePrintStreams multiple = getMultipleStreams(textArea);
            System.setOut(multiple);
            System.setErr(multiple);
            frame.setBackground(new Color(45, 45, 45));
            frame.add(new JScrollPane(textArea));
            frame.pack();
            frame.setSize(800, 600);
            return frame;
        } else {

            File file = new File(CONFIG_FILE.logFileLocation);
            if (file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            MultiplePrintStreams multiple = new MultiplePrintStreams(System.out, new PrintStream(file));
            System.setOut(multiple);
            System.setErr(multiple);
            return null;
        }
    }

    private static MultiplePrintStreams getMultipleStreams(JTextArea textArea) throws FileNotFoundException {
        ConsoleOutputStream taos = new ConsoleOutputStream(textArea, 60);
        PrintStream consoleStream = new PrintStream(taos);

        File file = new File(CONFIG_FILE.logFileLocation);
        if (file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        MultiplePrintStreams multiple = new MultiplePrintStreams(System.out, consoleStream, new PrintStream(file));
        return multiple;
    }
}
