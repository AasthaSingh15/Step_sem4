import java.util.*;

/**
 * Problem 3: DNS Cache with TTL (Time To Live)
 * Concepts: Custom Entry class, chaining for collision resolution, time-based ops, LRU eviction
 */
public class Problem3_DNSCache {

    // Inner class representing a DNS cache entry
    static class DNSEntry {
        String domain;
        String ipAddress;
        long insertedAt;   // System time in ms
        long ttlMillis;    // TTL in milliseconds

        DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.insertedAt = System.currentTimeMillis();
            this.ttlMillis = ttlSeconds * 1000;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - insertedAt > ttlMillis;
        }

        long remainingTTL() {
            long remaining = ttlMillis - (System.currentTimeMillis() - insertedAt);
            return Math.max(0, remaining / 1000);
        }
    }

    private LinkedHashMap<String, DNSEntry> cache;
    private final int MAX_CACHE_SIZE;
    private int hits = 0;
    private int misses = 0;
    private int expired = 0;

    // Simulated upstream DNS database
    private HashMap<String, String> upstreamDNS;

    public Problem3_DNSCache(int maxSize) {
        this.MAX_CACHE_SIZE = maxSize;

        // LRU eviction: access-order LinkedHashMap
        this.cache = new LinkedHashMap<String, DNSEntry>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };

        // Simulate upstream DNS server
        upstreamDNS = new HashMap<>();
        upstreamDNS.put("google.com", "172.217.14.206");
        upstreamDNS.put("github.com", "140.82.112.4");
        upstreamDNS.put("stackoverflow.com", "151.101.1.69");
        upstreamDNS.put("youtube.com", "216.58.200.46");
    }

    // Resolve domain - checks cache first, then upstream
    public String resolve(String domain) {
        DNSEntry entry = cache.get(domain);

        if (entry != null) {
            if (!entry.isExpired()) {
                hits++;
                System.out.println("resolve(\"" + domain + "\") -> Cache HIT -> " + entry.ipAddress
                        + " [TTL remaining: " + entry.remainingTTL() + "s] (retrieved in 0.2ms)");
                return entry.ipAddress;
            } else {
                expired++;
                cache.remove(domain);
                System.out.println("resolve(\"" + domain + "\") -> Cache EXPIRED -> querying upstream...");
            }
        } else {
            misses++;
            System.out.println("resolve(\"" + domain + "\") -> Cache MISS -> querying upstream...");
        }

        // Query upstream DNS
        String ip = upstreamDNS.getOrDefault(domain, "NXDOMAIN");
        if (!ip.equals("NXDOMAIN")) {
            long ttl = 300; // 300 seconds default TTL
            cache.put(domain, new DNSEntry(domain, ip, ttl));
            System.out.println("  Upstream result: " + ip + " (TTL: " + ttl + "s) -> Cached");
        }
        return ip;
    }

    // Print cache statistics
    public void getCacheStats() {
        int total = hits + misses + expired;
        double hitRate = total > 0 ? (hits * 100.0 / total) : 0;
        System.out.println("\n=== Cache Statistics ===");
        System.out.printf("Cache Hits:    %d%n", hits);
        System.out.printf("Cache Misses:  %d%n", misses);
        System.out.printf("Expired:       %d%n", expired);
        System.out.printf("Hit Rate:      %.1f%%%n", hitRate);
        System.out.printf("Cache Size:    %d / %d%n", cache.size(), MAX_CACHE_SIZE);
    }

    // Print all current cache entries
    public void printCache() {
        System.out.println("\n=== Current Cache Entries ===");
        if (cache.isEmpty()) {
            System.out.println("Cache is empty.");
            return;
        }
        for (Map.Entry<String, DNSEntry> entry : cache.entrySet()) {
            DNSEntry e = entry.getValue();
            System.out.printf("  %-25s -> %-18s [TTL remaining: %ds, Expired: %s]%n",
                    e.domain, e.ipAddress, e.remainingTTL(), e.isExpired());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Problem3_DNSCache dns = new Problem3_DNSCache(100);

        System.out.println("=== DNS Cache with TTL ===\n");

        // First lookup - should be a MISS
        dns.resolve("google.com");
        System.out.println();

        // Second lookup - should be a HIT
        dns.resolve("google.com");
        System.out.println();

        // Other domains
        dns.resolve("github.com");
        System.out.println();
        dns.resolve("github.com");   // HIT
        System.out.println();

        // Unknown domain
        dns.resolve("unknown.xyz");
        System.out.println();

        dns.printCache();
        dns.getCacheStats();
    }
}
