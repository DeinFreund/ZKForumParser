/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zkforumparser;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 *
 * @author User
 */
public class InterpolatedELO extends RatingSystem {

    WeightChangingELO low, high;
    int sep;
    
    public InterpolatedELO(int separation, int K){
        low = new WeightChangingELO(K);
        high = new WeightChangingELO(K);
        this.sep = separation;
    }
    
    @Override
    public void init(Collection<Integer> playerIds) {
        low.init(playerIds);
        high.init(playerIds);
    }

    @Override
    public void evaluateResult(Collection<Collection<Integer>> winners, Collection<Collection<Integer>> losers) {
        int players = winners.stream().map(t -> t.size()).reduce(0, Integer::sum);
        players += losers.stream().map(t -> t.size()).reduce(0, Integer::sum);
        float weightLow = Math.min(1, sep / (float)players);
        low.evaluateResult(winners, losers, weightLow);
        high.evaluateResult(winners, losers, 1 - weightLow);
    }

    @Override
    public List<Double> predictResult(List<Collection<Integer>> teams) {
        int players = teams.stream().map(t -> t.size()).reduce(0, Integer::sum);
        float weightLow = Math.min(1, sep / (float)players);
        List<Double> lowPredict = low.predictResult(teams).stream().map(d -> weightLow * d).collect(Collectors.toList());
        List<Double> highPredict = high.predictResult(teams).stream().map(d -> (1 - weightLow) * d).collect(Collectors.toList());
        return IntStream.range(0, teams.size()).mapToObj(i -> lowPredict.get(i) + highPredict.get(i)).collect(Collectors.toList());
    }
    
}
