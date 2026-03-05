import java.util.*;

/**
 * Problem 6: Distributed Rate Limiter for API Gateway
 * Concepts: HashMap for client tracking, time-based operations, token bucket algorithm
 */
public class Problem6_RateLimiter {

    // Token Bucket per client
    static class TokenBucket {
        double tokens;
        double maxTokens;
        double refillRatePerMs; // tokens per millisecond
        long lastRefillTime;

        TokenBucket(double maxTokens, double refillRatePerHour) {
            this.maxTokens = maxTokens;
            this.tokens = maxTokens; // Start full
            this.refillRatePerMs = refillRatePerHour / (3600.0 * 1000.0);
            this.lastRefillTime = System.currentTimeMillis();
        }

        // Refill tokens based on elapsed time
        void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            double tokensToAdd = elapsed * refillRatePerMs;
            tokens = Math.min(maxTokens, tokens + tokensToAdd);
            lastRefillTime = now;
        }

        // Try to consume 1 token
        boolean consume() {
            refill();
            if (tokens >= 1) {
                tokens--;
                return true;
            }
            return false;
        }

        int remainingTokens() {
            refill();
            return (int) Math.floor(tokens);
        }

        // Seconds until reset (when bucket is full again)
        long secondsUntilReset() {
            double tokensNeeded = maxTokens - tokens;
            if (tokensNeeded <= 0) return 0;
            return (long)(tokensNeeded / (refillRatePerMs * 1000));
        }
    }

    // clientId -> TokenBucket
    private HashMap<String, TokenBucket> clientBuckets;
    private final double MAX_REQUESTS_PER_HOUR;

    public Problem6_RateLimiter(double maxRequestsPerHour) {
        this.MAX_REQUESTS_PER_HOUR = maxRequestsPerHour;
        this.clientBuckets = new HashMap<>();
    }

    // Get or create a bucket for this client
    private TokenBucket getBucket(String clientId) {
        return clientBuckets.computeIfAbsent(clientId,
            k -> new TokenBucket(MAX_REQUESTS_PER_HOUR, MAX_REQUESTS_PER_HOUR));
    }

    // Check and consume a request
    public String checkRateLimit(String clientId) {
        TokenBucket bucket = getBucket(clientId);
        boolean allowed = bucket.consume();

        if (allowed) {
            return String.format("ALLOWED  | Client: %-10s | Remaining: %4d requests",
                clientId, bucket.remainingTokens());
        } else {
            long retryAfter = bucket.secondsUntilReset();
            return String.format("DENIED   | Client: %-10s | Limit exceeded! Retry after: %ds",
                clientId, retryAfter);
        }
    }

    // Get rate limit status for a client
    public void getRateLimitStatus(String clientId) {
        TokenBucket bucket = getBucket(clientId);
        int used = (int)(MAX_REQUESTS_PER_HOUR - bucket.remainingTokens());
        System.out.printf("%nRate Limit Status for '%s':%n", clientId);
        System.out.printf("  Used:      %d / %.0f%n", used, MAX_REQUESTS_PER_HOUR);
        System.out.printf("  Remaining: %d%n", bucket.remainingTokens());
        System.out.printf("  Reset in:  %ds%n", bucket.secondsUntilReset());
    }

    // Get all active clients count
    public void printSystemStats() {
        System.out.println("\n=== Rate Limiter System Stats ===");
        System.out.println("Active clients tracked: " + clientBuckets.size());
        System.out.println("Max requests/hour per client: " + (int)MAX_REQUESTS_PER_HOUR);
        clientBuckets.forEach((id, bucket) ->
            System.out.printf("  Client %-12s -> %d tokens remaining%n", id, bucket.remainingTokens()));
    }

    public static void main(String[] args) throws InterruptedException {
        // Allow 10 requests per hour for demo (instead of 1000)
        Problem6_RateLimiter limiter = new Problem6_RateLimiter(10);

        System.out.println("=== Distributed Rate Limiter Demo ===");
        System.out.println("Limit: 10 requests/hour per client\n");

        // Client abc123 makes 12 requests (should be denied after 10)
        System.out.println("--- Client 'abc123' making 12 requests ---");
        for (int i = 1; i <= 12; i++) {
            System.out.println(limiter.checkRateLimit("abc123"));
        }

        // Different client xyz789 - has its own fresh bucket
        System.out.println("\n--- Client 'xyz789' making 3 requests ---");
        for (int i = 1; i <= 3; i++) {
            System.out.println(limiter.checkRateLimit("xyz789"));
        }

        limiter.getRateLimitStatus("abc123");
        limiter.getRateLimitStatus("xyz789");
        limiter.printSystemStats();
    }
}
