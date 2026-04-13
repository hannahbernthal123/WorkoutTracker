import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SpotifyAuth {

    // These come from your Spotify dashboard
    private static final String CLIENT_ID = "d2c57d146afd4dc8b001014389ccae47";
    private static final String CLIENT_SECRET = "49223709406440e4b008c8c6b110a472";
    private static final String REDIRECT_URI = "https://localhost:8888/callback";


    // Scopes tells Spotify what my app wants permission to do
    private static final String SCOPES = "user-library-read playlist-modify-public playlist-modify-private";

    // This is what Spotify gives back after the user logs in
    private String accessToken;

    /**
     * Opens the user's browser to Spotify's login page.
     * The URL includes your Client ID so Spotify knows which app is asking,
     * and the scopes so the user sees what permissions they're granting.
     */
    public void startLogin() throws Exception {
        String url = "https://accounts.spotify.com/authorize"
                + "?client_id=" + CLIENT_ID
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(SCOPES, StandardCharsets.UTF_8);

        // This literally opens the URL in their default browser
        Desktop.getDesktop().browse(new URI(url));
    }
}