import java.util.*;

/**
 * Problem 9: Two-Sum Problem Variants for Financial Transactions
 * Concepts: HashMap complement lookup, O(1) lookup, multiple hash tables, time complexity
 */
public class Problem9_TwoSum {

    // Represents a financial transaction
    static class Transaction {
        int id;
        double amount;
        String merchant;
        String account;
        String time; // HH:MM format

        Transaction(int id, double amount, String merchant, String account, String time) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            this.time = time;
        }

        // Parse time to minutes for window comparison
        int toMinutes() {
            String[] parts = time.split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        }

        @Override
        public String toString() {
            return String.format("{id:%d, amount:%.0f, merchant:\"%s\", account:\"%s\", time:\"%s\"}",
                id, amount, merchant, account, time);
        }
    }

    private List<Transaction> transactions;

    public Problem9_TwoSum(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // Classic Two-Sum: Find all pairs that sum to target - O(n)
    public List<int[]> findTwoSum(double target) {
        List<int[]> result = new ArrayList<>();
        // complement amount -> transaction
        HashMap<Double, Transaction> seen = new HashMap<>();

        for (Transaction t : transactions) {
            double complement = target - t.amount;
            if (seen.containsKey(complement)) {
                result.add(new int[]{seen.get(complement).id, t.id});
            }
            seen.put(t.amount, t);
        }
        return result;
    }

    // Two-Sum with time window (within 60 minutes)
    public List<int[]> findTwoSumWithWindow(double target, int windowMinutes) {
        List<int[]> result = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t1 = transactions.get(i);
            HashMap<Double, Transaction> windowMap = new HashMap<>();
            windowMap.put(t1.amount, t1);

            for (int j = i + 1; j < transactions.size(); j++) {
                Transaction t2 = transactions.get(j);
                if (Math.abs(t2.toMinutes() - t1.toMinutes()) > windowMinutes) continue;

                double complement = target - t2.amount;
                if (windowMap.containsKey(complement)) {
                    result.add(new int[]{windowMap.get(complement).id, t2.id});
                }
                windowMap.put(t2.amount, t2);
            }
        }
        return result;
    }

    // K-Sum: Find K transactions that sum to target (uses recursion + HashMap)
    public List<List<Integer>> findKSum(int k, double target) {
        List<List<Integer>> result = new ArrayList<>();
        // Sort by amount for better pruning
        List<Transaction> sorted = new ArrayList<>(transactions);
        sorted.sort(Comparator.comparingDouble(t -> t.amount));
        kSumHelper(sorted, k, target, 0, new ArrayList<>(), result);
        return result;
    }

    private void kSumHelper(List<Transaction> list, int k, double target,
                             int start, List<Integer> current, List<List<Integer>> result) {
        if (k == 2) {
            // Use HashMap for O(n) two-sum
            HashMap<Double, Integer> seen = new HashMap<>();
            for (int i = start; i < list.size(); i++) {
                double complement = target - list.get(i).amount;
                if (seen.containsKey(complement)) {
                    List<Integer> combo = new ArrayList<>(current);
                    combo.add(seen.get(complement));
                    combo.add(list.get(i).id);
                    result.add(combo);
                }
                seen.put(list.get(i).amount, list.get(i).id);
            }
            return;
        }

        for (int i = start; i < list.size(); i++) {
            current.add(list.get(i).id);
            kSumHelper(list, k - 1, target - list.get(i).amount, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    // Detect duplicate transactions: same amount + same merchant from different accounts
    public void detectDuplicates() {
        // Key: "amount_merchant" -> list of transactions
        HashMap<String, List<Transaction>> groupMap = new HashMap<>();

        for (Transaction t : transactions) {
            String key = t.amount + "_" + t.merchant;
            groupMap.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        System.out.println("Duplicate Transaction Detection:");
        boolean found = false;
        for (Map.Entry<String, List<Transaction>> entry : groupMap.entrySet()) {
            List<Transaction> group = entry.getValue();
            if (group.size() > 1) {
                // Check if different accounts
                Set<String> accounts = new HashSet<>();
                group.forEach(t -> accounts.add(t.account));
                if (accounts.size() > 1) {
                    found = true;
                    System.out.printf("  DUPLICATE: amount=%.0f, merchant=\"%s\", accounts=%s%n",
                        group.get(0).amount, group.get(0).merchant, accounts);
                }
            }
        }
        if (!found) System.out.println("  No duplicates detected.");
    }

    public static void main(String[] args) {
        System.out.println("=== Two-Sum Variants: Financial Fraud Detection ===\n");

        List<Transaction> transactions = new ArrayList<>(Arrays.asList(
            new Transaction(1, 500, "Store A", "acc1", "10:00"),
            new Transaction(2, 300, "Store B", "acc2", "10:15"),
            new Transaction(3, 200, "Store C", "acc3", "10:30"),
            new Transaction(4, 700, "Store D", "acc4", "10:45"),
            new Transaction(5, 100, "Store E", "acc5", "11:30"),
            new Transaction(6, 500, "Store A", "acc6", "12:00"), // Potential duplicate
            new Transaction(7, 400, "Store F", "acc7", "13:00"),
            new Transaction(8, 600, "Store G", "acc8", "14:00")
        ));

        Problem9_TwoSum solver = new Problem9_TwoSum(transactions);

        // Two-Sum
        System.out.println("findTwoSum(target=500):");
        List<int[]> pairs = solver.findTwoSum(500);
        if (pairs.isEmpty()) System.out.println("  No pairs found.");
        pairs.forEach(p -> System.out.printf("  -> (id:%d, id:%d)%n", p[0], p[1]));

        // Two-Sum with time window (60 min)
        System.out.println("\nfindTwoSumWithWindow(target=500, window=60min):");
        List<int[]> windowPairs = solver.findTwoSumWithWindow(500, 60);
        if (windowPairs.isEmpty()) System.out.println("  No pairs found in window.");
        windowPairs.forEach(p -> System.out.printf("  -> (id:%d, id:%d)%n", p[0], p[1]));

        // K-Sum
        System.out.println("\nfindKSum(k=3, target=1000):");
        List<List<Integer>> kSums = solver.findKSum(3, 1000);
        if (kSums.isEmpty()) System.out.println("  No K-sum found.");
        kSums.forEach(combo -> System.out.println("  -> " + combo));

        // Duplicate detection
        System.out.println();
        solver.detectDuplicates();
    }
}
