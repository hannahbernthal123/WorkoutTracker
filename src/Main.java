import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    private static ArrayList<Song> allSongs = new ArrayList<>();
    ArrayList<Song> validSongs = new ArrayList<>();
    public static SpotifyAuth temp = new SpotifyAuth();

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new Viewer().setVisible(true));
        run();
    }

    public static void run() {
        try {
            temp.startLogin();
            temp.getName();
            temp.getLikedSongs();

        } catch (Exception e) {
            System.out.println("Spotify Auth failed :(");
        }

        Songstats songstats = new Songstats();
        try {
            Song song = songstats.buildSong("2plbrEY59IikOBgBGLjaoe"); // Mr. Brightside Spotify ID
            allSongs.add(song);
            System.out.println(song.getTitle());
            System.out.println(song.getArtist());
            System.out.println(song.getBpm());
            System.out.println(song.getDuration());
            System.out.println(song.getGenre());
            System.out.println(song.getEnergyLevel());
        } catch (Exception e) {
            System.out.println("Songstats failed :(");
        }
    }

    public void setBpmMatches(String runType, int targetBPM) {

        Song[] songList = allSongs.toArray(new Song[allSongs.size()]);

        for (Song song : songList) {
            int buffer = 30;
            int diff = Math.abs(song.getBpm() - targetBPM);

            if (diff <= buffer) {
                int ratio = 10 - (int) Math.pow(targetBPM - song.getBpm(), 2) / 25;
                song.setBpmMatchRatio(ratio);
                validSongs.add(song);
            } else {
                song.setBpmMatchRatio(0);
            }
        }
    }

    public int[][] knapsackAlgorithm(double runTime) {

        Song[] songs = validSongs.toArray(new Song[validSongs.size()]);
        int capacity = (int) (runTime * 60);
        int[][] table = new int[songs.length + 1][capacity + 1];

        for (int i = 0; i < songs.length; i++) {
            table[i][0] = 0;
        }

        for (int i = 0; i < capacity + 1; i++) {
            table[0][i] = 0;
        }

        for (int i = 1; i <= songs.length; i++) {
            Song current = songs[i - 1];
            int weight = current.getDuration();
            int value = current.getBpmMatchRatio();

            for (int j = 1; j <= capacity; j++) {

                int exclude = table[i - 1][j];

                int include = 0;
                if (j >= weight) {
                    include = value + table[i - 1][j - weight];
                }

                table[i][j] = Math.max(include, exclude);
            }
        }

        return table;
    }

    public ArrayList<Song> traceback(int[][] table, double runTime) {
        Song[] songs = validSongs.toArray(new Song[validSongs.size()]);
        int capacity = (int) (runTime * 60);
        ArrayList<Song> selected = new ArrayList<>();

        int i = songs.length;
        int j = capacity;

        while (i > 0 && j > 0) {
            // If this cell differs from the one above, this song was included
            if (table[i][j] != table[i - 1][j]) {
                selected.add(songs[i - 1]);
                j -= songs[i - 1].getDuration(); // move left by the song's weight
            }
            i--; // move up one row either way
        }

        return selected;
    }


}
