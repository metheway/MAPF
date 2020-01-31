package Benchmark;

import Common.Util;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Result {

    private int numberOfAgents;

    private double completionRate;

    private double averageCompletionTimeSeconds;

    private double averageCompletionTimeSteps;

    private double averageTravelDistance;

    public Result(int numberOfAgents,
                  double completionRate,
                  double averageCompletionTimeSeconds,
                  double averageCompletionTimeSteps,
                  double averageTravelDistance) {
        this.numberOfAgents = numberOfAgents;
        this.completionRate = completionRate;
        this.averageCompletionTimeSeconds = averageCompletionTimeSeconds;
        this.averageCompletionTimeSteps = averageCompletionTimeSteps;
        this.averageTravelDistance = averageTravelDistance;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Average a collection of result objects into one. To conform with benchmarking done in the BMAA paper,
     * for completionTimeSeconds, we only average result instances of up to 200 agents.
     * @param results the results to be averaged
     * @return the averaged result
     */
    public static Result averageResults(Collection<Result> results) {
        int averageNumAgents = results.stream()
                .mapToInt(result -> result.getNumberOfAgents())
                .sum() / results.size();

        double averageCompletionRate = results.stream()
                .mapToDouble(result -> result.getCompletionRate())
                .sum() / (double) results.size();

        int lessThan200Agents = (int) results.stream().filter(result -> result.getNumberOfAgents() <= 200).count();
        double averageCompletionTimeSeconds = results.stream()
                .filter(result -> result.getNumberOfAgents() <= 200)
                .mapToDouble(result -> result.getAverageCompletionTimeSeconds())
                .sum() / lessThan200Agents;

        // !!!!-------- TO BE DELETED
        // Average completion time (time steps) is only averaged over runs with 200 agents
        double averageCompletionTimeSteps = results.stream()
                .filter(result -> result.getNumberOfAgents() <= 200)
                .mapToDouble(result -> result.getAverageCompletionTimeSeconds())
                .average()
                .orElseThrow(RuntimeException::new);

        double averageTravelDistance =
                results.stream()
                        .mapToDouble(result -> result.getAverageTravelDistance())
                        .average()
                        .orElseThrow(RuntimeException::new);

        return new Result(averageNumAgents, averageCompletionRate, averageCompletionTimeSeconds, averageCompletionTimeSteps, averageTravelDistance);
    }

    /**
     * Average results of runs with the same number of agents in each.
     * The runs should be on the same map, with the same number of agents, the same algorithm parameters.
     * The only difference between runs is the problem set of which they represent.
     * Each run is a randomised problem set of the same graph, and the same number of agents.
     * @return an averaged result
     */
    public static Result averageInstanceResults(Collection<Result> results) {
        // Sanity check to make sure it is results of the same number of agents
        Integer numAgents = null;
        for (Result result : results) {
            if (!(numAgents == null) && !(result.getNumberOfAgents() == numAgents)) {
                throw new RuntimeException("Error - results of algorithm should be of the same number of agents but they were not");
            }
            numAgents = result.getNumberOfAgents();
        }

        // Average completion rate over every run
        double averageCompletionRate = results.stream()
                .mapToDouble(result -> result.getCompletionRate())
                .average()
                .orElseThrow(RuntimeException::new);

        // Average completion time (seconds) is done over every run
        double averageCompletionTimeSeconds = results.stream()
                .mapToDouble(result -> result.getAverageCompletionTimeSeconds())
                .average()
                .orElseThrow(RuntimeException::new);

        // Average completion time (time steps) over every run
        double averageCompletionTimeSteps = results.stream()
                .mapToDouble(result -> result.averageCompletionTimeSteps)
                .average()
                .orElseThrow(RuntimeException::new);

        // Average travel distance over every run
        double averageTravelDistance =
                results.stream()
                .mapToDouble(result -> result.getAverageTravelDistance())
                .average()
                .orElseThrow(RuntimeException::new);

        return new Result(numAgents,
                averageCompletionRate,
                averageCompletionTimeSeconds,
                averageCompletionTimeSteps,
                averageTravelDistance);
    }



    /**
     * Average results of runs of an algorithm.
     * The results should represent the same map, but with different number of agents for each result.
     * Each result itself should be averaged among 10 random problem sets.
     * For completion time (seconds) and completion time (time steps) we only average the results of runs with up to
     * 200 agents. This is because at this number of agents.
     * @return the averaged result
     */
    public static Result averageDifferentAgentCountsResults(Collection<Result> results) {

        // Average completion rate over every run
        double averageCompletionRate = results.stream()
                .mapToDouble(result -> result.getCompletionRate())
                .average()
                .orElseThrow(RuntimeException::new);

        // Average completion time (second) is only averaged over runs with up to 200 agents
        // If an agent's completion time is undefined (because it is not at its goal at the end of the algorithm)
        // then its completion time is 30 seconds, but this is done at the algorithm level
        double averageCompletionTimeSeconds = results.stream()
                .filter(result -> result.getNumberOfAgents() <= 200)
                .mapToDouble(result -> result.getAverageCompletionTimeSeconds())
                .average()
                .orElseThrow(RuntimeException::new);

        // Average completion time (time steps) is only averaged over runs with < 200 agents
        double averageCompletionTimeSteps = results.stream()
                .filter(result -> result.getNumberOfAgents() <= 200)
                .mapToDouble(result -> result.getAverageCompletionTimeSteps())
                .average()
                .orElseThrow(RuntimeException::new);

        // Average travel distance is only averaged over runs with < 200 agents
        double averageTravelDistance = results.stream()
                .filter(result -> result.getNumberOfAgents() <= 200)
                .mapToDouble(result -> result.getAverageTravelDistance())
                .average()
                .orElseThrow(RuntimeException::new);

        // -1 number of agents because number of agents doesn't make sense in this context of averaging results
        // across different numbers of agents
        return new Result(-1,
                averageCompletionRate,
                averageCompletionTimeSeconds,
                averageCompletionTimeSteps,
                averageTravelDistance);
    }

    //----------------------------------------------------------------------------------------------

    public int getNumberOfAgents() {
        return numberOfAgents;
    }

    /**
     * Get the percentage of agents who were at their goal on when the algorithm
     * terminated or the time limit was reached
     * @return the completion rate of the algorithm run
     */
    public double getCompletionRate() {
        return completionRate;
    }

    /**
     * Get the average completion time in seconds of all agents.
     * Completion time in seconds is defined as the amount of real time elapsed when an agent reaches its goal.
     * If an agent reaches does not reach its goal, it is assigned a completion time of 30 seconds.
     * As specified in the BMAA paper (somewhat ambiguously) we only completion rates of runs with up to 200 agents.
     * @return a double which is the average completion time (seconds).
     */
    public double getAverageCompletionTimeSeconds() {
        return averageCompletionTimeSeconds;
    }

    /**
     * Get the average completion time in time steps of all agents.
     * Completion time (time steps) is defined as the average time step at which an agent reaches its goal and stays there.
     * If an agent does not reach its goal at the end of the algorithm, we assign a completion time (time steps) that is
     * equal to the largest completion time (time steps) among those agents who were at their goal upon termination of the
     * algorithm.
     * @return a double which is the average completion (time steps)
     */
    public double getAverageCompletionTimeSteps() {
        return averageCompletionTimeSteps;
    }

    /**
     * Get the average travel distance
     * The travel distance of an agent is the sum of costs of edges traversed by the agent.
     * @return a double which is the travel distance of the agent
     */
    public double getAverageTravelDistance() {
        return averageTravelDistance;
    }

    @Override
    public String toString() {
        return "Result: " + "\n" +
                "\tCompletion Rate: " + this.completionRate + "\n" +
                "\tAverage Completion Time (Seconds): " + averageCompletionTimeSeconds + "\n" +
                "\tAverage Completion Time (Steps): " + averageCompletionTimeSteps + "\n" +
                "\tAverage Travel Distance: " + averageTravelDistance;

    }
}