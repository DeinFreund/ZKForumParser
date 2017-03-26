/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zkforumparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import whr.*;

/**
 *
 * @author User
 */
public class WHR extends RatingSystem {

    WholeHistoryRating base;

    @Override
    public void init(Collection<Integer> playerIds) {
        base = new WholeHistoryRating(30);
    }

    int lastDate = -1;
    int index = 0;

    @Override
    public void evaluateResult(Collection<Collection<Integer>> winners, Collection<Collection<Integer>> losers, int date) {
        Game game = base.createGame(winners.stream().flatMap(l -> l.stream()).collect(Collectors.toSet()), losers.stream().flatMap(l -> l.stream()).collect(Collectors.toSet()), "B", date);
        index++;
        //if (true) return;
        winners.forEach(a -> a.forEach( p -> base.getPlayerById(p).runOneNewtonIteration()));
        losers.forEach(a -> a.forEach(p -> base.getPlayerById(p).runOneNewtonIteration()));
        winners.forEach(a -> a.forEach(p -> base.getPlayerById(p).updateUncertainty()));
        losers.forEach(a -> a.forEach(p -> base.getPlayerById(p).updateUncertainty()));
        if (index - lastDate > 1000 && index > 0.5000000) {
            base.runIterations(1);
            lastDate = index;
        }
        if ( false && (winners.stream().flatMap(l -> l.stream()).collect(Collectors.toSet()).contains(185685) || losers.stream().flatMap(l -> l.stream()).collect(Collectors.toSet()).contains(185685))){
            List<int[]> ret = base.getPlayerRatings(185685);
            System.out.println(date + ";" + ret.get(ret.size() - 1)[1]);
        }
    }

    @Override
    public List<Double> predictResult(List<Collection<Integer>> teams, int date) {
        List<Double> retval = teams.stream()
                .map(t -> base.setup_game(t, teams.stream().filter(t2 -> !t2.equals(t))
                        .flatMap(t2 -> t2.stream()).collect(Collectors.toList()), "B", date).getBlackWinProbability() * 2 / teams.size())
                .collect(Collectors.toList());
        return retval;
        /*Game game = base.setup_game(teams.get(0), teams.get(1), "B", date);
        List<Double> chances = new ArrayList();
        double w = game.getWhiteWinProbability();
        double b = game.getBlackWinProbability();
        chances.add(b);
        chances.add(w);
        return chances;*/
    }

    @Override
    protected void evaluateResult(Collection<Collection<Integer>> winners, Collection<Collection<Integer>> losers) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Double> predictResult(List<Collection<Integer>> teams) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    Map<Integer, int[]> plot = new TreeMap();
    
    public void plot(int x, int y, int i){
        y+= 1500;
        if (!plot.containsKey(x)) plot.put(x, new int[]{0,0,0,0,0,0,0,0,0,0});
        int[] t = plot.get(x);
        t[i] = y;
        plot.put(x, t);
    }

    public void print() {
        if (true)return;
        for (int i = 0; i < 50; i++) {
            System.out.println("iteration: " + i);
            base.runIterations(1);
        }
        //elto = 166011
        List<int[]> ret = base.getPlayerRatings(341137);//proto
        for (int[] ints : ret) {
            System.out.println(ints[0] + ";" + ints[1] + ";" + (ints[1] + ints[2]) + ";" + (ints[1] - ints[2]));
            plot(ints[0], ints[1], 0);
        }
        System.out.println("----------------------");
        ret = base.getPlayerRatings(332601);//waka
        for (int[] ints : ret) {
            System.out.println(ints[0] + ";" + ints[1] + ";" + (ints[1] + ints[2]) + ";" + (ints[1] - ints[2]));
            plot(ints[0], ints[1], 1);
        }
        System.out.println("----------------------");
        ret = base.getPlayerRatings(232028);//dein
        for (int[] ints : ret) {
            System.out.println(ints[0] + ";" + ints[1] + ";" + (ints[1] + ints[2]) + ";" + (ints[1] - ints[2]));
            plot(ints[0], ints[1], 2);
        }
        System.out.println("----------------------");
        ret = base.getPlayerRatings(59430);//snuggly
        for (int[] ints : ret) {
            System.out.println(ints[0] + ";" + ints[1] + ";" + (ints[1] + ints[2]) + ";" + (ints[1] - ints[2]));
            plot(ints[0], ints[1], 3);
        }
        System.out.println("----------------------");
        ret = base.getPlayerRatings(313773);//sf33
        for (int[] ints : ret) {
            System.out.println(ints[0] + ";" + ints[1] + ";" + (ints[1] + ints[2]) + ";" + (ints[1] - ints[2]));
            plot(ints[0], ints[1], 4);
        }
        System.out.println("----------------------");
        ret = base.getPlayerRatings(232028);//juletz
        for (int[] ints : ret) {
            System.out.println(ints[0] + ";" + ints[1] + ";" + (ints[1] + ints[2]) + ";" + (ints[1] - ints[2]));
            plot(ints[0], ints[1], 5);
        }
        System.out.println("----------------------");
        ret = base.getPlayerRatings(169779);//northchileang
        for (int[] ints : ret) {
            System.out.println(ints[0] + ";" + ints[1] + ";" + (ints[1] + ints[2]) + ";" + (ints[1] - ints[2]));
            plot(ints[0], ints[1], 6);
        }
        System.out.println("----------------------");
        ret = base.getPlayerRatings(5986);//fealthas
        for (int[] ints : ret) {
            System.out.println(ints[0] + ";" + ints[1] + ";" + (ints[1] + ints[2]) + ";" + (ints[1] - ints[2]));
            plot(ints[0], ints[1], 7);
        }
        System.out.println("----------------------");
        for (Map.Entry<Integer, int[]> e : plot.entrySet()){
            int[] ints = e.getValue();
            System.out.println(e.getKey() + ";" + (ints[0]!=0?ints[0]:"") + ";" + (ints[1]!=0?ints[1]:"") + ";" + (ints[2]!=0?ints[2]:"") + ";" + (ints[3]!=0?ints[3]:"")+ ";" + (ints[4]!=0?ints[4]:"") + ";" + (ints[5]!=0?ints[5]:"")  + ";" + (ints[6]!=0?ints[6]:"") + ";" + (ints[7]!=0?ints[7]:"") );
        }
    }
}
