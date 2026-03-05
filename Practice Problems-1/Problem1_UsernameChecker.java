import java.util.*;

/**
 * Problem 1: Social Media Username Availability Checker
 * Concepts: Hash table basics, O(1) lookup, collision handling, frequency counting
 */
public class Problem1_UsernameChecker {

    // Stores registered username -> userId
    private HashMap<String, Integer> registeredUsers;

    // Tracks how many times a username was attempted
    private HashMap<String, Integer> attemptFrequency;

    public Problem1_UsernameChecker() {
        registeredUsers = new HashMap<>();
        attemptFrequency = new HashMap<>();

        // Pre-load some existing users
        registeredUsers.put("john_doe", 1001);
        registeredUsers.put("admin", 1002);
        registeredUsers.put("alice", 1003);
        registeredUsers.put("bob", 1004);
    }

    // Check if a username is available in O(1)
    public boolean checkAvailability(String username) {
        // Track how many times this username was attempted
        attemptFrequency.put(username, attemptFrequency.getOrDefault(username, 0) + 1);
        return !registeredUsers.containsKey(username);
    }

    // Register a new username
    public boolean register(String username, int userId) {
        if (checkAvailability(username)) {
            registeredUsers.put(username, userId);
            System.out.println("Registered: " + username + " with userId: " + userId);
            return true;
        }
        System.out.println("Username '" + username + "' is already taken!");
        return false;
    }

    // Suggest 3 alternative usernames if the desired one is taken
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();

        // Append numbers
        for (int i = 1; i <= 2; i++) {
            String suggestion = username + i;
            if (!registeredUsers.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        // Replace underscore with dot
        String dotVersion = username.replace("_", ".");
        if (!registeredUsers.containsKey(dotVersion)) {
            suggestions.add(dotVersion);
        }

        // Add underscore + random number if still not enough suggestions
        if (suggestions.size() < 3) {
            String extra = username + "_" + (int)(Math.random() * 100);
            suggestions.add(extra);
        }

        return suggestions;
    }

    // Get the most attempted username (most popular search)
    public String getMostAttempted() {
        String mostAttempted = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : attemptFrequency.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostAttempted = entry.getKey();
            }
        }
        return mostAttempted + " (" + maxCount + " attempts)";
    }

    public static void main(String[] args) {
        Problem1_UsernameChecker checker = new Problem1_UsernameChecker();

        System.out.println("=== Username Availability Checker ===\n");

        // Test availability
        System.out.println("checkAvailability(\"john_doe\") -> " + checker.checkAvailability("john_doe"));
        System.out.println("checkAvailability(\"jane_smith\") -> " + checker.checkAvailability("jane_smith"));

        // Test suggestions
        System.out.println("\nsuggestAlternatives(\"john_doe\") -> " + checker.suggestAlternatives("john_doe"));

        // Simulate multiple attempts on admin
        checker.checkAvailability("admin");
        checker.checkAvailability("admin");
        checker.checkAvailability("admin");
        checker.checkAvailability("admin");

        System.out.println("\ngetMostAttempted() -> " + checker.getMostAttempted());

        // Register a new user
        System.out.println();
        checker.register("jane_smith", 2001);
        checker.register("john_doe", 2002); // Should fail
    }
}
