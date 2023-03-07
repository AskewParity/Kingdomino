// Animates a single numeric property

public abstract class Animation {

    public final String propertyName;
    public final boolean deletePropertyOnEnd;
    public final double startValue;
    public final double endValue;
    public final Animator.TweenTypes tweenType;
    public final double delay;
    public final double duration;
    public double localTime = 0.0;

    public Animation(String propertyName, boolean deletePropertyOnEnd, Animator.TweenTypes tweenType, double startValue, double endValue, double delay, double duration) {
        this.propertyName = propertyName;
        this.tweenType = tweenType;
        this.startValue = startValue;
        this.endValue = endValue;
        this.delay = delay;
        this.duration = duration;
        this.deletePropertyOnEnd = deletePropertyOnEnd;
    }

    public abstract void animationEnd();
}
