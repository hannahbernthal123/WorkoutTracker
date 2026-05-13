import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.json.*;

public class SpotifyAuth {

    private static final String CLIENT_ID = "d2c57d146afd4dc8b001014389ccae47";
    private static final String CLIENT_SECRET = "49223709406440e4b008c8c6b110a472";
    private static final String REDIRECT_URI = "http://127.0.0.1:8888/callback";
    private static final String SCOPES =
            "user-library-read playlist-modify-public playlist-modify-private";

    private String accessToken;

    public void startLogin() throws Exception {
        String url = "https://accounts.spotify.com/authorize"
                + "?client_id=" + CLIENT_ID
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(SCOPES, StandardCharsets.UTF_8);

        Desktop.getDesktop().browse(new URI(url));
        String code = waitForCode();
        exchangeCodeForToken(code);
        System.out.println("Logged in!");
    }

    public String waitForCode() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);
        CompletableFuture<String> codeFuture = new CompletableFuture<>();

        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String code = query.split("code=")[1].split("&")[0];
            String response = "Got it! You can close this tab.";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
            codeFuture.complete(code);
        });

        server.start();
        String code = codeFuture.get();
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

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        String json = response.body();
        this.accessToken = json.split("\"access_token\":\"")[1].split("\"")[0];
    }

    public String getName() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/me"))
                .GET()
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

    // Loads liked songs into Main.allSongs via Main.addSong()
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
                System.out.println("Error " + response.statusCode()
                        + ": " + response.body());
                return;
            }

            JSONObject root = new JSONObject(response.body());
            JSONArray items = root.getJSONArray("items");

            Songstats songstats = new Songstats();

            for (int i = 0; i < items.length(); i++) {
                JSONObject track = items.getJSONObject(i).getJSONObject("track");
                String trackId = track.getString("id");

                try {
                    Song song = songstats.buildSong(trackId);
                    if (song != null) {
                        Main.addSong(song);
                        System.out.println("Loaded: " + song.getTitle()
                                + " (" + song.getBpm() + " BPM)");
                    }
                } catch (Exception e) {
                    System.out.println("Could not load song: "
                            + track.getString("name"));
                }
            }

            System.out.println("Loaded " + Main.allSongs.size()
                    + " songs into pool.");

        } catch (Exception e) {
            e.printStackTrace();
        }

//        // If no songs loaded successfully, fall back to default pop songs
//        if (Main.allSongs.isEmpty()) {
//            System.out.println("No liked songs loaded — using default pop songs.");
//            for (Song s : DefaultSongs.getDefaultSongs()) {
//                Main.addSong(s);
//            }
//        }
    }

    public void createPlaylist(ArrayList<String> trackIds) {
        HttpClient client = HttpClient.newHttpClient();

        // Print what we're working with
        System.out.println("Creating playlist with " + trackIds.size() + " tracks.");
        for (String id : trackIds) {
            System.out.println("  Track ID: " + id);
        }

        // ── Step 1: Get user ID ────────────────────────────────────────────
        String userId = "";
        try {
            HttpRequest meRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.spotify.com/v1/me"))
                    .GET()
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .build();

            HttpResponse<String> meResponse = client.send(meRequest,
                    HttpResponse.BodyHandlers.ofString());
            userId = meResponse.body().split("\"id\":\"")[1].split("\"")[0];
            System.out.println("User ID: " + userId);

        } catch (Exception e) {
            System.out.println("Failed to get user ID: " + e.getMessage());
            return;
        }

        // ── Step 2: Create empty playlist ─────────────────────────────────
        String playlistId = "";
        try {
            String createBody = "{"
                    + "\"name\":\"Your New Playlist!\","
                    + "\"description\":\"Generated by Soundtrack Your Workout\","
                    + "\"public\":false"
                    + "}";

            HttpRequest createRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.spotify.com/v1/users/"
                            + userId + "/playlists"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(createBody))
                    .build();

            HttpResponse<String> createResponse = client.send(createRequest,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println("Create playlist response code: "
                    + createResponse.statusCode());
            System.out.println("Create playlist response: "
                    + createResponse.body().substring(0,
                    Math.min(200, createResponse.body().length())));

            if (createResponse.statusCode() != 201) {
                System.out.println("Failed to create playlist.");
                return;
            }

            JSONObject createJson = new JSONObject(createResponse.body());
            playlistId = createJson.getString("id");
            System.out.println("Created playlist ID: " + playlistId);

        } catch (Exception e) {
            System.out.println("Failed to create playlist: " + e.getMessage());
            return;
        }

        // ── Step 3: Filter out null/empty track IDs ────────────────────────
        ArrayList<String> validIds = new ArrayList<>();
        for (String id : trackIds) {
            if (id != null && !id.trim().isEmpty()) {
                validIds.add(id.trim());
            } else {
                System.out.println("Skipping null/empty track ID.");
            }
        }

        if (validIds.isEmpty()) {
            System.out.println("No valid track IDs — playlist will be empty.");
            return;
        }

        // ── Step 4: Add songs in batches of 100 ───────────────────────────
        try {
            ArrayList<String> uris = new ArrayList<>();
            for (String id : validIds) {
                uris.add("spotify:track:" + id);
            }

            int batchSize = 100;
            for (int i = 0; i < uris.size(); i += batchSize) {
                List<String> batch = uris.subList(i,
                        Math.min(i + batchSize, uris.size()));

                StringBuilder uriArray = new StringBuilder("[");
                for (int j = 0; j < batch.size(); j++) {
                    uriArray.append("\"").append(batch.get(j)).append("\"");
                    if (j < batch.size() - 1) uriArray.append(",");
                }
                uriArray.append("]");

                String addBody = "{\"uris\":" + uriArray + "}";

                System.out.println("Adding batch: " + addBody);

                HttpRequest addRequest = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.spotify.com/v1/playlists/"
                                + playlistId + "/items"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + accessToken)
                        .POST(HttpRequest.BodyPublishers.ofString(addBody))
                        .build();

                HttpResponse<String> addResponse = client.send(addRequest,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("Add tracks response code: "
                        + addResponse.statusCode());
                System.out.println("Add tracks response: "
                        + addResponse.body());

                if (addResponse.statusCode() != 201) {
                    System.out.println("Failed to add batch at " + i);
                } else {
                    System.out.println("Successfully added tracks "
                            + i + "–" + Math.min(i + batchSize, uris.size()));
                }
            }

            System.out.println("Playlist creation complete!");

        } catch (Exception e) {
            System.out.println("Failed to add tracks: " + e.getMessage());
            e.printStackTrace();
        }
    }
}