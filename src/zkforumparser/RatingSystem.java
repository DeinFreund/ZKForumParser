package zkforumparser;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class RatingSystem {
    
    public abstract void init(Collection<Integer> playerIds);
    
    protected abstract void evaluateResult(Collection<Collection<Integer>> winners, Collection<Collection<Integer>> losers);
    
    protected void evaluateResult(Collection<Collection<Integer>> winners, Collection<Collection<Integer>> losers, int date){
        evaluateResult(winners, losers);
    }
    
    public abstract List<Double> predictResult(List<Collection<Integer> > teams);
    
    public List<Double> predictResult(List<Collection<Integer> > teams, int date){
        return predictResult(teams);
    }
    
}
