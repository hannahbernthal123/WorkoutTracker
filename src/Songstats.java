import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.*;

public class Songstats {

    private static final String API_KEY = "f8b66de1-1a14-4733-a192-b48796a04382";
    private static final String BASE_URL = "https://api.songstats.com/enterprise/v1";

    private HttpClient client;


    public Songstats() {
        this.client = HttpClient.newHttpClient();
    }

    // Fetches full track info + audio analysis from Songstats
    public JSONObject getTrackInfo(String spotifyTrackId) throws Exception {
        // Builds URL
        String url = BASE_URL + "/tracks/info?spotify_track_id=" + spotifyTrackId;

        // Forms full request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                .header("Accept-Encoding", "")
                .header("apikey", API_KEY)
                .build();

        // Sends request
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        // Avoids error codes (200 = success)
        if (response.statusCode() != 200) {
            System.out.println("Error " + response.statusCode() + ": " + response.body());
            return null;
        }

        // Response body that contains track info
        return new JSONObject(response.body());
    }

    // Parses the response and builds a Song object
    public Song buildSong(String spotifyTrackId) throws Exception {
        JSONObject root = getTrackInfo(spotifyTrackId);
        if (root == null) return null;

        JSONObject trackInfo = root.getJSONObject("track_info");

        String title = trackInfo.getString("title");

        String artist = trackInfo.getJSONArray("artists")
                .getJSONObject(0)
                .getString("name");

        String genre = "unknown";
        JSONArray genres = trackInfo.getJSONArray("genres");
        if (genres.length() > 0) {
            genre = genres.getString(0);
        }

        JSONArray analysis = root.getJSONArray("audio_analysis");

        int bpm = 0;
        int durationSec = 0;
        int energyLevel = 5;

        for (int i = 0; i < analysis.length(); i++) {
            JSONObject entry = analysis.getJSONObject(i);
            String key = entry.getString("key");
            String value = entry.getString("value");

            // Print every key/value so we can see what the API returns
            System.out.println("Key: " + key + "  Value: " + value);

            switch (key) {
                case "tempo":
                    bpm = (int) Math.round(Double.parseDouble(value));
                    break;
                case "duration":
                    try {
                        if (value.contains(":")) {
                            String[] parts = value.split(":");
                            durationSec = Integer.parseInt(parts[0]) * 60
                                    + Integer.parseInt(parts[1]);
                        } else {
                            durationSec = (int) (Double.parseDouble(value) / 1000.0);
                        }
                    } catch (Exception e) {
                        System.out.println("Could not parse duration: " + value);
                        durationSec = 200;
                    }
                    break;
                case "energy":
                    energyLevel = (int) Math.round(Double.parseDouble(value) * 10);
                    break;
            }
        }

        System.out.println("Built song: " + title
                + " bpm=" + bpm
                + " duration=" + durationSec
                + " energy=" + energyLevel);

        return new Song(spotifyTrackId, title, artist, bpm, durationSec, genre, energyLevel);
    }
}