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
public class SplitELO implements RatingSystem {

    WeightChangingELO low, high;
    int sep;
    
    public SplitELO(int sep, int K) {
        low = new WeightChangingELO(K);
        high = new WeightChangingELO(K);
        this.sep = sep;
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
        if (players <= sep) {
            low.evaluateResult(winners, losers, 1);
            high.evaluateResult(winners, losers, 1);
        } else {
            high.evaluateResult(winners, losers, 1);
        }
    }

    @Override
    public List<Double> predictResult(List<Collection<Integer>> teams) {
        int players = teams.stream().map(t -> t.size()).reduce(0, Integer::sum);
        if (players <= sep) {
            return low.predictResult(teams);
        } else {
            return high.predictResult(teams);
        }
    }

}
