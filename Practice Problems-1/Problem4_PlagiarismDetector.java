import java.util.*;

/**
 * Problem 4: Plagiarism Detection System
 * Concepts: String hashing, n-grams, frequency counting with hash maps
 */
public class Problem4_PlagiarismDetector {

    // n-gram -> set of document IDs that contain this n-gram
    private HashMap<String, Set<String>> ngramIndex;
    private final int N; // n-gram size

    public Problem4_PlagiarismDetector(int n) {
        this.N = n;
        this.ngramIndex = new HashMap<>();
    }

    // Extract n-grams from text
    private List<String> extractNgrams(String text) {
        List<String> ngrams = new ArrayList<>();
        String[] words = text.toLowerCase().replaceAll("[^a-z0-9 ]", "").split("\\s+");

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < i + N; j++) {
                if (j > i) sb.append(" ");
                sb.append(words[j]);
            }
            ngrams.add(sb.toString());
        }
        return ngrams;
    }

    // Index a document into the n-gram map
    public void indexDocument(String docId, String content) {
        List<String> ngrams = extractNgrams(content);
        System.out.println("Indexed \"" + docId + "\" -> Extracted " + ngrams.size() + " n-grams");

        for (String ngram : ngrams) {
            ngramIndex.computeIfAbsent(ngram, k -> new HashSet<>()).add(docId);
        }
    }

    // Analyze a new document for plagiarism against the index
    public void analyzeDocument(String docId, String content) {
        List<String> ngrams = extractNgrams(content);
        System.out.println("\nAnalyzing \"" + docId + "\"");
        System.out.println("Extracted " + ngrams.size() + " n-grams");

        // Count matches per document
        HashMap<String, Integer> matchCounts = new HashMap<>();
        for (String ngram : ngrams) {
            Set<String> matchingDocs = ngramIndex.get(ngram);
            if (matchingDocs != null) {
                for (String matchDoc : matchingDocs) {
                    if (!matchDoc.equals(docId)) {
                        matchCounts.merge(matchDoc, 1, Integer::sum);
                    }
                }
            }
        }

        if (matchCounts.isEmpty()) {
            System.out.println("No plagiarism detected.");
            return;
        }

        // Sort by match count descending and report
        matchCounts.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .forEach(entry -> {
                double similarity = (entry.getValue() * 100.0) / ngrams.size();
                String verdict = similarity > 50 ? "PLAGIARISM DETECTED" :
                                 similarity > 20 ? "Suspicious" : "Minor overlap";
                System.out.printf("  Found %d matching n-grams with \"%s\"%n", entry.getValue(), entry.getKey());
                System.out.printf("  Similarity: %.1f%% (%s)%n", similarity, verdict);
            });
    }

    public static void main(String[] args) {
        Problem4_PlagiarismDetector detector = new Problem4_PlagiarismDetector(5);

        System.out.println("=== Plagiarism Detection System ===\n");

        // Index existing essays
        String essay089 = "The impact of climate change on global ecosystems has been a topic of significant research. " +
            "Scientists have observed rising temperatures and shifting weather patterns across multiple continents. " +
            "The melting of polar ice caps contributes to rising sea levels threatening coastal communities worldwide. " +
            "Renewable energy solutions are being developed to mitigate these environmental challenges effectively.";

        String essay092 = "Java is a powerful object-oriented programming language widely used in enterprise applications. " +
            "Hash tables provide O(1) average case lookup time making them ideal for high performance systems. " +
            "The HashMap class in Java uses chaining to handle collisions between keys that produce the same hash. " +
            "Understanding data structures and algorithms is essential for every software engineer today.";

        detector.indexDocument("essay_089.txt", essay089);
        detector.indexDocument("essay_092.txt", essay092);

        // New essay - partially plagiarized from essay_092
        String newEssay = "Java is a powerful object-oriented programming language widely used in enterprise applications. " +
            "Hash tables provide O(1) average case lookup time making them ideal for high performance systems. " +
            "The HashMap class in Java uses chaining to handle collisions between keys that produce the same hash. " +
            "However, TreeMap provides O(log n) operations and maintains keys in sorted order for iteration. " +
            "Data structures form the backbone of efficient algorithm design in modern software engineering.";

        detector.analyzeDocument("essay_123.txt", newEssay);

        // Original essay - no plagiarism
        String originalEssay = "Machine learning has transformed how computers learn patterns from large datasets. " +
            "Neural networks mimic biological neurons to recognize images sounds and textual information. " +
            "Deep learning models require significant computational power and vast amounts of training data. " +
            "These technologies are reshaping industries from healthcare to autonomous vehicle development today.";

        detector.analyzeDocument("essay_200.txt", originalEssay);
    }
}
