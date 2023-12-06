package net.sn0wix_.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sn0wix_.Main;
import net.sn0wix_.files.FilesUtil;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;

public class HttpUtil {
    public static Response getLoginResponse() {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\r\n    \"username\": \"" + Main.CONFIG.fmcsAccount + "\",\r\n    \"password\": \"" + Main.CONFIG.fmcsPassword + "\",\r\n    \"scope\": \"USER\"\r\n}");
        Request request = new Request.Builder().url("https://freemcserver.p.rapidapi.com/v4/user/login")
                .post(body).addHeader("content-type", "application/json")
                .addHeader("X-FMCS-Token", Main.CONFIG.fmcsApiKey)
                .addHeader("X-RapidAPI-Key", Main.CONFIG.rapidApiKey)
                .addHeader("X-RapidAPI-Host", "freemcserver.p.rapidapi.com").build();

        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            System.out.println("Can not request http.");
            throw new RuntimeException(e);
        }
    }

    public static Response getServerDetailsRequest() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(Main.CONFIG.serverUrl)
                .get()
                .addHeader("X-FMCS-Token", Main.CONFIG.fmcsApiKey)
                .addHeader("X-RapidAPI-Key", Main.CONFIG.rapidApiKey)
                .addHeader("User-Agent", Main.CONFIG.userAgent)
                .addHeader("X-RapidAPI-Host", "freemcserver.p.rapidapi.com")
                .build();

        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            System.out.println("Can not request http.");
            throw new RuntimeException(e);
        }
    }


    public static HashMap<String, String> read(Response response ,boolean readDns, boolean updateUserAgent, String filename, String... keys) {
        try {
            String responseBody = response.body().string();

            if (Main.CONFIG.printOutput) {
                System.out.println(responseBody);
            }

            FilesUtil.writeFile(filename, responseBody);


            HashMap<String, String> list = new HashMap<>();

            ObjectMapper objectMapper = new ObjectMapper();

            if (updateUserAgent && (Main.CONFIG.userAgent.equals("FMCS-USER-") || Main.CONFIG.userAgent.equals("FMCS-USER-0000000") || Main.CONFIG.userAgent.isEmpty())) {
                JsonNode rootNode = objectMapper.readTree(responseBody);
                if (!Boolean.parseBoolean(rootNode.get("success").toString())) {
                    Main.CONFIG.userAgent = getUserAgent(responseBody);
                    Thread.sleep(Main.CONFIG.delayBetweenRequests);
                    return read(HttpUtil.getServerDetailsRequest(), readDns, false, filename, keys);
                }
            }

            if (readDns) {
                for (String key: keys) {
                    JsonNode rootNode;
                    try {
                        rootNode = objectMapper.readTree(responseBody);

                        if ("dns_name".equals(key)) {
                            String value = rootNode.path("server").path("node").path("dns_name").asText();
                            list.put(key, value);
                        } else if ("running".equals(key)) {
                            String value = rootNode.path("server").path("node_override_id").asText();
                            list.put(key, value);
                        } else if ("suspension_reason".equals(key)) {
                            String value = rootNode.path("server").asText();
                            list.put(key, value);
                        } else if ("ban_reason".equals(key)) {
                            String value = rootNode.path("user").asText();
                            list.put(key, value);
                        } else {
                            String value = rootNode.path(key).asText();
                            list.put(key, value);
                        }
                    } catch (JsonProcessingException e) {
                        System.err.println("Key not found: " + key);
                        e.printStackTrace();
                    }
                }
            } else {
                for (String key: keys) {
                    JsonNode rootNode;
                    try {
                        rootNode = objectMapper.readTree(responseBody);
                        String value = rootNode.path(key).asText();
                        list.put(key, value);
                    } catch (JsonProcessingException e) {
                        System.err.println("Key not found: " + key);
                        e.printStackTrace();
                    }
                }
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
