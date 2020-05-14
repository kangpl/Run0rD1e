package ch.epfl.sdp.geometry;

import ch.epfl.sdp.map.Displayable;

/**
 * Represents an area in the 2D plane.
 */
public abstract class Area implements Positionable, Displayable {
    protected GeoPoint center;
    protected GeoPoint oldCenter;
    protected GeoPoint newCenter;
    protected double time;
    protected double finalTime;
    protected boolean isShrinking;

    public Area(GeoPoint center) {
        this.center = center;
    }

    public boolean isShrinking() {
        return isShrinking;
    }

    public boolean isInside(GeoPoint geoPoint) {
        Vector vec = center.toVector().subtract(geoPoint.toVector());
        return isInside(vec);
    }

    /**
     * This method find a smaller area that fits entirely inside the current area
     *
     * @param factor it is a number we multiply with the current area's size to get the new size
     * @return A random area inside the current one
     */
    public abstract void shrink(double factor);

    protected abstract void setShrinkTransition();

    public void setTime(double time) {
        this.time = time;
    }

    public void setFinalTime(double finalTime) {
        this.finalTime = finalTime;
    }

    protected abstract boolean isInside(Vector vector);

    @Override
    public GeoPoint getLocation() {
        return center;
    }

    public GeoPoint getOldLocation() {
        return oldCenter;
    }

    public GeoPoint getNewLocation() {
        return newCenter;
    }

    public abstract GeoPoint randomLocation();

    public abstract void finishShrink();
}
