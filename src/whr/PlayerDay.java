package whr;

import java.util.ArrayList;
import java.util.List;

public class PlayerDay {

    List<Game> wonGames, lostGames;
    String name;
    int day;
    Player player;
    double r;
    boolean isFirstDay;
    double uncertainty;

    public PlayerDay(Player player, int day) {
        this.day = day;
        this.player = player;
        isFirstDay = false;
        this.wonGames = new ArrayList();
        this.lostGames = new ArrayList();
    }

    public void setGamma(double gamma) {
        r = Math.log(gamma);
    }

    public double getGamma() {
        return Math.exp(r);
    }

    public void setElo(double elo) {
        r = elo * (Math.log(10) / 400.0);
    }

    public double getElo() {
        return (r * 400.0) / (Math.log(10));
    }

    List<double[]> won_game_terms, lost_game_terms;

    public void clearGameTermsCache() {
        won_game_terms = null;
        lost_game_terms = null;
    }

    public List<double[]> getWonGameTerms() {
        if (won_game_terms == null) {
            won_game_terms = new ArrayList();
            for (Game g : wonGames) {
                double other_gamma = g.getOpponentsAdjustedGamma(player);
                won_game_terms.add(new double[]{1.0, 0.0, 1.0, other_gamma, 1d / g.getPlayerTeammates(player).size()/*g.getPlayerWeight(player)*/ });
            }
            if (isFirstDay) {
                won_game_terms.add(new double[]{1, 0, 1, 1, 1});
            }
        }
        return won_game_terms;
    }

    public List<double[]> getLostGameTerms() {
        if (lost_game_terms == null) {
            lost_game_terms = new ArrayList();
            for (Game g : lostGames) {
                double other_gamma = g.getOpponentsAdjustedGamma(player);
                lost_game_terms.add(new double[]{0.0, other_gamma, 1.0, other_gamma, 1d / g.getPlayerTeammates(player).size()/*g.getPlayerWeight(player)*/ });
            }
            if (isFirstDay) {
                lost_game_terms.add(new double[]{0, 1, 1, 1, 1});

            }
        }
        return lost_game_terms;
    }

    public double getLogLikelyhoodSecondDerivative() {
        double sum = 0.0;
        double gamma = getGamma();
        for (double[] terms : getWonGameTerms()) {
            sum += terms[4] * (terms[2] * terms[3]) / Math.pow(terms[2] * gamma + terms[3], 2);
        }
        for (double[] terms : getLostGameTerms()) {
            sum += terms[4] * (terms[2] * terms[3]) / Math.pow(terms[2] * gamma + terms[3], 2);
        }
        return -1 * gamma * sum;
    }

    public double getLogLikelyhoodFirstDerivative() {
        double tally = 0.0;
        double gamma = getGamma();
        double size = 0;
        for (double[] terms : getWonGameTerms()) {
            tally += terms[4] * terms[2] / (terms[2] * gamma + terms[3]);
            size += terms[4];
        }
        for (double[] terms : getLostGameTerms()) {
            tally += terms[4] * terms[2] / (terms[2] * gamma + terms[3]);
        }
        return size - gamma * tally;
    }

    public double getLogLikelyhood() {
        double tally = 0.0;
        double gamma = getGamma();
        for (double[] terms : getWonGameTerms()) {
            tally += terms[4] * Math.log(terms[0] * gamma);
            tally -= terms[4] * Math.log(terms[2] * gamma + terms[3]);
        }
        for (double[] terms : getLostGameTerms()) {
            tally += terms[4] * Math.log(terms[1]);
            tally -= terms[4] * Math.log(terms[2] * gamma + terms[3]);
        }
        return tally;
    }

    public void addGame(Game game) {
        if ((game.winner.equalsIgnoreCase("W") && game.whitePlayers.contains(player))
                || (game.winner.equalsIgnoreCase("B") && game.blackPlayers.contains(player))) {
            wonGames.add(game);
        } else if ((game.winner.equalsIgnoreCase("B") && game.whitePlayers.contains(player))
                || (game.winner.equalsIgnoreCase("W") && game.blackPlayers.contains(player))) {
            lostGames.add(game);
        } else {
            throw new RuntimeException();
        }

    }

    public void updateBy1DNewton() {
        double dr = (getLogLikelyhoodFirstDerivative() / getLogLikelyhoodSecondDerivative());
        double new_r = r - dr;
        r = new_r;
    }

}
