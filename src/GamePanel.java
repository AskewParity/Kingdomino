import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

public class GamePanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener, ComponentListener {
    private final Game game;
    private final Animator anim;
    JButton discard;
    private boolean readyToPaint = false;
    private int dim;
    private final CacheableImage background;
    private final CacheableImage[] ranks;
    private int roundedCornerRadius;

    public GamePanel(JFrame parent, Game game) {

        Debug.threadCheck("GamePanel > GamePanel", true);

        this.game = game;

        background = CacheableImage.loadImage("background.jpg");

        ranks = new CacheableImage[4];
        for (int i = 0; i < 4; i++) {
            int rank = i + 1;
            ranks[i] = CacheableImage.loadImage("Rank" + rank + ".png");
        }

        setLayout(null);

        discard = new JButton("Discard");
        discard.setFocusable(false);

        discard.addActionListener(e -> { // you can discard only if you can't play
            if (game.canDiscard()) {
                game.getCurrentPlayer().setTurnPhase(Game.TurnPhases.PICK);
                repaint();
            }
        });

        discard.setEnabled(false);
        add(discard);

        setVisible(false);
        parent.add(this);
        parent.validate();
        parent.doLayout();
        parent.validate();
        setVisible(true);
        parent.validate();
        parent.doLayout();
        parent.validate();

        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);

        setFocusable(true);
        requestFocus();

        anim = new Animator(60) {
            @Override
            public void update() {
                repaint();
            }
        };

