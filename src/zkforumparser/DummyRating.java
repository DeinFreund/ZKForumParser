/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zkforumparser;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author User
 */
public class DummyRating implements RatingSystem {

    @Override
    public void init(Collection<Integer> playerIds) {
    }

    @Override
    public void evaluateResult(Collection<Collection<Integer>> winners, Collection<Collection<Integer>> losers) {
    }

    @Override
    public List<Double> predictResult(List<Collection<Integer>> teams) {
        return teams.stream().map(t -> 0.5d).collect(Collectors.toList());
    }
    
}
