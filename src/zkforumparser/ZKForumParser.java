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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    public static synchronized void write(String s) {
        if (s.isEmpty()) return;
        data.put(Integer.valueOf(s.split("\\|")[0]), s);
    }

    public static synchronized void writefile(String s) {
        try {
            Files.write(Paths.get("posts3.txt"), s.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static Map<Integer, String> data = new TreeMap();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        List<String> file = Files.readAllLines(Paths.get("posts2.txt"));
        for (String line : file) {
            int postId = Integer.valueOf(line.split("\\|")[0]);
            data.put(postId, line + '\n');
        }
        for (int index = 1; index < 160000; index++) {
            if (data.containsKey(index)) {
                continue;
            }
            final int indexfinal = index;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        write(parsePost(indexfinal));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }).start();
            Thread.sleep(30);
        }
        for (Map.Entry<Integer, String> entry : data.entrySet()) {
            writefile(entry.getValue());
            if (entry.getKey() % 100 == 0)System.out.println("written " + entry.getKey());
        }
    }

}
