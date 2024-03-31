package src;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
  private final Map<String, String> users;

  DatabaseManager() throws IOException {
    this.users = parseUsers(Paths.get("src/database/users.json"));
  }

  private Map<String, String> parseUsers(Path path) throws IOException {
    String content = new String(Files.readAllBytes(path));
    Map<String, String> usersMap = new HashMap<>();
    String[] pairs = content.substring(1, content.length() - 1).split(",");
    for (String pair : pairs) {
      String[] keyValue = pair.split(":");
      String key = keyValue[0].trim();
      key = key.substring(1, key.length() - 1);
      String value = keyValue[1].trim();
      value = value.substring(1, value.length() - 1);
      usersMap.put(key, value);
    }

    return usersMap;
  }

  public boolean validateCredentials(String username, String password) {
    if (users.containsKey(username)) {
      if (users.get(username).equals(password)) {
        return true;
      }
    }
    return false;
  }
}
