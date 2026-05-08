import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.FileWriter;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import org.json.*;

public class SpotifyAuth {

    // These come from your Spotify dashboard
    private static final String CLIENT_ID = "d2c57d146afd4dc8b001014389ccae47";
    private static final String CLIENT_SECRET = "49223709406440e4b008c8c6b110a472";
    private static final String REDIRECT_URI = "http://127.0.0.1:8888/callback";


    // Scopes tells Spotify what my app wants permission to do
    private static final String SCOPES = "user-library-read playlist-modify-public playlist-modify-private";

    // This is what Spotify gives back after the user logs in
    private String accessToken;


    // Method kicks off Spotify's authentification
    public void startLogin() throws Exception {
        // Builds authorization url that points to authorize endpoint
        // --4 Queries
        // 1) client ID to identify your app to Spotify
        // 2) tells Spotify you want an authorization code back
        // 3) gives the link that Spotify should send the user back to after they approve/deny access
        // 4) gives the permissions that my app is actually requesting
        String url = "https://accounts.spotify.com/authorize"
                + "?client_id=" + CLIENT_ID
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(SCOPES, StandardCharsets.UTF_8);

        // This opens the URL in their default browser
        Desktop.getDesktop().browse(new URI(url));

        String code = waitForCode();
        exchangeCodeForToken(code);
        System.out.println("Logged in! Token: " + accessToken);
    }

    public String waitForCode() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);
        CompletableFuture<String> codeFuture = new CompletableFuture<>();

        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery(); // "code=AQD..."
            String code = query.split("code=")[1].split("&")[0];
            String response = "Got it! You can close this tab.";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
            codeFuture.complete(code);
        });

        server.start();
        String code = codeFuture.get(); // blocks until callback arrives
        server.stop(0);
        return code;
    }

    public void exchangeCodeForToken(String code) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String body = "grant_type=authorization_code"
                + "&code=" + code
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);

        String credentials = Base64.getEncoder()
                .encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + credentials)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response to get the token
        // response.body() looks like: {"access_token":"BQD...","token_type":"Bearer","expires_in":3600,...}
        String json = response.body();
        this.accessToken = json.split("\"access_token\":\"")[1].split("\"")[0];
    }


    public String getName() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/me"))
                .GET() // Default is GET
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .build();

        try {
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            String json = response.body();
            String name = json.split("\"display_name\":\"")[1].split("\"")[0];
            System.out.println("Name: " + name);
            return name;
        } catch (Exception e) {
            System.out.println("Request failed");
        }
        return "";
    }

    public void createPlaylist(ArrayList<String> songs) {
        //TODO: CREATE PLAYLIST
    }


    public void getLikedSongs() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/me/tracks?limit=50"))
                .GET()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .build();

        try {
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("Error " + response.statusCode() + ": " + response.body());
                return;
            }

            JSONObject root = new JSONObject(response.body());
            JSONArray items = root.getJSONArray("items");

            Songstats songstats = new Songstats();
            FileWriter writer = new FileWriter("liked_songs.txt");

            for (int i = 0; i < items.length(); i++) {
                JSONObject track = items.getJSONObject(i).getJSONObject("track");
                String name = track.getString("name");
                String artist = track.getJSONArray("artists")
                        .getJSONObject(0)
                        .getString("name");
                String trackId = track.getString("id");

                // Fetch BPM from Songstats API
                int bpm = 0;
                try {
                    JSONObject trackInfo = songstats.getTrackInfo(trackId);
                    if (trackInfo != null) {
                        JSONArray analysis = trackInfo.getJSONArray("audio_analysis");
                        for (int j = 0; j < analysis.length(); j++) {
                            JSONObject entry = analysis.getJSONObject(j);
                            if (entry.getString("key").equals("tempo")) {
                                bpm = (int) Math.round(Double.parseDouble(entry.getString("value")));
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Could not fetch BPM for: " + name);
                }

                writer.write(name + ", " + artist + ", " + bpm + "\n");
            }

            writer.close();
            System.out.println("Saved " + items.length() + " songs to liked_songs.txt");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}