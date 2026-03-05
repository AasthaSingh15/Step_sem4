import java.util.*;
import java.util.stream.*;

/**
 * Problem 5: Real-Time Analytics Dashboard for Website Traffic
 * Concepts: Frequency counting, multiple hash tables, load factor, time/space optimization
 */
public class Problem5_AnalyticsDashboard {

    // Page URL -> total visit count
    private HashMap<String, Integer> pageViews;

    // Page URL -> set of unique userIds
    private HashMap<String, Set<String>> uniqueVisitors;

    // Traffic source -> count
    private HashMap<String, Integer> trafficSources;

    // Country/location -> count
    private HashMap<String, Integer> locationData;

    public Problem5_AnalyticsDashboard() {
        pageViews = new HashMap<>();
        uniqueVisitors = new HashMap<>();
        trafficSources = new HashMap<>();
        locationData = new HashMap<>();
    }

    // Process a single page view event
    public void processEvent(String url, String userId, String source, String country) {
        // Track total page views
        pageViews.merge(url, 1, Integer::sum);

        // Track unique visitors
        uniqueVisitors.computeIfAbsent(url, k -> new HashSet<>()).add(userId);

        // Track traffic sources
        trafficSources.merge(source, 1, Integer::sum);

        // Track location
        locationData.merge(country, 1, Integer::sum);
    }

    // Get top N most visited pages
    public List<Map.Entry<String, Integer>> getTopPages(int n) {
        return pageViews.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .limit(n)
            .collect(Collectors.toList());
    }

    // Get unique visitor count for a page
    public int getUniqueVisitors(String url) {
        Set<String> visitors = uniqueVisitors.get(url);
        return visitors != null ? visitors.size() : 0;
    }

    // Get total traffic source distribution as percentages
    public Map<String, Double> getTrafficSourcePercentages() {
        int total = trafficSources.values().stream().mapToInt(Integer::intValue).sum();
        Map<String, Double> percentages = new LinkedHashMap<>();
        trafficSources.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .forEach(e -> percentages.put(e.getKey(), (e.getValue() * 100.0) / total));
        return percentages;
    }

    // Print full dashboard
    public void getDashboard(int topN) {
        System.out.println("\n========== ANALYTICS DASHBOARD ==========");

        System.out.println("\nTop " + topN + " Pages:");
        List<Map.Entry<String, Integer>> topPages = getTopPages(topN);
        for (int i = 0; i < topPages.size(); i++) {
            Map.Entry<String, Integer> entry = topPages.get(i);
            System.out.printf("  %d. %-35s %,6d views  (%,d unique)%n",
                i + 1,
                entry.getKey(),
                entry.getValue(),
                getUniqueVisitors(entry.getKey()));
        }

        System.out.println("\nTraffic Sources:");
        getTrafficSourcePercentages().forEach((source, pct) ->
            System.out.printf("  %-15s %.1f%%%n", source + ":", pct));

        System.out.println("\nTop Countries:");
        locationData.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .limit(5)
            .forEach(e -> System.out.printf("  %-15s %,d visits%n", e.getKey() + ":", e.getValue()));

        int totalEvents = pageViews.values().stream().mapToInt(Integer::intValue).sum();
        System.out.printf("%nTotal Page Views Processed: %,d%n", totalEvents);
        System.out.println("==========================================");
    }

    public static void main(String[] args) {
        Problem5_AnalyticsDashboard dashboard = new Problem5_AnalyticsDashboard();

        System.out.println("=== Real-Time Analytics Dashboard ===");
        System.out.println("Simulating 1000 page view events...\n");

        String[] pages = {"/article/breaking-news", "/sports/championship", "/tech/java-tutorial",
                          "/entertainment/movies", "/business/stocks", "/article/climate-change"};
        String[] sources = {"Google", "Direct", "Facebook", "Twitter", "Instagram"};
        String[] countries = {"India", "USA", "UK", "Germany", "Canada", "Australia"};
        int[] sourceWeights = {45, 30, 15, 7, 3}; // Weighted distribution

        Random rand = new Random(42);

        // Simulate 1000 events
        for (int i = 0; i < 1000; i++) {
            String page = pages[rand.nextInt(pages.length)];
            String userId = "user_" + (rand.nextInt(500) + 1);  // 500 unique users

            // Weighted source selection
            int r = rand.nextInt(100);
            String source;
            if (r < 45) source = "Google";
            else if (r < 75) source = "Direct";
            else if (r < 90) source = "Facebook";
            else if (r < 97) source = "Twitter";
            else source = "Instagram";

            String country = countries[rand.nextInt(countries.length)];
            dashboard.processEvent(page, userId, source, country);
        }

        dashboard.getDashboard(5);
    }
}
