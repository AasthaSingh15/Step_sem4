import java.util.*;
import java.util.concurrent.*;

/**
 * Problem 2: E-commerce Flash Sale Inventory Manager
 * Concepts: HashMap for stock, collision resolution, load factor, thread safety
 */
public class Problem2_InventoryManager {

    // Product stock: productId -> stock count (thread-safe)
    private ConcurrentHashMap<String, Integer> inventory;

    // Waiting list per product: productId -> queue of userIds (FIFO)
    private HashMap<String, Queue<Integer>> waitingLists;

    public Problem2_InventoryManager() {
        inventory = new ConcurrentHashMap<>();
        waitingLists = new HashMap<>();

        // Load some initial stock for the flash sale
        inventory.put("IPHONE15_256GB", 100);
        inventory.put("MACBOOK_PRO", 50);
        inventory.put("AIRPODS_PRO", 200);

        waitingLists.put("IPHONE15_256GB", new LinkedList<>());
        waitingLists.put("MACBOOK_PRO", new LinkedList<>());
        waitingLists.put("AIRPODS_PRO", new LinkedList<>());
    }

    // Check current stock - O(1)
    public int checkStock(String productId) {
        return inventory.getOrDefault(productId, 0);
    }

    // Purchase item - thread-safe decrement
    public String purchaseItem(String productId, int userId) {
        // Atomic check-and-decrement using compute
        int[] result = {-1};
        inventory.compute(productId, (key, stock) -> {
            if (stock != null && stock > 0) {
                result[0] = stock - 1;
                return stock - 1;
            }
            result[0] = -1;
            return stock;
        });

        if (result[0] >= 0) {
            return "Success! UserId " + userId + " purchased " + productId + ". Stock remaining: " + result[0];
        } else {
            // Add to waiting list
            waitingLists.get(productId).offer(userId);
            int position = waitingLists.get(productId).size();
            return "Out of stock! UserId " + userId + " added to waiting list. Position #" + position;
        }
    }

    // Get waiting list info
    public String getWaitingListInfo(String productId) {
        Queue<Integer> queue = waitingLists.get(productId);
        if (queue == null || queue.isEmpty()) {
            return "No waiting list for " + productId;
        }
        return "Waiting list for " + productId + ": " + queue.size() + " users";
    }

    // Restock product and notify first in waiting list
    public void restock(String productId, int quantity) {
        inventory.merge(productId, quantity, Integer::sum);
        System.out.println("Restocked " + productId + " with " + quantity + " units. New stock: " + inventory.get(productId));

        // Notify first person in waiting list
        Queue<Integer> queue = waitingLists.get(productId);
        if (queue != null && !queue.isEmpty()) {
            int nextUser = queue.poll();
            System.out.println("Notified userId " + nextUser + " - item is available!");
        }
    }

    public static void main(String[] args) {
        Problem2_InventoryManager manager = new Problem2_InventoryManager();

        System.out.println("=== Flash Sale Inventory Manager ===\n");

        System.out.println("Stock check - IPHONE15_256GB: " + manager.checkStock("IPHONE15_256GB") + " units");

        // Simulate rapid purchases
        System.out.println("\n--- Simulating purchases ---");
        for (int i = 1; i <= 3; i++) {
            System.out.println(manager.purchaseItem("IPHONE15_256GB", 10000 + i));
        }

        // Drain all stock quickly
        System.out.println("\n--- Draining all 97 remaining units ---");
        for (int i = 4; i <= 100; i++) {
            manager.purchaseItem("IPHONE15_256GB", 10000 + i);
        }
        System.out.println("Stock after drain: " + manager.checkStock("IPHONE15_256GB"));

        // These should go to waiting list
        System.out.println("\n--- Attempting purchase after stock is 0 ---");
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 99997));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 99998));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 99999));

        System.out.println("\n" + manager.getWaitingListInfo("IPHONE15_256GB"));

        // Restock
        System.out.println("\n--- Restock event ---");
        manager.restock("IPHONE15_256GB", 10);
    }
}
