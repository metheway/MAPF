package Algorithm.Bmaa.Experiments;

import Algorithm.Bmaa.Bmaa;
import Benchmark.Benchmark;
import Benchmark.ProblemMap;
import Benchmark.ProblemSet;
import com.google.common.base.Stopwatch;
import DataStructures.graph.Graph;
import Benchmark.Result;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class is just some basic testing of the algorithms and testing methods that have been developed.
 */
public class BasicTesting {

    public static void main(String[] args) {
        experiment1();
    }

    /**
     * Run a single instance of a BMAA on a map, for one agent count, to ensure that basic elements of my programming
     * works.
     */
    public static void experiment1() {
        String map = "maps/WCIII-blastedlands (512*512).map";
        Graph graph = ProblemMap.graphFromMap(map);
        ProblemSet problemSet = ProblemSet.randomProblemSet(graph, 100);
        System.out.println(problemSet);
        problemSet.printST();

        System.out.println();
        System.out.println("Beginning execution");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Long start = System.currentTimeMillis();
        Result result = new Bmaa(graph,
                problemSet.getS(),
                problemSet.getT(),
                Bmaa.DEFAULT_EXPANSIONS,
                Bmaa.DEFAULT_VISION,
                Bmaa.DEFAULT_MOVES,
                false,
                false).runWithTimeLimit(Duration.ofSeconds(5));
        stopwatch.stop();
        Long end = System.currentTimeMillis();
        System.out.println("Finished execution");
        System.out.println("Millis elapsed (Stopwatch): " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
        System.out.println("Millis elapsed (System.currentTimeMillis): " + (end - start));

        System.out.println(result);
    }

    /**
     * This tests main goal is to try running multiple instances of algorithm and averaging a result.
     */
    public static void experiment2() {
        String map = "maps/BGII-AR0504SR (512*512).map";

        List<Result> results = new ArrayList<>();
        List<ProblemSet> problemSets = new ArrayList<>();

        for (int i=0; i<10; i++) {
            Graph graph = ProblemMap.graphFromMap(map);
            ProblemSet problemSet = ProblemSet.randomProblemSet(graph, 500);

            Stopwatch stopwatch = Stopwatch.createStarted();
            Result result = new Bmaa(graph,
                    problemSet.getS(),
                    problemSet.getT(),
                    Bmaa.DEFAULT_EXPANSIONS,
                    Bmaa.DEFAULT_VISION,
                    Bmaa.DEFAULT_MOVES,
                    false,
                    false).runWithTimeLimit(Duration.ofSeconds(30));

            problemSets.add(problemSet);
            results.add(result);

            System.out.println(problemSet);
            System.out.println(result);
            System.out.println("Elapsed (ms): " + stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        }

        // Average instances
        Result averaged = Result.averageInstanceResults(results);

        System.out.println();
        System.out.println("Problem sets");
        System.out.println("---------------------------");
        System.out.println(ProblemSet.csvHeader());
        for (ProblemSet ps : problemSets) {
            System.out.println(ps.csvStatistics());
        }

        System.out.println();
        System.out.println("Results");
        System.out.println("---------------------------");
        System.out.println(Result.csvHeaders());
        for (Result r : results) {
            System.out.println(r.toCsvString());
        }

        System.out.println();
        System.out.println("Averaged");
        System.out.println("---------------------------");
        System.out.println(averaged.toCsvString());
    }

    /**
     * This experiment is to a basic test of running the algorithm at multiple agent counts, making sure the results look
     * ok.
     */
    public static void experiment3() {
        String map = "maps/BGII-AR0504SR (512*512).map";

        List<Result> results = new ArrayList<>();

        for (int n : Benchmark.BMAA_AGENT_COUNTS) {
            List<Result> instanceResults = new ArrayList<>();

            for (int i=0; i<10; i++) {
                Graph graph = ProblemMap.graphFromMap(map);
                ProblemSet problemSet = ProblemSet.randomProblemSet(graph, n);

                Result result = new Bmaa(graph,
                        problemSet.getS(),
                        problemSet.getT(),
                        Bmaa.DEFAULT_EXPANSIONS,
                        Bmaa.DEFAULT_VISION,
                        Bmaa.DEFAULT_MOVES,
                        false,
                        false).runWithTimeLimit(Duration.ofSeconds(30));

                instanceResults.add(result);
                System.out.println(n + " agents, instance " + i);
            }

            results.add(Result.averageInstanceResults(instanceResults));
        }

        System.out.println();
        System.out.println("Result per agent count");
        System.out.println("------------------------");
        System.out.println(Result.csvHeaders());
        for (Result r : results) {
            System.out.println(r.toCsvString());
        }

        Result averaged = Result.averageDifferentAgentCountsResults(results);

        System.out.println();
        System.out.println("Averaged result across all agent counts");
        System.out.println("------------------------");
        System.out.println(Result.csvHeaders());
        System.out.println(averaged.toCsvString());
    }

}
