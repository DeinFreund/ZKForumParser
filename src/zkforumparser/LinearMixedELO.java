/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zkforumparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author User
 */
public class LinearMixedELO implements RatingSystem {

    List<WeightChangingELO> elos = new ArrayList();
    List<Integer> eloPoints;

    public LinearMixedELO(Integer... points) {
        eloPoints = Arrays.asList(points);
        eloPoints.forEach(t -> elos.add(new WeightChangingELO()));
    }

    @Override
    public void init(Collection<Integer> playerIds) {
        elos.forEach(r -> r.init(playerIds));
    }

    @Override
    public void evaluateResult(Collection<Collection<Integer>> winners, Collection<Collection<Integer>> losers) {
        int players = winners.stream().map(t -> t.size()).reduce(0, Integer::sum);
        players += losers.stream().map(t -> t.size()).reduce(0, Integer::sum);
        int lowI = 0;
        for (int i = 0; i < eloPoints.size(); i++) {
            if (eloPoints.get(i) < players) {
                lowI = i;
            }
        }
        int low = eloPoints.get(lowI);
        int high = eloPoints.get(Math.min(eloPoints.size() - 1, lowI + 1));
        float weightLow = (high - low == 0) ? 1 : 1 - (players - low) / (float) (high - low);
        elos.get(lowI).evaluateResult(winners, losers, weightLow);
        elos.get(Math.min(eloPoints.size() - 1, lowI + 1)).evaluateResult(winners, losers, 1 - weightLow);
    }

    @Override
    public List<Double> predictResult(List<Collection<Integer>> teams) {
        int players = teams.stream().map(t -> t.size()).reduce(0, Integer::sum);
        int lowI = 0;
        for (int i = 0; i < eloPoints.size(); i++) {
            if (eloPoints.get(i) < players) {
                lowI = i;
            }
        }
        int low = eloPoints.get(lowI);
        int high = eloPoints.get(Math.min(eloPoints.size() - 1, lowI + 1));
        float weightLow = (high - low == 0) ? 1 : 1 - (players - low) / (float) (high - low);
        List<Double> lowPredict = elos.get(lowI).predictResult(teams).stream().map(d -> weightLow * d).collect(Collectors.toList());
        List<Double> highPredict = elos.get(Math.min(eloPoints.size() - 1, lowI + 1)).predictResult(teams).stream().map(d -> (1 - weightLow) * d).collect(Collectors.toList());
        return IntStream.range(0, teams.size()).mapToObj(i -> lowPredict.get(i) + highPredict.get(i)).collect(Collectors.toList());
    }

}