        (new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                for (Player p : game.getPlayers()) {
                    if (p instanceof AiPlayer) {
                        ((AiPlayer) p).connectGUI(GamePanel.this);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                relayout();
            }
        }).execute();
    }

    public Map<String, Object> getGridGraphicalMeasurements(Grid grid) {

        Debug.threadCheck("GamePanel > getGridGraphicalMeasurements", false);

        final int leftMargin = (getWidth() / 3);
        final int maxPaintableSize = dim * 6;
        final Rect gridMaxPaintBounds = new Rect(maxPaintableSize / 10 + leftMargin, maxPaintableSize / 10, maxPaintableSize * 9 / 10 + leftMargin, maxPaintableSize * 9 / 10); // if you make this not a square, you need to change the stuff below
        final int maxPaintWidth = gridMaxPaintBounds.getWidth();

        final Rect gridTileBounds = grid.getBounds();
        final int gridHorizontalTileCount = gridTileBounds.getWidth();
        final int gridVerticalTileCount = gridTileBounds.getHeight();
        final int tileSize = maxPaintWidth / Math.max(gridHorizontalTileCount, gridVerticalTileCount);

        final int xPaintOffset = (maxPaintWidth - gridHorizontalTileCount * tileSize) / 2;
        final int yPaintOffset = (maxPaintWidth - gridVerticalTileCount * tileSize) / 2;

        Map<String, Object> returnData = new HashMap<>();
        returnData.put("bounds", new Rect(
                xPaintOffset + gridMaxPaintBounds.x1,
                yPaintOffset + gridMaxPaintBounds.y1,
                xPaintOffset + gridHorizontalTileCount * tileSize + gridMaxPaintBounds.x1,
                yPaintOffset + gridVerticalTileCount * tileSize + gridMaxPaintBounds.y1
        ));
        returnData.put("tileSize", tileSize);
        returnData.put("horizontalTileCount", gridHorizontalTileCount);
        returnData.put("verticalTileCount", gridVerticalTileCount);
        return returnData;
    }

    public void recalculateMetrics() {
        (new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                Debug.threadCheck("GamePanel > recalculateMetrics > (SwingWorker) > doInBackground", false);

                dim = Math.min(getHeight() / 6, (int) (getWidth() / (32 / 3.0)));

                roundedCornerRadius = dim / 4;

                for (Player p : game.getPlayers()) {
                    p.getGrid().deleteCachedGraphicalMeasurements();
                    p.getGrid().getCachedGraphicalMeasurements(GamePanel.this);
                }

                return null;
            }

            @Override
            protected void done() {
                readyToPaint = true;
                repaint();
            }
        }).execute();
    }

    // for animation
    // this method should be called from drawCurrentDomino and nowhere else
    public double getCurrentDominoAngle() {

        Debug.threadCheck("GamePanel > getCurrentDominoAngle", true);

        if (anim.properties.containsKey("currentDominoAngle")) return anim.properties.get("currentDominoAngle");
        return 0.0;
    }

    private void rotateCurrentPlayerDomino() {

        (new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                Debug.threadCheck("GamePanel > rotateCurrentPlayerDomino > (SwingWorker) > doInBackground", false);

                game.rotateCurrentDomino();
                
                if (anim.getAnimationByPropertyName("currentDominoAngle") == null)
                    anim.properties.remove("currentDominoAngle");

                return null;
            }

            @Override
            protected void done() {
                repaint();
            }
        }).execute();
    }

    @Override // use paintComponent not paint
    public void paintComponent(Graphics g) {


        Debug.threadCheck("GamePanel > paintComponent", true);

        if (!readyToPaint) return;

        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        //Large Images drawn before graphics gets better
        g2d.drawImage(CacheableImage.getScaledBufferedImage(background, getWidth(), getHeight()), 0, 0, null);

        if (game.isGameEnd()) {
            discard.setVisible(false);

            drawScoreBoard(g2d);

        } else {
            drawCurrentHand(g2d);

            if (game.isPastStartUp()) {
                if (!game.getCurrentPlayer().grid.canPlayAny(game.getCurrentPlayer().getCurrentDomino()))
                    discard.setEnabled(true);
                if (game.getNextHand() != null)
                    drawNextHand(g2d);
                drawCurrentDomino(g2d);
            }

            Player currentPlayer = game.getCurrentPlayer();

            drawAreaScores(g2d, currentPlayer);
            drawGrid(g2d, currentPlayer);
            for (int i = 0; i < game.NUMPLAYERS; i++) {
                drawMini(g2d, game.getPlayerAbs(i), new Point(getWidth() * 15 / 16, 20 + dim * i));
            }
        }
    }

    private void drawScoreBoard(Graphics2D g2d) {
        Player[] temp = game.getScoreBoard();
        int offset = getHeight() / 25, localsize = Math.min(getHeight() / 35, getWidth() * 9 / 560), gridSize = 5;
        for (int i = 0; i < game.NUMPLAYERS; i++) {
            Grid x = temp[i].getGrid();
            Tile[][] grid = x.getArray();

            g2d.setColor(i == 0 ? new Color(79, 135, 227) : new Color(154, 154, 153));
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.fillRoundRect(getHeight() / 3, offset, getWidth() * 4 / 6, getHeight() / 5, roundedCornerRadius, roundedCornerRadius);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2d.setColor(Color.black);

            g2d.drawImage(CacheableImage.getScaledBufferedImage(temp[i].getIcon(), localsize * 5, localsize * 5), getHeight() * 7 / 18, offset + getHeight() / 45, null);
            g2d.drawImage(CacheableImage.getScaledBufferedImage(ranks[temp[i].getRank() - 1], localsize * 5, localsize * 5), getHeight() / 6, offset, null);
            g2d.setFont(new Font("Times New Roman", Font.BOLD, dim / 3));
            g2d.drawString("SCORE - " + temp[i].getGrid().getCachedScore(), getWidth() / 3 - localsize, offset + getHeight() * 9 / 70);
            g2d.setFont(new Font("Times New Roman", Font.BOLD, localsize));
            g2d.drawString(temp[i].name, getWidth() * 21 / 100, offset + getHeight() / 50);
            if (temp[i] instanceof AiPlayer)
                g2d.drawString("AI: " + AiPlayer.difficultyNames[((AiPlayer) temp[i]).ABILITY_LEVEL], getWidth() * 21 / 100, offset + getHeight() / 45 + localsize * 6);

            int smallSize = localsize;
            g2d.setFont(new Font("TimesRoman", Font.BOLD, localsize));
            HashMap<Tile.TileTypes, Integer> map = x.scoresByType;
            for (Map.Entry<Tile.TileTypes, Integer> entry : map.entrySet()) {
                g2d.setColor(Color.white);
                g2d.drawString(entry.getKey() + " - " + entry.getValue(), getWidth() / 2 - localsize, offset + smallSize);
                smallSize += getHeight() / 35;
            }
            g2d.drawString("TOTAL - " + temp[i].getGrid().getCachedScore(), getWidth() / 2 - localsize, offset + smallSize);
            g2d.setColor(Color.black);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            Point mini = new Point(getWidth() * 9 / 12, offset + getHeight() / 45);

            int localSizeBeforeDrawingMini = localsize;

            if (game.GAME_GRID_SIZE == 7) {
                localsize = localsize * 5 / 7;
                g2d.setFont(new Font("Times New Roman", Font.BOLD, localsize));
                gridSize = 7;
            }

            for (int j = x.getWest(); j < x.getWest() + gridSize; j++) {
                for (int k = x.getNorth(); k < x.getNorth() + gridSize; k++) {
                    g2d.drawRect(mini.x - getWidth() / 7 + (j - x.getWest()) * localsize, mini.y + (k - x.getNorth()) * localsize, localsize, localsize);
                    if (grid[k][j].TYPE != Tile.TileTypes.EMPTY) {
                        g2d.drawImage(CacheableImage.getScaledBufferedImage(grid[k][j].getImage(), localsize, localsize), mini.x - getWidth() / 7 + (j - x.getWest()) * localsize, mini.y + (k - x.getNorth()) * localsize, null);
                    }
                    if (grid[k][j].TYPE == Tile.TileTypes.CASTLE) {
                        g2d.drawImage(CacheableImage.getScaledBufferedImage(Tile.king, localsize, localsize), mini.x + (j - x.getWest()) * localsize, mini.y + (k - x.getNorth()) * localsize, null);
                    } else {
                        g2d.setColor(Tile.mini.get(grid[k][j].TYPE));
                        g2d.fillRect(mini.x + (j - x.getWest()) * localsize, mini.y + (k - x.getNorth()) * localsize, localsize, localsize);
                    }
                    g2d.setColor(Color.black);
                    if (grid[k][j].CROWNS > 0)
                        g2d.drawString("" + grid[k][j].CROWNS, mini.x + (j - x.getWest()) * localsize + localsize / 4, mini.y + (k - x.getNorth() + 1) * localsize - localsize / 5);
                }
            }
            offset += getHeight() * 6 / 25;

            localsize = localSizeBeforeDrawingMini;
        }
    }

    public void drawHand(Graphics2D g2d, String heading, Map<Domino, Player> hand, boolean isCurrentHand, Point position) {

        boolean isPrevDraft = isCurrentHand && game.isPastStartUp();

        int offset = dim / 3, localSize = dim * 4 / 5;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Times New Roman", Font.BOLD, localSize / 4));
        g2d.drawString(heading, position.x, offset + position.y - 10);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        List<Entry<Domino, Player>> entries = new ArrayList<>(hand.entrySet());
        for (int i = 0; i < game.HANDSIZE; i++) {
            Entry<Domino, Player> d = entries.get(i);
            CacheableImage[] original = d.getKey().ORIGINAL;

            int cdi = game.getCurrentDominoIndex();
            boolean isTimeToPick = game.getCurrentPlayer().getTurnphase() == Game.TurnPhases.PICK && cdi == i;
            if (isPrevDraft && (cdi > i || isTimeToPick)) {
                setPaintOpacity(g2d, 0.4f);
            }

            g2d.drawImage(CacheableImage.getScaledBufferedImage(original[0], localSize, localSize), position.x, offset + position.y, null);
            g2d.drawImage(CacheableImage.getScaledBufferedImage(original[1], localSize, localSize), position.x + localSize, offset + position.y, null);
            g2d.drawString(String.valueOf(d.getKey().number), position.x, offset + position.y + localSize);
            
            if (d.getValue() != null) {
                if (isPrevDraft && isTimeToPick) {
                    setPaintOpacity(g2d, 1f);
                }
                g2d.drawImage(CacheableImage.getScaledBufferedImage(d.getValue().getIcon(), localSize / 2, localSize / 2), dim * 3 / 5 + position.x, offset + position.y + localSize / 4, null);
            }

            setPaintOpacity(g2d, 1f);

            offset += localSize + dim / 5;
        }
    }

    public void drawCurrentHand(Graphics2D g2d) {
        drawHand(
                g2d,
                game.getCurrentPlayer().getTurnphase() == Game.TurnPhases.PICK ? (game.getCurrentPlayer() instanceof AiPlayer ? "AI PLAYER PLAYING" : "1-" + game.HANDSIZE + (game.getNextHand() != null ? " or mouse to pick " : " to move on")) : "CURRENT HAND",
                game.getCurrentHand(), true, new Point(dim / 4, 0)
        );
    }

    public void drawNextHand(Graphics2D g2d) {
        drawHand(g2d, "NEXT HAND", game.getNextHand(), false, new Point(dim * 9 / 4, 0));
    }

    private void setPaintOpacity(Graphics2D g2d, float opacity) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
    }

    public void drawGrid(Graphics2D g2d, Player p) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Times New Roman", Font.BOLD, dim / 5));
        g2d.drawString("Cards Remaining - " + game.cardsRemaining(), getWidth() / 2 + dim * 4 / 5, dim / 3 - dim / 10);
        if (p.getTurnphase() == Game.TurnPhases.PUT)
            g2d.drawString(p instanceof AiPlayer ? "AI PLAYER PLAYING" : "Click to place || Press D to discard", getWidth() / 2 + dim * 4 / 5, dim / 3 + dim / 10);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);


        Debug.threadCheck("GamePanel > drawGrid", true);

        Grid grid = p.getGrid();

        Map<String, Object> gridInfo = grid.getCachedGraphicalMeasurements(this);

        final int tileSize = (int) gridInfo.get("tileSize");

        for (int x = 0; x < (int) gridInfo.get("horizontalTileCount"); x++) {
            for (int y = 0; y < (int) gridInfo.get("verticalTileCount"); y++) {
                int gridXcoord = x + grid.getBounds().x1;
                int gridYcoord = y + grid.getBounds().y1;
                Tile t = grid.getTile(gridXcoord, gridYcoord);

                int xPos = ((Rect) gridInfo.get("bounds")).x1 + x * tileSize;
                int yPos = ((Rect) gridInfo.get("bounds")).y1 + y * tileSize;

                g2d.drawRect(xPos, yPos, tileSize, tileSize);

                if (t.getImage() != null) {
                    g2d.drawImage(CacheableImage.getScaledBufferedImage(t.getImage(), tileSize, tileSize), xPos, yPos, null);
                }
                
                	String hlColor = t.highlightColor;
                if (hlColor != null) {
                    if (p.dominoIsDroppableAtMouseLocation) {
                        CacheableImage img;
                        Point hoverCoords = p.getMouseoveredTileIndices();
                        if (hoverCoords.x == gridXcoord && hoverCoords.y == gridYcoord) {
                            img = p.getCurrentDomino().ONE.getImage();
                        } else {
                            img = p.getCurrentDomino().TWO.getImage();
                        }
                        float SUGGESTED_PLACEMENT_OPACITY = 0.5f;
                        setPaintOpacity(g2d, SUGGESTED_PLACEMENT_OPACITY);
                        g2d.drawImage(CacheableImage.getScaledBufferedImage(img, tileSize, tileSize), xPos, yPos, null);
                        setPaintOpacity(g2d, 1.0f);
                    }

                    g2d.setStroke(new BasicStroke(2));
                    g2d.setColor(Color.decode(hlColor));

                    g2d.drawRect(xPos + 2, yPos + 2, tileSize - 4, tileSize - 4);

                    // changes stroke back to the way it was before
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(1));
                    
                }
            }
        }
    }

    public void drawAreaScores(Graphics2D g2d, Player p) {

        g2d.setColor(Color.black);
        Point offset = new Point(getWidth() * 15 / 16, getHeight() - 2 * dim);
        int localsize = dim * 13 / 100;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        g2d.fillRoundRect(offset.x - localsize * 51 / 6, offset.y + localsize / 2, localsize * 14, localsize * 15, roundedCornerRadius, roundedCornerRadius);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setFont(new Font("TimesRoman", Font.BOLD, localsize));

        Grid currentGrid = p.getGrid();
        Grid calcGrid = p.getPossibleGrid();

        BiConsumer<Integer, Integer> setTextColor = (possibleScore, realScore) -> g2d.setColor(possibleScore == realScore ? Color.white : Color.yellow);

        Iterator<Map.Entry<Tile.TileTypes, Integer>> possibleScoresPerTileType = calcGrid.scoresByType.entrySet().iterator();
        Iterator<Map.Entry<Tile.TileTypes, Integer>> existingScoresPerTileType = currentGrid.scoresByType.entrySet().iterator();
        while (possibleScoresPerTileType.hasNext() && existingScoresPerTileType.hasNext()) {
            Map.Entry<Tile.TileTypes, Integer> possibleEntry = possibleScoresPerTileType.next();
            Map.Entry<Tile.TileTypes, Integer> existingEntry = existingScoresPerTileType.next();
            offset.y = offset.y + dim / 4;
            setTextColor.accept(possibleEntry.getValue(), existingEntry.getValue());
            g2d.drawString(possibleEntry.getKey() + " - " + possibleEntry.getValue(), offset.x - localsize * 8, offset.y);
        }

        offset.y = offset.y + dim / 4;
        setTextColor.accept(calcGrid.getCachedScore(), currentGrid.getCachedScore());
        g2d.drawString("TOTAL - " + calcGrid.getCachedScore(), offset.x - localsize * 8, offset.y);
        g2d.setColor(Color.black);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    public void drawCurrentDomino(Graphics2D g2d) {

        Debug.threadCheck("GamePanel > drawCurrentDomino", true);

        int localSize = dim * 4 / 5;

        if (game.getCurrentPlayer().getTurnphase() == Game.TurnPhases.PUT) {
            g2d.setColor(Color.WHITE);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setFont(new Font("Times New Roman", Font.BOLD, dim / 5));
            g2d.drawString("Press R to rotate", dim / 4, 26 * dim / 6 - dim / 15);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        g2d.setColor(Color.black);
        g2d.drawRect(dim / 4, 26 * dim / 6, localSize * 2, localSize * 2);
        g2d.setColor(Color.CYAN);

        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        Domino d = game.getCurrentPlayer().getCurrentDomino();
        double angle = (d.getRotation() - 1) * Math.PI / 2;
        Point currentDominoBoxCenter = new Point(dim / 4 + localSize, 26 * dim / 6 + localSize);

        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(currentDominoBoxCenter.x, currentDominoBoxCenter.y);
        g2d.rotate(getCurrentDominoAngle());

        g2d.drawImage(CacheableImage.getScaledBufferedImage(d.ONE.getImage(), localSize, localSize), -(localSize / 2) - (int) Math.cos(angle) * localSize / 2, -(localSize / 2) - (int) Math.sin(angle) * localSize / 2, null);
        g2d.drawImage(CacheableImage.getScaledBufferedImage(d.TWO.getImage(), localSize, localSize), -(localSize / 2) + (int) Math.cos(angle) * localSize / 2, -(localSize / 2) + (int) Math.sin(angle) * localSize / 2, null);
        g2d.drawRect(-(localSize / 2) - (int) Math.cos(angle) * localSize / 2, -(localSize / 2) - (int) Math.sin(angle) * localSize / 2, localSize, localSize);

        g2d.setColor(Color.black);


        g2d.setTransform(oldTransform);
    }

    public void drawMini(Graphics2D g2d, Player p, Point offset) {
        int localsize = dim * 13 / 100, gridSize = 5;

        Grid temp = p.getGrid();
        Tile[][] grid = temp.getArray();

        g2d.setColor(p == game.getCurrentPlayer() ? new Color(79, 135, 227) : new Color(154, 154, 153));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        g2d.fillRoundRect(offset.x - localsize * 51 / 6, offset.y - localsize / 2, localsize * 14, localsize * 6, roundedCornerRadius, roundedCornerRadius);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setColor(Color.black);

        g2d.drawImage(CacheableImage.getScaledBufferedImage(p.getIcon(), localsize * 3, localsize * 3), offset.x - localsize * 8, offset.y + localsize, null);

        g2d.setFont(new Font("Times New Roman", Font.BOLD, dim / 8));
        g2d.drawString(p.name, offset.x - localsize * 8, offset.y + localsize / 2);
        if (p instanceof AiPlayer)
            g2d.drawString("AI: " + AiPlayer.difficultyNames[((AiPlayer) p).ABILITY_LEVEL], offset.x - localsize * 8, offset.y + localsize * 5);
        g2d.drawString("" + p.getGrid().getCachedScore(), offset.x - localsize * 3, offset.y + localsize * 3 - localsize / 5);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        if (game.GAME_GRID_SIZE == 7) {
            localsize = dim * 13 / 140;
            gridSize = 7;
            g2d.setFont(new Font("Times New Roman", Font.BOLD, dim * 5 / 56));
        }

        for (int i = temp.getWest(); i < temp.getWest() + gridSize; i++) {
            for (int j = temp.getNorth(); j < temp.getNorth() + gridSize; j++) {
                if (grid[j][i].TYPE == Tile.TileTypes.CASTLE) {
                    g2d.drawImage(CacheableImage.getScaledBufferedImage(Tile.king, localsize, localsize), offset.x + (i - temp.getWest()) * localsize, offset.y + (j - temp.getNorth()) * localsize, null);
                } else {
                    g2d.setColor(Tile.mini.get(grid[j][i].TYPE));
                    g2d.fillRect(offset.x + (i - temp.getWest()) * localsize, offset.y + (j - temp.getNorth()) * localsize, localsize, localsize);
                }
                g2d.setColor(Color.black);
                if (grid[j][i].CROWNS > 0)
                    g2d.drawString("" + grid[j][i].CROWNS, offset.x + (i - temp.getWest()) * localsize + localsize / 4, offset.y + (j - temp.getNorth() + 1) * localsize - localsize / 5);
            }
        }
    }

    public void action(int x) {
        (new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                Debug.threadCheck("GamePanel > action > (SwingWorker) > doInBackground", false);

                if (x >= 0 && x < game.HANDSIZE) {
                    game.playTurnPick(x);
                }

                return null;
            }

            @Override
            protected void done() {
                discard.setEnabled(game.canDiscard());
            }
        }).execute();
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (game.getCurrentPlayer() instanceof AiPlayer)
            return;
        (new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                Debug.threadCheck("GamePanel > keyPressed > (SwingWorker) > doInBackground", false);

                Player p = game.getCurrentPlayer();
                if (p.getTurnphase() == Game.TurnPhases.PICK) {
                    action(e.getKeyChar() - '1');
                } else if (e.getKeyChar() == 'r') {
                    if (anim.properties.containsKey("currentDominoAngle")) {
                        anim.removeAnimation("currentDominoAngle"); // prevents simultaneous, conflicting animations
                    }
                    anim.addAnimation(new Animation("currentDominoAngle", false, Animator.TweenTypes.SMOOTHERSTEP, 0.0, Math.PI / 2, 0.0, 0.25) {
                        @Override
                        public void animationEnd() {
                            rotateCurrentPlayerDomino();
                        }
                    });
                } else if (e.getKeyChar() == 'd') {
                    // EDT
                    SwingUtilities.invokeLater(() -> discard.doClick());
                }
                return null;
            }

            @Override
            protected void done() {
                repaint();
            }
        }).execute();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (game.getCurrentPlayer() instanceof AiPlayer)
            return;
        (new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                Debug.threadCheck("GamePanel > mouseReleased > (SwingWorker) > doInBackground", false);

                Player p = game.getCurrentPlayer();
                switch (p.getTurnphase()) {
                    case PICK:
                        int temp = dim / 4;
                        int localSize = dim * 4 / 5;
                        if (game.isPastStartUp()) {
                            temp += dim * 2;
                        }
                        if (e.getX() >= temp && e.getX() <= temp + 2 * localSize) {
                            if (e.getY() % (localSize + dim / 5) > dim / 5) {
                                action(e.getY() / (localSize + dim / 5));
                            }
                        }
                        break;
                    case PUT:
                        if (game.isPastStartUp()) {
                            Grid playerGrid = p.getGrid();
                            // exits function if mouse is out of grid bounds
                            if (!((Rect) (playerGrid.getCachedGraphicalMeasurements(GamePanel.this).get("bounds"))).pointIsInside(e.getX(), e.getY()))
                                return null;

                            if (!p.dominoIsDroppableAtMouseLocation) return null;

                            p.playDomino(p.getMouseoveredTileIndices().x, p.getMouseoveredTileIndices().y);
                            playerGrid.getCachedGraphicalMeasurements(GamePanel.this);
                            playerGrid.getScore();
                        }
                        break;
                }
                return null;
            }

            @Override
            protected void done() {
                repaint();
            }
        }).execute();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (game.getCurrentPlayer() instanceof AiPlayer)
            return;
        (new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                Debug.threadCheck("GamePanel > mouseMoved > (SwingWorker) > doInBackground", false);

                Player p = game.getCurrentPlayer();
                if (p.getTurnphase() == Game.TurnPhases.PUT && !game.readyToContinue) {
                    Grid playerGrid = p.getGrid();

                    final Map<String, Object> gridInfo = game.getCurrentPlayer().getGrid().getCachedGraphicalMeasurements(GamePanel.this);
                    final Rect bounds = (Rect) gridInfo.get("bounds");
                    final int tileSize = (int) gridInfo.get("tileSize");

                    //PointerInfo a = MouseInfo.getPointerInfo();
                    //Point b = a.getLocation();
                    int mouseX = e.getX();
                    int mouseY = e.getY();
                    int clickedTileXcoord = (mouseX - bounds.x1) / tileSize + playerGrid.getBounds().x1;
                    int clickedTileYcoord = (mouseY - bounds.y1) / tileSize + playerGrid.getBounds().y1;

                    p.setMouseoveredTileIndices(clickedTileXcoord, clickedTileYcoord);

                    Domino currentDomino = p.getCurrentDomino();

                    p.validate();

                    p.updatePossibleGrid();

                    playerGrid.autoHighlight(currentDomino, clickedTileXcoord, clickedTileYcoord);

                    repaint(); // repaint can be called from anywhere and it always runs in the EDT
                }

                return null;
            }
        }).execute();
        //out.println("mouseover @ tile " + clickedTileXcoord + ", " + clickedTileYcoord); // debug

    }

    public void relayout() {
        recalculateMetrics();

        final int DISCARD_BTN_WIDTH = 80, DISCARD_BTN_HEIGHT = 30;

        discard.setBounds((getWidth() - DISCARD_BTN_WIDTH) / 2, 10, DISCARD_BTN_WIDTH, DISCARD_BTN_HEIGHT);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        relayout();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }
}
