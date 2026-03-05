import java.util.*;

/**
 * Problem 10: Multi-Level Cache System with Hash Tables (Netflix-style)
 * Concepts: Multiple hash tables, LRU eviction, LinkedHashMap, resizing/rehashing, performance tiers
 */
public class Problem10_MultiLevelCache {

    // Simulated video data
    static class VideoData {
        String videoId;
        String title;
        long sizeKB;

        VideoData(String videoId, String title, long sizeKB) {
            this.videoId = videoId;
            this.title = title;
            this.sizeKB = sizeKB;
        }

        @Override
        public String toString() {
            return String.format("[%s: \"%s\" (%,d KB)]", videoId, title, sizeKB);
        }
    }

    // L1: In-memory LRU cache (fastest, smallest)
    private LinkedHashMap<String, VideoData> l1Cache;
    private final int L1_CAPACITY;

    // L2: SSD-backed cache (simulated with HashMap + file path)
    private HashMap<String, String> l2Cache; // videoId -> filePath (simulated)
    private final int L2_CAPACITY;

    // L3: Database (slowest, all videos)
    private HashMap<String, VideoData> l3Database;

    // Access counts for promotion decisions
    private HashMap<String, Integer> accessCount;

    // Statistics
    private int l1Hits, l2Hits, l3Hits;
    private int totalRequests;
    private long totalLatencyMs;

    public Problem10_MultiLevelCache(int l1Size, int l2Size) {
        this.L1_CAPACITY = l1Size;
        this.L2_CAPACITY = l2Size;

        // L1: access-order LinkedHashMap for LRU
        this.l1Cache = new LinkedHashMap<String, VideoData>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                if (size() > L1_CAPACITY) {
                    System.out.println("  [L1 Evict] Removing LRU: " + eldest.getKey());
                    // Demote to L2
                    l2Cache.put(eldest.getKey(), "/ssd/videos/" + eldest.getKey() + ".mp4");
                    return true;
                }
                return false;
            }
        };

        this.l2Cache = new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > L2_CAPACITY;
            }
        };

        this.l3Database = new HashMap<>();
        this.accessCount = new HashMap<>();

        // Pre-populate L3 database with videos
        for (int i = 1; i <= 20; i++) {
            String id = "video_" + String.format("%03d", i);
            l3Database.put(id, new VideoData(id, "Movie Title " + i, (long)(500 + Math.random() * 4500)));
        }
    }

    // Get a video - checks L1 -> L2 -> L3
    public VideoData getVideo(String videoId) {
        totalRequests++;
        long startTime = System.nanoTime();
        VideoData result = null;
        String cacheLevel;
        long simulatedLatencyMs;

        // Track access count
        accessCount.merge(videoId, 1, Integer::sum);

        // Check L1 (in-memory) - ~0.5ms simulated
        if (l1Cache.containsKey(videoId)) {
            result = l1Cache.get(videoId);
            l1Hits++;
            cacheLevel = "L1 HIT";
            simulatedLatencyMs = 1;

        // Check L2 (SSD) - ~5ms simulated
        } else if (l2Cache.containsKey(videoId)) {
            result = l3Database.get(videoId); // Load from "SSD path" (using L3 as backing store)
            l2Hits++;
            cacheLevel = "L2 HIT";
            simulatedLatencyMs = 5;

            // Promote to L1 if accessed frequently
            int count = accessCount.getOrDefault(videoId, 0);
            if (count >= 2) {
                l1Cache.put(videoId, result);
                cacheLevel += " -> Promoted to L1";
            }

        // Check L3 (Database) - ~150ms simulated
        } else if (l3Database.containsKey(videoId)) {
            result = l3Database.get(videoId);
            l3Hits++;
            cacheLevel = "L3 DB HIT";
            simulatedLatencyMs = 150;

            // Add to L2 cache
            l2Cache.put(videoId, "/ssd/videos/" + videoId + ".mp4");
            cacheLevel += " -> Added to L2";

        } else {
            cacheLevel = "NOT FOUND";
            simulatedLatencyMs = 200;
        }

        totalLatencyMs += simulatedLatencyMs;

        System.out.printf("getVideo(\"%-12s\") -> %-30s | Latency: %dms | Access#%d%n",
            videoId, cacheLevel, simulatedLatencyMs,
            accessCount.getOrDefault(videoId, 0));

        return result;
    }

    // Invalidate a cached video (e.g., content update)
    public void invalidate(String videoId) {
        l1Cache.remove(videoId);
        l2Cache.remove(videoId);
        System.out.println("Invalidated \"" + videoId + "\" from all cache levels.");
    }

    // Print comprehensive statistics
    public void getStatistics() {
        int total = l1Hits + l2Hits + l3Hits;
        double overallHitRate = total > 0 ? (total * 100.0) / totalRequests : 0;
        double avgLatency = totalRequests > 0 ? (double) totalLatencyMs / totalRequests : 0;

        System.out.println("\n========== Cache Statistics ==========");
        System.out.printf("L1 Cache:  Hit Rate %d/%d (%.0f%%), Avg Time: ~1ms%n",
            l1Hits, totalRequests, l1Hits * 100.0 / totalRequests);
        System.out.printf("L2 Cache:  Hit Rate %d/%d (%.0f%%), Avg Time: ~5ms%n",
            l2Hits, totalRequests, l2Hits * 100.0 / totalRequests);
        System.out.printf("L3 DB:     Hit Rate %d/%d (%.0f%%), Avg Time: ~150ms%n",
            l3Hits, totalRequests, l3Hits * 100.0 / totalRequests);
        System.out.printf("Overall:   Hit Rate %.0f%%, Avg Latency: %.1fms%n", overallHitRate, avgLatency);
        System.out.printf("%nL1 Size: %d/%d | L2 Size: %d/%d%n",
            l1Cache.size(), L1_CAPACITY, l2Cache.size(), L2_CAPACITY);
        System.out.println("======================================");
    }

    public static void main(String[] args) {
        // L1: 3 videos, L2: 8 videos (small for demo)
        Problem10_MultiLevelCache cache = new Problem10_MultiLevelCache(3, 8);

        System.out.println("=== Multi-Level Cache System (Netflix-style) ===\n");

        // First access - all go to L3
        System.out.println("--- First Access (cold cache) ---");
        cache.getVideo("video_001");
        cache.getVideo("video_002");
        cache.getVideo("video_003");

        // Second access - L2 hits, promotions to L1 start
        System.out.println("\n--- Second Access (warming up) ---");
        cache.getVideo("video_001");  // L2 hit -> promotes to L1
        cache.getVideo("video_002");  // L2 hit -> promotes to L1

        // Third access - L1 hits
        System.out.println("\n--- Third Access (hot data) ---");
        cache.getVideo("video_001");  // L1 hit
        cache.getVideo("video_002");  // L1 hit

        // New videos push old ones out of L1
        System.out.println("\n--- Filling L1 cache (capacity: 3) ---");
        cache.getVideo("video_004");
        cache.getVideo("video_005");
        cache.getVideo("video_006");

        // Cache invalidation
        System.out.println("\n--- Content update: invalidate video_001 ---");
        cache.invalidate("video_001");
        cache.getVideo("video_001");  // Should be L3 miss now

        cache.getStatistics();
    }
}
