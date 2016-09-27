/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zkforumparser;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author User
 */
public class ELO implements RatingSystem {
    
    Map<Integer, Double> ratings = new HashMap();
    
    double K = 64;

    public ELO(){
        
    }
    
    public ELO(double k){
        this.K = k;
    }
    
    @Override
    public void init(Collection<Integer> playerIds) {
        playerIds.stream().forEach(p -> ratings.put(p, 1500d));
    }

    @Override
    public List<Double> predictResult(List<Collection<Integer>> teams) {
        return teams.stream().map(t -> (2d / teams.size()) * 1d / (1d + Math.pow(10, (-t.stream().mapToDouble(x -> ratings.get(x)).average().getAsDouble() + teams.stream().flatMap(x -> x.stream()).filter(x -> !t.contains(x)).mapToDouble(x -> ratings.get(x)).average().getAsDouble()) / 400d))).collect(Collectors.toList()); 
    }

    @Override
    public void evaluateResult(Collection<Collection<Integer>> winnerTeams, Collection<Collection<Integer>> loserTeams) {
        Set<Integer> winners = winnerTeams.stream().flatMap(x -> x.stream()).collect(Collectors.toSet());
        Set<Integer> losers = loserTeams.stream().flatMap(x -> x.stream()).collect(Collectors.toSet());
        double dif = winners.stream().mapToDouble(x -> ratings.get(x)).average().getAsDouble() - losers.stream().mapToDouble(x -> ratings.get(x)).average().getAsDouble();
        winners.forEach(x -> ratings.put(x, ratings.get(x) + Math.sqrt((winners.size() + losers.size()) / 2d) * (K - K / (1 + Math.pow(10, -dif / 400d))) / winners.size()));
        losers.forEach(x -> ratings.put(x, ratings.get(x) - Math.sqrt((winners.size() + losers.size()) / 2d) * (K / (1 + Math.pow(10, dif / 400d))) / losers.size()));
    }
    
    //Optional
    public double getRating(int player){
        return ratings.get(player);
    }
}
