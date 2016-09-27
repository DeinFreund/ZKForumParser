/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools \\| Templates
 * and open the template in the editor.
 */
package zkforumparser;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author User
 */
public class Stats {

    //static BufferedImage bimg = new BufferedImage(1650, 1000, BufferedImage.TYPE_INT_ARGB);
    static BufferedImage bimg = new BufferedImage(4 * 3840, 4 * 2160, BufferedImage.TYPE_INT_ARGB);
    static JPanel pnl = new JPanel() {
        @Override
        public void paint(Graphics g) {
            g.drawImage(bimg, 0, 0, this);
        }
    };
    static JFrame frm = new JFrame();

    static Checker atrue = new Checker() {
        @Override
        public boolean check(String username) {
            return true; //To change body of generated methods, choose Tools | Templates.
        }
    };

    public static void simulate(Map<String, Map<String, Double>> relations, Set<String> players, Map<String, Double> positivity) throws Exception {
        boolean graphics = true;
        if (graphics) {
            bimg = new BufferedImage(1650, 1000, BufferedImage.TYPE_INT_ARGB);
            frm.add(pnl);
            frm.setSize(bimg.getWidth(), bimg.getHeight());
            frm.setVisible(true);
            frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        int frame = 0;
        Map<String, Double> xpos = new HashMap();
        Map<String, Double> ypos = new HashMap();
        Map<String, Double> xvel = new HashMap();
        Map<String, Double> yvel = new HashMap();
        for (String p : players) {
            xpos.put(p, Math.random() * bimg.getWidth());
            ypos.put(p, Math.random() * bimg.getHeight());
            xvel.put(p, 0d);
            yvel.put(p, 0d);
        }
        double smul = 0.001;
        int endframe = 3000;
        while (graphics || frame < endframe) {
            frame++;
            if (graphics && frame % 4 == 0) {
                Graphics g = bimg.getGraphics();
                g.setColor(Color.black);
                g.fillRect(0, 0, bimg.getWidth(), bimg.getHeight());
                for (String p : players) {
                    float fac = (float) Math.pow(1 / (1 + (positivity.get(p))), 0.2f);
                    g.setColor(new Color(fac, 1 - fac, 0f));
                    g.drawString(p, (int) (double) xpos.get(p), (int) (double) ypos.get(p));
                }
                pnl.updateUI();
                if (graphics) {
                    Thread.sleep(25);
                }
            }
            if (!graphics && frame == endframe - 10) {
                Graphics g = bimg.getGraphics();
                g.setColor(Color.black);
                g.fillRect(0, 0, bimg.getWidth(), bimg.getHeight());
                for (String p : players) {
                    float fac = (float) Math.pow(1 / (1 + (positivity.get(p))), 0.2f);
                    g.setColor(new Color(fac, 1 - fac, 0f));
                    g.drawString(p, (int) (double) xpos.get(p), (int) (double) ypos.get(p));
                }
                ImageIO.write(bimg, "PNG", new File("rendered" + System.currentTimeMillis() / 1000 + ".png"));
            }
            for (String player : players) {
                xpos.put(player, xpos.get(player) + xvel.get(player));
                ypos.put(player, ypos.get(player) + yvel.get(player));

                double dx = bimg.getWidth() / 2 - xpos.get(player);
                double dy = bimg.getHeight() / 2 - ypos.get(player);
                double dist = Math.sqrt(dx * dx + dy * dy);
                //dx = Math.exp(Math.abs(dx / 1000d)) * Math.signum(dx);
                //dy = Math.exp(Math.abs(dy / 1000d)) * Math.signum(dy);
                //if (dist > bimg.getWidth() * 0.7) {
                xvel.put(player, xvel.get(player) + 0.05 * 4e1 * smul * dx);
                yvel.put(player, yvel.get(player) + 0.05 * 4e1 * smul * dy);
                //}

                for (Map.Entry<String, Double> rel : relations.get(player).entrySet()) {
                    if (!players.contains(rel.getKey()) || rel.getKey().equals(player)) {
                        continue;
                    }
                    dx = xpos.get(rel.getKey()) - xpos.get(player);
                    dy = ypos.get(rel.getKey()) - ypos.get(player);
                    dist = Math.sqrt(dx * dx + dy * dy);
                    xvel.put(player, xvel.get(player) + rel.getValue() * smul * dx / Math.max(50, dist));
                    yvel.put(player, yvel.get(player) + rel.getValue() * smul * dy / Math.max(50, dist));
                }

                for (String p : players) {
                    if (p.equals(player)) {
                        continue;
                    }
                    dx = xpos.get(p) - xpos.get(player);
                    dy = ypos.get(p) - ypos.get(player);
                    dist = (dx * dx + dy * dy);
                    dist *= dist;
                    xvel.put(player, xvel.get(player) - 3e8 * Math.min(1d, 1d * frame / endframe) * smul * Math.signum(dx) / Math.max(50, dist));
                    yvel.put(player, yvel.get(player) - 3e8 * Math.min(1d, 1d * frame / endframe) * smul * Math.signum(dy) / Math.max(50, dist));
                }

                if (frame < endframe / 2) {
                    xvel.put(player, xvel.get(player) + smul * 2000000 * (1f / (frame + 20)) * (Math.random() - 0.5));
                    yvel.put(player, yvel.get(player) + smul * 2000000 * (1f / (frame + 20)) * (Math.random() - 0.5));
                }

                xvel.put(player, xvel.get(player) * 0.9);
                yvel.put(player, yvel.get(player) * 0.9);
            }
            if (frame % 345 == 0) {
                System.out.println(frame * 100 / endframe + "%");
            }
        }
    }

    public static TreeMap<Double, String> reverseMap(Map<String, Integer> map, boolean reverse, Checker checker) {
        TreeMap<Double, String> rev = new TreeMap();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            rev.put((reverse ? (-entry.getValue()) : (entry.getValue())) + Math.random() / 3, entry.getKey());
        }
        int index = 1;

        for (Map.Entry<Double, String> entry : rev.entrySet()) {
            if (!checker.check(entry.getValue())) {
                continue;
            }
            System.out.println(index + ". @" + entry.getValue() + " : " + ((int) Math.round(reverse ? -entry.getKey() : entry.getKey())));
            //System.out.println(index + ". @" + entry.getValue() + " : " + (float) (0.001d * Math.round(reverse ? -entry.getKey() : entry.getKey())));
            if (index++ >= 10000) {
                break;
            }
        }
        return rev;
    }

