/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zkforumparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 *
 * @author User
 */
public class ELO extends RatingSystem {
    
    Map<Integer, Double> ratings = new HashMap();
    
    double K = 64;
    
    private List<Integer> trackedPlayers = new ArrayList();


    public ELO(){
    }
    
    public ELO(double k){
        this.K = k;
        trackedPlayers.add(185685);
        trackedPlayers.add(201951);
        trackedPlayers.add(232028);
        trackedPlayers.add(314871);
        trackedPlayers.add(224173);
        trackedPlayers.add(142479);
    }
    
    @Override
    public void init(Collection<Integer> playerIds) {
        playerIds.stream().forEach(p -> ratings.put(p, 1500d));
    }

    @Override
    public List<Double> predictResult(List<Collection<Integer>> teams, int date) {
        teams.stream().flatMap(t -> t.stream()).filter(p -> trackedPlayers.contains(p)).forEach(p -> plot(date, (int)Math.round(getRating(p) - 1500), trackedPlayers.indexOf(p)));
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
    
    
    Map<Integer, int[]> plot = new TreeMap();
    
    public void plot(int x, int y, int i){
        if (!plot.containsKey(x)) plot.put(x, new int[]{0,0,0,0,0,0,0,0});
        int[] t = plot.get(x);
        t[i] = y;
        plot.put(x, t);
    }

    public void print() {
        if (true) return;
        
        for (Map.Entry<Integer, int[]> e : plot.entrySet()){
            int[] ints = e.getValue();
            System.out.println(e.getKey() + ";" + (ints[0]!=0?ints[0]:"") + ";" + (ints[1]!=0?ints[1]:"") + ";" + (ints[2]!=0?ints[2]:"") + ";" + (ints[3]!=0?ints[3]:"")+ ";" + (ints[4]!=0?ints[4]:"") + ";" + (ints[5]!=0?ints[5]:"")  );
        }
    }

    @Override
    public List<Double> predictResult(List<Collection<Integer>> teams) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
