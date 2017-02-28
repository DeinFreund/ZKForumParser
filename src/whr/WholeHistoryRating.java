// Implementation of WHR based on original by Pete Schwamb https://github.com/goshrine/whole_history_rating

package whr;

import java.util.*;
import java.util.stream.Collectors;



public class WholeHistoryRating {

    Map<Integer, Player> players;
    List<Game> games;
    double w2; //elo range expand per day

    public WholeHistoryRating(double w2) {
        this.w2 = w2;
        games = new ArrayList();
        players = new TreeMap();
    }
    public Player getPlayerById(Integer id) {
        if (!players.containsKey(id)) {
            players.put(id, new Player(id, w2));
        }
        return players.get(id);
    }

    public List<int[]> getPlayerRatings(Integer id) {
        Player player = getPlayerById(id);
        return player.days.stream().map(d -> new int[]{d.day, (int) Math.round(d.getElo()), (int) Math.round((d.uncertainty * 100))}).collect(Collectors.toList());
    }

    public Game setup_game(Collection<Integer> black, Collection<Integer> white, String winner, int time_step) {

        // Avoid self-played games (no info)
        if (black.equals(white)) {
            throw new RuntimeException("Invalid game (black player == white player)");
        }

        Collection<Player> white_player = white.stream().map(p -> getPlayerById(p)).collect(Collectors.toSet());
        Collection<Player> black_player = black.stream().map(p -> getPlayerById(p)).collect(Collectors.toSet());
        Game game = new Game(black_player, white_player, winner, time_step);
        return game;
    }

    public Game createGame(Collection<Integer> black, Collection<Integer> white, String winner, int time_step) {
        Game game = setup_game(black, white, winner, time_step);
        return addGame(game);
    }

    private Game addGame(Game game) {
        game.whitePlayers.forEach(p -> p.addGame(game));
        game.blackPlayers.forEach(p -> p.addGame(game));

        games.add(game);
        return game;
    }

    public void runIterations(int count) {
        for (int i = 0; i < count; i++) {
            runSingleIteration();
        }
        for (Player p : players.values()) {
            p.updateUncertainty();
        }
    }
    
    public void printStats(){
        double sum = 0;
        int bigger = 0;
        int total = 0;
        double lowest = 0;
        double highest = 0;
        for (Player p : players.values()) {
            if (p.days.size() > 0){
                total ++;
                double elo = p.days.get(p.days.size() - 1).getElo();
                sum += elo;
                if (elo > 0) bigger ++;
                lowest = Math.min(lowest, elo);
                highest = Math.max(highest, elo);
            }
        }
        System.out.println("Lowest elo: " + lowest);
        System.out.println("Highest elo: " + highest);
        System.out.println("sum elo: " + sum);
        System.out.println("Average elo: " + (sum/total));
        System.out.println("Amount > 0: " + bigger);
        System.out.println("Amount < 0: " + (total - bigger));
    }

    private void runSingleIteration() {
        for (Player p : players.values()) {
            p.runOneNewtonIteration();
        }
    }
}
