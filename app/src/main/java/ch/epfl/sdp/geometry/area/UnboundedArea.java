package ch.epfl.sdp.geometry.area;

import androidx.annotation.NonNull;

import ch.epfl.sdp.geometry.Vector;
import ch.epfl.sdp.map.display.MapApi;
import ch.epfl.sdp.map.location.GeoPoint;
import ch.epfl.sdp.utils.RandomGenerator;

/**
 * Represents an unbounded area. Any point in the 2D plane is inside the area.
 */
public class UnboundedArea extends Area {

    /**
     * The constructor for the unbounded area. We only need a center which can be random
     */
    public UnboundedArea() {
        super(new RandomGenerator().randomGeoPoint());
    }

    @Override
    public void shrink(double factor) {
        //we do not need to shrink anything here
    }

    @Override
    public void setShrinkTransition() {
        //there is no shrink transition here
    }

    @Override
    protected boolean isInside(Vector vector) {
        return true;
    }

    @Override
    public GeoPoint randomLocation() {
        return new RandomGenerator().randomGeoPoint();
    }

    @Override
    public void finishShrink() {
        //We do not need to do anything here
    }

    @Override
    public void updateGameArea(Area area) {
        setRemainingTimeString(area.getRemainingTimeString());
    }

    @NonNull
    @Override
    public String toString() {
        return "UnboundedArea " + this.remainingTimeString;
    }

    @Override
    public void displayOn(MapApi mapApi) {
        //We do not need to display it
    }
}
