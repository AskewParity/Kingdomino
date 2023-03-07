import javax.swing.*;
import java.awt.*;

public class KingDominoRunner {

    public static void main(String[] args) {

        // buggy on school computer
        //System.setProperty("sun.java2d.opengl", "true");

        Debug.threadCheck("KingDominoRunner > main", false);

        SwingUtilities.invokeLater(() -> new GameFrame("KINGDOMINO"));
    }
}


class GameFrame extends JFrame {
    public static final int WIDTH = 1280, HEIGHT = 720;

    public GameFrame(String title) {
        super(title);
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new StartPanel(this));

        setVisible(true); // calls validate() internally
    }
}
