import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class StartPanel extends JPanel {
    private final JFrame parent;
    public static final CacheableImage bgrndStart = CacheableImage.loadJpg("backgroundStart");
    public static final CacheableImage titleImg = CacheableImage.loadImage("kingdomino-logo.png");
    public static final double titleImgAspectRatio = (double)titleImg.getWidth() / (double)titleImg.getHeight();

    public StartPanel(JFrame parent) {
        this.parent = parent;

        JButton playButton = new JButton("PLAY");

        JButton instructions = new JButton("INSTRUCTIONS");

        playButton.setSize(80, 80);
        playButton.setBounds(getWidth() / 6, getHeight() * 17 / 36, getWidth() / 6, getWidth() / 18);

        instructions.setSize(80, 80);
        instructions.setBounds(getWidth() / 6, getHeight() * 17 / 36, getWidth() / 6, getWidth() / 18);

        playButton.addActionListener(e -> start());

        instructions.addActionListener(e -> {
            instructions(instructions);
            instructions.setEnabled(false);
        });

        JPanel subPanel = new JPanel();
        subPanel.add(playButton);
        subPanel.add(instructions);
        subPanel.setBackground(new Color(20, 83, 218));

        add(subPanel, BorderLayout.SOUTH);
        validate();
        parent.validate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Better graphics
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(CacheableImage.getScaledBufferedImage(bgrndStart, getWidth(), getHeight()), 0, 0, null);
        g2d.drawImage(CacheableImage.getScaledBufferedImage(titleImg, (int)(titleImgAspectRatio * getHeight() / 6), getHeight() / 6), getWidth() / 20, getHeight() / 10, null);

        	/*
        g2d.setFont(new Font("Times New Roman", Font.BOLD, Math.min(getWidth() / 20, getHeight() / 10)));
        g2d.drawString("KINGDOMINO", getWidth() / 20, getHeight() / 8);
        	*/
    }

    protected void start() {
        setVisible(false);
        parent.add(new GameSetupPanel(parent));
        parent.validate();
    }

    public void instructions(JButton x) {
        SwingUtilities.invokeLater(() -> new InstructionFrame("INSTURCTIONS", StartPanel.this.getHeight(), StartPanel.this.getWidth(), x));
    }
}


class GameSetupPanel extends JPanel {
    final String[] iconImageNames = {"blue.jpg", "green.jpg", "orange.jpg", "yellow.jpg"};
    final String[] iconChoices = {"---", "Squirtle", "Bulbasaur", "Charmander", "Pikachu"};
    final String[] typeChoices = {"Human", "AI"};
    final String[] aiLevelChoices = {"---", AiPlayer.difficultyNames[0], AiPlayer.difficultyNames[1], AiPlayer.difficultyNames[2]};
    private final JFrame parent;

    JComboBox<String>[] iconSelectors = (JComboBox<String>[]) new JComboBox[4];
    JTextField[] nameFields = new JTextField[4];
    JComboBox<String>[] modeSelectors = (JComboBox<String>[]) new JComboBox[4];
    JComboBox<String>[] aiDifficultySelectors = (JComboBox<String>[]) new JComboBox[4];
    
    private final int MAX_NAME_LENGTH = 15;

