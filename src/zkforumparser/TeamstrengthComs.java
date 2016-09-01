package zkforumparser;

//Teamstrength (nicer version)(with extra coms)
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TeamstrengthComs extends ELO {

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
        //winnerTeams and loserTeams are combined to use it in predictResult, but I'm not sure if this works with stream.concat (Collection.addAll does not necessarily work because it's an optional operation):
        winnerTeams.stream().forEach(t -> t.stream().forEach(x -> ratings.put(x, ratings.get(x) + 32 * (1 - predictResult(Stream.concat(winnerTeams.stream(), loserTeams.stream()).collect(Collectors.toList()), t)) / t.size())));
        loserTeams.stream().forEach(t -> t.stream().forEach(x -> ratings.put(x, ratings.get(x) - 32 * predictResult(Stream.concat(winnerTeams.stream(), loserTeams.stream()).collect(Collectors.toList()), t) / t.size())));
    }
}
