import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class InstructionFrame extends JFrame implements WindowListener {
    public final int WIDTH, HEIGHT;
    private final JButton x;

    public InstructionFrame(String title, int HEIGHT, int WIDTH, JButton x) {
        super(title);
        this.x = x;
        this.HEIGHT = HEIGHT;
        this.WIDTH = WIDTH;
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new InstructionPanel());

        setVisible(true); // calls validate() internally

        addWindowListener(this);
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        x.setEnabled(true);
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}

class InstructionPanel extends JPanel {

    public InstructionPanel() {

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int dim = Math.min(getHeight() / 7, getWidth() / 12);

        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.black);

        //HEADERS

        g2d.setFont(new Font("Times New Roman", Font.BOLD, dim / 4));

        g2d.drawString("INTRODUCTION", dim, dim / 2);
        g2d.drawString("OBJECT OF THE GAME", dim, getHeight() / 3);
        g2d.drawString("SETUP", getWidth() / 2, dim / 2);
        g2d.drawString("INSTRUCTIONS", getWidth() / 2, getHeight() / 3);

        //INSTRUCTIONS PROPER
        g2d.setFont(new Font("Times New Roman", Font.PLAIN, dim / 5));
        g2d.drawString("You are a Lord seeking new lands to expand your Kingdom. ", dim, dim * 3 / 4);
        g2d.drawString("You must explore all the lands, wheat fields, lakes,  ", dim, dim * 19 / 20);
        g2d.drawString("mountains in order to spot the best plots. But be careful,", dim, dim * 23 / 20);
        g2d.drawString("as some other Lords are also coveting theses lands...", dim, dim * 27 / 20);

        g2d.drawString("Connect your dominoes in order to build your kingdom", dim, getHeight() / 3 + dim / 4);
        g2d.drawString("in a 5x5 grid in a way to score the most prestige points.", dim, getHeight() / 3 + dim * 9 / 20);

        g2d.drawString("2 Player - 7x7: 2 kings per color, 5x5: 1 king per color, remove 24 cards", getWidth() / 2, dim * 3 / 4);
        g2d.drawString("3/4 Player: one king of their color.", getWidth() / 2, dim * 19 / 20);
        g2d.drawString("3 Player: remove 12 dominoes randomly. Play with remaining 36", getWidth() / 2, dim * 23 / 20);
        g2d.drawString("4 Player: use all 48 dominoes.", getWidth() / 2, dim * 27 / 20);

        g2d.drawString("FIRST TURN: Players pick domino by pressing 1-4 or by clicking", getWidth() / 2, getHeight() / 3 + dim / 4);
        g2d.drawString("PUT PHASE: Players place dominoes onto their grid by clicking", getWidth() / 2, getHeight() / 3 + dim);
        g2d.drawString("Players can rotate their domino by pressing R", getWidth() / 2, getHeight() / 3 + dim * 6 / 5);
        g2d.drawString("If the player cannot place, they can discard by pressing the button or by pressing D", getWidth() / 2, getHeight() / 3 + dim * 7 / 5);
        g2d.drawString("PICK PHASE : PLayer will pick their next domino using 1-4 or mouse", getWidth() / 2, getHeight() / 3 + dim * 2);
    }
}
