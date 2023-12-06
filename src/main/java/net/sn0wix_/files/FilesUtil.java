package net.sn0wix_.files;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.sn0wix_.Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FilesUtil {
    public static void createReadMeFile() throws IOException {
        String filePath = Main.CONFIG_FILE.dataLocation + File.separator + "README.txt";
        File file = new File(filePath);
        if (!file.exists()) {
            if (file.createNewFile()) {
                if (file.canWrite()) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                        writer.write("Hi!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void createReadMe() {
        File backupsFolder = new File(Main.CONFIG_FILE.backupsLocation);

        if (!backupsFolder.exists()) {
            backupsFolder.mkdirs();
        }

        try {
            createReadMeFile();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(String name, String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        File responseFile = new File(Main.CONFIG_FILE.dataLocation + File.separator + name);
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode jsonNode = mapper.readTree(response);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(responseFile, jsonNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
