package net.sn0wix_.files;

import net.sn0wix_.Main;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class MaxBackupsChecker {
    public static void checkMaxBackups() {
        File backups = new File(Main.CONFIG_FILE.backupsLocation);
        if (!backups.exists() || backups.listFiles() == null) {
            System.err.println("Error while checking for oldest backup.");
            System.err.println("Backups folder does not exist, or there are no files in it.");
            Main.exit(false, false, false, false);
        }
        ArrayList<File> validFiles = new ArrayList<>(5);

        for (File file : Objects.requireNonNull(backups.listFiles())) {
            if (file.getName().equals(".") || file.getName().equals("..")) {
                continue;
            }

            if (isCorrectName(file.getName())) {
                validFiles.add(file);
            }
        }

        File[] files = validFiles.toArray(new File[0]);
        // Sort the directories based on file name in ascending order (oldest first)
        Arrays.sort(files, Comparator.comparing(File::getName));

        int numberOfBackups = validFiles.size();
        int maxBackups = Main.CONFIG.maxBackups;
        int difference = numberOfBackups - maxBackups;

        if (difference > 0) {
            for (int i = 0; i < difference; i++) {
                File oldestDirectory = files[i];

                if (deleteDirectory(oldestDirectory)) {
                    if (Main.CONFIG.printOutput) {
                        System.out.println("Oldest backup deleted successfully: " + oldestDirectory.getAbsolutePath());
                    }
                } else {
                    System.out.println("Failed to delete the oldest backup.");
                }
            }
        }
    }

    private static boolean deleteDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return false;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (file.delete()) {
                        if (Main.CONFIG.printOutput) {
                            System.out.println("DELETED file " + file.getPath());
                        }
                    } else {
                        System.out.println("COULD NOT delete file " + file.getPath());
                    }
                }
            }
        }

        return directory.delete();
    }

    public static boolean isCorrectName(String name) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < name.length(); i++) {
            String s = String.valueOf(name.charAt(i));
            if (isInt(s)) {
                builder.append("x");
            } else {
                builder.append(" ");
            }
        }

        return builder.toString().equals("xxxx xx xx xx xx xx xxxxxxxxx");
    }

    private static boolean isInt(String string) {
        try {
            Integer.parseInt(String.valueOf(string));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean hasTimeFromLastCheckPassed() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd'T'HH_mm_ss.SSSSSSSSS");

        LocalDateTime dateTime = LocalDateTime.parse(Main.CONFIG_FILE.getLocalDateAndTime(), formatter);

        LocalDateTime currentDateTime = LocalDateTime.now();

        long hoursDifference = java.time.Duration.between(currentDateTime, dateTime).toHours();

        return hoursDifference >= Main.CONFIG.backupIntervalInHours;
    }
}
