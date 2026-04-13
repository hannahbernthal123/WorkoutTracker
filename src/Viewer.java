import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;

public class GameViewer extends JFrame {

    private final int WINDOW_WIDTH = 1000;
    private final int WINDOW_HEIGHT = 800;

    public Viewer() {
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setTitle("Playlist");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        createBufferStrategy(2);
    }

    public void myPaint(Graphics g) {
        g.setColor(new Color(0x068306));
        g.fillRoundRect(250, 500, 500, 100, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Impact", Font.BOLD, 50));
        g.drawString("START", 430, 570);
    }

    // Draws the hole, the ball, and the number of obstacles needed
    public void setUpPlay(int round, Graphics g) {
        game.getHole().draw(g);
        game.getBall().draw(g);
        for (int i = 0; i < game.getObstacles().size(); i++) {
            game.getObstacles().get(i).draw(g, round);
        }
    }


    public void paint(Graphics g) {
        BufferStrategy bf = this.getBufferStrategy();
        if (bf == null)
            return;
        Graphics g2 = null;
        try {
            g2 = bf.getDrawGraphics();
            myPaint(g2);
        }
        finally {
            g2.dispose();
        }
        bf.show();
        Toolkit.getDefaultToolkit().sync();
    }
}
