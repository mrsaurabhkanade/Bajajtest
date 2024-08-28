import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar test.jar <PRN Number> <path to JSON file>");
            return;
        }

        String prn = args[0].toLowerCase().replaceAll("\\s+", "");
        String jsonFilePath = args[1];

        try {
            String destinationValue = readJsonFile(jsonFilePath);
            if (destinationValue == null) {
                System.out.println("Key 'destination' not found in JSON file.");
                return;
            }

            String randomString = generateRandomString(8);
            String concatenatedString = prn + destinationValue + randomString;
            String hash = md5Hash(concatenatedString);

            System.out.println(hash + ";" + randomString);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public static String findDestinationKey(JSONObject jsonObject) {
        // Check if 'destination' is directly in the current JSON object
        if (jsonObject.has("destination")) {
            Object value = jsonObject.get("destination");
            if (value instanceof String) {
                return (String) value;
            }
        }

        // Recursively search in nested JSON objects and arrays
        for (Object keyObj : jsonObject.keySet()) {
            String key = (String) keyObj;
            Object value = jsonObject.get(key);

            if (value instanceof JSONObject) {
                String result = findDestinationKey((JSONObject) value);
                if (result != null) {
                    return result;
                }
            } else if (value instanceof JSONArray) {
                for (int i = 0; i < ((JSONArray) value).length(); i++) {
                    Object arrayItem = ((JSONArray) value).get(i);
                    if (arrayItem instanceof JSONObject) {
                        String result = findDestinationKey((JSONObject) arrayItem);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }

        return null;
    }

    public static String readJsonFile(String filePath) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONObject jsonObject = new JSONObject(content);
        return findDestinationKey(jsonObject);
    }

    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static String md5Hash(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : messageDigest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
