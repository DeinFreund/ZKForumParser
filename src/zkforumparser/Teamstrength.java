/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zkforumparser;

//Teamstrength (nicer version)(with extra coms)
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Teamstrength extends ELO {

    public Teamstrength() {

    }

    public Teamstrength(double k) {
        this.K = k;
    }

    //Map<Integer, Double> ratings = new HashMap();
    @Override
    public List<Double> predictResult(List<Collection<Integer>> teams) {
        return teams.stream().map(t -> predictResult(teams, t)).collect(Collectors.toList());
    }
//------------
    //another implementation of predictResult to return single doubles:
    //@Override

    public double predictResult(List<Collection<Integer>> teams, Collection<Integer> team) {
        double D = 1;//Math.pow(teams.stream().mapToInt(t -> t.size()).average().getAsDouble(), 0.4);
        double p = (team.stream().mapToDouble(x -> (Math.pow(10, ratings.get(x) / 400))).reduce(0.0, Double::sum) / teams.stream().flatMap(x -> x.stream()).mapToDouble(x -> (Math.pow(10, ratings.get(x) / 400))).reduce(0.0, Double::sum));
        return Math.pow(p, D) / (Math.pow(p, D) + Math.pow(1 - p, D));
    }
//------------

    @Override
    public void evaluateResult(Collection<Collection<Integer>> winnerTeams, Collection<Collection<Integer>> loserTeams) {
        Set<Integer> winners = winnerTeams.stream().flatMap(x -> x.stream()).collect(Collectors.toSet());
        Set<Integer> losers = loserTeams.stream().flatMap(x -> x.stream()).collect(Collectors.toSet());
        Map<Integer, Double> ratingsc = new HashMap();
        winnerTeams.stream().forEach(t -> t.stream().forEach(x -> ratingsc.put(x, ratings.get(x) + Math.sqrt((winners.size() + losers.size()) / 2d) * K * (1 - predictResult(Stream.concat(winnerTeams.stream(), loserTeams.stream()).collect(Collectors.toList()), t)) / t.size())));
        loserTeams.stream().forEach(t -> t.stream().forEach(x -> ratingsc.put(x, ratings.get(x) - Math.sqrt((winners.size() + losers.size()) / 2d) * K * predictResult(Stream.concat(winnerTeams.stream(), loserTeams.stream()).collect(Collectors.toList()), t) / t.size())));
        ratingsc.forEach((x, y) -> ratings.put(x, y));
    }
}
