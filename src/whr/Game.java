package whr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Game {

    int day;
    Collection<Player> whitePlayers;
    Collection<Player> blackPlayers;
    String winner;
    Map<Player, PlayerDay> whiteDays = new HashMap();
    Map<Player, PlayerDay> blackDays = new HashMap();
    
    public Game(Collection<Player> black, Collection<Player> white, String winner, int time_step) { //extras?

        day = time_step;
        whitePlayers = white;
        blackPlayers = black;
        this.winner = winner;
    }
    
    private double getWhiteElo(){
        double ret = 0;
        for (PlayerDay pd : whiteDays.values()){
            ret += pd.getElo();
        }
        return ret / whiteDays.size();
    }
    
    private double getBlackElo(){
        double ret = 0;
        for (PlayerDay pd : blackDays.values()){
            ret += pd.getElo();
        }
        return ret / blackDays.size();
    }
    
    
    private double getWhiteGamma(){
        return Math.exp(getWhiteElo() * Math.log(10)/ 400.0);
    }
    private double getBlackGamma(){
        return Math.exp(getBlackElo() * Math.log(10)/ 400.0);
    }
    //*/
    /*
    private double getWhiteGamma(){
        double ret = 0;
        for (PlayerDay pd : whiteDays.values()){
            ret += pd.getGamma();
        }
        return ret / whiteDays.size();
    }
    
    private double getBlackGamma(){
        double ret = 0;
        for (PlayerDay pd : blackDays.values()){
            ret += pd.getGamma();
        }
        return ret / blackDays.size();
    }
    
    private double getWhiteElo(){
        return Math.log(getWhiteGamma()) * 400 / Math.log(10);
    }
    private double getBlackElo(){
        return Math.log(getBlackGamma()) * 400 / Math.log(10);
    }
//*/
    public double getOpponentsAdjustedGamma(Player player) {

        double opponent_elo;
        double blackElo = getBlackElo();
        double whiteElo = getWhiteElo();
        if ((whitePlayers.contains(player))) {
            opponent_elo = blackElo +(- whiteElo + whiteDays.get(player).getElo())/* / Math.sqrt(whiteDays.size())*/;
        } else if (blackPlayers.contains(player)) {
            opponent_elo = whiteElo +(- blackElo + blackDays.get(player).getElo())/* / Math.sqrt(blackDays.size())*/;
        } else {
            throw new RuntimeException("No opponent for player " + player.id + ", since they're not in this game: #{self.inspect}.");
        }
        double rval = Math.pow(10, (opponent_elo / 400.0));
        if (rval == 0 || Double.isInfinite(rval) || Double.isNaN(rval)) {
            throw new RuntimeException("Gamma ");
        }
        return rval;
    }
    
    public Collection<Player> getPlayerTeammates(Player player){
        if ((whitePlayers.contains(player))) {
            return whitePlayers;
        } else if (blackPlayers.contains(player)) {
            return blackPlayers;
        } else {
            throw new RuntimeException("No opponent for player " + player.id + ", since they're not in this game: #{self.inspect}.");
        }
    }

    public double getWhiteWinProbability() {
        if (whiteDays.isEmpty() || blackDays.isEmpty()){
            whitePlayers.forEach(p -> p.fakeGame(this));
            blackPlayers.forEach(p -> p.fakeGame(this));
        }
        return getWhiteGamma() / (getWhiteGamma() + getBlackGamma());
    }

    public double getBlackWinProbability() {
        return getBlackGamma() / (getBlackGamma() + getWhiteGamma());
    }
}
