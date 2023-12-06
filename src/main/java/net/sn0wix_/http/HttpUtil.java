package net.sn0wix_.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sn0wix_.Main;
import net.sn0wix_.files.FilesUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;

public class HttpUtil {
    public static HttpRequest getLoginRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create("https://freemcserver.p.rapidapi.com/v4/user/login"))
                .header("content-type", "application/json")
                .header("X-FMCS-Token", Main.CONFIG.fmcsApiKey)
                .header("X-RapidAPI-Key", Main.CONFIG.rapidApiKey)
                .header("X-RapidAPI-Host", "freemcserver.p.rapidapi.com")//tIxfIIeXz2QYPEloyJIz0UI8Q-bp57Et
                .method("POST", HttpRequest.BodyPublishers.ofString("{\r\n    \"username\": \"" + Main.CONFIG.fmcsAccount + "\",\r\n    \"password\": \"" + Main.CONFIG.fmcsPassword + "\",\r\n    \"scope\": \"USER\"\r\n}"))
                .build();
    }

    public static HttpRequest getServerDetailsRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(Main.CONFIG.serverUrl))
                .header("X-FMCS-Token", Main.CONFIG.fmcsApiKey)
                .header("X-RapidAPI-Key", Main.CONFIG.rapidApiKey)
                .header("User-Agent", Main.CONFIG.userAgent)
                .header("X-RapidAPI-Host", "freemcserver.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
    }


    public static HashMap<String, String> sendAndRead(HttpRequest request, boolean readDns, boolean updateUserAgent, String filename, String... keys) {
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (Main.CONFIG.printOutput) {
                System.out.println(response.body());
            }

            FilesUtil.writeFile(filename, response.body());

            HashMap<String, String> list = new HashMap<>();

            ObjectMapper objectMapper = new ObjectMapper();

            if (updateUserAgent && (Main.CONFIG.userAgent.equals("FMCS-USER-") || Main.CONFIG.userAgent.equals("FMCS-USER-0000000") || Main.CONFIG.userAgent.isEmpty())) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                if (!Boolean.parseBoolean(rootNode.get("success").toString())) {
                    Main.CONFIG.userAgent = getUserAgent(response.body());
                    Thread.sleep(1100);
                    return sendAndRead(HttpUtil.getServerDetailsRequest(), readDns, false, filename, keys);
                }
            }

            if (readDns) {
                Arrays.stream(keys).toList().forEach(key -> {
                    JsonNode rootNode;
                    try {
                        switch (key) {
                            case "dns_name" -> {
                                rootNode = objectMapper.readTree(response.body());
                                String value = rootNode.path("server").path("node").path("dns_name").asText();
                                list.put(key, value);
                            }
                            case "running" -> {
                                rootNode = objectMapper.readTree(response.body());
                                String value = rootNode.path("server").path("node_override_id").asText();
                                list.put(key, value);
                            }
                            case "suspension_reason" -> {
                                rootNode = objectMapper.readTree(response.body());
                                String value = rootNode.path("server").asText();
                                list.put(key, value);
                            }case "ban_reason" -> {
                                rootNode = objectMapper.readTree(response.body());
                                String value = rootNode.path("user").asText();
                                list.put(key, value);
                            }
                            default -> {
                                rootNode = objectMapper.readTree(response.body());
                                String value = rootNode.path(key).asText();
                                list.put(key, value);
                            }
                        }
                    } catch (JsonProcessingException e) {
                        System.err.println("Key not found: " + key);
                        e.printStackTrace();
                    }
                });
            } else {
                Arrays.stream(keys).toList().forEach(key -> {
                    JsonNode rootNode;
                    try {
                        rootNode = objectMapper.readTree(response.body());
                        String value = rootNode.path(key).asText();
                        list.put(key, value);
                    } catch (JsonProcessingException e) {
                        System.err.println("Key not found: " + key);
                        e.printStackTrace();
                    }
                });
            }
            return list;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getUserAgent(String response) {
        StringBuilder builder = new StringBuilder();
        builder.append("FMCS-USER-");

        int i = response.indexOf("FMCS-USER-") + 10;
        boolean bl = true;

        while (bl) {
            if (response.charAt(i) == '\"' || response.charAt(i) == '\'') {
                bl = false;
            } else {
                builder.append(response.charAt(i));
                i++;
            }
        }

        return builder.toString();
    }
}