    public GameSetupPanel(JFrame parent) {

        // GridBagLayout is a super complicated layout manager, but it just happens to be useful for centering

        this.parent = parent;
        setLayout(new GridLayout(3, 1));

        JPanel headerContainer = new JPanel();
        headerContainer.setLayout(new GridBagLayout());
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("New Game");
        final Font defaultFont = title.getFont();
        title.setFont(defaultFont.deriveFont(32.0f));
        infoPanel.add(title);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10))); // spacer
        infoPanel.add(new JLabel("Leave player name box blank for no player. Must have 2 \u2013 4 players."));
        headerContainer.add(infoPanel);
        add(headerContainer);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new GridBagLayout());

        JPanel playerOptions = new JPanel();
        playerOptions.setLayout(new GridLayout(5, 5));

        	Font boldFont = defaultFont.deriveFont(Font.BOLD);
        JLabel headerLabelPlayer = new JLabel("Player:");
        headerLabelPlayer.setFont(boldFont);
        JLabel headerLabelIcon = new JLabel("Icon:");
        headerLabelIcon.setFont(boldFont);
        JLabel headerLabelName = new JLabel("Name:");
        headerLabelName.setFont(boldFont);
        JLabel headerLabelMode = new JLabel("Mode:");
        headerLabelMode.setFont(boldFont);
        JLabel headerLabelAIlevel = new JLabel("AI Difficulty:");
        headerLabelAIlevel.setFont(boldFont);
        playerOptions.add(headerLabelPlayer);
        playerOptions.add(headerLabelIcon);
        playerOptions.add(headerLabelName);
        playerOptions.add(headerLabelMode);
        playerOptions.add(headerLabelAIlevel);

        int ALLOWED_PLAYER_COUNT = 4;
        for (int i = 0; i < ALLOWED_PLAYER_COUNT; i++) {

            playerOptions.add(new JLabel("Player #" + (i + 1)));
            
            

            iconSelectors[i] = new JComboBox<>(iconChoices);
            //iconSelectors[i].setSelectedIndex(i + 1);
            playerOptions.add(iconSelectors[i]);
          

            
            nameFields[i] = new JTextField();
            nameFields[i].setText("Player " + (i + 1));
            nameFields[i].setColumns(10);
            playerOptions.add(nameFields[i]);
            
            	final int fieldGroupIndex = i;
            	// character limit
            	nameFields[i].addKeyListener(new KeyAdapter() {
            		public void keyTyped(KeyEvent e) {
            			if (nameFields[fieldGroupIndex].getText().length() >= MAX_NAME_LENGTH) 
            				e.consume();
            		}
            	});

            	
            	
            JComboBox<String> currentModeSelector = new JComboBox<>(typeChoices);
            //currentModeSelector.setSelectedIndex(1);
            modeSelectors[i] = currentModeSelector;
            
            JComboBox<String> currentAiDifficultySelector = new JComboBox<>(aiLevelChoices);
            currentAiDifficultySelector.setEnabled(false);
            //currentAiDifficultySelector.setSelectedIndex(2);
            aiDifficultySelectors[i] = currentAiDifficultySelector;

            // AI difficulty selection is disabled when player mode is set to "Human"
            modeSelectors[i].addActionListener(e -> currentAiDifficultySelector.setEnabled(isAImodeSelected(currentModeSelector)));

            playerOptions.add(modeSelectors[i]);
            playerOptions.add(aiDifficultySelectors[i]);
        }

        mainContainer.add(playerOptions);
        add(mainContainer);

        JPanel footerContainer = new JPanel();
        footerContainer.setLayout(new GridBagLayout());
        JButton startButton = new JButton("Start Game");
        startButton.setFont(defaultFont.deriveFont(24.0f).deriveFont(Font.BOLD));
        startButton.addActionListener(e -> start());
        footerContainer.add(startButton);
        add(footerContainer);

        validate();
        parent.validate();
    }
    
    public boolean isAImodeSelected(JComboBox<String> dropdownMenu) {
    	return dropdownMenu.getSelectedItem().toString().equals("AI");
    }

    public void start() {

        Debug.threadCheck("GameSetupPanel > start", true);

        int numPlayers = 0;
        ArrayList<String> playerIcons = new ArrayList<>();
        ArrayList<String> playerNames = new ArrayList<>();
        ArrayList<Boolean> arePlayersBots = new ArrayList<>();
        ArrayList<Integer> botLevels = new ArrayList<>();

        for (int i = 0; i < nameFields.length; i++) {
            String name = nameFields[i].getText();
            if (name.length() > 0) {

                if (playerNames.contains(name)) {
                    JOptionPane.showMessageDialog(parent, "All player names must be unique.", "Cannot start game", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                numPlayers++;

                if (iconSelectors[i].getSelectedIndex() == 0) {
                    JOptionPane.showMessageDialog(parent, "Every player must have an icon.", "Cannot start game", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String iconName = iconImageNames[iconSelectors[i].getSelectedIndex() - 1];
                if (playerIcons.contains(iconName)) {
                    JOptionPane.showMessageDialog(parent, "All players' icons must be unique because icons are used as king pawns.", "Cannot start game", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                playerIcons.add(iconName);
                
                playerNames.add(name);
                
                	int aiDifficultyDropdownSelectedIndex = aiDifficultySelectors[i].getSelectedIndex();
                boolean isPlayerAI = isAImodeSelected(modeSelectors[i]);
                if (isPlayerAI && aiDifficultyDropdownSelectedIndex == 0) {
                    JOptionPane.showMessageDialog(parent, "AIs must have their difficulty levels set.", "Cannot start game", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                arePlayersBots.add(isPlayerAI);

                botLevels.add(isPlayerAI ? aiDifficultyDropdownSelectedIndex - 1 : null);
            }
        }

        if (numPlayers < 2) {
            JOptionPane.showMessageDialog(parent, "At least 2 players must have names.", "Cannot start game", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        	//boolean gameHasAI = arePlayersBots.contains(true);

        int gridSize = 5;

        // in the case of a 2-player game, ask the user if they want to play the 7x7 variant
        if (numPlayers == 2) {
            int dialogButtons = JOptionPane.YES_NO_OPTION;
            	/*
            	int dialogResult = 1;
            	if (gameHasAI) {
            		dialogResult = JOptionPane.showConfirmDialog(this, "Your game has 2 players, and at least 1 of them is an AI. The AI is disabled for 7x7 games, so a 5x5 game will start no matter whether you press yes or no.", "Proceed", dialogButtons);
            } else {
            		dialogResult = JOptionPane.showConfirmDialog(this, "Your game has 2 players. Would you like to play the 7x7 variant?", "7x7?", dialogButtons);
            	}
            	
            if (dialogResult == 0 && !gameHasAI) {
                gridSize = 7;
            }
          	*/
            
	        	int dialogResult = JOptionPane.showConfirmDialog(this, "Your game has 2 players. Would you like to play the 7x7 variant?", "7x7?", dialogButtons);
	        	
	        if (dialogResult == 0) {
	            gridSize = 7;
	        }
        }

        setVisible(false);

        final int NUM_PLAYERS = numPlayers;
        final int GRID_SIZE = gridSize;

        // runs game code outside EDT
        (new SwingWorker<Game, Void>() {
            @Override
            public Game doInBackground() {

                Debug.threadCheck("GameSetupPanel > start > (SwingWorker) > doInBackground", false);

                return new Game(NUM_PLAYERS, GRID_SIZE, playerIcons.toArray(new String[playerIcons.size()]), playerNames.toArray(new String[playerNames.size()]), arePlayersBots.toArray(new Boolean[playerNames.size()]), botLevels.toArray(new Integer[botLevels.size()]));
            }

            public void done() {
                // shows game panel in EDT
                try {
                    Game game = get();
                    new GamePanel(parent, game);
                    game.panelInitialized();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).execute();
    }
}


