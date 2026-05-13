import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    //TODO: do more documentation
    public static ArrayList<Song> allSongs = new ArrayList<>();
    public static ArrayList<Song> availableSongs = new ArrayList<>();

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new Viewer().setVisible(true));
    }

    // Called by SpotifyAuth after getLikedSongs() finishes loading
    public static void addSong(Song song) {
        allSongs.add(song);
    }

    public static void resetAvailableSongs() {
        // Only fall back to defaults if liked songs completely failed to load
        if (allSongs.isEmpty()) {
            System.out.println("No liked songs found — using default pop songs.");
            for (Song s : DefaultSongs.getDefaultSongs()) {
                allSongs.add(s);
            }
        }
        availableSongs = new ArrayList<>(allSongs);
        System.out.println("Available pool: " + availableSongs.size() + " songs.");
    }

    // Runs setBpmMatches, knapsack, and traceback for a single segment.
    // Removes used songs from availableSongs so they can't be reused.
    // capacity is the segment duration in seconds.
    public static ArrayList<Song> buildSegmentPlaylist(int targetBPM, int capacitySeconds, String segmentType) {

        ArrayList<Song> validSongs = new ArrayList<>();
        int buffer = 60;

        for (Song song : availableSongs) {
            if (song.getDuration() == 0) continue;

            int bpm = song.getBpm();
            int directDiff = Math.abs(bpm - targetBPM);
            int halfDiff   = Math.abs((bpm * 2) - targetBPM);
            int doubleDiff = Math.abs((bpm / 2) - targetBPM);
            int bestDiff   = Math.min(directDiff, Math.min(halfDiff, doubleDiff));

            if (bestDiff <= buffer) {
                // Base BPM score out of 10
                int bpmScore = Math.max(1, 10 - (int) Math.pow(bestDiff, 2) / 25);

                // Energy score — reward songs whose energy matches the segment type
                int energy = song.getEnergyLevel();
                int energyScore;
                switch (segmentType) {
                    case "Sprint":
                        // Prefers energy 8-10
                        energyScore = (energy >= 8) ? 10 :
                                (energy >= 5) ?  5 : 1;
                        break;
                    case "Run":
                        // Prefers energy 4-7
                        energyScore = (energy >= 4 && energy <= 7) ? 10 :
                                (energy >= 2) ?  5 : 1;
                        break;
                    case "Jog":
                        // Prefers energy 1-3
                        energyScore = (energy <= 3) ? 10 :
                                (energy <= 6) ?  5 : 1;
                        break;
                    default:
                        energyScore = 5;
                        break;
                }

                // Combined score — BPM match weighted slightly more than energy
                int combined = (bpmScore * 6 + energyScore * 4) / 10;
                song.setBpmMatchRatio(Math.max(combined, 1));
                validSongs.add(song);
            } else {
                song.setBpmMatchRatio(0);
            }
        }

        if (validSongs.isEmpty()) {
            System.out.println("No songs found for BPM " + targetBPM);
            return new ArrayList<>();
        }

        int shortestSong = validSongs.stream()
                .mapToInt(Song::getDuration).min().orElse(0);

        if (capacitySeconds <= shortestSong) {
            Song bestSong    = null;
            int  bestScore   = -1;
            int  closestDiff = Integer.MAX_VALUE;

            for (Song song : validSongs) {
                int diff = Math.abs(song.getDuration() - capacitySeconds);
                if (diff < closestDiff ||
                        (diff == closestDiff && song.getBpmMatchRatio() > bestScore)) {
                    closestDiff = diff;
                    bestScore   = song.getBpmMatchRatio();
                    bestSong    = song;
                }
            }

            ArrayList<Song> result = new ArrayList<>();
            if (bestSong != null) {
                result.add(bestSong);
                availableSongs.remove(bestSong);
                System.out.println("Segment " + capacitySeconds + "s → picked: "
                        + bestSong.getTitle()
                        + " (" + bestSong.getDuration() + "s, "
                        + bestSong.getBpm() + " BPM, "
                        + "energy=" + bestSong.getEnergyLevel() + ")");
            }
            return result;
        }

        // Knapsack for longer segments
        Song[] songs  = validSongs.toArray(new Song[0]);
        int[][] table = new int[songs.length + 1][capacitySeconds + 1];

        for (int i = 1; i <= songs.length; i++) {
            Song current = songs[i - 1];
            int weight   = current.getDuration();
            int value    = current.getBpmMatchRatio();

            for (int j = 1; j <= capacitySeconds; j++) {
                int exclude = table[i - 1][j];
                int include = 0;
                if (j >= weight) {
                    include = value + table[i - 1][j - weight];
                }
                table[i][j] = Math.max(include, exclude);
            }
        }

        ArrayList<Song> selected = new ArrayList<>();
        int i = songs.length;
        int j = capacitySeconds;

        while (i > 0 && j > 0) {
            if (table[i][j] != table[i - 1][j]) {
                selected.add(songs[i - 1]);
                j -= songs[i - 1].getDuration();
            }
            i--;
        }

        int totalDuration = selected.stream().mapToInt(Song::getDuration).sum();
        System.out.println("Segment " + capacitySeconds + "s → picked "
                + selected.size() + " songs, total: " + totalDuration + "s");

        availableSongs.removeAll(selected);
        return selected;
    }

    private static ArrayList<Song> runKnapsack(ArrayList<Song> validSongs, int capacity) {
        Song[] songs = validSongs.toArray(new Song[0]);
        int[][] table = new int[songs.length + 1][capacity + 1];

        for (int i = 1; i <= songs.length; i++) {
            Song current = songs[i - 1];
            int weight = current.getDuration();
            int value  = current.getBpmMatchRatio();

            for (int j = 1; j <= capacity; j++) {
                int exclude = table[i - 1][j];
                int include = 0;
                if (j >= weight) {
                    include = value + table[i - 1][j - weight];
                }
                table[i][j] = Math.max(include, exclude);
            }
        }

        return traceback(table, songs, capacity);
    }

    private static ArrayList<Song> traceback(int[][] table, Song[] songs, int capacity) {
        ArrayList<Song> selected = new ArrayList<>();
        int i = songs.length;
        int j = capacity;

        while (i > 0 && j > 0) {
            if (table[i][j] != table[i - 1][j]) {
                selected.add(songs[i - 1]);
                j -= songs[i - 1].getDuration();
            }
            i--;
        }

        return selected;
    }

    // Master method called by Viewer's "Build Playlist" button.
    // segments is the list of IntervalSegment-like data passed from Viewer.
    // Returns the full ordered playlist across all segments.
    public static ArrayList<Song> buildFullPlaylist(
            ArrayList<String> segmentTypes,
            ArrayList<Integer> segmentDurations,
            double jogCadence,
            double runCadence,
            double sprintCadence) {

        System.out.println("allSongs size: " + allSongs.size());

        resetAvailableSongs();

        System.out.println("availableSongs size: " + availableSongs.size());

        ArrayList<Song> fullPlaylist = new ArrayList<>();

        for (int i = 0; i < segmentTypes.size(); i++) {
            String type     = segmentTypes.get(i);
            int    duration = segmentDurations.get(i);

            int targetBPM;
            switch (type) {
                case "Jog":    targetBPM = (int) jogCadence;    break;
                case "Run":    targetBPM = (int) runCadence;    break;
                case "Sprint": targetBPM = (int) sprintCadence; break;
                default:       targetBPM = (int) runCadence;    break;
            }

            System.out.println("Building segment " + (i + 1)
                    + ": " + type + " " + duration + "s @ " + targetBPM + " BPM");

            ArrayList<Song> segmentSongs = buildSegmentPlaylist(targetBPM, duration, type);

            // Tag each song with its segment type for coloring in the UI
            for (Song s : segmentSongs) {
                s.setSegmentType(type);
            }

            fullPlaylist.addAll(segmentSongs);
        }

        System.out.println("Playlist size: " + fullPlaylist.size());

        return fullPlaylist;
    }
}