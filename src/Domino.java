import java.awt.*;

public class Domino implements Comparable<Domino> {
    public final Tile ONE, TWO;
    public final int number;
    public final CacheableImage[] ORIGINAL;
    private int rotation;

    public Domino(int number, Tile ONE, Tile TWO) {
        ORIGINAL = new CacheableImage[]{ONE.getImage(), TWO.getImage()};
        rotation = 1;
        this.number = number;
        this.ONE = ONE;
        this.TWO = TWO;
    }

    public Point getTile2Offset() {

        int xOffset = 0;
        int yOffset = 0;
        switch (getRotation()) {
            case 1 -> xOffset = 1;
            case 2 -> yOffset = 1;
            case 3 -> xOffset = -1;
            case 0 -> yOffset = -1;
        }
        return new Point(xOffset, yOffset);
    }

    public void rotate() {
    	rotate(1);
    }
    
    public void rotate(int rot) {
    	int degrees = rot * 90;
        ONE.rotate(degrees);
        TWO.rotate(degrees);
        rotation = Math.floorMod(rotation + rot, 4);
    }

    public int getRotation() {
        return rotation;
    }


    @Override
    public int compareTo(Domino o) {
        return Integer.compare(number, o.number);
    }

    @Override
    public Domino clone() {
        return new Domino(number, ONE.clone(), TWO.clone());
    }

	public void setRotation(int rot) {
		rotate(rot - getRotation());
	}
}
