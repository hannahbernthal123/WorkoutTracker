import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class SongDataFetcher {

    // Your API key from getsongbpm.com
    private static final String API_KEY = "YOUR_GETSONGBPM_API_KEY";

    // We reuse one HttpClient for all requests — it's like opening one
    // connection to the internet rather than opening a new one each time
    private HttpClient client;

    public SongDataFetcher() {
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Searches GetSongBPM for a song by title and artist.
     * Returns the raw JSON response as a String.
     *
     * For example, searching for "Master of Puppets" by "Metallica"
     * would hit this URL:
     * https://api.getsongbpm.com/search/?api_key=YOUR_KEY&type=song&lookup=song:Master+of+Puppets+artist:Metallica
     */
    public String searchSong(String title, String artist) throws Exception {
        // URL-encode the search terms so spaces become "+" and special
        // characters don't break the URL
        String query = "song:" + title + " artist:" + artist;
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String url = "https://api.getsongbpm.com/search/"
                + "?api_key=" + API_KEY
                + "&type=song"
                + "&lookup=" + encodedQuery;

        // Build the HTTP request — this is like typing a URL into
        // your browser, but your program does it
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        // Send the request and get the response body as a String
        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        return response.body();
    }

    /**
     * Once we have a song's ID from the search results, this method
     * fetches the full details — BPM, danceability, genres, etc.
     */
    public String getSongDetails(String songId) throws Exception {
        String url = "https://api.getsongbpm.com/song/"
                + "?api_key=" + API_KEY
                + "&id=" + songId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        return response.body();
    }
}