package zkforumparser;

//Teamstrength (nicer version)(with extra coms)
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TeamstrengthComs extends ELO {

    public TeamstrengthComs(){
        
    }
    
    public TeamstrengthComs(double k){
        this.K = k;
    }
    
    //Map<Integer, Double> ratings = new HashMap();
    @Override
    public List<Double> predictResult(List<Collection<Integer>> teams) {
        return teams.stream().map(t -> (t.stream().mapToDouble(x -> (Math.pow(10,ratings.get(x)/400))).average().getAsDouble() / teams.stream().map(t2 -> t2.stream().mapToDouble(x -> (Math.pow(10,ratings.get(x)/400))).average().getAsDouble()).reduce(0.0, Double::sum))).collect(Collectors.toList());
    }
//------------
    //another implementation of predictResult to return single doubles:
    //@Override

    public double predictResult(List<Collection<Integer>> teams, Collection<Integer> team) {
        return (team.stream().mapToDouble(x -> (Math.pow(10, ratings.get(x) / 400))).average().getAsDouble() / teams.stream().map(t -> t.stream().mapToDouble(x -> (Math.pow(10, ratings.get(x) / 400))).average().getAsDouble()).reduce(0.0, Double::sum));
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
    