import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Soundtrack Your Workout — multi-screen wizard frontend.
 *
 *   1. Welcome            ── "Ready to begin"
 *   2. Type select        ── Long Run · Intervals · Easy Run · Tempo Run
 *   3a. Long Run          ── distance + mile time           ─┐
 *   3b. Easy / Tempo      ── duration + mile time            │
 *   3c. Intervals paces   ── jog / run / sprint mile times   ├─→  4. Profile  ──→  5. Spotify  ──→  blank
 *        ↓                                                   │
 *   (Intervals only)                                         │
 *   3d. Interval builder  ── add segments one at a time     ─┘
 *
 *   4. Runner profile     ── height + weight (+ optional inseam)  →  RunnerProfile
 *   5. Spotify login      ── "Log into Spotify" button
 *   6. Blank white
 */
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

    // ── Card screen names ──────────────────────────────────────────────────
    static final String SCREEN_WELCOME = "welcome";
    static final String SCREEN_TYPE    = "type";
    static final String SCREEN_LONG    = "long";
    static final String SCREEN_TIMED   = "timed";
    static final String SCREEN_PACES   = "paces";
    static final String SCREEN_BUILD   = "build";
    static final String SCREEN_PROFILE = "profile";
    static final String SCREEN_SPOTIFY = "spotify";
    static final String SCREEN_BLANK   = "blank";
    static final String SCREEN_HELLO   = "hello";

    // ── State ──────────────────────────────────────────────────────────────
    private String workoutType = "";

    // The finished profile, populated when the user advances past SCREEN_PROFILE.
    private RunnerProfile runnerProfile;

    // Long Run inputs
    private JTextField longDistanceField;
    private JTextField longMinField, longSecField;

    // Timed (Easy / Tempo) inputs
    private JLabel     timedHeadingLabel;
    private JTextField timedMinutesField;
    private JTextField timedPaceMinField, timedPaceSecField;

    // Interval pace inputs
    private JTextField jogMinField,    jogSecField;
    private JTextField runMinField,    runSecField;
    private JTextField sprintMinField, sprintSecField;

    // Interval builder
    private JComboBox<String> segmentTypeBox;
    private JTextField        segMinField, segSecField;
    private JPanel            segmentListPanel;
    private JLabel            addErrorLabel;
    private JLabel            helloNameLabel;
    private JButton           intervalReadyBtn;
    private final List<IntervalSegment> segments = new ArrayList<>();

    // Profile inputs
    private JTextField heightField, weightField, inseamField;
    private JLabel     profileErrorLabel;

    // Card layout host
    private CardLayout cards;
    private JPanel     cardHost;

    // ── Data class ─────────────────────────────────────────────────────────
    private static final class IntervalSegment {
        final String type;
        final int minutes;
        final int seconds;
        IntervalSegment(String type, int minutes, int seconds) {
            this.type = type;
            this.minutes = minutes;
            this.seconds = seconds;
        }
        String formatDuration() {
            return minutes + ":" + String.format("%02d", seconds);
        }
    }

    // ── Constructor ────────────────────────────────────────────────────────
    public Viewer() {
        super("Soundtrack Your Workout");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 720);
        setMinimumSize(new Dimension(820, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);

        cards = new CardLayout();
        cardHost = new JPanel(cards);
        cardHost.setBackground(BG_MAIN);

        cardHost.add(buildWelcomeScreen(), SCREEN_WELCOME);
        cardHost.add(buildTypeScreen(),    SCREEN_TYPE);
        cardHost.add(buildLongRunScreen(), SCREEN_LONG);
        cardHost.add(buildTimedScreen(),   SCREEN_TIMED);
        cardHost.add(buildPacesScreen(),   SCREEN_PACES);
        cardHost.add(buildBuilderScreen(), SCREEN_BUILD);
        cardHost.add(buildProfileScreen(), SCREEN_PROFILE);
        cardHost.add(buildSpotifyScreen(), SCREEN_SPOTIFY);
        cardHost.add(buildHelloScreen(), SCREEN_HELLO);
        cardHost.add(buildBlankScreen(),   SCREEN_BLANK);

        setContentPane(cardHost);
        cards.show(cardHost, SCREEN_WELCOME);
    }

    // ──────────────────────────────────────────────────────────────────────
    //  SCREEN 1 — Welcome
    // ──────────────────────────────────────────────────────────────────────
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
        ready.addActionListener(e -> cards.show(cardHost, SCREEN_TYPE));

        content.add(title);
        content.add(Box.createVerticalStrut(14));
        content.add(sub);
        content.add(Box.createVerticalStrut(48));
        content.add(ready);

        screen.add(content);
        return screen;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  SCREEN 2 — Workout type
    // ──────────────────────────────────────────────────────────────────────
    private JPanel buildTypeScreen() {
        JPanel screen = plainScreen();

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel hello = new JLabel("Hello! Please click on the type of workout you want!");
        hello.setFont(new Font("Arial", Font.BOLD, 22));
        hello.setForeground(TEXT_PRIMARY);
        hello.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel grid = new JPanel(new GridLayout(2, 2, 18, 18));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(580, 280));
        grid.setAlignmentX(Component.CENTER_ALIGNMENT);

        grid.add(typeCard("LONG RUN", "Set a distance and pace", () -> {
            workoutType = "Long Run";
            cards.show(cardHost, SCREEN_LONG);
        }));
        grid.add(typeCard("INTERVALS", "Sprint, run, jog mixes", () -> {
            workoutType = "Intervals";
            cards.show(cardHost, SCREEN_PACES);
        }));
        grid.add(typeCard("EASY RUN", "Steady, relaxed pace", () -> {
            workoutType = "Easy Run";
            timedHeadingLabel.setText("Easy Run");
            cards.show(cardHost, SCREEN_TIMED);
        }));
        grid.add(typeCard("TEMPO RUN", "Sustained, challenging pace", () -> {
            workoutType = "Tempo Run";
            timedHeadingLabel.setText("Tempo Run");
            cards.show(cardHost, SCREEN_TIMED);
        }));

        content.add(hello);
        content.add(Box.createVerticalStrut(40));
        content.add(grid);

        screen.add(content);
        return screen;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  SCREEN 3a — Long Run
    // ──────────────────────────────────────────────────────────────────────
    private JPanel buildLongRunScreen() {
        longDistanceField = textField(5);
        longMinField      = textField(3);
        longSecField      = textField(3);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = baseGbc();

        c.gridx = 0; c.gridy = 0;
        form.add(formLabel("Distance"), c);
        c.gridx = 1;
        JPanel dist = inlineRow();
        dist.add(longDistanceField);
        dist.add(unitLabel("miles"));
        form.add(dist, c);

        c.gridx = 0; c.gridy = 1;
        form.add(formLabel("Mile time"), c);
        c.gridx = 1;
        form.add(timeRow(longMinField, longSecField), c);

        JButton ready = primaryButton("NEXT  →", 240, 50);
        ready.addActionListener(e -> cards.show(cardHost, SCREEN_PROFILE));

        return inputScreen(new JLabel("Long Run"), "Enter your distance and target pace.", form, ready);
    }


    private JPanel buildHelloScreen() {
        JPanel screen = gradientScreen();

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel hello = new JLabel("Hello,");
        hello.setFont(new Font("Arial", Font.BOLD, 48));
        hello.setForeground(TEXT_PRIMARY);
        hello.setAlignmentX(Component.CENTER_ALIGNMENT);

        helloNameLabel = new JLabel("there!");
        helloNameLabel.setFont(new Font("Arial", Font.BOLD, 48));
        helloNameLabel.setForeground(ACCENT_GREEN);
        helloNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(hello);
        content.add(Box.createVerticalStrut(8));
        content.add(helloNameLabel);

        screen.add(content);
        return screen;
    }


    // ──────────────────────────────────────────────────────────────────────
    //  SCREEN 3b — Easy / Tempo (shared, heading swaps based on choice)
    // ──────────────────────────────────────────────────────────────────────
    private JPanel buildTimedScreen() {
        timedMinutesField  = textField(4);
        timedPaceMinField  = textField(3);
        timedPaceSecField  = textField(3);
        timedHeadingLabel  = new JLabel("Easy Run");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = baseGbc();

        c.gridx = 0; c.gridy = 0;
        form.add(formLabel("Duration"), c);
        c.gridx = 1;
        JPanel dur = inlineRow();
        dur.add(timedMinutesField);
        dur.add(unitLabel("minutes"));
        form.add(dur, c);

        c.gridx = 0; c.gridy = 1;
        form.add(formLabel("Mile time"), c);
        c.gridx = 1;
        form.add(timeRow(timedPaceMinField, timedPaceSecField), c);

        JButton ready = primaryButton("NEXT  →", 240, 50);
        ready.addActionListener(e -> cards.show(cardHost, SCREEN_PROFILE));

        return inputScreen(timedHeadingLabel, "Enter your workout duration and target pace.", form, ready);
    }

    // ──────────────────────────────────────────────────────────────────────
    //  SCREEN 3c — Interval paces
    // ──────────────────────────────────────────────────────────────────────
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

    // ──────────────────────────────────────────────────────────────────────
    //  SCREEN 3d — Interval builder
    // ──────────────────────────────────────────────────────────────────────
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

        JLabel sub = new JLabel("Add segments one at a time to compose your interval workout.");
        sub.setFont(new Font("Arial", Font.PLAIN, 14));
        sub.setForeground(TEXT_SECONDARY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(8));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));

        // ── Existing-segments card ─────────────────────────────────────────
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

        // ── Add-segment row ────────────────────────────────────────────────
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
                addErrorLabel.setText("Enter a valid duration (seconds must be 0–59).");
                return;
            }
            if (m == 0 && s == 0) {
                addErrorLabel.setText("Duration must be greater than zero.");
                return;
            }
            String type = (String) segmentTypeBox.getSelectedItem();
            segments.add(new IntervalSegment(type, m, s));
            segMinField.setText("");
            segSecField.setText("");
            addErrorLabel.setText(" ");
            rerenderSegments();
            segMinField.requestFocusInWindow();
        } catch (NumberFormatException ex) {
            addErrorLabel.setText("Enter numeric values for minutes and seconds.");
        }
    }

    private void rerenderSegments() {
        segmentListPanel.removeAll();

        if (segments.isEmpty()) {
            JLabel empty = new JLabel("No segments yet — add one below.");
            empty.setFont(new Font("Arial", Font.ITALIC, 13));
            empty.setForeground(TEXT_SECONDARY);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.setBorder(new EmptyBorder(4, 0, 4, 0));
            segmentListPanel.add(empty);
        } else {
            for (int i = 0; i < segments.size(); i++) {
                final int idx = i;
                IntervalSegment seg = segments.get(i);

                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false);
                row.setBorder(new EmptyBorder(5, 0, 5, 0));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

                JLabel l = new JLabel((i + 1) + ".  " + seg.type + "   —   " + seg.formatDuration());
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
                remove.setToolTipText("Remove segment");
                remove.addActionListener(e -> {
                    segments.remove(idx);
                    rerenderSegments();
                });
                row.add(remove, BorderLayout.EAST);

                segmentListPanel.add(row);
            }
        }

        intervalReadyBtn.setEnabled(!segments.isEmpty());
        segmentListPanel.revalidate();
        segmentListPanel.repaint();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  SCREEN 4 — Runner profile
    // ──────────────────────────────────────────────────────────────────────
    private JPanel buildProfileScreen() {
        heightField = textField(4);
        weightField = textField(4);
        inseamField = textField(4);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = baseGbc();

        c.gridx = 0; c.gridy = 0;
        form.add(formLabel("Height"), c);
        c.gridx = 1;
        JPanel hRow = inlineRow();
        hRow.add(heightField);
        hRow.add(unitLabel("inches"));
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
        String hStr = heightField.getText().trim();
        String wStr = weightField.getText().trim();
        String iStr = inseamField.getText().trim();

        if (hStr.isEmpty() || wStr.isEmpty()) {
            profileErrorLabel.setText("Height and weight are required.");
            return false;
        }
        try {
            int h = Integer.parseInt(hStr);
            int w = Integer.parseInt(wStr);
            if (h < 36 || h > 96) {
                profileErrorLabel.setText("Height should be between 36 and 96 inches.");
                return false;
            }
            if (w < 50 || w > 500) {
                profileErrorLabel.setText("Weight should be between 50 and 500 lbs.");
                return false;
            }
            if (iStr.isEmpty()) {
                runnerProfile = new RunnerProfile(h, w);
            } else {
                int leg = Integer.parseInt(iStr);
                if (leg < 15 || leg > 50) {
                    profileErrorLabel.setText("Inseam should be between 15 and 50 inches.");
                    return false;
                }
                runnerProfile = new RunnerProfile(h, w, leg);
            }
            profileErrorLabel.setText(" ");
            return true;
        } catch (NumberFormatException ex) {
            profileErrorLabel.setText("Enter numeric values for height, weight, and inseam.");
            return false;
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  SCREEN 5 — Spotify login
    // ──────────────────────────────────────────────────────────────────────
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
                    SpotifyAuth auth = new SpotifyAuth();
                    auth.startLogin();
                    String name = auth.getName();
                    SwingUtilities.invokeLater(() -> {
                        helloNameLabel.setText(name + "!");
                        statusLabel.setText("Logged in! Songs loaded.");
                        cards.show(cardHost, SCREEN_HELLO);
                    });
                    auth.getLikedSongs();
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

    // ──────────────────────────────────────────────────────────────────────
    //  SCREEN 6 — Blank
    // ──────────────────────────────────────────────────────────────────────
    private JPanel buildBlankScreen() {
        JPanel screen = new JPanel();
        screen.setBackground(Color.WHITE);
        return screen;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Layout / styling helpers
    // ──────────────────────────────────────────────────────────────────────

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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(252, 252, 254),
                        0, getHeight(), new Color(243, 248, 245));
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

    private JPanel inputScreen(JLabel heading, String subtext, JComponent content, JButton primary) {
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

    private JPanel typeCard(String title, String desc, Runnable onClick) {
        return new JPanel() {
            boolean hover = false;
            {
                setOpaque(false);
                setPreferredSize(new Dimension(260, 130));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hover = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setColor(hover ? BG_CARD_HOVER : BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                g2.setColor(hover ? ACCENT_GREEN : DIVIDER);
                g2.setStroke(new BasicStroke(hover ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

                g2.setFont(new Font("Arial", Font.BOLD, 22));
                g2.setColor(ACCENT_GREEN);
                g2.drawString(title, 26, 56);

                g2.setFont(new Font("Arial", Font.PLAIN, 13));
                g2.setColor(TEXT_SECONDARY);
                g2.drawString(desc, 26, 86);

                g2.dispose();
            }
        };
    }

    private JButton primaryButton(String text, int width, int height) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = isEnabled() ? ACCENT_PURPLE : new Color(190, 190, 200);
                Color c2 = isEnabled() ? ACCENT_GREEN  : new Color(170, 185, 175);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
            this.color = color;
            this.radius = radius;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(2, 2, 2, 2); }
        @Override public Insets getBorderInsets(Component c, Insets i) {
            i.set(2, 2, 2, 2);
            return i;
        }
    }
}