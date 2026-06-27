package com.isa.backend;

import com.isa.backend.service.VideoService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.util.StopWatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
@Disabled
@SpringBootTest
public class LocalTrendingAnalysisTest {

    @Autowired
    private VideoService videoService;

    @Autowired
    private CacheManager cacheManager;

    // Optimal Measure and Latency.
    @Test
    public void testOptimalMeasureAndLatency() throws InterruptedException {
        int requests = 100;
        StopWatch report = new StopWatch("\nOptimal Measure & Latency Analysis");

        // Clear cache to measure initial database response time
        evictCache();
        report.start("Real-time (No Cache)");
        runSimulation(requests, 44.81, 20.46, 0.05);
        report.stop();

        // Measure performance with warm cache (the "optimal measure")
        report.start("Optimal (With Cache)");
        runSimulation(requests, 44.81, 20.46, 0.05);
        report.stop();

        System.out.println(report.prettyPrint());
        printAnalysis(report, requests);
    }

    // Geographic Area Simulation.
    @Test
    public void testAreaSimulationAndImpact() throws InterruptedException {
        int requests = 100;
        StopWatch report = new StopWatch("\nArea Simulation (Database Focus)");

        // Scenario A: Hotspot - Heavy activity in a small area
        evictCache();
        report.start("Hotspot Simulation (Small Area)");
        runSimulation(requests, 44.81, 20.46, 0.01);
        report.stop();

        // Scenario B: Distributed - Activity from different cities
        evictCache();
        report.start("Distributed Simulation (Multiple Cities)");
        runSimulation(requests, 44.00, 21.00, 2.0);
        report.stop();

        System.out.println(report.prettyPrint());
        printAnalysis(report, requests);
    }

    private void runSimulation(int count, double baseLat, double baseLon, double spread) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < count; i++) {
            double lat = baseLat + (Math.random() - 0.5) * spread;
            double lon = baseLon + (Math.random() - 0.5) * spread;
            executor.execute(() -> videoService.resolveUserLocation(lat, lon, null));
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }

    private void evictCache() {
        if (cacheManager.getCache("localTrending") != null) {
            cacheManager.getCache("localTrending").clear();
        }
    }

    private void printAnalysis(StopWatch sw, int reqCount) {
        System.out.println("---- PERFORMANCE ANALYSIS REPORT ----");
        for (StopWatch.TaskInfo task : sw.getTaskInfo()) {
            double avg = (task.getTimeMillis() / (double) reqCount);
            System.out.printf("Task: %-35s | Avg Response Time: %.2f ms%n",
                    task.getTaskName(), avg);
        }
        System.out.println("-------------------------------------\n");
    }
}