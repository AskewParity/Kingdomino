
import java.awt.*;
import java.awt.geom.Point2D;

public class Rect {
    public int x1, y1, x2, y2;

    public Rect(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public int getWidth() {
        return x2 - x1;
    }

    public int getHeight() {
        return y2 - y1;
    }

    public boolean pointIsInside(Point p) {
        return pointIsInside(p.x, p.y);
    }

    public boolean pointIsInside(int x, int y) {
        return (y >= y1 && y < y2 && x >= x1 && x < x2);
    }
    
    public Point2D.Double getCenter() {
    	return new Point2D.Double(x1 + getWidth() / 2.0, y1 + getHeight() / 2.0);
    }
    
    
    
    @Override
   	public int hashCode() {
   		final int prime = 31;
   		int result = 1;
   		result = prime * result + x1;
   		result = prime * result + x2;
   		result = prime * result + y1;
   		result = prime * result + y2;
   		return result;
   	}
    
    @Override
    public boolean equals(Object o) {
    	if (o == this) { 
            return true; 
        }
        if (!(o instanceof Rect)) { 
            return false; 
        }
        	
        	Rect r = (Rect) o;
        
    	return x1 == r.x1 && x2 == r.x2 && y1 == r.y1 && y2 == r.y2;
    }
}
