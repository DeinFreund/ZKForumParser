/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools \\| Templates
 * and open the template in the editor.
 */
package zkforumparser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author User
 */
public class StatsBattles {

    private static Set<Integer> players = new HashSet();
    private static Set<Integer> clans = new HashSet();
    private static Set<String> maps = new HashSet();
    private static Map<Integer, String> usernames = new HashMap();
    private static String path;

    private static int parseTime(String duration) {
        int ret = 0;
        if (duration.contains("sec")) {
            ret = Integer.valueOf(duration.split(" ")[0]);
        }
        if (duration.contains("min")) {
            ret = Integer.valueOf(duration.split(" ")[0]) * 60;
        }
        if (duration.contains("hour")) {
            ret = Integer.valueOf(duration.split(" ")[0]) * 3600;
        }
        if (duration.contains("day")) {
            ret = Integer.valueOf(duration.split(" ")[0]) * 3600 * 24;
        }
        if (duration.contains("month")) {
            ret = Integer.valueOf(duration.split(" ")[0]) * 3600 * 24 * 30;
        }
        if (duration.contains("year")) {
            ret = Integer.valueOf(duration.split(" ")[0]) * 3600 * 24 * 365;
        }
        return ret;
    }

    private static String getClanName(int id) {
        if (id == 0) {
            return "{redacted}";
        }
        try {
            List<String> html = new ArrayList<>();
            URL oracle = new URL("http://zero-k.info/Clans/Detail/" + id);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                html.add(inputLine);
            }

            in.close();
            for (String line : html) {
                if (line.contains("clan detail")) {
                    return line.substring(line.indexOf("- ") + 2, line.indexOf(" clan detail"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("clanname not found");
        System.exit(1);
        return "";
    }

    private static String getUserName(int id) {
        if (usernames.containsKey(id)) {
            return usernames.get(id);
        }
        if (id == -1) {
            return "-";
        }
        if (id == 0) {
            return "{redacted}";
        }
        try {
            List<String> html = new ArrayList<>();
            URL oracle = new URL("http://zero-k.info/Users/Detail/" + id);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                html.add(inputLine);
            }

            in.close();
            for (String line : html) {
                if (line.contains("user page")) {
                    usernames.put(id, line.trim().split(" ")[0]);
                    try {
                        FileOutputStream fileOut = new FileOutputStream(path);
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);
                        out.writeObject(usernames);
                        out.close();
                        fileOut.close();
                        //command.debug("Saved to " + path);
                    } catch (IOException i) {
                        System.err.println("Saving to " + path + " failed!");
                    }
                    return line.trim().split(" ")[0];
                }
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        return "{Unknown}";
    }

    static class Player {

        int id, clan;

        public Player(String data) {
            this.id = Integer.valueOf(data.split("/")[0]);
            if (data.split("/").length > 1 && data.split("/")[1].length() > 0) {
                this.clan = Integer.valueOf(data.split("/")[1]);
            }
            players.add(id);
            clans.add(clan);
        }

        public String getName() {
            return getUserName(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Player other = (Player) obj;
            if (this.hashCode() != other.hashCode()) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return id * 997 + clan;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    static class Award {

        String name;
        int value;

        public Award(String data) {
            this.name = data.split(":")[0];
            if (data.split(":").length > 1 && data.split(":")[1].length() > 0 && data.split(":")[1].length() < 8) {
                this.value = Integer.valueOf(data.split(":")[1]);
            }
        }
    }

    static class Team {

        int xp;
        List<Player> players = new ArrayList();

        Map<Player, Integer> deathTime = new HashMap();
        Map<Player, Set<Award>> awards = new HashMap();

        public Team(String data) {
            int xp = data.split(";")[0].isEmpty() ? 0 : Integer.valueOf(data.split(";")[0]);
            List<String> players = Arrays.stream(data.split(";"), 1, data.split(";").length).collect(Collectors.toList());
            for (String p : players) {
                Player player = new Player(p);
                this.players.add(player);
                if (p.split("/").length > 2) {
                    this.deathTime.put(player, parseTime(p.split("/")[2]));
                    this.awards.put(player, Arrays.stream(p.split("/"), 3, p.split("/").length).map(Award::new).collect(Collectors.toSet()));
                }
            }
        }
    }

    static class Battle {

        int id, duration, ago, engine, firstCommenter;
        boolean bots, mission;
        String title, host, map, game;
        List<Team> winners, losers, teams;
        Collection<Collection<Integer>> winnerPlayers, loserPlayers;

        public Battle(String line) {
            line += "|blub";
            this.title = line.split("\\|")[1];
            if (line.split("\\|").length > 14) {
                for (int i = 2; i < line.split("\\|").length - 12; i++) {
                    this.title += "|" + line.split("\\|")[i];
                }
                line = line.replace(this.title, "blub");
            }
            this.id = Integer.valueOf(line.split("\\|")[0]);
            this.engine = line.split("\\|")[7].isEmpty() ? 0 : Integer.valueOf(line.split("\\|")[7].split("\\.")[0]);
            this.firstCommenter = line.split("\\|")[12].isEmpty() ? 0 : Integer.valueOf(line.split("\\|")[12]);
            this.duration = parseTime(line.split("\\|")[5]);
            this.ago = parseTime(line.split("\\|")[4]);

            this.bots = Boolean.valueOf(line.split("\\|")[8]);
            this.mission = Boolean.valueOf(line.split("\\|")[9]);
            this.host = line.split("\\|")[2];
            this.map = line.split("\\|")[3];
            this.game = line.split("\\|")[6];
            winners = Arrays.stream(line.split("\\|")[10].split("#")).filter(s -> !s.isEmpty()).map(Team::new).collect(Collectors.toList());
            losers = Arrays.stream(line.split("\\|")[11].split("#")).filter(s -> !s.isEmpty()).map(Team::new).collect(Collectors.toList());
            winnerPlayers = Arrays.stream(line.split("\\|")[10].split("#")).filter(s -> !s.isEmpty()).map(Team::new).map(t -> t.players.stream().map(p -> p.id).collect(Collectors.toSet())).collect(Collectors.toSet());
            loserPlayers = Arrays.stream(line.split("\\|")[11].split("#")).filter(s -> !s.isEmpty()).map(Team::new).map(t -> t.players.stream().map(p -> p.id).collect(Collectors.toSet())).collect(Collectors.toSet());
            teams = Stream.concat(winners.stream(), losers.stream()).collect(Collectors.toList());
            maps.add(this.map);
        }

        public boolean isFunMap() {
            return this.map.toLowerCase().contains("metal") || this.map.toLowerCase().contains("super") || this.map.toLowerCase().contains("long") || this.map.toLowerCase().contains("duck") || this.map.toLowerCase().contains("trol")
                    || this.map.toLowerCase().contains("duke");
        }
    }

    public static void main(String[] args) throws Exception {

        long time = System.currentTimeMillis();

        Map<Integer, Battle> battles = new TreeMap();
        Files.readAllLines(Paths.get("A:\\battles.txt")).stream().map((line) -> new Battle(line)).forEach((b) -> {
            battles.put(b.id, b);
        });

        System.out.println(System.currentTimeMillis() - time);

        try {
            path = "usernames";
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            usernames = (Map<Integer, String>) in.readObject();
            in.close();
            fileIn.close();
            System.out.println("Loaded usernames for " + usernames.size() + " users.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        Map<Integer, Map<String, Integer>> playerMapWins = new HashMap();
        Map<Integer, Map<String, Integer>> playerMapLosses = new HashMap();

        for (Integer p : players) {
            playerMapWins.put(p, maps.stream().collect(Collectors.toMap(m -> m, m -> 0)));
            playerMapLosses.put(p, maps.stream().collect(Collectors.toMap(m -> m, m -> 0)));
        }

        for (Battle b : battles.values()) {
            if (b.bots || b.mission || b.duration < 120) {
                continue;
            }
            if (!b.title.toLowerCase().contains("zero-k: all welcome") && !b.title.toLowerCase().contains("teams")) {
                continue;
            }
            if (!b.map.toLowerCase().contains("folsom") || b.map.toLowerCase().contains("core"))continue;
            for (Team t : b.winners) {
                for (Player p : t.players) {
                    if (!playerMapWins.get(p.id).containsKey(b.map)) {
                        playerMapWins.get(p.id).put(b.map, 0);
                    }
                    playerMapWins.get(p.id).put(b.map, playerMapWins.get(p.id).get(b.map) + 1);
                }
            }
            for (Team t : b.losers) {
                for (Player p : t.players) {
                    if (!playerMapLosses.get(p.id).containsKey(b.map)) {
                        playerMapLosses.get(p.id).put(b.map, 0);
                    }
                    playerMapLosses.get(p.id).put(b.map, playerMapLosses.get(p.id).get(b.map) + 1);
                }
            }
        }

        Map<Double, Pair<Integer, String>> bestMapPlayers = new TreeMap();

        for (Integer p : players) {
            for (String map : maps) {
                int wins = playerMapWins.get(p).get(map);
                int losses = playerMapLosses.get(p).get(map);
                if (wins + losses > 25) {
                    bestMapPlayers.put(-(double) wins / losses, new Pair<Integer, String>(p, map));
                }
            }
        }
        int index = 1;
        Set<String> usedMaps = new HashSet();
        for (Map.Entry<Double, Pair<Integer, String>> entry : bestMapPlayers.entrySet()) {
            if (usedMaps.contains(entry.getValue().getSecond())) {
                continue;
            }
            //usedMaps.add(entry.getValue().getSecond());
            System.out.println(index + ". @" + getUserName(entry.getValue().getFirst()).replaceAll("[\\[\\]]",  "") + " : " + -(Math.round(entry.getKey() * 100) / 100d) + " on " + entry.getValue().getSecond());
            if (index++ >= 50) {
                break;
            }
        }
         
         */
 /*
        Map<Integer, Integer> clanWins = new HashMap();
        Map<Integer, Integer> clanLosses = new HashMap();
        for (Integer c : clans) {
            clanWins.put(c, 0);
            clanLosses.put(c, 0);
        }

        for (Battle b : battles.values()) {
            if (b.bots || b.mission || b.duration < 120) {
                continue;
            }
            if (!b.title.toLowerCase().contains("zero-k: all welcome") && !b.title.toLowerCase().contains("teams")) {
                continue;
            }
            for (Team t : b.winners) {
                Map<Integer, Integer> clanMembers = new HashMap();
                for (Player p : t.players) {
                    if (!clanMembers.containsKey(p.clan)) {
                        clanMembers.put(p.clan, 0);
                    }
                    clanMembers.put(p.clan, clanMembers.get(p.clan) + 1);

                }
                for (int clan : clanMembers.keySet()) {
                    if (clanMembers.get(clan) * 3  >= t.players.size() && clanMembers.get(clan) > 1) {
                        clanWins.put(clan, clanWins.get(clan) + 1);
                        if (clan == 776){
                            System.out.println(b.id);
                        }
                    }
                }
            }
            for (Team t : b.losers) {
                Map<Integer, Integer> clanMembers = new HashMap();
                for (Player p : t.players) {
                    if (!clanMembers.containsKey(p.clan)) {
                        clanMembers.put(p.clan, 0);
                    }
                    clanMembers.put(p.clan, clanMembers.get(p.clan) + 1);

                }
                for (int clan : clanMembers.keySet()) {
                    if (clanMembers.get(clan) * 3 >= t.players.size() && clanMembers.get(clan) > 1) {
                        clanLosses.put(clan, clanLosses.get(clan) + 1);
                    }
                }
            }
        }

        Map<Double, Integer> bestClans = new TreeMap();

        for (Integer c : clans) {
            int wins = clanWins.get(c);
            int losses = clanLosses.get(c);
            if (wins + losses > 25) {
                bestClans.put((double) -wins / losses, c);
            }
        }
        int index = 1;
        for (Map.Entry<Double, Integer> entry : bestClans.entrySet()) {
            //usedMaps.add(entry.getValue().getSecond());
            System.out.println(index + ". @" + getClanName(entry.getValue()).replaceAll("[\\[\\]]", "") + " : " + -(Math.round(entry.getKey() * 100) / 100d));
            if (index++ >= 50) {
                break;
            }
        }
         */
 /*
        Map<Integer, Integer> winComments = new HashMap();
        Map<Integer, Integer> lostComments = new HashMap();
        for (Integer p : players) {
            winComments.put(p, 0);
            lostComments.put(p, 0);
        }

        for (Battle b : battles.values()) {
            
            for (Team t : b.winners) {
                for (Player p : t.players) {
                    if (p.id == b.firstCommenter){
                        winComments.put(p.id, winComments.get(p.id) + 1);
                    }
                }
            }
            for (Team t : b.losers) {
                for (Player p : t.players) {
                    if (p.id == b.firstCommenter){
                        lostComments.put(p.id, lostComments.get(p.id) + 1);
                    }
                }
            }
        }

        Map<Double, Integer> mostBragging = new TreeMap();

        for (Integer p : players) {
            int wins = winComments.get(p);
            int losses = lostComments.get(p);
            if (wins + losses > 25) {
                mostBragging.put((double) -wins / losses, p);
            }
        }
        int index = 1;
        for (Map.Entry<Double, Integer> entry : mostBragging.entrySet()) {
            //usedMaps.add(entry.getValue().getSecond());
            System.out.println(index + ". @" + getUserName(entry.getValue()).replaceAll("[\\[\\]]", "") + " : " + -(Math.round(entry.getKey() * 100) / 100d));
            if (index++ >= 50) {
                break;
            }
        }
         */
 /*
        Map<Integer, Integer> playerBattles = new HashMap();
        for (Integer p : players) {
            playerBattles.put(p, 0);
        }
        final int minSize = 2;
        final int maxSize = 6;
        for (Battle b : battles.values()) {
            if (b.bots || b.mission || b.duration < 120 || b.winners.isEmpty() || b.losers.isEmpty()
                    || b.winners.get(0).players.size() < minSize || b.losers.get(0).players.size() < minSize
                    || b.winners.get(0).players.size() > maxSize || b.losers.get(0).players.size() > maxSize
                    || b.isFunMap()) {
                continue;
            }
            for (Team t : b.winners) {
                for (Player p : t.players) {
                    playerBattles.put(p.id, playerBattles.get(p.id) + 1);
                }
            }
            for (Team t : b.losers) {
                for (Player p : t.players) {
                    playerBattles.put(p.id, playerBattles.get(p.id) + 1);
                }
            }
        }
        
        Map<Double, Integer> topPlayers = new TreeMap();
        for (Map.Entry<Integer, Integer> entry : playerBattles.entrySet()) {
            topPlayers.put(-entry.getValue() + Math.random(), entry.getKey());
        }
        Set<Integer> selectedPlayers = new HashSet();

        for (Integer player : topPlayers.values()) {
            if (selectedPlayers.size() >= 2000) {
                break;
            }
            selectedPlayers.add(player);
        }

        Map<Pair<Integer, Integer>, Integer> pairWins = new HashMap();
        Map<Pair<Integer, Integer>, Integer> pairLosses = new HashMap();
        Map<Integer, Integer> playerWins = new HashMap();
        Map<Integer, Integer> playerLosses = new HashMap();
        for (Integer p : selectedPlayers) {
            playerWins.put(p, 0);
            playerLosses.put(p, 0);
            for (Integer p2 : selectedPlayers) {
                if (p2 > p) {
                    continue;
                }
                pairWins.put(new Pair(p, p2), 0);
                pairLosses.put(new Pair(p, p2), 0);
            }
        }

        for (Battle b : battles.values()) {
            if (b.bots || b.mission || b.duration < 120 || b.winners.isEmpty() || b.losers.isEmpty()
                    || b.winners.get(0).players.size() < minSize || b.losers.get(0).players.size() < minSize
                    || b.winners.get(0).players.size() > maxSize || b.losers.get(0).players.size() > maxSize
                    || b.isFunMap()) {
                continue;
            }
            for (Team t : b.winners) {
                for (Player p : t.players) {
                    if (selectedPlayers.contains(p.id)) {
                        playerWins.put(p.id, playerWins.get(p.id) + 1);
                    }
                    for (Player p2 : t.players) {
                        if (p2.id < p.id && selectedPlayers.contains(p.id) && selectedPlayers.contains(p2.id)) {
                            //if (p.id == 348500 && p2.id == 169779) System.out.print("@B" + b.id + " ");
                            pairWins.put(new Pair(p.id, p2.id), pairWins.get(new Pair(p.id, p2.id)) + 1);
                        }
                    }
                }
            }
            for (Team t : b.losers) {
                for (Player p : t.players) {
                    if (selectedPlayers.contains(p.id)) {
                        playerLosses.put(p.id, playerLosses.get(p.id) + 1);
                    }
                    for (Player p2 : t.players) {
                        if (p2.id < p.id && selectedPlayers.contains(p.id) && selectedPlayers.contains(p2.id)) {
                            if (p.id == 348500 && p2.id == 169779) System.out.print("@B" + b.id + " ");
                            pairLosses.put(new Pair(p.id, p2.id), pairLosses.get(new Pair(p.id, p2.id)) + 1);
                        }
                    }
                }
            }
        }

        Map<Double, Pair<Integer, Integer>> mostSynergy = new TreeMap();
        Map<Integer, Pair<Integer, Double>> bestColleague1 = new TreeMap();
        Map<Integer, Pair<Integer, Double>> bestColleague2 = new TreeMap();
        Map<Integer, Pair<Integer, Double>> bestColleague3 = new TreeMap();

        for (Pair<Integer, Integer> p : pairWins.keySet()) {

            int wins = pairWins.get(p);
            int losses = pairLosses.get(p);
            if (wins + losses >= 0) {
                double individualWR = playerWins.get(p.getFirst()) * playerWins.get(p.getSecond())
                        / (double) (playerLosses.get(p.getFirst()) * playerLosses.get(p.getSecond()));
                double synergy = (double) wins / losses;
                mostSynergy.put(-synergy + Math.random() / 10000, p);

                if ((!bestColleague1.containsKey(p.getFirst()) || bestColleague1.get(p.getFirst()).getSecond() < synergy)
                        && wins + losses >= 20 * Math.sqrt((double) playerBattles.get(p.getFirst()) / playerBattles.get(232028))) {
                    bestColleague1.put(p.getFirst(), new Pair(p.getSecond(), synergy));
                }
                if ((!bestColleague1.containsKey(p.getSecond()) || bestColleague1.get(p.getSecond()).getSecond() < synergy)
                        && wins + losses >= 20 * Math.sqrt((double) playerBattles.get(p.getSecond()) / playerBattles.get(232028))) {
                    bestColleague1.put(p.getSecond(), new Pair(p.getFirst(), synergy));
                }
                if ((!bestColleague2.containsKey(p.getFirst()) || bestColleague2.get(p.getFirst()).getSecond() < synergy)
                        && wins + losses >= 40 * Math.sqrt((double) playerBattles.get(p.getFirst()) / playerBattles.get(232028))) {
                    bestColleague2.put(p.getFirst(), new Pair(p.getSecond(), synergy));
                }
                if ((!bestColleague2.containsKey(p.getSecond()) || bestColleague2.get(p.getSecond()).getSecond() < synergy)
                        && wins + losses >= 40 * Math.sqrt((double) playerBattles.get(p.getSecond()) / playerBattles.get(232028))) {
                    bestColleague2.put(p.getSecond(), new Pair(p.getFirst(), synergy));
                }
                if ((!bestColleague3.containsKey(p.getFirst()) || bestColleague3.get(p.getFirst()).getSecond() < synergy)
                        && wins + losses >= 80 * Math.sqrt((double) playerBattles.get(p.getFirst()) / playerBattles.get(232028))) {
                    bestColleague3.put(p.getFirst(), new Pair(p.getSecond(), synergy));
                }
                if ((!bestColleague3.containsKey(p.getSecond()) || bestColleague3.get(p.getSecond()).getSecond() < synergy)
                        && wins + losses >= 80 * Math.sqrt((double) playerBattles.get(p.getSecond()) / playerBattles.get(232028))) {
                    bestColleague3.put(p.getSecond(), new Pair(p.getFirst(), synergy));
                }
            }
        }
        
        for (Integer player : topPlayers.values()) {
            if (!bestColleague1.containsKey(player))bestColleague1.put(player, new Pair(-1,0));
            if (!bestColleague2.containsKey(player))bestColleague2.put(player, new Pair(-1,0));
            if (!bestColleague3.containsKey(player))bestColleague3.put(player, new Pair(-1,0));
        }
        
        int index = 1;
        System.out.println(mostSynergy.size() + " entries");

        String rplc = "";
        
        for (Integer player : topPlayers.values()) {
            //player = 169779;
            System.out.println("" + getUserName(player).replaceAll(rplc, "") + ": @"
                    + getUserName(bestColleague1.get(player).getFirst()).replaceAll(rplc, "") + " @"
                    + getUserName(bestColleague2.get(player).getFirst()).replaceAll(rplc, "") + " @"
                    + getUserName(bestColleague3.get(player).getFirst()).replaceAll(rplc, "") + "");
                //    + (Math.round(bestColleague1.get(player).getSecond() * 100) / 100d) + ")");
            if (index == 100){
                
        rplc = "[\\[\\]]";
            }
            if (index++ >= 300) {
                break;
            }
        }//
        for (Map.Entry<Double, Pair<Integer, Integer>> entry : mostSynergy.entrySet()) {
            if (entry.getValue().getFirst() != 169779 && entry.getValue().getSecond() != 169779) {
                continue;
            }
            System.out.println(index + ". @" + getUserName(entry.getValue().getFirst()).replaceAll(rplc, "") + " <-> @"
                    + getUserName(entry.getValue().getSecond()).replaceAll(rplc, "") + " : "
                    + -(Math.round(entry.getKey() * 100) / 100d));
            //System.out.println( entry.getValue().getFirst() +  " | " + entry.getValue().getSecond());
            if (index++ >= 50) {
                break;
            }
        }
         //*/
        Map<Integer, Integer> smallTeamsWins = new HashMap();
        Map<Integer, Integer> smallTeamsLosses = new HashMap();
        Map<Integer, Integer> bigTeamsWins = new HashMap();
        Map<Integer, Integer> bigTeamsLosses = new HashMap();
        Map<Integer, Integer> ffaWins = new HashMap();
        Map<Integer, Integer> ffaLosses = new HashMap();

        for (Integer p : players) {
            smallTeamsWins.put(p, 0);
            smallTeamsLosses.put(p, 0);
            bigTeamsWins.put(p, 0);
            bigTeamsLosses.put(p, 0);
            ffaWins.put(p, 0);
            ffaLosses.put(p, 0);
        }

        Set<Integer> trackedPlayers = new HashSet();
        //fffa
//        trackedPlayers.add(232028);
//        trackedPlayers.add(343713);
//        trackedPlayers.add(43981);
//        trackedPlayers.add(77714);
//        trackedPlayers.add(6079);
//        trackedPlayers.add(185685);
//        trackedPlayers.add(1165);
//        trackedPlayers.add(228156);
//        trackedPlayers.add(139663);
//        trackedPlayers.add(224173);
        //metal
//        trackedPlayers.add(204213);
//        trackedPlayers.add(357426);
        // 1v1
//        trackedPlayers.add(169802);
//        trackedPlayers.add(139663);
//        trackedPlayers.add(218372);
//        trackedPlayers.add(15114);
//        trackedPlayers.add(85949);
//        trackedPlayers.add(161294);
//        trackedPlayers.add(251232);
        trackedPlayers.add(139663);
        trackedPlayers.add(262575);
        trackedPlayers.add(224173);
        trackedPlayers.add(6079);
        trackedPlayers.add(88618);
        trackedPlayers.add(9058);
        trackedPlayers.add(185685);
        trackedPlayers.add(232028);
        final Map<RatingSystem, String> ratingNames = new HashMap();
        final Map<RatingSystem, Double> ratingScores = new HashMap();
        ratingNames.put(new ELO(), "ZK Elo");
        ratingNames.put(new TeamstrengthComs(), "Teamstrength with Coms");
        ratingNames.put(new Teamstrength(), "Teamstrength without Coms");
        ratingNames.put(new DummyRating(), "Always 0.5");
        int ratingMaxScore = 0;
        for (RatingSystem rs : ratingNames.keySet()) {
            rs.init(players);
            ratingScores.put(rs, 0d);
        }
        ELO ffaRating = new ELO();
        ffaRating.init(players);
        ELO teamsRating = new ELO();
        teamsRating.init(players);
        ELO metalRating = new ELO();
        metalRating.init(players);
        ELO duelRating = new ELO();
        duelRating.init(players);
        ELO smallTeamsRating = new ELO();
        smallTeamsRating.init(players);
        ELO bigTeamsRating = new ELO();
        bigTeamsRating.init(players);
        Map<Integer, Double> trackedWins = new HashMap();
        for (Integer p : trackedPlayers) {
            System.out.print(";" + getUserName(p));
            trackedWins.put(p, 1d);
        }
        System.out.println();
        final double conv = 0.99;

        int ffa = 0;
        for (Battle b : battles.values()) {
            if (!(b.bots || b.mission || b.duration < 120 || b.winners.size() != 1 || b.losers.size() != 1
                    || b.winners.get(0).players.size() < 1 || b.losers.get(0).players.size() < 1
                    || b.winners.get(0).players.size() > 1 || b.losers.get(0).players.size() > 1)
                    && !b.isFunMap()) {
                duelRating.evaluateResult(b.winnerPlayers, b.loserPlayers);

            }
            if (!(b.bots || b.mission || b.duration < 120 || b.winners.isEmpty() || b.losers.isEmpty())
                    && b.map.toLowerCase().contains("mini") && b.map.toLowerCase().contains("wide")) {
                metalRating.evaluateResult(b.winnerPlayers, b.loserPlayers);

            }
            /*
            if (!(b.bots || b.mission || b.duration < 120 || b.winners.isEmpty() || b.losers.isEmpty()
                    || b.losers.size() < 2) && !b.isFunMap()) {*/
            
            if (!(b.bots || b.mission || b.duration < 120 || b.winners.size() != 1 || b.losers.size() != 1
                    || b.winners.get(0).players.size() < 8 || b.losers.get(0).players.size() < 8
                    || b.winners.get(0).players.size() > 16 || b.losers.get(0).players.size() > 16
                    || b.winners.get(0).players.size() !=  b.losers.get(0).players.size())
                    && !b.isFunMap()) {

                ratingMaxScore += b.teams.size();
                for (RatingSystem rs : ratingNames.keySet()) {
                    List<Collection<Integer>> players = new ArrayList();
                    for (int i = 0; i < b.teams.size(); i++) {
                        players.add(new ArrayList());
                        for (Player p : b.teams.get(i).players) {
                            players.get(players.size() - 1).add(p.id);
                        }
                    }
                    //List<Double> pred = rs.predictResult(b.teams.stream().map(t -> t.players.stream().map(p -> p.id).collect(Collectors.toList())).collect(Collectors.toList()));
                    List<Double> pred = rs.predictResult(players);

                    double score = ratingScores.get(rs);
                    for (int i = 0; i < b.teams.size(); i++) {
                        score += b.winners.contains(b.teams.get(i)) ? (1 + Math.log(pred.get(i)) / Math.log(2)) : (1 + Math.log(1 - pred.get(i)) / Math.log(2));
                    }
                    ratingScores.put(rs, score);

                    rs.evaluateResult(b.winnerPlayers, b.loserPlayers);
                }
                final int norm = ratingMaxScore;
                //ratingNames.keySet().forEach(rs -> System.out.println(ratingNames.get(rs) + ": " + (ratingScores.get(rs) / norm)));
                /*smallTeamsRating.evaluateResult(b.winnerPlayers, b.loserPlayers);
                teamsRating.evaluateResult(b.winnerPlayers, b.loserPlayers);
                Set<Integer> changed = new HashSet();
                for (Team t : b.winners) {
                    for (Player p : t.players) {
                        //if (p.id == 232028) System.out.println("Won " + b.id + " against " + b.winners.get(0).players.get(0).getName());
                        ffaWins.put(p.id, ffaWins.get(p.id) + b.losers.size());
                        if (trackedPlayers.contains(p.id)) {
                            trackedWins.put(p.id, trackedWins.get(p.id) * conv + (1 - conv) * (b.losers.size() + 1));
                            changed.add(p.id);
                        }
                    }
                }
                for (Team t : b.losers) {
                    for (Player p : t.players) {
                        //if (p.id == 232028) System.out.println("Lost " + b.id + " against " + b.winners.get(0).players.get(0).getName());
                        ffaLosses.put(p.id, ffaLosses.get(p.id) + 1);
                        if (trackedPlayers.contains(p.id)) {
                            trackedWins.put(p.id, trackedWins.get(p.id) * conv);
                            changed.add(p.id);
                        }
                    }
                }
                System.out.print(ffa++);
                for (Integer p : trackedPlayers) {
                    System.out.print(";" + ((changed.contains(p)) ? smallTeamsRating.getRating(p) : ""));
                } 
                System.out.println();*/
            }
            if (!(b.bots || b.mission || b.duration < 120 || b.winners.isEmpty() || b.losers.isEmpty()
                    || b.winners.get(0).players.size() < 5 || b.losers.get(0).players.size() < 5
                    || b.winners.get(0).players.size() > 55 || b.losers.get(0).players.size() > 55)
                    && !b.isFunMap()) {

                bigTeamsRating.evaluateResult(b.winnerPlayers, b.loserPlayers);
                teamsRating.evaluateResult(b.winnerPlayers, b.loserPlayers);
                for (Team t : b.winners) {
                    for (Player p : t.players) {
                        bigTeamsWins.put(p.id, bigTeamsWins.get(p.id) + 1);
                    }
                }
                for (Team t : b.losers) {
                    for (Player p : t.players) {
                        bigTeamsLosses.put(p.id, bigTeamsLosses.get(p.id) + 1);
                    }
                }
            }
            if (!(b.bots || b.mission || b.duration < 120 || b.winners.isEmpty() || b.losers.isEmpty()
                    || b.losers.size() < 2) && !b.isFunMap()) {

                ffaRating.evaluateResult(b.winnerPlayers, b.loserPlayers);
                teamsRating.evaluateResult(b.winnerPlayers, b.loserPlayers);

            }
        }
        final int norm = ratingMaxScore;
        ratingNames.keySet().forEach(rs -> System.out.println(ratingNames.get(rs) + ": " + (ratingScores.get(rs) / norm)));

        System.exit(0);

        Map<Double, Integer> bestSmallTeams = new TreeMap();
        Map<Double, Integer> bestBigTeams = new TreeMap();
        Map<Double, Integer> bestFFA = new TreeMap();
        Map<Double, Integer> bestMetal = new TreeMap();

        for (Integer p : smallTeamsWins.keySet()) {
            double elo = 0;
            for (RatingSystem rs : ratingNames.keySet()) {
                if (ratingNames.get(rs).contains("ZK")) {
                    elo = ((ELO) rs).getRating(p);
                }
            }
            bestSmallTeams.put(-elo + Math.random() / 10000, p);
        }
        for (Integer p : bigTeamsWins.keySet()) {
            double elo = bigTeamsRating.getRating(p);
            bestBigTeams.put(-elo + Math.random() / 10000, p);
        }
        for (Integer p : ffaWins.keySet()) {
            double elo = ffaRating.getRating(p);
            bestFFA.put(-elo + Math.random() / 10000, p);
        }
        for (Integer p : ffaWins.keySet()) {
            double elo = metalRating.getRating(p);
            bestMetal.put(-elo + Math.random() / 10000, p);
        }

        System.out.println(Arrays.toString(metalRating.predictResult(Arrays.asList(new Collection[]{Arrays.asList(new Integer[]{234640, 42434}), Arrays.asList(new Integer[]{134367})})).toArray()));

        int place = 1;
        String replace = "";
        for (Map.Entry<Double, Integer> entry : bestSmallTeams.entrySet()) {
            System.out.println(place + ". @" + getUserName(entry.getValue()).replaceAll(replace, "") + " : "
                    + -(Math.round(entry.getKey() * 10) / 10d) + " " + entry.getValue());
            if (place >= 10) {
                replace = "[\\[\\]]";
            }
            if (place++ >= 50) {
                break;
            }
        }
        place = 1;
        replace = "";
        for (Map.Entry<Double, Integer> entry : bestBigTeams.entrySet()) {
            System.out.println(place + ". @" + getUserName(entry.getValue()).replaceAll(replace, "") + " : "
                    + -(Math.round(entry.getKey() * 10) / 10d));
            if (place >= 10) {
                replace = "[\\[\\]]";
            }
            if (place++ >= 50) {
                break;
            }
        }
        place = 1;
        replace = "";
        for (Map.Entry<Double, Integer> entry : bestFFA.entrySet()) {
            if (Math.abs(-entry.getKey() - 1500d) < 0.1d) {
                continue;
            }
            System.out.println(place + ". @" + getUserName(entry.getValue()).replaceAll(replace, "") + " : "
                    + -(Math.round(entry.getKey() * 10) / 10d));
            if (place >= 10) {
                replace = "[\\[\\]]";
            }
            if (place++ >= 50) {
                break;
            }
        }
        place = 1;
        replace = "";
        for (Map.Entry<Double, Integer> entry : bestMetal.entrySet()) {
            System.out.println(place + ". @" + getUserName(entry.getValue()).replaceAll(replace, "") + " : "
                    + -(Math.round(entry.getKey() * 10) / 10d));
            if (place >= 10) {
                replace = "[\\[\\]]";
            }
            if (place++ >= 50) {
                break;
            }
        }
        //*/

        Map<Integer, Integer> playerBattles = new HashMap();
        for (Integer p : players) {
            playerBattles.put(p, 0);
        }
        final int minSize = 2;
        final int maxSize = 4;
        for (Battle b : battles.values()) {
            if (b.bots || b.mission || b.duration < 120 || b.winners.isEmpty() || b.losers.isEmpty()
                    || b.winners.get(0).players.size() < minSize || b.losers.get(0).players.size() < minSize
                    || b.winners.get(0).players.size() > maxSize || b.losers.get(0).players.size() > maxSize) {
                continue;
            }
            for (Team t : b.winners) {
                for (Player p : t.players) {
                    playerBattles.put(p.id, playerBattles.get(p.id) + 1);
                }
            }
            for (Team t : b.losers) {
                for (Player p : t.players) {
                    playerBattles.put(p.id, playerBattles.get(p.id) + 1);
                }
            }
        }

        Map<Double, Integer> topPlayers = new TreeMap();
        for (Map.Entry<Integer, Integer> entry : playerBattles.entrySet()) {
            topPlayers.put(-entry.getValue() + Math.random(), entry.getKey());
        }
        Set<Integer> selectedPlayers = new HashSet();

        for (Integer player : topPlayers.values()) {
            if (selectedPlayers.size() >= 3000) {
                break;
            }
            selectedPlayers.add(player);
        }

        Map<Pair<Integer, Integer>, Integer> pairWins = new HashMap();
        Map<Pair<Integer, Integer>, Integer> pairLosses = new HashMap();
        Map<Integer, Integer> playerWins = new HashMap();
        Map<Integer, Integer> playerLosses = new HashMap();
        for (Integer p : selectedPlayers) {
            playerWins.put(p, 0);
            playerLosses.put(p, 0);
            for (Integer p2 : selectedPlayers) {
                pairWins.put(new Pair(p, p2), 0);
                pairLosses.put(new Pair(p, p2), 0);
            }
        }

        for (Battle b : battles.values()) {
            if (b.bots || b.mission || b.duration < 120 || b.winners.isEmpty() || b.losers.isEmpty()
                    || b.winners.get(0).players.size() < minSize || b.losers.get(0).players.size() < minSize
                    || b.winners.get(0).players.size() > maxSize || b.losers.get(0).players.size() > maxSize) {
                continue;
            }
            for (Team t : b.winners) {
                for (Player p : t.players) {
                    if (selectedPlayers.contains(p.id)) {
                        playerWins.put(p.id, playerWins.get(p.id) + 1);
                    }
                    for (Team t2 : b.losers) {
                        for (Player p2 : t2.players) {
                            if (selectedPlayers.contains(p.id) && selectedPlayers.contains(p2.id)) {
                                //if (p.id == 185685 && p2.id == 134367) System.out.print("@B" + b.id + "\n");
                                pairWins.put(new Pair(p.id, p2.id), pairWins.get(new Pair(p.id, p2.id)) + 1);
                            }
                        }
                    }
                }
            }
            for (Team t : b.losers) {
                for (Player p : t.players) {
                    if (selectedPlayers.contains(p.id)) {
                        playerLosses.put(p.id, playerLosses.get(p.id) + 1);
                    }
                    for (Team t2 : b.winners) {
                        for (Player p2 : t2.players) {
                            if (selectedPlayers.contains(p.id) && selectedPlayers.contains(p2.id)) {
                                if (p.id == 185685 && p2.id == 134367) {
                                    System.out.print("@B" + b.id + "\n");
                                }
                                pairLosses.put(new Pair(p.id, p2.id), pairLosses.get(new Pair(p.id, p2.id)) + 1);
                            }
                        }
                    }
                }
            }
        }

        Map<Double, Pair<Integer, Integer>> mostSynergy = new TreeMap();
        Map<Integer, Pair<Integer, Double>> bestColleague1 = new TreeMap();
        Map<Integer, Pair<Integer, Double>> bestColleague2 = new TreeMap();
        Map<Integer, Pair<Integer, Double>> bestColleague3 = new TreeMap();

        for (Pair<Integer, Integer> p : pairWins.keySet()) {

            int wins = pairWins.get(p);
            int losses = pairLosses.get(p);
            if (wins + losses >= 100) {
                double individualWR = playerWins.get(p.getFirst()) * playerWins.get(p.getSecond())
                        / (double) (playerLosses.get(p.getFirst()) * playerLosses.get(p.getSecond()));
                double synergy = Math.min((double) wins / losses, 1e5 * (Math.random() + 1));
                mostSynergy.put(-synergy + Math.random() / 10000, p);

                /*
                if ((!bestColleague1.containsKey(p.getFirst()) || bestColleague1.get(p.getFirst()).getSecond() < synergy)
                        && wins + losses >= 20 * Math.sqrt((double) playerBattles.get(p.getFirst()) / playerBattles.get(232028))) {
                    bestColleague1.put(p.getFirst(), new Pair(p.getSecond(), synergy));
                }
                if ((!bestColleague1.containsKey(p.getSecond()) || bestColleague1.get(p.getSecond()).getSecond() < synergy)
                        && wins + losses >= 20 * Math.sqrt((double) playerBattles.get(p.getSecond()) / playerBattles.get(232028))) {
                    bestColleague1.put(p.getSecond(), new Pair(p.getFirst(), synergy));
                }
                if ((!bestColleague2.containsKey(p.getFirst()) || bestColleague2.get(p.getFirst()).getSecond() < synergy)
                        && wins + losses >= 40 * Math.sqrt((double) playerBattles.get(p.getFirst()) / playerBattles.get(232028))) {
                    bestColleague2.put(p.getFirst(), new Pair(p.getSecond(), synergy));
                }
                if ((!bestColleague2.containsKey(p.getSecond()) || bestColleague2.get(p.getSecond()).getSecond() < synergy)
                        && wins + losses >= 40 * Math.sqrt((double) playerBattles.get(p.getSecond()) / playerBattles.get(232028))) {
                    bestColleague2.put(p.getSecond(), new Pair(p.getFirst(), synergy));
                }
                if ((!bestColleague3.containsKey(p.getFirst()) || bestColleague3.get(p.getFirst()).getSecond() < synergy)
                        && wins + losses >= 80 * Math.sqrt((double) playerBattles.get(p.getFirst()) / playerBattles.get(232028))) {
                    bestColleague3.put(p.getFirst(), new Pair(p.getSecond(), synergy));
                }
                if ((!bestColleague3.containsKey(p.getSecond()) || bestColleague3.get(p.getSecond()).getSecond() < synergy)
                        && wins + losses >= 80 * Math.sqrt((double) playerBattles.get(p.getSecond()) / playerBattles.get(232028))) {
                    bestColleague3.put(p.getSecond(), new Pair(p.getFirst(), synergy));
                }//*/
            }
        }

        for (Integer player : topPlayers.values()) {
            if (!bestColleague1.containsKey(player)) {
                bestColleague1.put(player, new Pair(-1, 0));
            }
            if (!bestColleague2.containsKey(player)) {
                bestColleague2.put(player, new Pair(-1, 0));
            }
            if (!bestColleague3.containsKey(player)) {
                bestColleague3.put(player, new Pair(-1, 0));
            }
        }

        int index = 1;
        System.out.println(mostSynergy.size() + " entries");

        String rplc = "";
        //rplc = "[\\[\\]]";
        /*
        for (Integer player : topPlayers.values()) {
            System.out.println("" + getUserName(player).replaceAll(rplc, "") + ": @"
                    + getUserName(bestColleague1.get(player).getFirst()).replaceAll(rplc, "") + " @"
                    + getUserName(bestColleague2.get(player).getFirst()).replaceAll(rplc, "") + " @"
                    + getUserName(bestColleague3.get(player).getFirst()).replaceAll(rplc, "") + "");
                //    + (Math.round(bestColleague1.get(player).getSecond() * 100) / 100d) + ")");
            if (index++ >= 100) {
                break;
            }
        }//*/
        for (Map.Entry<Double, Pair<Integer, Integer>> entry : mostSynergy.entrySet()) {
            if (entry.getValue().getFirst() != 360610 && entry.getValue().getSecond() != 360610) {
                //continue;
            }
            System.out.println(index + ". @" + getUserName(entry.getValue().getFirst()).replaceAll(rplc, "") + " > @"
                    + getUserName(entry.getValue().getSecond()).replaceAll(rplc, "") + " : "
                    + -(Math.round(entry.getKey() * 100) / 100d));
            //System.out.println( entry.getValue().getFirst() +  " | " + entry.getValue().getSecond());
            if (index >= 10) {
                rplc = "[\\[\\]]";
            }
            if (index++ >= 50) {
                break;
            }
        }
    }

}
