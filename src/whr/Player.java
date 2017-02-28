package whr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Player {

    int id;
    double anchor_gamma;
    List<PlayerDay> days = new ArrayList();
    double w2;
    final static double MAX_RATING_CHANGE = 5;

    public Player(int id, double w2) {
        this.id = id;
        this.w2 = Math.pow(Math.sqrt(w2) * Math.log(10) / 400, 2);  // Convert from elo^2 to r^2

    }
    
    double[][] __m = new double[100][100];

    public double[][] generateHessian(List<PlayerDay> days, List<Double> sigma2) {

        int n = days.size();
        if (__m.length < n+1){
            __m = new double[n+100][n+100];
        }
        double[][] m = __m;
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                if (row == col) {
                    double prior = 0;
                    if (row < (n - 1)) {
                        prior += -1.0 / sigma2.get(row);
                    }
                    if (row > 0) {
                        prior += -1.0 / sigma2.get(row - 1);
                    }
                    m[row][col] = days.get(row).getLogLikelyhoodSecondDerivative() + prior - 0.001;
                } else if (row == col - 1) {
                    m[row][col] = 1.0 / sigma2.get(row);
                } else if (row == col + 1) {
                    m[row][col] = 1.0 / sigma2.get(col);
                } else {
                    m[row][col] = 0;
                }
            }
        }
        return m;
    }

    public List<Double> generateGradient(List<Double> r, List<PlayerDay> days, List<Double> sigma2) {
        List<Double> g = new ArrayList();
        int n = days.size();
        for (int i = 0; i < days.size(); i++) {
            double prior = 0;
            if (i < (n - 1)) {
                prior += -(r.get(i) - r.get(i + 1)) / sigma2.get(i);
            }
            if (i > 0) {
                prior += -(r.get(i) - r.get(i - 1)) / sigma2.get(i - 1);
            }
            g.add(days.get(i).getLogLikelyhoodFirstDerivative() + prior);
        }
        return g;
    }

    public void runOneNewtonIteration() {
        for (PlayerDay day : days) {
            day.clearGameTermsCache();
        }

        if (days.size() == 1) {
            days.get(0).updateBy1DNewton();
        } else if (days.size() > 1) {
            updateByNDimNewton();
        }
    }

    public List<Double> generateSigma2() {
        List<Double> sigma2 = new ArrayList();
        for (int i = 0; i < days.size() - 1; i++) {
            sigma2.add(Math.abs(days.get(i + 1).day - days.get(i).day) * w2);
        }
        return sigma2;
    }

    private ArrayList<Double> makeList(int size) {
        ArrayList<Double> temp = new ArrayList();
        for (int i = 0; i < size; i++) {
            temp.add(0d);
        }
        return temp;
    }

    public void updateByNDimNewton() {
        List<Double> r = new ArrayList();
        for (PlayerDay d : days) {
            r.add(d.r);
        }

        if (false) {
            System.out.println("name: " + id);
            for (PlayerDay day : days) {
                System.out.println("day[#" + day.day + "] r = " + day.r);
                System.out.println("day[#" + day.day + "] win terms = #" + Arrays.deepToString(day.getWonGameTerms().toArray()) + "");
                System.out.println("day[#" + day.day + "] win games = #" + Arrays.toString(day.wonGames.toArray()) + "");
                System.out.println("day[#" + day.day + "] lose terms = #" + Arrays.deepToString(day.getLostGameTerms().toArray()) + "");
                System.out.println("day[#" + day.day + "] lost games = #" + Arrays.toString(day.lostGames.toArray()) + "");
                System.out.println("day[#" + day.day + "] log(p) = #" + (day.getLogLikelyhood()) + "");
                System.out.println("day[#" + day.day + "] dlp = #" + (day.getLogLikelyhoodFirstDerivative()) + "");
                System.out.println("day[#" + day.day + "] dlp2 = #" + (day.getLogLikelyhoodSecondDerivative()) + "");
            }
        }
        // sigma squared (used in the prior)
        List<Double> sigma2 = generateSigma2();

        double[][] h = generateHessian(days, sigma2);
        List<Double> g = generateGradient(r, days, sigma2);

        int n = r.size();

        List<Double> a = makeList(n);
        List<Double> d = makeList(n);
        List<Double> b = makeList(n);
        d.set(0, h[0][0]);
        b.set(0, h[0][1]);

        for (int i = 1; i < n; i++) {
            a.set(i, h[i][i-1] / d.get(i - 1));
            d.set(i, h[i][i] - a.get(i) * b.get(i - 1));
            b.set(i, h[i][i + 1]);
        }

        List<Double> y = makeList(n);
        y.set(0, g.get(0));
        for (int i = 1; i < n; i++) {
            y.set(i, g.get(i) - a.get(i) * y.get(i - 1));
        }

        List<Double> x = makeList(n);
        x.set(n - 1, y.get(n - 1) / d.get(n - 1));
        for (int i = n - 2; i >= 0; i--) {
            x.set(i, (y.get(i) - b.get(i) * x.get(i + 1)) / d.get(i));
        }


        for (int i = 0; i < n; i++) {
            if (Math.abs(x.get(i)) > _maxChg){
                _maxChg = Math.abs(x.get(i));
                System.out.println("New max change of " + _maxChg + " for player " + id);
            }
        }

        for (int i = 0; i < days.size(); i++) {
            days.get(i).r -=  Math.max(-MAX_RATING_CHANGE,Math.min(MAX_RATING_CHANGE, x.get(i)));
        }

    }    
    static double _maxChg = 3;
    
    double[][] __cov = new double[100][100];

    public double[][] generateCovariance() {
        List<Double> r = new ArrayList();
        for (PlayerDay d : days) {
            r.add(d.r);
        }

        List<Double> sigma2 = generateSigma2();
        double[][] h = generateHessian(days, sigma2);
        List<Double> g = generateGradient(r, days, sigma2);

        int n = r.size();

        List<Double> a = makeList(n);
        List<Double> d = makeList(n);
        List<Double> b = makeList(n);
        d.set(0, h[0][0]);
        b.set(0, h[0][1]);

        for (int i = 1; i < n; i++) {
            a.set(i, h[i][i-1] / d.get(i - 1));
            d.set(i, h[i][i] - a.get(i) * b.get(i - 1));
            b.set(i, h[i][i + 1]);
        }

        List<Double> dp = makeList(n);
        dp.set(n - 1, h[n - 1][ n - 1]);
        List<Double> bp = makeList(n);
        bp.set(n - 1, n >=2 ? h[n - 1][ n - 2] : 0);
        List<Double> ap = makeList(n);
        for (int i = n - 2; i >= 0; i--) {
            ap.set(i, h[i][ i + 1] / dp.get(i + 1));
            dp.set(i, h[i][i] - ap.get(i) * bp.get(i + 1));
            bp.set(i, i > 0 ? h[i][i - 1] : 0);
        }

        List<Double> v = makeList(n);
        for (int i = 0; i < n - 1; i++) {
            v.set(i, dp.get(i + 1) / (b.get(i) * bp.get(i + 1) - d.get(i) * dp.get(i + 1)));
        }
        v.set(n - 1, -1 / d.get(n - 1));

        if (__cov.length < n+1){
            __cov = new double[n+100][n+100];
        }
        double[][] cov = __cov;

        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                if (row == col) {
                    cov[row][col] = v.get(row);
                } else if (row == col - 1) {
                    cov[row][col] =  -1 * a.get(col) * v.get(col);
                } else {
                    cov[row][col] =  0;
                }
            }
        }
        return cov;
    }

    public void updateUncertainty() {
        if (days.size() > 0) {
            double[][] c = generateCovariance();
            for (int i = 0; i < days.size(); i++) {
                days.get(i).uncertainty = c[i][i];
            }
        }
    }

    public void addGame(Game game) {
        if (days.isEmpty() || days.get(days.size() - 1).day != game.day) {
            PlayerDay newPDay = new PlayerDay(this, game.day);
            if (days.isEmpty()) {
                newPDay.isFirstDay = true;
                newPDay.setGamma(1);
                newPDay.uncertainty = 10;
            } else {
                newPDay.setGamma(days.get(days.size() - 1).getGamma());
                newPDay.uncertainty = days.get(days.size() - 1).uncertainty + Math.sqrt(game.day - days.get(days.size() - 1).day) * w2;
            }
            days.add(newPDay);
        }
        if (game.whitePlayers.contains(this)) {
            game.whiteDays.put(this, days.get(days.size() - 1));
        } else {
            game.blackDays.put(this, days.get(days.size() - 1));
        }

        days.get(days.size() - 1).addGame(game);
    }
    
    
    public void fakeGame(Game game) {
        PlayerDay d;
        if (days.isEmpty() || days.get(days.size() - 1).day != game.day) {
            PlayerDay new_pday = new PlayerDay(this, game.day);
            if (days.isEmpty()) {
                new_pday.isFirstDay = true;
                new_pday.setGamma(1);
            } else {
                new_pday.setGamma(days.get(days.size() - 1).getGamma());
            }
            d = (new_pday);
        }else{
            d = days.get(days.size() - 1);
        }
        if (game.whitePlayers.contains(this)) {
            game.whiteDays.put(this, d);
        } else {
            game.blackDays.put(this, d);
        }

        //days.get(days.size() - 1).add_game(game);
    }

}
