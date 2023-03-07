import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

public class Grid {
    public final int gridSize;
    private final Tile[][] grid;
    public HashMap<Tile.TileTypes, Integer> scoresByType;
    private Rect bounds;
    private Rect trueBounds;
    private int largestRegion, totalCrowns, score;
    private Map<String, Object> cachedGraphicalMeasurements;

    public Grid(int size) {
    	
        Debug.threadCheck("Grid > Grid", false);

        gridSize = size;
        final int fullGridSize = gridSize * 2 - 1;
        grid = new Tile[fullGridSize][fullGridSize];
        score = 0;
        for (int i = 0; i < grid.length; i++)
            for (int j = 0; j < grid[i].length; j++)
                grid[i][j] = new Tile();
        grid[fullGridSize / 2][fullGridSize / 2] = new Tile(Tile.TileTypes.CASTLE, 0, "0");

        scoringReset();

        recalibrate();
    }

    public boolean canPlayAny(Domino D) {
        //recalibrate();
        Domino temp = D.clone();
        for (int i = 0; i < 4; i++) {
            for (int j = getWest(); j < getEast(); j++)
                for (int k = getNorth(); k < getSouth(); k++)
                    if (canPlay(temp, j, k))
                        return true;
            temp.rotate();
        }
        return false;
    }

    public boolean canPlay(Domino D, int x, int y) {

        Point tile2Offset = D.getTile2Offset();
        int x2 = x + tile2Offset.x;
        int y2 = y + tile2Offset.y;

        if (!coordIsInBounds(x, y))
            return false;
        if (!coordIsInBounds(x2, y2))
            return false;

        if (getTile(x, y).isOccupied())
            return false;
        if (getTile(x2, y2).isOccupied())
            return false;

        return tileChecker(D.ONE, x, y) || tileChecker(D.TWO, x2, y2);
    }

    public boolean tileChecker(Tile tile, int x, int y) {
        if (getTile(x, y).isOccupied())
            return false;
        if (!coordIsInBounds(x, y))
            return false;
        if (x != 0)
            if (tile.canConnect(getTile(x - 1, y)))
                return true;
        if (x != grid[0].length - 1)
            if (tile.canConnect(getTile(x + 1, y)))
                return true;
        if (y != 0)
            if (tile.canConnect(getTile(x, y - 1)))
                return true;
        if (y != grid.length - 1)
            return tile.canConnect(getTile(x, y + 1));
        return false;
    }

    public boolean coordIsInBounds(int x, int y) {
        return getBounds().pointIsInside(new Point(x, y));
    }
    
    public void recalibrate() {
    	recalculateBounds();
        deleteCachedGraphicalMeasurements();
    }

    public void recalculateBounds() {

        Debug.threadCheck("Grid > recalibrate", false);

        bounds = new Rect(grid[0].length, grid.length, 0, 0);

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {

                if (getTile(x, y).isOccupied()) {
                    if (y < bounds.y1)
                        bounds.y1 = y;
                    if (x >= bounds.x2)
                        bounds.x2 = x + 1;
                    if (y >= bounds.y2)
                        bounds.y2 = y + 1;
                    if (x < bounds.x1)
                        bounds.x1 = x;
                }
            }
        }
        
        	trueBounds = bounds;

        if (bounds.y2 - bounds.y1 == gridSize - 1) {
            bounds.y1--;
            bounds.y2++;
        }
        if (bounds.x2 - bounds.x1 == gridSize - 1) {
            bounds.x1--;
            bounds.x2++;
        }
        if (bounds.y2 - bounds.y1 < gridSize - 1) {
            bounds.y1 -= 2;
            bounds.y2 += 2;
        }
        if (bounds.x2 - bounds.x1 < gridSize - 1) {
            bounds.x1 -= 2;
            bounds.x2 += 2;
        }

