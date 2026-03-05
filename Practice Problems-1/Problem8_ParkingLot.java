import java.util.*;

/**
 * Problem 8: Parking Lot Management with Open Addressing
 * Concepts: Open addressing (linear probing), custom hash functions, load factor management
 */
public class Problem8_ParkingLot {

    // Status constants for each spot
    static final int EMPTY = 0;
    static final int OCCUPIED = 1;
    static final int DELETED = 2;  // Tombstone for deleted entries

    // Represents a parked vehicle
    static class ParkingRecord {
        String licensePlate;
        long entryTime;
        int status;

        ParkingRecord(String licensePlate) {
            this.licensePlate = licensePlate;
            this.entryTime = System.currentTimeMillis();
            this.status = OCCUPIED;
        }
    }

    private ParkingRecord[] spots;
    private int[] spotStatus;
    private int totalSpots;
    private int occupiedCount;
    private int totalProbes;
    private int totalVehicles;
    private HashMap<Integer, Integer> hourlyTraffic; // hour -> vehicle count

    public Problem8_ParkingLot(int totalSpots) {
        this.totalSpots = totalSpots;
        this.spots = new ParkingRecord[totalSpots];
        this.spotStatus = new int[totalSpots];
        this.occupiedCount = 0;
        this.totalProbes = 0;
        this.totalVehicles = 0;
        this.hourlyTraffic = new HashMap<>();

        Arrays.fill(spotStatus, EMPTY);
    }

    // Hash function: maps license plate to preferred spot number
    private int hash(String licensePlate) {
        int hash = 0;
        for (char c : licensePlate.toCharArray()) {
            hash = (hash * 31 + c) % totalSpots;
        }
        return Math.abs(hash);
    }

    // Park a vehicle using linear probing
    public String parkVehicle(String licensePlate) {
        if (occupiedCount >= totalSpots) {
            return "Parking lot FULL! Cannot park " + licensePlate;
        }

        int preferredSpot = hash(licensePlate);
        int currentSpot = preferredSpot;
        int probes = 0;

        // Linear probing to find the next available spot
        while (spotStatus[currentSpot] == OCCUPIED) {
            currentSpot = (currentSpot + 1) % totalSpots;
            probes++;
            if (currentSpot == preferredSpot) {
                return "Parking lot full!";
            }
        }

        spots[currentSpot] = new ParkingRecord(licensePlate);
        spotStatus[currentSpot] = OCCUPIED;
        occupiedCount++;
        totalProbes += probes;
        totalVehicles++;

        // Track hourly
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        hourlyTraffic.merge(hour, 1, Integer::sum);

        return String.format("Vehicle %-12s -> Spot #%03d (preferred: #%03d, probes: %d)",
            licensePlate, currentSpot, preferredSpot, probes);
    }

    // Exit a vehicle and calculate fee
    public String exitVehicle(String licensePlate) {
        // Search for the vehicle
        int preferredSpot = hash(licensePlate);
        int currentSpot = preferredSpot;

        do {
            if (spotStatus[currentSpot] == OCCUPIED &&
                spots[currentSpot] != null &&
                spots[currentSpot].licensePlate.equals(licensePlate)) {

                long durationMs = System.currentTimeMillis() - spots[currentSpot].entryTime;
                double durationHours = Math.max(durationMs / 3600000.0, 0.1); // min 0.1hr for demo
                double fee = Math.ceil(durationHours * 2) * 2.50; // $2.50 per 30 min

                // Mark as DELETED (tombstone) - important for open addressing
                spotStatus[currentSpot] = DELETED;
                spots[currentSpot] = null;
                occupiedCount--;

                return String.format("Vehicle %-12s -> Spot #%03d freed | Duration: %.1f hrs | Fee: $%.2f",
                    licensePlate, currentSpot, durationHours, fee);
            }
            currentSpot = (currentSpot + 1) % totalSpots;
        } while (currentSpot != preferredSpot && spotStatus[currentSpot] != EMPTY);

        return "Vehicle " + licensePlate + " not found!";
    }

    // Find nearest available spot (closest to entrance = spot 0)
    public int findNearestAvailableSpot() {
        for (int i = 0; i < totalSpots; i++) {
            if (spotStatus[i] != OCCUPIED) return i;
        }
        return -1;
    }

    // Print parking statistics
    public void getStatistics() {
        double occupancyRate = (occupiedCount * 100.0) / totalSpots;
        double avgProbes = totalVehicles > 0 ? (double) totalProbes / totalVehicles : 0;

        // Find peak hour
        String peakHour = hourlyTraffic.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(e -> e.getKey() + ":00 - " + (e.getKey() + 1) + ":00")
            .orElse("N/A");

        System.out.println("\n=== Parking Lot Statistics ===");
        System.out.printf("Total Spots:   %d%n", totalSpots);
        System.out.printf("Occupied:      %d (%.1f%%)%n", occupiedCount, occupancyRate);
        System.out.printf("Available:     %d%n", totalSpots - occupiedCount);
        System.out.printf("Avg Probes:    %.2f%n", avgProbes);
        System.out.printf("Peak Hour:     %s%n", peakHour);
        System.out.printf("Load Factor:   %.2f (rehash threshold: 0.70)%n", (double) occupiedCount / totalSpots);
    }

    public static void main(String[] args) {
        Problem8_ParkingLot lot = new Problem8_ParkingLot(20);

        System.out.println("=== Smart Parking Lot (20 spots) ===\n");

        // Park some vehicles
        String[] vehicles = {"ABC-1234", "ABC-1235", "XYZ-9999", "DEF-5678",
                             "GHI-1111", "JKL-2222", "MNO-3333", "PQR-4444"};
        System.out.println("--- Parking vehicles ---");
        for (String v : vehicles) {
            System.out.println(lot.parkVehicle(v));
        }

        // Exit a vehicle
        System.out.println("\n--- Exiting vehicles ---");
        System.out.println(lot.exitVehicle("ABC-1234"));
        System.out.println(lot.exitVehicle("XYZ-9999"));

        // Park more after some exits
        System.out.println("\n--- Parking more vehicles ---");
        System.out.println(lot.parkVehicle("NEW-0001"));
        System.out.println(lot.parkVehicle("NEW-0002"));

        // Nearest available spot
        System.out.println("\nNearest available spot: #" + lot.findNearestAvailableSpot());

        // Stats
        lot.getStatistics();
    }
}
