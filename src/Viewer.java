import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class Viewer extends JFrame {

    // ── Color palette ──────────────────────────────────────────────────────
    static final Color BG_MAIN        = Color.WHITE;
    static final Color BG_CARD        = new Color(245, 245, 248);
    static final Color BG_CARD_HOVER  = new Color(238, 240, 248);
    static final Color BG_FIELD       = new Color(252, 252, 254);
    static final Color ACCENT_GREEN   = new Color(30, 160, 90);
    static final Color ACCENT_PURPLE  = new Color(120, 70, 220);
    static final Color TEXT_PRIMARY   = new Color(20, 20, 30);
    static final Color TEXT_SECONDARY = new Color(110, 110, 130);
    static final Color DIVIDER        = new Color(200, 200, 215);
    static final Color ERROR_RED      = new Color(200, 60, 70);

    // ── Screen names ───────────────────────────────────────────────────────
    static final String SCREEN_WELCOME  = "welcome";
    static final String SCREEN_PACES    = "paces";
    static final String SCREEN_BUILD    = "build";
    static final String SCREEN_PROFILE  = "profile";
    static final String SCREEN_SPOTIFY  = "spotify";
    static final String SCREEN_HELLO    = "hello";
    static final String SCREEN_LOADING  = "loading";
    static final String SCREEN_PLAYLIST = "playlist";

    // ── State ──────────────────────────────────────────────────────────────
    private RunnerProfile runnerProfile;
    private SpotifyAuth   spotifyAuth;

    private double jogCadence;
    private double runCadence;
    private double sprintCadence;

    // Pace inputs
    private JTextField jogMinField,    jogSecField;
    private JTextField runMinField,    runSecField;
    private JTextField sprintMinField, sprintSecField;

    // Interval builder
    private JComboBox<String>   segmentTypeBox;
    private JTextField          segMinField, segSecField;
    private JPanel              segmentListPanel;
    private JLabel              addErrorLabel;
    private JButton             intervalReadyBtn;
    private final List<String>  segmentTypes     = new ArrayList<>();
    private final List<Integer> segmentDurations = new ArrayList<>();
    private final List<String>  segmentLabels    = new ArrayList<>();

    // Profile inputs
    private JTextField heightFeetField, heightInchField, weightField, inseamField;
    private JLabel     profileErrorLabel;

    // Playlist screen
    private JPanel     playlistListPanel;

    // Card layout
    private CardLayout cards;
    private JPanel     cardHost;

    // ── Constructor ────────────────────────────────────────────────────────
    public Viewer() {
        super("Soundtrack Your Workout");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 720);
        setMinimumSize(new Dimension(820, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);

        cards    = new CardLayout();
        cardHost = new JPanel(cards);
        cardHost.setBackground(BG_MAIN);

        cardHost.add(buildWelcomeScreen(),  SCREEN_WELCOME);
        cardHost.add(buildPacesScreen(),    SCREEN_PACES);
        cardHost.add(buildBuilderScreen(),  SCREEN_BUILD);
        cardHost.add(buildProfileScreen(),  SCREEN_PROFILE);
        cardHost.add(buildSpotifyScreen(),  SCREEN_SPOTIFY);
        cardHost.add(buildHelloScreen(),    SCREEN_HELLO);
        cardHost.add(buildLoadingScreen(),  SCREEN_LOADING);
        cardHost.add(buildPlaylistScreen(), SCREEN_PLAYLIST);

        setContentPane(cardHost);
        cards.show(cardHost, SCREEN_WELCOME);
    }

    // ── SCREEN 1 — Welcome ─────────────────────────────────────────────────
    private JPanel buildWelcomeScreen() {
        JPanel screen = gradientScreen();

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("SOUNDTRACK YOUR WORKOUT");
        title.setFont(new Font("Arial", Font.BOLD, 38));
        title.setForeground(ACCENT_GREEN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("the perfect playlist for every run");
        sub.setFont(new Font("Arial", Font.PLAIN, 15));
        sub.setForeground(TEXT_SECONDARY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton ready = primaryButton("READY TO BEGIN  →", 260, 54);
        ready.setAlignmentX(Component.CENTER_ALIGNMENT);
        ready.addActionListener(e -> cards.show(cardHost, SCREEN_PACES));

        content.add(title);
        content.add(Box.createVerticalStrut(14));
        content.add(sub);
        content.add(Box.createVerticalStrut(48));
        content.add(ready);

        screen.add(content);
        return screen;
    }

    // ── SCREEN 2 — Paces ───────────────────────────────────────────────────
    private JPanel buildPacesScreen() {
        jogMinField    = textField(3); jogSecField    = textField(3);
        runMinField    = textField(3); runSecField    = textField(3);
        sprintMinField = textField(3); sprintSecField = textField(3);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = baseGbc();

        c.gridx = 0; c.gridy = 0;
        form.add(formLabel("Jog pace"), c);
        c.gridx = 1;
        form.add(timeRow(jogMinField, jogSecField), c);

        c.gridx = 0; c.gridy = 1;
        form.add(formLabel("Run pace"), c);
        c.gridx = 1;
        form.add(timeRow(runMinField, runSecField), c);

        c.gridx = 0; c.gridy = 2;
        form.add(formLabel("Sprint pace"), c);
        c.gridx = 1;
        form.add(timeRow(sprintMinField, sprintSecField), c);

        JButton next = primaryButton("BUILD THE RUN  →", 240, 50);
        next.addActionListener(e -> cards.show(cardHost, SCREEN_BUILD));

        return inputScreen(new JLabel("Intervals"),
                "Enter your mile time at each effort level.", form, next);
    }

    // ── SCREEN 3 — Builder ─────────────────────────────────────────────────
    private JPanel buildBuilderScreen() {
        JPanel screen = plainScreen();

        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(36, 50, 36, 50));

        JLabel heading = new JLabel("Build the run");
        heading.setFont(new Font("Arial", Font.BOLD, 28));
        heading.setForeground(TEXT_PRIMARY);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(heading);

        JLabel sub = new JLabel("Add segments one at a time.");
        sub.setFont(new Font("Arial", Font.PLAIN, 14));
        sub.setForeground(TEXT_SECONDARY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(8));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));

        JPanel listCard = new JPanel();
        listCard.setLayout(new BoxLayout(listCard, BoxLayout.Y_AXIS));
        listCard.setOpaque(false);
        listCard.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(DIVIDER, 12),
                new EmptyBorder(14, 18, 14, 18)));
        listCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        listCard.setMaximumSize(new Dimension(560, Integer.MAX_VALUE));

        JLabel listHeader = new JLabel("YOUR RUN");
        listHeader.setFont(new Font("Arial", Font.BOLD, 11));
        listHeader.setForeground(TEXT_SECONDARY);
        listHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        listCard.add(listHeader);
        listCard.add(Box.createVerticalStrut(8));

        segmentListPanel = new JPanel();
        segmentListPanel.setOpaque(false);
        segmentListPanel.setLayout(new BoxLayout(segmentListPanel, BoxLayout.Y_AXIS));
        segmentListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        listCard.add(segmentListPanel);

        card.add(listCard);
        card.add(Box.createVerticalStrut(20));

        JPanel addRow = new JPanel(new GridBagLayout());
        addRow.setOpaque(false);
        addRow.setMaximumSize(new Dimension(560, 90));
        addRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints g = baseGbc();
        g.gridx = 0; g.gridy = 0;
        addRow.add(formLabel("Type"), g);

        g.gridx = 1;
        segmentTypeBox = new JComboBox<>(new String[]{"Sprint", "Run", "Jog"});
        styleComboBox(segmentTypeBox);
        segmentTypeBox.setPreferredSize(new Dimension(140, 32));
        addRow.add(segmentTypeBox, g);

        g.gridx = 0; g.gridy = 1;
        addRow.add(formLabel("Duration"), g);

        g.gridx = 1;
        segMinField = textField(3);
        segSecField = textField(3);
        addRow.add(timeRow(segMinField, segSecField), g);

        card.add(addRow);

        addErrorLabel = new JLabel(" ");
        addErrorLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        addErrorLabel.setForeground(ERROR_RED);
        addErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(4));
        card.add(addErrorLabel);
        card.add(Box.createVerticalStrut(6));

        ActionListener addAction = e -> tryAddSegment();
        segMinField.addActionListener(addAction);
        segSecField.addActionListener(addAction);

        JButton addBtn = secondaryButton("+ ADD SEGMENT");
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.addActionListener(addAction);
        card.add(addBtn);
        card.add(Box.createVerticalStrut(20));

        intervalReadyBtn = primaryButton("NEXT  →", 240, 50);
        intervalReadyBtn.setEnabled(false);
        intervalReadyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        intervalReadyBtn.addActionListener(e -> cards.show(cardHost, SCREEN_PROFILE));
        card.add(intervalReadyBtn);

        rerenderSegments();
        screen.add(card);
        return screen;
    }

    private void tryAddSegment() {
        String mins = segMinField.getText().trim();
        String secs = segSecField.getText().trim();
        try {
            int m = mins.isEmpty() ? 0 : Integer.parseInt(mins);
            int s = secs.isEmpty() ? 0 : Integer.parseInt(secs);
            if (m < 0 || s < 0 || s >= 60) {
                addErrorLabel.setText("Seconds must be 0–59.");
                return;
            }
            if (m == 0 && s == 0) {
                addErrorLabel.setText("Duration must be greater than zero.");
                return;
            }
            String type = (String) segmentTypeBox.getSelectedItem();
            int totalSecs = (m * 60) + s;

            segmentTypes.add(type);
            segmentDurations.add(totalSecs);
            segmentLabels.add(type + "  —  " + m + ":" + String.format("%02d", s));

            segMinField.setText("");
            segSecField.setText("");
            addErrorLabel.setText(" ");
            rerenderSegments();
            segMinField.requestFocusInWindow();
        } catch (NumberFormatException ex) {
            addErrorLabel.setText("Enter numeric values.");
        }
    }

    private void rerenderSegments() {
        segmentListPanel.removeAll();

        if (segmentTypes.isEmpty()) {
            JLabel empty = new JLabel("No segments yet — add one below.");
            empty.setFont(new Font("Arial", Font.ITALIC, 13));
            empty.setForeground(TEXT_SECONDARY);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.setBorder(new EmptyBorder(4, 0, 4, 0));
            segmentListPanel.add(empty);
        } else {
            for (int i = 0; i < segmentLabels.size(); i++) {
                final int idx = i;

                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false);
                row.setBorder(new EmptyBorder(5, 0, 5, 0));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

                JLabel l = new JLabel((i + 1) + ".  " + segmentLabels.get(i));
                l.setFont(new Font("Arial", Font.PLAIN, 14));
                l.setForeground(TEXT_PRIMARY);
                row.add(l, BorderLayout.WEST);

                JButton remove = new JButton("✕");
                remove.setFont(new Font("Arial", Font.PLAIN, 12));
                remove.setForeground(TEXT_SECONDARY);
                remove.setBorder(new EmptyBorder(2, 8, 2, 8));
                remove.setContentAreaFilled(false);
                remove.setBorderPainted(false);
                remove.setFocusPainted(false);
                remove.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                remove.addActionListener(e -> {
                    segmentTypes.remove(idx);
                    segmentDurations.remove(idx);
                    segmentLabels.remove(idx);
                    rerenderSegments();
                });
                row.add(remove, BorderLayout.EAST);
                segmentListPanel.add(row);
            }
        }

        intervalReadyBtn.setEnabled(!segmentTypes.isEmpty());
        segmentListPanel.revalidate();
        segmentListPanel.repaint();
    }

    // ── SCREEN 4 — Profile ─────────────────────────────────────────────────
    private JPanel buildProfileScreen() {
        heightFeetField = textField(3);
        heightInchField = textField(3);
        weightField     = textField(4);
        inseamField     = textField(4);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = baseGbc();

        c.gridx = 0; c.gridy = 0;
        form.add(formLabel("Height"), c);
        c.gridx = 1;
        JPanel hRow = inlineRow();
        hRow.add(heightFeetField);
        hRow.add(unitLabel("ft"));
        hRow.add(Box.createHorizontalStrut(10));
        hRow.add(heightInchField);
        hRow.add(unitLabel("in"));
        form.add(hRow, c);

        c.gridx = 0; c.gridy = 1;
        form.add(formLabel("Weight"), c);
        c.gridx = 1;
        JPanel wRow = inlineRow();
        wRow.add(weightField);
        wRow.add(unitLabel("lbs"));
        form.add(wRow, c);

        c.gridx = 0; c.gridy = 2;
        form.add(formLabel("Inseam"), c);
        c.gridx = 1;
        JPanel iRow = inlineRow();
        iRow.add(inseamField);
        iRow.add(unitLabel("inches  (optional)"));
        form.add(iRow, c);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(form);

        profileErrorLabel = new JLabel(" ");
        profileErrorLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        profileErrorLabel.setForeground(ERROR_RED);
        profileErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(Box.createVerticalStrut(8));
        content.add(profileErrorLabel);

        JButton next = primaryButton("NEXT  →", 240, 50);
        next.addActionListener(e -> {
            if (tryBuildProfile()) {
                cards.show(cardHost, SCREEN_SPOTIFY);
            }
        });

        return inputScreen(new JLabel("Tell us about you"),
                "Inseam helps dial in your cadence — but it's optional.",
                content, next);
    }

    private boolean tryBuildProfile() {
        String fStr   = heightFeetField.getText().trim();
        String iInStr = heightInchField.getText().trim();
        String wStr   = weightField.getText().trim();
        String iStr   = inseamField.getText().trim();

        if (fStr.isEmpty() || iInStr.isEmpty() || wStr.isEmpty()) {
            profileErrorLabel.setText("Height (feet and inches) and weight are required.");
            return false;
        }

        try {
            int feet   = Integer.parseInt(fStr);
            int inches = Integer.parseInt(iInStr);
            int w      = Integer.parseInt(wStr);

            if (feet < 3 || feet > 8) {
                profileErrorLabel.setText("Feet should be between 3 and 8.");
                return false;
            }
            if (inches < 0 || inches > 11) {
                profileErrorLabel.setText("Inches should be between 0 and 11.");
                return false;
            }

            int totalInches = (feet * 12) + inches;

            if (w < 50 || w > 500) {
                profileErrorLabel.setText("Weight should be between 50 and 500 lbs.");
                return false;
            }

            if (iStr.isEmpty()) {
                runnerProfile = new RunnerProfile(totalInches, w);
            } else {
                int leg = Integer.parseInt(iStr);
                if (leg < 15 || leg > 50) {
                    profileErrorLabel.setText("Inseam should be between 15 and 50 inches.");
                    return false;
                }
                runnerProfile = new RunnerProfile(totalInches, w, leg);
            }

            // ── Parse pace fields with specific error messages ─────────────────
            int jogMin, jogSec, runMin, runSec, sprintMin, sprintSec;
            try {
                String jMinStr = jogMinField.getText().trim();
                String jSecStr = jogSecField.getText().trim();
                String rMinStr = runMinField.getText().trim();
                String rSecStr = runSecField.getText().trim();
                String sMinStr = sprintMinField.getText().trim();
                String sSecStr = sprintSecField.getText().trim();

                if (jMinStr.isEmpty() || jSecStr.isEmpty() ||
                        rMinStr.isEmpty() || rSecStr.isEmpty() ||
                        sMinStr.isEmpty() || sSecStr.isEmpty()) {
                    profileErrorLabel.setText(
                            "Please go back and fill in all pace fields.");
                    return false;
                }

                jogMin    = Integer.parseInt(jMinStr);
                jogSec    = Integer.parseInt(jSecStr);
                runMin    = Integer.parseInt(rMinStr);
                runSec    = Integer.parseInt(rSecStr);
                sprintMin = Integer.parseInt(sMinStr);
                sprintSec = Integer.parseInt(sSecStr);

            } catch (NumberFormatException ex) {
                profileErrorLabel.setText(
                        "Please go back and fill in all pace fields with numbers.");
                return false;
            }

            jogCadence    = runnerProfile.calcCadenceFromPace(jogMin, jogSec);
            runCadence    = runnerProfile.calcCadenceFromPace(runMin, runSec);
            sprintCadence = runnerProfile.calcCadenceFromPace(sprintMin, sprintSec);

            System.out.println("Jog cadence: "    + (int) jogCadence    + " BPM");
            System.out.println("Run cadence: "    + (int) runCadence    + " BPM");
            System.out.println("Sprint cadence: " + (int) sprintCadence + " BPM");

            profileErrorLabel.setText(" ");
            return true;

        } catch (NumberFormatException ex) {
            profileErrorLabel.setText("Enter numeric values for all fields.");
            return false;
        }
    }

    // ── SCREEN 5 — Spotify login ───────────────────────────────────────────
    private JPanel buildSpotifyScreen() {
        JPanel screen = plainScreen();

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("One last thing");
        heading.setFont(new Font("Arial", Font.BOLD, 28));
        heading.setForeground(TEXT_PRIMARY);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Log in to Spotify to build your playlist.");
        sub.setFont(new Font("Arial", Font.PLAIN, 14));
        sub.setForeground(TEXT_SECONDARY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginBtn = primaryButton("LOG INTO SPOTIFY  →", 280, 54);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> {
            loginBtn.setEnabled(false);
            statusLabel.setText("Opening Spotify login...");
            new Thread(() -> {
                try {
                    spotifyAuth = new SpotifyAuth();
                    spotifyAuth.startLogin();
                    String name = spotifyAuth.getName();

                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Logged in! Loading your songs...");
                    });

                    // Load liked songs into Main.allSongs
                    spotifyAuth.getLikedSongs();

                    SwingUtilities.invokeLater(() -> {
                        cards.show(cardHost, SCREEN_HELLO);
                    });

                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Login failed — please try again.");
                        loginBtn.setEnabled(true);
                    });
                }
            }).start();
        });

        content.add(heading);
        content.add(Box.createVerticalStrut(12));
        content.add(sub);
        content.add(Box.createVerticalStrut(40));
        content.add(loginBtn);
        content.add(Box.createVerticalStrut(16));
        content.add(statusLabel);

        screen.add(content);
        return screen;
    }

    // ── SCREEN 6 — Hello ───────────────────────────────────────────────────
    private JPanel buildHelloScreen() {
        JPanel screen = gradientScreen();

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel hello = new JLabel("Hello!");
        hello.setFont(new Font("Arial", Font.BOLD, 48));
        hello.setForeground(ACCENT_GREEN);
        hello.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Your songs are loaded and ready.");
        sub.setFont(new Font("Arial", Font.PLAIN, 15));
        sub.setForeground(TEXT_SECONDARY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton buildBtn = primaryButton("BUILD PLAYLIST  →", 260, 54);
        buildBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        buildBtn.addActionListener(e -> {
            cards.show(cardHost, SCREEN_LOADING);
            new Thread(() -> {
                ArrayList<String>  types     = new ArrayList<>(segmentTypes);
                ArrayList<Integer> durations = new ArrayList<>(segmentDurations);

                ArrayList<Song> playlist = Main.buildFullPlaylist(
                        types, durations,
                        jogCadence, runCadence, sprintCadence);

                SwingUtilities.invokeLater(() -> {
                    ArrayList<Song> finishedPlaylist = playlist;
                    renderPlaylist(playlist);
                    cards.show(cardHost, SCREEN_PLAYLIST);
                });
            }).start();
        });

        content.add(hello);
        content.add(Box.createVerticalStrut(16));
        content.add(sub);
        content.add(Box.createVerticalStrut(40));
        content.add(buildBtn);

        screen.add(content);
        return screen;
    }

    // ── SCREEN 7 — Loading ─────────────────────────────────────────────────
    private JPanel buildLoadingScreen() {
        JPanel screen = gradientScreen();

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Building your playlist...");
        label.setFont(new Font("Arial", Font.BOLD, 28));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("This may take a moment.");
        sub.setFont(new Font("Arial", Font.PLAIN, 15));
        sub.setForeground(TEXT_SECONDARY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(label);
        content.add(Box.createVerticalStrut(14));
        content.add(sub);

        screen.add(content);
        return screen;
    }

    // ── SCREEN 8 — Playlist ────────────────────────────────────────────────
    private JPanel buildPlaylistScreen() {
        JPanel screen = plainScreen();

        JPanel outer = new JPanel();
        outer.setOpaque(false);
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel heading = new JLabel("PLAYLIST");
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.add(heading);
        outer.add(Box.createVerticalStrut(16));

        // Column headers
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel numH   = columnHeader("#");
        JLabel titleH = columnHeader("Title");
        numH.setPreferredSize(new Dimension(36, 24));

        header.add(numH,   BorderLayout.WEST);
        header.add(titleH, BorderLayout.CENTER);
        outer.add(header);

        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        outer.add(sep);
        outer.add(Box.createVerticalStrut(8));

        // Scrollable song list
        playlistListPanel = new JPanel();
        playlistListPanel.setOpaque(false);
        playlistListPanel.setLayout(new BoxLayout(playlistListPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(playlistListPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.add(scroll);

        screen.add(outer);
        return screen;
    }

    // Populates the playlist screen with the final ordered song list
    private void renderPlaylist(ArrayList<Song> songs) {
        System.out.println("renderPlaylist called with " + songs.size() + " songs");
        for (Song s : songs) {
            System.out.println("  - " + s.getTitle()
                    + " duration:" + s.getDuration()
                    + " type:" + s.getSegmentType());
        }

        playlistListPanel.removeAll();

        if (songs.isEmpty()) {
            JLabel empty = new JLabel("No songs found — try wider paces or longer segments.");
            empty.setFont(new Font("Arial", Font.ITALIC, 14));
            empty.setForeground(TEXT_SECONDARY);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            playlistListPanel.add(empty);
            playlistListPanel.revalidate();
            playlistListPanel.repaint();
            return;
        }

        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);

            Color boxColor;
            switch (song.getSegmentType()) {
                case "Sprint": boxColor = new Color(255, 140, 0);  break;
                case "Run":    boxColor = new Color(255, 213, 0);  break;
                case "Jog":    boxColor = new Color(30,  160, 90); break;
                default:       boxColor = BG_CARD;                 break;
            }

            JPanel row = new JPanel(new BorderLayout(12, 0));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(5, 0, 5, 0));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel num = new JLabel(String.valueOf(i + 1));
            num.setFont(new Font("Arial", Font.PLAIN, 14));
            num.setForeground(TEXT_SECONDARY);
            num.setPreferredSize(new Dimension(36, 20));
            num.setVerticalAlignment(SwingConstants.CENTER);

            JPanel box = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(boxColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            box.setOpaque(false);
            box.setBorder(new EmptyBorder(8, 14, 8, 14));

            String displayText = song.getTitle() + "  —  " + song.getArtist();
            JLabel titleLabel = new JLabel(displayText);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
            titleLabel.setForeground(song.getSegmentType().equals("Run")
                    ? new Color(60, 50, 0)
                    : Color.WHITE);

            box.add(titleLabel, BorderLayout.CENTER);
            row.add(num, BorderLayout.WEST);
            row.add(box, BorderLayout.CENTER);

            playlistListPanel.add(row);
            playlistListPanel.add(Box.createVerticalStrut(4));
        }

        playlistListPanel.revalidate();
        playlistListPanel.repaint();
    }

    private JLabel columnHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    // ── Layout helpers ─────────────────────────────────────────────────────

    private JPanel plainScreen() {
        JPanel screen = new JPanel(new GridBagLayout());
        screen.setBackground(BG_MAIN);
        return screen;
    }

    private JPanel gradientScreen() {
        JPanel screen = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0,
                        new Color(252, 252, 254), 0, getHeight(),
                        new Color(243, 248, 245));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 0, 0, 14));
                for (int x = 0; x < getWidth(); x += 32)
                    for (int y = 0; y < getHeight(); y += 32)
                        g2.fillOval(x, y, 2, 2);
                g2.dispose();
            }
        };
        screen.setBackground(BG_MAIN);
        return screen;
    }

    private JPanel inputScreen(JLabel heading, String subtext,
                               JComponent content, JButton primary) {
        JPanel screen = plainScreen();

        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 50, 40, 50));

        heading.setFont(new Font("Arial", Font.BOLD, 28));
        heading.setForeground(TEXT_PRIMARY);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(heading);

        if (subtext != null) {
            JLabel s = new JLabel(subtext);
            s.setFont(new Font("Arial", Font.PLAIN, 14));
            s.setForeground(TEXT_SECONDARY);
            s.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(Box.createVerticalStrut(10));
            card.add(s);
        }

        card.add(Box.createVerticalStrut(36));
        content.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(content);

        if (primary != null) {
            card.add(Box.createVerticalStrut(36));
            primary.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(primary);
        }

        screen.add(card);
        return screen;
    }

    private JButton primaryButton(String text, int width, int height) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = isEnabled() ? ACCENT_PURPLE : new Color(190, 190, 200);
                Color c2 = isEnabled() ? ACCENT_GREEN  : new Color(170, 185, 175);
                GradientPaint gp = new GradientPaint(0, 0, c1,
                        getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Dimension d = new Dimension(width, height);
        btn.setPreferredSize(d);
        btn.setMinimumSize(d);
        btn.setMaximumSize(d);
        return btn;
    }

    private JButton secondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                g2.setColor(hover ? BG_CARD_HOVER : BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(hover ? ACCENT_GREEN : DIVIDER);
                g2.setStroke(new BasicStroke(hover ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setForeground(ACCENT_GREEN);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Dimension d = new Dimension(180, 40);
        btn.setPreferredSize(d);
        btn.setMaximumSize(d);
        btn.setMinimumSize(d);
        return btn;
    }

    private JTextField textField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(new Font("Arial", Font.PLAIN, 15));
        tf.setForeground(TEXT_PRIMARY);
        tf.setBackground(BG_FIELD);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(DIVIDER, 8),
                new EmptyBorder(6, 10, 6, 10)));
        tf.setHorizontalAlignment(JTextField.CENTER);
        return tf;
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.PLAIN, 14));
        l.setForeground(TEXT_PRIMARY);
        l.setPreferredSize(new Dimension(110, 24));
        return l;
    }

    private JLabel unitLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.PLAIN, 13));
        l.setForeground(TEXT_SECONDARY);
        l.setBorder(new EmptyBorder(0, 8, 0, 0));
        return l;
    }

    private JPanel inlineRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        return p;
    }

    private JPanel timeRow(JTextField minField, JTextField secField) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.add(minField);

        JLabel colon = new JLabel(" : ");
        colon.setFont(new Font("Arial", Font.BOLD, 18));
        colon.setForeground(TEXT_PRIMARY);
        p.add(colon);

        p.add(secField);
        p.add(Box.createHorizontalStrut(10));

        JLabel u = new JLabel("min : sec");
        u.setFont(new Font("Arial", Font.PLAIN, 12));
        u.setForeground(TEXT_SECONDARY);
        p.add(u);

        return p;
    }

    private GridBagConstraints baseGbc() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill   = GridBagConstraints.NONE;
        return c;
    }

    private void styleComboBox(JComboBox<String> box) {
        box.setFont(new Font("Arial", Font.PLAIN, 14));
        box.setBackground(BG_FIELD);
        box.setForeground(TEXT_PRIMARY);
        box.setBorder(new RoundedLineBorder(DIVIDER, 8));
        box.setFocusable(false);
    }

    static final class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int   radius;
        RoundedLineBorder(Color color, int radius) {
            this.color  = color;
            this.radius = radius;
        }
        @Override public void paintBorder(Component c, Graphics g,
                                          int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
        @Override public Insets getBorderInsets(Component c, Insets i) {
            i.set(2, 2, 2, 2);
            return i;
        }
    }
}