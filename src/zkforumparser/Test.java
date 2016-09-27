/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zkforumparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author User
 */
public class Test {
    
    
    
    public static void main(String argv[]){
        
        ELO elo = new Teamstrength();
        elo.ratings.put(1, 1500d);
        elo.ratings.put(2, 1900d);
        List<Integer> t1 = new ArrayList();
        t1.add(1);
        List<Integer> t2 = new ArrayList();
        t2.add(2);
        List<Collection<Integer>> teams = new ArrayList();
        List<Collection<Integer>> teams1 = new ArrayList();
        List<Collection<Integer>> teams2 = new ArrayList();
        teams.add(t1);
        teams.add(t2);
        teams1.add(t1);
        teams2.add(t2);
        System.out.println(elo.predictResult(teams));
        elo.evaluateResult(teams1, teams2);
        System.out.println(elo.getRating(1));
        System.out.println(elo.getRating(2));
    }
}
