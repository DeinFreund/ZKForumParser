/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zkforumparser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author User
 */
public class ZKForumParser {

    public static List<String> getHTML(String url) throws Exception {
        List<String> html = new ArrayList<>();
        URL oracle = new URL(url);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(oracle.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            html.add(inputLine);
        }

        in.close();
        return html;
    }

    public static String parsePost(int postId) throws Exception {
        int correct = -1;
        int index = 0;
        int start = -1;
        int depth = 0;
        int upvotes = 0;
        int downvotes = 0;
        long date = 0;
        String username = "";
        String post = "";
        String upvoters = "";
        String downvoters = "";
        try {
            List<String> html = getHTML("http://zero-k.info/Forum/Post/" + postId);

            for (String line : html) {
                if (line.trim().equalsIgnoreCase("<td class=\"forumPostHead\" valign=\"top\">")) {
                    start = index;
                }

                if (line.trim().equalsIgnoreCase("<td valign=\"top\" class=\"forumPostText\" id=\"" + postId + "\">")) {
                    correct = line.indexOf("<");
                }
                if (line.trim().startsWith("<img src='/img/flags/") && correct < 0) {
                    try {
                        username = line.substring(line.lastIndexOf("'>") + 2, line.indexOf("</a><br/>"));
                    } catch (Exception ex) {
                    }
                }
                if (line.trim().startsWith("<span nicetitle='$forumVot")) {
                    upvotes = Integer.parseInt(line.substring(line.indexOf('+') + 1, line.indexOf('<', line.indexOf('+'))));
                    downvotes = Integer.parseInt(line.substring(line.indexOf('-') + 1, line.indexOf('<', line.indexOf('-'))));
                }
                if (line.contains("ago</span>")) {
                    date = (new Date(line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""))).getTime());
                }
                if (correct >= 0 && line.indexOf("</td") == correct) {
                    for (int i = start; i < index + 1; i++) {
                        post += html.get(i) + '\n';
                    }
                    break;
                }
                index++;
            }
            boolean down = false;
            html = getHTML("http://zero-k.info/Home/GetTooltip?key=%24forumVotes%24" + postId);
            for (String line : html) {
                if (line.contains("Downvotes")) {
                    down = true;
                }
                if (line.trim().startsWith("<small><img")) {
                    if (down) {
                        downvoters += line.substring(line.lastIndexOf("'>") + 2, line.lastIndexOf("</a><")) + ";";
                    } else {
                        upvoters += line.substring(line.lastIndexOf("'>") + 2, line.lastIndexOf("</a><")) + ";";
                    }
                }
            }
            System.err.println("parsed " + postId);
            //return post;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            System.err.println("failed" + postId + "\n" + sw.toString());
        }
        if (correct < 0) {
            return "";
        }
        return postId + "|" + username + "|" + upvotes + "|" + downvotes + "|" + date + "|" + upvoters + "|" + downvoters + "\n";
    }

    private static int count(String container, String substring) {
        int count = 0;
        while (container.contains(substring)) {
            count++;
            container = container.substring(container.indexOf(substring) + substring.length());
        }
        return count;
    }

    public static String parseBattle(int battleId) throws Exception {
        int correct = -1;
        int index = 0;
        int paramIndex = -1;
        String ago = "";
        String title = "";
        String host = "";
        String engine = "";
        String game = "";
        String duration = "";
        String map = "";
        boolean bots = false;
        boolean mission = false;
        String firstCommenter = "";
        List<String> winningTeams = new ArrayList();
        List<String> losingTeams = new ArrayList();
        boolean comments = false;
        try {
            List<String> html = getHTML("http://zero-k.info/Battles/Detail/" + battleId + "?ShowWinners=True");

            for (String line : html) {
                if (line.contains("$map$")) {
                    map = line.substring(line.indexOf("$map$") + 5, line.indexOf("\" style"));
                }
                if (line.trim().equalsIgnoreCase("<div class=\"fleft\" style=\"padding: 5px;\">")) {
                    paramIndex = 0;
                }
                if (line.trim().startsWith("<a href='/replays/")) {

                    int startIndex = -1;
                    Pattern pattern = Pattern.compile( "\\d{8}_\\d{6}");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        startIndex = matcher.start();
                    }
                    String strDate = line.substring(startIndex, startIndex + 8 + 1 + 6);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = sdf.parse(strDate);
                    ago = (System.currentTimeMillis() - date.getTime()) / 1000 + " seconds";
                }
                if (paramIndex >= 0 && line.trim().equalsIgnoreCase("<tr>")) {
                    String a = html.get(index + 2).trim();
                    String b = html.get(index + 4).trim().substring(4);
                    switch (a) {
                        case "Title:":
                            title = b;
                            break;
                        case "Host:":
                            if (b.contains("Nobody")) {
                                host = "";
                            } else {
                                host = b.substring(b.lastIndexOf("'>") + 2, b.lastIndexOf("</a>"));
                            }
                            break;
                        case "Game version:":
                            game = b;
                            break;
                        case "Engine version:":
                            engine = b;
                            break;
                        case "Duration:":
                            duration = b;
                            break;
                        case "Bots:":
                            bots = Boolean.valueOf(b);
                            break;
                        case "Mission:":
                            mission = Boolean.valueOf(b);
                            break;
                    }
                    paramIndex++;
                }

                if (paramIndex >= 0 && line.trim().equalsIgnoreCase("</table>")) {
                    paramIndex = -1;
                }

                if (line.trim().equalsIgnoreCase("<div class='fleft battle_loser'>") || line.trim().equalsIgnoreCase("<div class='fleft battle_winner'>")) {
                    String l = "";
                    int i = index;
                    String team = "";
                    while (!l.trim().equalsIgnoreCase("</div>")) {
                        l = html.get(i++);
                        if (l.contains("XP gained")) {
                            team += l.substring(l.indexOf(": ") + 2, l.indexOf("</small>"));
                        }
                        if (l.contains("class='flag' ")) {
                            team += ";" + l.substring(l.lastIndexOf("$user$") + 6, l.lastIndexOf("'"));
                        }
                        if (l.contains("{redacted}")) {
                            team += ";0";
                        }
                        if (l.contains("$clan$")) {
                            while (count(team.substring(team.lastIndexOf(";")), "/") < 0) {
                                team += "/";
                            }
                            team += "/" + l.substring(l.lastIndexOf("$clan$") + 6, l.lastIndexOf("'><img src='/img/clans"));
                        }
                        if (l.contains("died in")) {
                            while (count(team.substring(team.lastIndexOf(";")), "/") < 1) {
                                team += "/";
                            }
                            String t = l;
                            if (t.contains("Awards")) {
                                t = t.substring(0, t.indexOf("Awards"));
                            }
                            team += "/" + t.substring(t.lastIndexOf("in ") + 3, (t.contains("<") ? t.indexOf("<") : t.indexOf("Elo"))).trim();
                        }

                        if (l.contains("/img/Awards")) {
                            while (count(team.substring(team.lastIndexOf(";")), "/") < 2) {
                                team += "/";
                            }
                            team += "/" + l.substring(l.lastIndexOf("ards/") + 5, l.lastIndexOf(".png"));
                            team += ":" + l.substring(l.lastIndexOf("title=\"") + 7, l.lastIndexOf("\"")).replaceAll("[^0-9]", "");
                        }
                    }
                    if (line.trim().equalsIgnoreCase("<div class='fleft battle_loser'>")) {
                        losingTeams.add(team);
                    } else if (line.trim().equalsIgnoreCase("<div class='fleft battle_winner'>")) {
                        winningTeams.add(team);
                    }
                }

                if (line.trim().equalsIgnoreCase("<div id=\"comments\" class=\"border-test\">")) {
                    comments = true;
                    correct = 1;
                }

                if (comments && line.contains("class='flag' ")) {
                    firstCommenter = line.substring(line.lastIndexOf("$user$") + 6, line.lastIndexOf("'"));
                    break;
                }

                index++;
            }
            boolean down = false;

            //return post;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            System.err.println("failed" + battleId + "\n" + sw.toString());
        }
        if (correct < 0) {
            return "";
        }
        String winners = "";
        if (winningTeams.size() > 0) {
            winners = winningTeams.get(0);
            for (int i = 1; i < winningTeams.size(); i++) {
                winners += "#" + winningTeams.get(i);
            }
        }
        String losers = "";
        if (losingTeams.size() > 0) {
            losers = losingTeams.get(0);
            for (int i = 1; i < losingTeams.size(); i++) {
                losers += "#" + losingTeams.get(i);
            }
        }
        if (!title.contains("MatchMaker")) {
            return "";
        }//*/
        System.err.println("parsed " + battleId);
        return battleId + "|" + title + "|" + host + "|" + map + "|" + ago + "|" + duration + "|" + game + "|" + engine + "|" + bots + "|" + mission
                + "|" + winners + "|" + losers + "|" + firstCommenter + "\n";
    }

    public static synchronized void write(String s) {
        if (s.isEmpty()) {
            return;
        }
        data.put(Integer.valueOf(s.split("\\|")[0]), s);
    }

    public static synchronized void writefile(String s, String filename) {
        try {
            Files.write(Paths.get(filename), s.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static Map<Integer, String> data = new TreeMap();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

//        List<String> file = Files.readAllLines(Paths.get("posts2.txt"));
//        for (String line : file) {
//            int postId = Integer.valueOf(line.split("\\|")[0]);
//            data.put(postId, line + '\n');
//        }
//        for (int index = 1; index < 160000; index++) {
//            if (data.containsKey(index)) {
//                continue;
//            }
//            final int indexfinal = index;
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        write(parsePost(indexfinal));
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//
//            }).start();
//            Thread.sleep(30);
//        }
//        for (Map.Entry<Integer, String> entry : data.entrySet()) {
//            writefile(entry.getValue());
//            if (entry.getKey() % 100 == 0)System.out.println("written " + entry.getKey());
//        }
        List<String> file = new ArrayList();
        //file = Files.readAllLines(Paths.get("battles.txt"));
        /*System.out.println(parseBattle(428849));
        if (true) {
            return;
        }*/
        for (String line : file) {
            int postId = Integer.valueOf(line.split("\\|")[0]);
            data.put(postId, line + '\n');
        }
        System.out.println("Read " + data.size() + " battles");
        parseBattle(430440);
        for (int index = 425000; index < 450000; index++) {
            if (index % 1000 == 0){
                System.out.println(index);
            }
            if (data.containsKey(index)) {
                continue;
            }
            final int indexfinal = index;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        write(parseBattle(indexfinal));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }).start();
            Thread.sleep(60);
        }
        Thread.sleep(2000);
        for (Map.Entry<Integer, String> entry : data.entrySet()) {
            writefile(entry.getValue(), "battles2.txt");
            if (entry.getKey() % 100 == 0) {
                System.out.println("written " + entry.getKey());
            }
        }
    }

}