        //out.println("Current player grid recalibrated to (" + bounds.x1 + ", " + bounds.y1 + "), (" + bounds.x2 + ", " + bounds.y2 + ")");
    }

    private boolean isLocked5x5() {
        if (bounds.x2 - bounds.x1 != gridSize || bounds.y2 - bounds.y1 != gridSize) {
            return false;
        }

        boolean horizontallyMaxed = false, verticallyMaxed = false;

        for (Tile t : grid[bounds.y1]) {
            if (t.isOccupied()) verticallyMaxed = true;
        }

        for (Tile[] row : grid) {
            if (row[bounds.x1].isOccupied()) horizontallyMaxed = true;
        }

        return horizontallyMaxed && verticallyMaxed;
    }

    private boolean hasMiddleKingdomBonus() {  // checks if center tile is castle
        return isLocked5x5() && getTile(bounds.x1 + gridSize / 2, bounds.y1 + gridSize / 2).TYPE == Tile.TileTypes.CASTLE;
    }

    private boolean hasHarmonyBonus() { // checks if all tiles are filled
        for (int x = bounds.x1; x < bounds.x1 + gridSize; x++)
            for (int y = bounds.y1; y < bounds.y1 + gridSize; y++)
                if (getTile(x, y).TYPE == Tile.TileTypes.EMPTY)
                    return false;
        return true;
    }
    
    private boolean hasUnplayableAreas() {
    	if (!isLocked5x5()) return false;
		for (int x = bounds.x1; x < bounds.x2; x++)
            for (int y = bounds.y1; y < bounds.y2; y++) {
            		Tile tile = getTile(x, y);
                if (tile.TYPE == Tile.TileTypes.EMPTY && !tileChecker(tile, x, y))
                    return true;
            }
		return false;
    }

    public int getCachedScore() {
        return score;
    }
    
    private boolean middleKingdomIsPossible() {
    	Point2D.Double maxOffset = new Point2D.Double((gridSize - trueBounds.getWidth()) / 2.0, (gridSize - trueBounds.getHeight()) / 2.0);
    	double centerCoord = grid.length / 2.0;
    	return Math.abs(bounds.getCenter().x - centerCoord) <= maxOffset.x && Math.abs(bounds.getCenter().y - centerCoord) <= maxOffset.y;
    }
    
    public static final int HARMONY_BONUS = 5;
    public static final int MIDDLE_KINGDOM_BONUS = 10;
    
    public int getScoreWithPotentialBonuses() {
    	int s = getScore();
    	if (!hasMiddleKingdomBonus() && middleKingdomIsPossible()) s += MIDDLE_KINGDOM_BONUS;
    	if (!hasHarmonyBonus() && !hasUnplayableAreas()) s += HARMONY_BONUS;
    	return s;
    }

    public int getScore() {

        Debug.threadCheck("Grid > getScore", false);

        int score = 0;
        scoringReset();
        if (hasHarmonyBonus())
            score += HARMONY_BONUS;
        if (hasMiddleKingdomBonus())
            score += MIDDLE_KINGDOM_BONUS;

        for (int x = getWest(); x < getEast(); x++) {
            for (int y = getNorth(); y < getSouth(); y++) {
                Tile t = getTile(x, y);
                if (t.isOccupied() && t.TYPE != Tile.TileTypes.CASTLE) {
                    int[] nc = new int[2];// holds number of tiles and crowns within a region
                    floodFillScore(x, y, t, nc);
                    score += nc[0] * nc[1];
                    scoresByType.put(t.TYPE, scoresByType.get(t.TYPE) + nc[0] * nc[1]);
                    totalCrowns += nc[1];
                    if (nc[0] > largestRegion) {
                        largestRegion = nc[0];
                    }
                }
            }
        }
        return this.score = score;
    }

    public void floodFillScore(int x, int y, Tile target, int[] nc) {
        if (x < 0 || x > 8 || y < 0 || y > 8)
            return;
        Tile tile = getTile(x, y);
        if (tile.TYPE != target.TYPE || tile.skipScoring)
            return;
        nc[0]++;
        nc[1] += tile.CROWNS;
        tile.skipScoring = true;
        floodFillScore(x + 1, y, target, nc);
        floodFillScore(x - 1, y, target, nc);
        floodFillScore(x, y + 1, target, nc);
        floodFillScore(x, y - 1, target, nc);
    }

    public void scoringReset() {
        totalCrowns = 0;
        largestRegion = 0;
        scoresByType = new HashMap<Tile.TileTypes, Integer>() {
            {
                put(Tile.TileTypes.MEADOW, 0);
                put(Tile.TileTypes.FOREST, 0);
                put(Tile.TileTypes.WHEAT, 0);
                put(Tile.TileTypes.LAKE, 0);
                put(Tile.TileTypes.MINES, 0);
                put(Tile.TileTypes.SWAMP, 0);
            }
        };

        for (Tile[] row : grid) {
            for (Tile t : row) {
                t.skipScoring = false;
            }
        }
    }

    public Map<String, Object> getCachedGraphicalMeasurements(GamePanel panel) {
        return cachedGraphicalMeasurements == null ? cachedGraphicalMeasurements = panel.getGridGraphicalMeasurements(this) : cachedGraphicalMeasurements;
    }

    public void deleteCachedGraphicalMeasurements() {
        cachedGraphicalMeasurements = null;
    }

    public Tile getTile(int x, int y) {
        return grid[y][x];
    }

    public void setTile(Tile t, int x, int y) {
        grid[y][x] = t.clone();
    }

    public int getLargestRegion() {
        return largestRegion;
    }

    public int getTotalCrowns() {
        return totalCrowns;
    }

    public void unhighlightAll() {
        for (Tile[] row : grid)
            for (Tile t : row)
                t.highlightColor = null;
    }

    private void highlight(Domino d, int x, int y, String color) {
        getTile(x, y).highlightColor = color;
        getTile(x + d.getTile2Offset().x, y + d.getTile2Offset().y).highlightColor = color;
    }

    public void autoHighlight(Domino d, int x, int y) {
        unhighlightAll();
        Point tile2Offset = d.getTile2Offset();
        if (canPlay(d, x, y)) {
            highlight(d, x, y, Tile.VALID_HIGHLIGHT_COLOR);
        } else if (coordIsInBounds(x, y) && coordIsInBounds(x + tile2Offset.x, y + tile2Offset.y)) {
            highlight(d, x, y, Tile.INVALID_HIGHLIGHT_COLOR);
        }
    }

    public Rect getBounds() {
        return bounds;
    }

    public int getNorth() {
        return bounds.y1;
    }

    public int getEast() {
        return bounds.x2;
    }

    public int getSouth() {
        return bounds.y2;
    }

    public int getWest() {
        return bounds.x1;
    }

    public Tile[][] getArray() {
        return grid;
    }

    @Override
    public Grid clone() {
        Grid temp = new Grid(gridSize);
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++)
                temp.grid[i][j] = grid[i][j].clone();
        }
        	temp.recalculateBounds();
        return temp;
    }

}
