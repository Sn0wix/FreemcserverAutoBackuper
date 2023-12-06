package net.sn0wix_.files.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.sn0wix_.Main;
import net.sn0wix_.files.MaxBackupsChecker;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class ConfigFile {
    public final String dataLocation;
    public final String configFileName;
    public final String configFileLocation;
    public final String backupsLocation;
    public final String currentBackupLocation;
    public final String logFileLocation;

    public ConfigFile(String dataLocation, String configFileName, String backupsLocation, String logFileLocation) {
        this.dataLocation = dataLocation;
        this.configFileName = configFileName;
        this.configFileLocation = dataLocation + File.separator + configFileName;
        this.backupsLocation = dataLocation + File.separator + backupsLocation;
        this.currentBackupLocation = dataLocation + File.separator + backupsLocation + File.separator + getLocalDateAndTime();
        this.logFileLocation = dataLocation + File.separator + logFileLocation;
    }

    public String getLocalDateAndTime() {
        String dateTime = LocalDate.now() + "|" + LocalTime.now();
        dateTime = dateTime.replaceAll("\\|", "T");
        dateTime = dateTime.replaceAll(":", "_");
        dateTime = dateTime.replaceAll("-", "_");

        if (!MaxBackupsChecker.isCorrectName(dateTime)) {
            StringBuilder builder = new StringBuilder(dateTime);
            int i = 29 - dateTime.length();

            if (i > 0) {
                for (int j = 0; j < i; j++) {
                    builder.append("0");
                }
            } else if (i < 0) {
                for (int j = 0; j < Math.abs(i); j++) {
                    builder.deleteCharAt(builder.length() - 1);
                }
            }
            return builder.toString();
        }
        return dateTime;
    }

    public boolean create() throws IOException {
        File cofingFile = new File(dataLocation + File.separator + configFileName);

        if (!cofingFile.exists()) {
            System.out.println("Config file does not exist. Creating a new one.");
            if (!cofingFile.createNewFile()) {
                System.out.println("Could not create config file in " + cofingFile.getAbsolutePath());
                return false;
            }
            writeDefaults();
        }else {
            return false;
        }

        return true;
    }

    public void writeDefaults() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        File configFile = new File(configFileLocation);

        try {
            objectMapper.writeValue(configFile, Main.CONFIG);
            System.out.println("Config has been successfully created: " + configFileLocation);
            System.out.println("Tutorial on how to set up config is in README.txt");
        } catch (IOException e) {
            System.err.println("Error writing default values to: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Config read() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            File configFile = new File(configFileLocation);
            if (!configFile.exists()) {
                return null;
            }

            return objectMapper.readValue(configFile, Config.class);
        } catch (IOException e) {
            System.err.println("Error reading config from file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void write(Config config) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        File configFile = new File(configFileLocation);

        try {
            objectMapper.writeValue(configFile, config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
