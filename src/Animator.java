import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Animator {

    private final CopyOnWriteArrayList<Animation> animations = new CopyOnWriteArrayList<>();
    private final double MAX_DELTA;
    private final double SHUTDOWN_TIMEOUT = 1.0;
    public Map<String, Double> properties = new HashMap<>();
    private double oldSysTime = 0;
    private double lastFrameTime;
    private boolean idle = true;
    private final Timer timer;

    public Animator(int FPS) {

        // property values can't change by a greater magnitude between frames than they would during this time in seconds
        MAX_DELTA = 2.0 / FPS; // prevents choppiness when FPS drops below half the intended FPS

        // Swing timer, not java.util.Timer
        timer = new Timer(1000 / FPS, e -> {

            final double newSysTime = getTime();
            final double delta = Math.min(newSysTime - oldSysTime, MAX_DELTA);
            oldSysTime = newSysTime;

            if (idle) {
                //out.println("Animator idle");
                if (newSysTime - lastFrameTime >= SHUTDOWN_TIMEOUT) {
                    stopTimer();
                }
                return;
            }

            for (Animation a : animations) {

                a.localTime += delta;

                double progress = (a.localTime - a.delay) / a.duration;

                double currentValue = a.endValue;

                switch (a.tweenType) {
                    case LINEAR:
                        currentValue = lerp(a.startValue, a.endValue, progress);
                        break;
                    case SMOOTHSTEP:
                        currentValue = smoothstep(a.startValue, a.endValue, progress);
                        break;
                    case SMOOTHERSTEP:
                        currentValue = smootherstep(a.startValue, a.endValue, progress);
                        break;
                    default:
                        break;
                }
                properties.put(a.propertyName, currentValue);

                if (a.localTime >= a.duration + a.delay) {
                    removeAnimation(a, a.deletePropertyOnEnd);
                }
            }

            //out.println("Animator computed frame");

            lastFrameTime = getTime();

            update();
        });
    }

    // linear interpolation tween
    public static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    public static double smoothstep(double a, double b, double t) {
        return lerp(a, b, t * t * (3.0 - 2.0 * t));
    }

    public static double smootherstep(double a, double b, double t) {
        return lerp(a, b, t * t * t * (t * (t * 6.0 - 15.0) + 10.0));
    }

    public static double getTime() {
        return System.nanoTime() / 1000000000.0;
    }

    public abstract void update();

    private void startTimer() {
        //out.println("Animator started");
        oldSysTime = getTime();
        timer.start();
    }

    private void stopTimer() {
        timer.stop();
        //out.println("Animator stopped");
    }

    public Animation getAnimationByPropertyName(String animPropertyName) {

        for (Animation a : animations) {
            if (a.propertyName.equals(animPropertyName)) return a;
        }
        return null;
    }

    public void removeAnimation(Animation a, boolean doRemoveProperty) {

        (new Thread(a::animationEnd)).start();

        animations.remove(a);
        if (doRemoveProperty)
        		properties.remove(a.propertyName);

        if (animations.size() == 0/* && properties.size() == 0*/) {
            idle = true;
        }
    }
    public void removeAnimation(String animPropertyName, boolean doRemoveProperty) {
        removeAnimation(getAnimationByPropertyName(animPropertyName), doRemoveProperty);
    }
    public void removeAnimation(String animPropertyName) {
        removeAnimation(animPropertyName, true);
    }

    public void addAnimation(Animation a) {

        Debug.threadCheck("Animator > addAnimation", false);

        animations.add(a);
        properties.put(a.propertyName, a.startValue);
        idle = false;

        if (!timer.isRunning()) startTimer();
    }

    public enum TweenTypes {
        LINEAR, SMOOTHSTEP, SMOOTHERSTEP
    }
}
