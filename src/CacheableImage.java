import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class CacheableImage extends BufferedImage {
	
	private static int nextID = 0;
	
	private String id;
	
	public String getID() {
		return id;
	}
	public void setID(String i) {
		id = i;
	}
	public void setID() {
		id = Integer.toString(nextID);
		nextID++;
	}

	public CacheableImage(int width, int height, int imageType) {
		super(width, height, imageType);
		setID();
	}
	
	private double angleTracker = 0;

	private static final Map<String, LinkedHashMap<Rect, BufferedImage>> rotScaleCache = new HashMap<>();

	private static final int MAX_STORED_RESOLUTIONS = 5;

    public static CacheableImage rotateByDegrees(CacheableImage imageToRotate, double angle) {
    	
    	double newAngleTracker = imageToRotate.angleTracker;
    	
    	newAngleTracker += angle;
    	while (newAngleTracker >= 360) {
    		newAngleTracker -= 360;
    	}
    	while (newAngleTracker < 0) {
    		newAngleTracker += 360;
    	}
    	
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = imageToRotate.getWidth();
        int h = imageToRotate.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        CacheableImage rotated = new CacheableImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        	String imID = imageToRotate.getID();
        	int cutoffIndex = imID.indexOf('+');
        	rotated.setID((cutoffIndex > -1 ? imID.substring(0, cutoffIndex) : imID) + ((newAngleTracker == 0) ? "" : "+" + newAngleTracker));
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((double)(newWidth - w) / 2, (double)(newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(imageToRotate, 0, 0, null);
        g2d.dispose();
        
        	rotated.angleTracker = newAngleTracker;

        return rotated;
    }
	// scales images
	public static BufferedImage getScaledBufferedImage(CacheableImage imageToScale, int dWidth, int dHeight) {
	    BufferedImage scaledImage;
	    
	    Rect dRect = new Rect(0, 0, dWidth, dHeight);
	    
	    if (rotScaleCache.containsKey(imageToScale.getID()) && rotScaleCache.get(imageToScale.getID()).containsKey(dRect)) {
	    		scaledImage = rotScaleCache.get(imageToScale.getID()).get(dRect);
	    }
	    else {
	    
		    scaledImage = new BufferedImage(dWidth, dHeight, imageToScale.getType());
		    Graphics2D g2d = scaledImage.createGraphics();
		    
		    g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		    g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		    g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    
		    g2d.drawImage(imageToScale, 0, 0, dWidth, dHeight, null);
		    g2d.dispose();
		    
		    LinkedHashMap<Rect, BufferedImage> resMap;
		    if (rotScaleCache.containsKey(imageToScale.getID())) {
		    		resMap = rotScaleCache.get(imageToScale.getID());
		    }
		    else {
		    		resMap = new LinkedHashMap<>() {
			        @Override
			        protected boolean removeEldestEntry(final Map.Entry<Rect, BufferedImage> eldest) {
			            return size() > MAX_STORED_RESOLUTIONS;
			        }
			    };
		    }
		    resMap.put(dRect, scaledImage);
		    
		    rotScaleCache.put(imageToScale.getID(), resMap);
	    }
	
	
		return scaledImage;
	}
	public static CacheableImage loadJpg(String filename) {
	    return loadImage(filename + ".jpg");
	}
	public static CacheableImage loadImage(String filename) {
	    BufferedImage img = null;
	    try {
	        img = ImageIO.read(Tile.class.getResource("/Images/" + filename));
	    } catch (IOException E) {
	        System.out.println(E.getMessage());
	    }
	    
	    CacheableImage argbImage = new CacheableImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2d = argbImage.createGraphics();
	    g2d.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
	    g2d.dispose();
	
	    return argbImage;
	}

}
