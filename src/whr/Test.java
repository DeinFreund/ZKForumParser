
package whr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author User
 */
public class Test {
    
    public static void main(String args[]){
        WholeHistoryRating base = new WholeHistoryRating(300);
        Collection<Integer> shusaku = new ArrayList();
        shusaku.add(1);
        shusaku.add(2);
        Collection<Integer> shusai = new ArrayList();
        shusai.add(3);
        shusai.add(4);
        Collection<Integer> three = new ArrayList();
        Collection<Integer> four = new ArrayList();
        three.add(3);
        four.add(4);
        
        base.createGame(shusaku, shusai, "B", 1);
        base.createGame(shusaku, shusai, "W", 2);
        base.createGame(shusaku, shusai, "W", 3);
        base.createGame(shusaku, shusai, "W", 4);
        base.createGame(shusaku, shusai, "W", 4);
        //base.create_game(three, four, "W", 4);
        base.runIterations(50);
        //System.out.println(base.setup_game(shusaku, shusai, "W", 4).white_win_probability());
        /*base.create_game(shusaku, shusai, "W", 41);
        base.create_game(shusaku, shusai, "W", 42);
        base.create_game(shusaku, shusai, "W", 43);
        base.create_game(shusaku, shusai, "W", 44);
        base.iterate(50);*/
        printRatings(base.getPlayerRatings(1));
        printRatings(base.getPlayerRatings(2));
        printRatings(base.getPlayerRatings(3));
        printRatings(base.getPlayerRatings(4));
    }
    
    public static void printRatings(List<int[]> ratings){
        for (int i = 0; i < ratings.size(); i++){
            System.out.println(i + ": " + Arrays.toString(ratings.get(i)));
        }
    }
}
