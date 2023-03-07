import java.awt.*;
import java.util.HashMap;

public class Tile {

    public static final CacheableImage king = CacheableImage.loadJpg("king");
    public static HashMap<TileTypes, Color> mini;

    static {
        mini = new HashMap<>();
        mini.put(TileTypes.CASTLE, Color.RED);
        mini.put(TileTypes.MINES, new Color(61, 61, 61));
        mini.put(TileTypes.MEADOW, new Color(164, 203, 68));
        mini.put(TileTypes.FOREST, new Color(11, 138, 0));
        mini.put(TileTypes.WHEAT, new Color(254, 194, 46));
        mini.put(TileTypes.LAKE, new Color(20, 83, 218));
        mini.put(TileTypes.SWAMP, new Color(203, 174, 102));
        mini.put(TileTypes.EMPTY, Color.WHITE);
    }

    public final TileTypes TYPE;
    public final int CROWNS;
    public String highlightColor;
    public boolean skipScoring = false;
    private CacheableImage IMAGE;
    
    public static final String VALID_HIGHLIGHT_COLOR = "#00ff00";
    public static final String INVALID_HIGHLIGHT_COLOR = "#ff0000";

    public Tile() {
        this.TYPE = TileTypes.EMPTY;
        this.CROWNS = 0;
    }

    public Tile(TileTypes type, int crowns, CacheableImage buffImg) {
        this.TYPE = type;
        this.CROWNS = crowns;
        IMAGE = buffImg;
    }

    public Tile(TileTypes type, int crowns, String buffImg) {
        this(type, crowns, CacheableImage.loadJpg(buffImg));
    }
    
    public boolean canConnect(Tile tile) {
        if (this.TYPE == TileTypes.EMPTY)
            return tile.TYPE == TileTypes.EMPTY;
        if (this.TYPE == TileTypes.CASTLE || tile.TYPE == TileTypes.CASTLE)
            return true;
        return this.TYPE == tile.TYPE;
    }

    public boolean isOccupied() {
        return TYPE != TileTypes.EMPTY;
    }

    public void rotate(int degrees) {
        if (IMAGE == null)
            return;
        IMAGE = CacheableImage.rotateByDegrees(IMAGE, degrees);
    }

    public CacheableImage getImage() {
        return IMAGE;
    }

    @Override
    public String toString() {
        return TYPE + " || " + CROWNS;
    }

    @Override
    public Tile clone() {
        return new Tile(TYPE, CROWNS, IMAGE);
    }


    public enum TileTypes {
        EMPTY, MEADOW, FOREST, WHEAT, LAKE, MINES, SWAMP, CASTLE
    }
}