    public static void main(String[] args) throws Exception {
        final long freqDiv = 1000000000l;
        final List<String> file = Files.readAllLines(Paths.get("posts3.txt"));
        final Map<String, Integer> poskarma = new HashMap();
        final Map<String, Integer> negkarma = new HashMap();
        final Map<String, Integer> posgiven = new HashMap();
        final Map<String, Integer> neggiven = new HashMap();
        final Map<String, Integer> posts = new HashMap();
        final Map<String, Map< Long, Integer>> postsTime = new HashMap();
        final Map<String, Map< Long, Integer>> negKarmaTime = new HashMap();
        final Map<String, Integer> worstposts = new HashMap();
        final Map<String, Integer> bestposts = new HashMap();
        final Map<String, Integer> friendships = new HashMap();
        final Map<String, Integer> hostiles = new HashMap();
        final Map<Long, Integer> postFreq = new TreeMap();
        final Map<Long, Set<String>> divFreq = new TreeMap();
        final Map<Long, Integer> upFreq = new TreeMap();
        final Map<Long, Integer> downFreq = new TreeMap();
        final Map<String, Map<String, Double>> relations = new HashMap();

        final Set<Integer> ids = new HashSet();
        for (String line : file) {
            int postId = Integer.valueOf(line.split("\\|")[0]);
            String username = line.split("\\|")[1];
            int upvotes = Integer.valueOf(line.split("\\|")[2]);
            int downvotes = Integer.valueOf(line.split("\\|")[3]);
            long time = Long.valueOf(line.split("\\|")[4]);
            String[] upvoters = new String[0];
            if (line.split("\\|").length > 5) {
                upvoters = line.split("\\|")[5].split(";");
            }
            String[] downvoters = new String[0];
            if (line.split("\\|").length > 6) {
                downvoters = line.split("\\|")[6].split(";");
            }

            if (username.isEmpty() || ids.contains(postId)) {
                continue;
            }
            if (!postFreq.containsKey(time / freqDiv)) {
                postFreq.put(time / freqDiv, 0);
                upFreq.put(time / freqDiv, 0);
                downFreq.put(time / freqDiv, 0);
                divFreq.put(time / freqDiv, new HashSet());
            }
            postFreq.put(time / freqDiv, postFreq.get(time / freqDiv) + 1);
            divFreq.get(time / freqDiv).add(username);
            //upFreq.put(time / freqDiv, upFreq.get(time / freqDiv) + upvotes);
            //downFreq.put(time / freqDiv, downFreq.get(time / freqDiv) + downvotes);
            ids.add(postId);
            worstposts.put(username + " [url=http://zero-k.info/Forum/Post/" + postId + "]Post[/url]", downvotes);
            bestposts.put(username + " [url=http://zero-k.info/Forum/Post/" + postId + "]Post[/url]", upvotes);
            if (!poskarma.containsKey(username)) {
                poskarma.put(username, 0);
                negkarma.put(username, 0);
                posts.put(username, 0);
                postsTime.put(username, new TreeMap());
                negKarmaTime.put(username, new TreeMap());
            }
            if (!postsTime.get(username).containsKey(time / freqDiv)) {
                postsTime.get(username).put(time / freqDiv, 0);
            }
            negKarmaTime.get(username).put(time / freqDiv, negkarma.get(username));
            postsTime.get(username).put(time / freqDiv, postsTime.get(username).get(time / freqDiv) + 1);
            if (!posgiven.containsKey(username)) {
                posgiven.put(username, 0);
            }
            if (!neggiven.containsKey(username)) {
                neggiven.put(username, 0);
            }
            posts.put(username, posts.get(username) + 1);
            poskarma.put(username, poskarma.get(username) + upvotes);
            negkarma.put(username, negkarma.get(username) + downvotes);
            for (String user : upvoters) {
                if (!posgiven.containsKey(user)) {
                    posgiven.put(user, 0);
                }
                if (user.equals("DeinFreund")) {
                    upFreq.put(time / freqDiv, upFreq.get(time / freqDiv) + 1);
                }
                posgiven.put(user, posgiven.get(user) + 1);
                if (!friendships.containsKey((user + " <-> @" + username))) {
                    friendships.put((user + " <-> @" + username), 0);
                }
                if (!relations.containsKey(user)) {
                    relations.put(user, new HashMap());
                }
                if (!relations.get(user).containsKey(username)) {
                    relations.get(user).put(username, 0d);
                }
                relations.get(user).put(username, relations.get(user).get(username) + 1);
                friendships.put((user + " <-> @" + username), friendships.get((user + " <-> @" + username)) + 1);
            }
            for (String user : downvoters) {
                if (!neggiven.containsKey(user)) {
                    neggiven.put(user, 0);
                }
                if (user.equals("DeinFreund")) {
                    downFreq.put(time / freqDiv, downFreq.get(time / freqDiv) + 1);
                }
                neggiven.put(user, neggiven.get(user) + 1);
                if (!hostiles.containsKey((user + " -> @" + username))) {
                    hostiles.put((user + " -> @" + username), 0);
                }
                if (!relations.containsKey(user)) {
                    relations.put(user, new HashMap());
                }
                if (!relations.get(user).containsKey(username)) {
                    relations.get(user).put(username, 0d);
                }
                relations.get(user).put(username, relations.get(user).get(username) - 10);
                hostiles.put((user + " -> @" + username), hostiles.get((user + " -> @" + username)) + 1);
            }
        }
        for (Map.Entry<String, Integer> entry : friendships.entrySet()) {

            friendships.put(entry.getKey(), 1000 * entry.getValue() / posgiven.get(entry.getKey().split(" ")[0]));
        }/*
        for (Map.Entry<String, Integer> entry : friendships.entrySet().toArray(new Map.Entry[0])) {
            String key = entry.getKey().split(" ")[2].substring(1) + " <-> @" + entry.getKey().split(" ")[0];
            if (friendships.containsKey(key)) {
                if (entry.getValue() > friendships.get(key)) {
                    friendships.remove(entry.getKey());
                }
                //friendships.put(entry.getKey(), Math.min(entry.getValue(), friendships.get(key)));
            } else {
                friendships.remove(entry.getKey());
                //friendships.put(entry.getKey(), 0);
            }
        }*/
        for (Map.Entry<String, Integer> entry : hostiles.entrySet()) {
            hostiles.put(entry.getKey(), 1000 * entry.getValue() / neggiven.get(entry.getKey().split(" ")[0]));
        }
        for (String p : relations.keySet()) {
            if (!posts.containsKey(p)) {
                continue;
            }
            for (String p2 : relations.get(p).keySet()) {
                relations.get(p).put(p2, 1000 * relations.get(p).get(p2) / (posgiven.get(p) + neggiven.get(p)));
            }
        }
        final Map<String, Integer> negPerPost = new HashMap();
        for (String user : posts.keySet()) {
            negPerPost.put(user, 100 * negkarma.get(user) / posts.get(user));
        }
        final Map<String, Integer> posPerNeg = new HashMap();
        for (String user : posts.keySet()) {
            if (poskarma.get(user) == 0) {
                posPerNeg.put(user, poskarma.get(user) * 10000000);
            } else {
                posPerNeg.put(user, 1000 * negkarma.get(user) / poskarma.get(user));
            }
        }
        final Map<String, Integer> negPerPos = new HashMap();
        final Map<String, Double> positivity = new HashMap();
        for (String user : posts.keySet()) {
            if (neggiven.get(user) == 0) {
                negPerPos.put(user, 12345000);
            } else {
                negPerPos.put(user, 1000 * posgiven.get(user) / neggiven.get(user));
            }
            positivity.put(user, posgiven.get(user) / (double) neggiven.get(user));
        }

        System.out.println(posgiven.get("Shadowfury333"));
        System.out.println(neggiven.get("Shadowfury333"));
        System.out.println(neggiven.get("[2up]knorke"));
        System.out.println(neggiven.get("Aquanim"));
        System.out.println(neggiven.get("CrazyEddie"));
        reverseMap(friendships, true, new Checker() {
            @Override
            public boolean check(String username) {//|| posts.get(username) > 100
                String username2 = "asdf";
                if (username.split(" ").length > 2) {
                    username2 = username.split(" ")[2].substring(1);
                }

                username = username.split(" ")[0];
                if (!posts.containsKey(username)) {
                    return false;
                }
                /*if (!posts.containsKey(username2)) {
                    return false;
                }*/
                //return true;
                //return (neggiven.get(username) > 10) && username.length() > 0;
                return (posgiven.get(username) + neggiven.get(username) > 100) && username.length() > 0;
                //        && (posgiven.get(username2) + neggiven.get(username2) > 100) && username2.length() > 0;
                //return (poskarma.get(username) + negkarma.get(username) > 100) && username.length() > 0;
            }
        });
        
        Map<String, Integer> postsTimeSummed = new HashMap();
        for (String name : posts.keySet()) {
            postsTimeSummed.put(name, 0);
        }
        for (long time : postFreq.keySet()) {
            int count = 0;
            if (time * freqDiv < 483 * 3000000000l * freqDiv / 1000000000l) continue;
            for (String name : posts.keySet()) {
                if (postsTime.get(name).containsKey(time)){
                    postsTimeSummed.put(name, postsTimeSummed.get(name) + postsTime.get(name).get(time));
                }
            }
        }
        TreeMap<Double, String> topposters = reverseMap(posts, true, atrue);
        TreeMap<Double, String> toppostersRecent = reverseMap(postsTimeSummed, true, atrue);

        postsTimeSummed = new HashMap();

        for (String name : toppostersRecent.values()) {
            postsTimeSummed.put(name, 0);
        }
        for (long time : postFreq.keySet()) {
            int count = 0;
            if (time * freqDiv < 483 * 3000000000l * freqDiv / 1000000000l) continue;
            //System.out.print(time + "\t");
            for (String name : toppostersRecent.values()) {
                if (count++ >= 10) {
                    break;
                }
                if (postsTime.get(name).containsKey(time)){
                    postsTimeSummed.put(name, postsTimeSummed.get(name) + postsTime.get(name).get(time));
                }
                System.out.print(negKarmaTime.get(name) + "\t");
            }
            System.out.println();
            //System.out.println(divFreq.get(time).size() + "\t" + postFreq.get(time));
            //System.out.println();
        }
        /*
        Set<String> players = new HashSet();
        int count = 0;
        for (Map.Entry<Double, String> entry : topposters.entrySet()) {
            if (count++ > 20) {
                break;
            }
            if (relations.containsKey(entry.getValue())) {
                players.add(entry.getValue());
            }
        }
        count = 0;
        for (Map.Entry<Double, String> entry : reverseMap(posgiven, true, atrue).entrySet()) {
            count++;
            if (-entry.getKey() < 90000) {
                break;
            }
            if (relations.containsKey(entry.getValue()) && posts.containsKey(entry.getValue())) {
                players.add(entry.getValue());
            }
        }

        simulate(relations, players, positivity);
        //*/

    }
}
