//General Mix of Elo and Teamstrength (K, K mod, D mod, extra coms)

package zkforumparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeneralEloTeamstrength extends ELO {

    private boolean withKMod;
    private boolean withDMod;
    private boolean withExtraComs;
    //Map<Integer, Double> ratings = new HashMap();

    public GeneralEloTeamstrength(){
        withKMod=false;
        withDMod=false;
        withExtraComs=true;
    }
    
    public GeneralEloTeamstrength(double k, boolean withKMod, boolean withDMod, boolean withExtraComs){
        this.K = k;
        this.withKMod = withKMod;
        this.withDMod = withDMod;
        this.withExtraComs = withExtraComs;
    }
    
    @Override
    public List<Double> predictResult(List<Collection<Integer>> teams) {
        return teams.stream().map(team -> predictResult(teams, team)).collect(Collectors.toList());
    }
//------------
    //another implementation of predictResult to return single doubles:
    //@Override

    public double predictResult(List<Collection<Integer>> teams, Collection<Integer> team) {
        final double dMod;
        if(withDMod){
            dMod = Math.sqrt(teams.stream().mapToDouble(t -> t.size()).reduce(0.0, Double::sum) / teams.size());
        }else{
            dMod = 1.0;
        }
        if(withExtraComs){
            return (Math.pow(10, dMod*(team.stream().mapToDouble(player -> ratings.get(player)).average().getAsDouble())/400) / teams.stream().map(t -> Math.pow(10, dMod*(t.stream().mapToDouble(player -> ratings.get(player)).average().getAsDouble())/400)).reduce(0.0, Double::sum));
        }
        else{
            return (team.size() * Math.pow(10, dMod*(team.stream().mapToDouble(player -> ratings.get(player)).average().getAsDouble())/400) / teams.stream().map(t -> t.size() * Math.pow(10, dMod*(t.stream().mapToDouble(player -> ratings.get(player)).average().getAsDouble())/400)).reduce(0.0, Double::sum));
        }
    }
//------------
    @Override
    public void evaluateResult(Collection<Collection<Integer>> winnerTeams, Collection<Collection<Integer>> loserTeams) {
        //winnerTeams and loserTeams are combined to use it in predictResult, but I'm not sure if this works with stream.concat (Collection.addAll does not necessarily work because it's an optional operation):
        Map<Integer, Double> ratingsc = new HashMap();
        final List<Collection<Integer>> AllTeams = Stream.concat(winnerTeams.stream(), loserTeams.stream()).collect(Collectors.toList());
        final double kMod;
        if(withKMod){
            kMod = Math.sqrt(AllTeams.stream().mapToDouble(team -> team.size()).reduce(0.0, Double::sum) / AllTeams.size());
        }else{
            kMod = 1.0;
        }
        winnerTeams.stream().forEach(t -> t.stream().forEach(x -> ratingsc.put(x, ratings.get(x) + K * kMod * (1 - predictResult(AllTeams , t)) / t.size())));
        loserTeams.stream().forEach(t -> t.stream().forEach(x -> ratingsc.put(x, ratings.get(x) - K * kMod * predictResult(AllTeams, t) / t.size())));
        ratingsc.forEach((x, y) -> ratings.put(x, y));
    }
//------------
    public boolean getWithKMod(){
        return withKMod;
    }
//------------
    public boolean getWithDMod(){
        return withDMod;
    }
//------------
    public boolean getWithExtraComs(){
        return withExtraComs;
    }
}