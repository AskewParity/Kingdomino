import java.awt.*;
import java.awt.image.BufferedImage;

public class Player {
    public final String name;
    protected Grid grid;
    protected final Game game;
    public boolean dominoIsDroppableAtMouseLocation = false;
    private Domino currentDomino, second;
    private Point mouseoveredTileIndices = new Point(0, 0);
    private Game.TurnPhases turnphase;
    private CacheableImage icon;
    protected Grid possibleGrid;
    private int rank;
    
    /*
    public Player(Game game, String icon, String name, Object o) {

        Debug.threadCheck("Player > Player", false);
        
    	this.game = game;
        this.name = name;
        currentDomino = null;
        second = null;
        setTurnPhase(Game.TurnPhases.PICK);
        
        try {
            this.icon = KingDominoRunner.loadImage(icon);
        } catch (Exception E) {
            System.out.println(E.getMessage());
        }
    }

    public Player(Game game, String icon, String name) {
        
        	this(game, icon, name, null);
        
        grid = new Grid(game.GAME_GRID_SIZE);
        	possibleGrid = grid;
    }
    */
    
    public Player(Game game, String icon, String name) {

        Debug.threadCheck("Player > Player", false);
        
    	this.game = game;
        this.name = name;
        currentDomino = null;
        second = null;
        setTurnPhase(Game.TurnPhases.PICK);
        
        try {
            this.icon = CacheableImage.loadImage(icon);
        } catch (Exception E) {
            System.out.println(E.getMessage());
        }
        
        grid = new Grid(game.GAME_GRID_SIZE);
        	possibleGrid = grid;
    }
    
    // this method is for the SOLE PURPOSE of setting the flag that the player's current domino can legally be placed at the current mouse location
    public void validate() {
        dominoIsDroppableAtMouseLocation = grid.canPlay(currentDomino, mouseoveredTileIndices.x, mouseoveredTileIndices.y);
    }
    
    public Grid getPossibleGrid() {
        	return possibleGrid;
    }
    
    public void updatePossibleGrid() {
    	if (currentDomino != null &&
        		grid.canPlay(currentDomino, mouseoveredTileIndices.x, mouseoveredTileIndices.y)) {
            Grid temp = grid.clone();
            playDomino(temp, mouseoveredTileIndices.x, mouseoveredTileIndices.y);
            temp.getScore();
            	possibleGrid = temp;
        }
    	else possibleGrid = grid;
    }

    public Game.TurnPhases getTurnphase() {
        return turnphase;
    }

    public Domino getCurrentDomino() {
        return currentDomino;
    }

    public void setCurrentDomino(Domino currentDomino) {
        if (game.GAME_GRID_SIZE == 7) {
            if (game.isPastStartUp()) {
                if (second == null) {
                    second = currentDomino;
                } else {
                    this.currentDomino = currentDomino;
                }
                setTurnPhase(Game.TurnPhases.PUT);
            } else {
                if (getCurrentDomino() != null && second == null) {
                    second = currentDomino;
                    setTurnPhase(Game.TurnPhases.PUT);
                    reassignChosenDominoes();
                } else {
                    this.currentDomino = currentDomino;
                }
            }

        } else {
            this.currentDomino = currentDomino;
            setTurnPhase(Game.TurnPhases.PUT);
        }
    }

    public void setTurnPhase(Game.TurnPhases turnphase) {
        this.turnphase = turnphase;
    }

    public Point getMouseoveredTileIndices() {
        return mouseoveredTileIndices;
    }

    public void setMouseoveredTileIndices(int x, int y) {
        mouseoveredTileIndices = new Point(x, y);
    }

    public Grid getGrid() {
        return grid;
    }

    public CacheableImage getIcon() {
        return icon;
    }
    
    public void playDomino(Domino d, Grid grid, int x, int y) {

        Debug.threadCheck("Player > playDomino", false);

        grid.setTile(d.ONE, x, y);

        Point tile2Offset = d.getTile2Offset();
        grid.setTile(d.TWO, x + tile2Offset.x, y + tile2Offset.y);

        grid.recalibrate();
        
        if (grid.gridSize == 7 && grid == this.grid) {
            currentDomino = second;
            second = null;
        }
    }
    
    public boolean canDiscard() {
    	return getTurnphase() == Game.TurnPhases.PUT && !getGrid().canPlayAny(getCurrentDomino());
    }

    public void playDomino(Grid grid, int x, int y) {
        playDomino(getCurrentDomino(), grid, x, y);
    }
    
    public void playDomino(int x, int y) {
	    playDomino(grid, x, y);
        setTurnPhase(Game.TurnPhases.PICK);
    }

    public void reassignChosenDominoes() {
        if (second == null || (game.isPastStartUp() && game.getNextHand().containsKey(second)))
            return;
        if (second.compareTo(currentDomino) < 0) {
            Domino temp = currentDomino;
            currentDomino = second;
            second = temp;
        }
    }

    public int currentScore() {
        return grid.getScore();
    }

    public int compareTo(Player p2) {
        //1 means p2 is less, 0 means tie, -1 means p2 is greater
        if (currentScore() > p2.currentScore()) {
            return 1;
        } else if (currentScore() < p2.currentScore()) {
            return -1;
        } else {
            if (grid.getLargestRegion() > p2.grid.getLargestRegion()) {
                return 1;
            } else if (grid.getLargestRegion() < p2.grid.getLargestRegion()) {
                return -1;
            } else {
                return Integer.compare(grid.getTotalCrowns(), p2.grid.getTotalCrowns());
            }
        }
    }
    
    public void setRank(int rank)
    {
    	this.rank = rank;
    }
	
    public int getRank()
    {
    	return rank;
    }
}
