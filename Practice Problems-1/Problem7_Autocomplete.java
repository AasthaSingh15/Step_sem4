import java.util.*;
import java.util.stream.*;

/**
 * Problem 7: Autocomplete System for Search Engine
 * Concepts: HashMap for frequency, Trie + HashMap hybrid, string hashing, prefix search
 */
public class Problem7_Autocomplete {

    // Trie Node
    static class TrieNode {
        HashMap<Character, TrieNode> children;
        boolean isEndOfWord;
        String fullWord;

        TrieNode() {
            children = new HashMap<>();
            isEndOfWord = false;
            fullWord = null;
        }
    }

    private TrieNode root;

    // Query -> frequency count
    private HashMap<String, Integer> queryFrequency;

    // Cache: prefix -> top suggestions (to avoid recomputing)
    private HashMap<String, List<String>> prefixCache;

    public Problem7_Autocomplete() {
        root = new TrieNode();
        queryFrequency = new HashMap<>();
        prefixCache = new HashMap<>();
    }

    // Insert or update a query in the Trie and frequency map
    public void addQuery(String query, int frequency) {
        String lowerQuery = query.toLowerCase().trim();
        queryFrequency.put(lowerQuery, queryFrequency.getOrDefault(lowerQuery, 0) + frequency);

        // Insert into Trie
        TrieNode current = root;
        for (char c : lowerQuery.toCharArray()) {
            current.children.putIfAbsent(c, new TrieNode());
            current = current.children.get(c);
        }
        current.isEndOfWord = true;
        current.fullWord = lowerQuery;

        // Invalidate cache for all prefixes of this query
        for (int i = 1; i <= lowerQuery.length(); i++) {
            prefixCache.remove(lowerQuery.substring(0, i));
        }
    }

    // Update frequency of an existing query (e.g., trending)
    public void updateFrequency(String query) {
        String lowerQuery = query.toLowerCase().trim();
        int oldFreq = queryFrequency.getOrDefault(lowerQuery, 0);
        queryFrequency.put(lowerQuery, oldFreq + 1);
        System.out.println("updateFrequency(\"" + query + "\") -> Frequency: " + oldFreq + " -> " + (oldFreq + 1));

        // Invalidate prefix cache
        for (int i = 1; i <= lowerQuery.length(); i++) {
            prefixCache.remove(lowerQuery.substring(0, i));
        }
    }

    // Collect all words in trie that start from a given node
    private void collectWords(TrieNode node, List<String> words) {
        if (node.isEndOfWord && node.fullWord != null) {
            words.add(node.fullWord);
        }
        for (TrieNode child : node.children.values()) {
            collectWords(child, words);
        }
    }

    // Search top K suggestions for a given prefix
    public List<String> search(String prefix, int topK) {
        String lowerPrefix = prefix.toLowerCase().trim();

        // Check cache first
        if (prefixCache.containsKey(lowerPrefix)) {
            return prefixCache.get(lowerPrefix);
        }

        // Navigate trie to end of prefix
        TrieNode current = root;
        for (char c : lowerPrefix.toCharArray()) {
            if (!current.children.containsKey(c)) {
                return Collections.emptyList(); // No suggestions
            }
            current = current.children.get(c);
        }

        // Collect all words with this prefix
        List<String> allMatches = new ArrayList<>();
        collectWords(current, allMatches);

        // Sort by frequency descending, take topK
        List<String> topSuggestions = allMatches.stream()
            .sorted((a, b) -> queryFrequency.getOrDefault(b, 0) - queryFrequency.getOrDefault(a, 0))
            .limit(topK)
            .collect(Collectors.toList());

        // Cache the result
        prefixCache.put(lowerPrefix, topSuggestions);
        return topSuggestions;
    }

    // Print search results nicely
    public void printSuggestions(String prefix) {
        List<String> suggestions = search(prefix, 5);
        System.out.println("\nsearch(\"" + prefix + "\") ->");
        if (suggestions.isEmpty()) {
            System.out.println("  No suggestions found.");
        } else {
            for (int i = 0; i < suggestions.size(); i++) {
                String q = suggestions.get(i);
                System.out.printf("  %d. \"%s\" (%,d searches)%n",
                    i + 1, q, queryFrequency.getOrDefault(q, 0));
            }
        }
    }

    public static void main(String[] args) {
        Problem7_Autocomplete ac = new Problem7_Autocomplete();

        System.out.println("=== Autocomplete System ===");
        System.out.println("Loading search queries...\n");

        // Add queries with frequencies
        ac.addQuery("java tutorial", 1234567);
        ac.addQuery("javascript", 987654);
        ac.addQuery("java download", 456789);
        ac.addQuery("java 21 features", 1);
        ac.addQuery("java interview questions", 234567);
        ac.addQuery("java spring boot", 345678);
        ac.addQuery("python tutorial", 1500000);
        ac.addQuery("python django", 400000);
        ac.addQuery("python for beginners", 890000);
        ac.addQuery("data structures", 567890);
        ac.addQuery("data science course", 678901);
        ac.addQuery("hash table implementation", 123456);
        ac.addQuery("hash map java", 234567);

        // Test autocomplete
        ac.printSuggestions("jav");
        ac.printSuggestions("java");
        ac.printSuggestions("py");
        ac.printSuggestions("data");
        ac.printSuggestions("hash");
        ac.printSuggestions("xyz"); // No results

        // Simulate trending update
        System.out.println();
        ac.updateFrequency("java 21 features");
        ac.updateFrequency("java 21 features");
        ac.updateFrequency("java 21 features");
        ac.printSuggestions("java 2"); // Should now show updated frequency
    }
}
