package zkforumparser;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface RatingSystem {
    
    public void init(Collection<Integer> playerIds);
    
    public void evaluateResult(Collection<Collection<Integer>> winners, Collection<Collection<Integer>> losers);
    
    public List<Double> predictResult(List<Collection<Integer> > teams);
    
}
