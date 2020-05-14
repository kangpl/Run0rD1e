package ch.epfl.sdp.logic;

import org.junit.Test;

import ch.epfl.sdp.geometry.GeoPoint;
import ch.epfl.sdp.geometry.CircleArea;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GameAreaTest {

    @Test
    public void shrinkWithNegativeOrTooBigFactorDoesNotDoAnything() {
        CircleArea circleArea = new CircleArea(100, new GeoPoint(40, 50));
        circleArea.shrink(-1);
        assertEquals(circleArea.getRadius(), 100, 0.01);
        circleArea.shrink(1.5);
        assertEquals(circleArea.getRadius(), 100, 0.01);
        assertFalse(circleArea.isShrinking());
    }

    @Test
    public void shrinkWorks() {
        GeoPoint oldCenter = new GeoPoint(40, 50);
        CircleArea oldCircleArea = new CircleArea(10000, oldCenter);
        oldCircleArea.shrink(0.5);
        assertEquals(oldCircleArea.getNewRadius(), 10000 * 0.5, 0.01);
        assertTrue(oldCenter.distanceTo(oldCircleArea.getNewLocation()) < 10000 * 0.5);
        assertEquals(oldCircleArea.getOldRadius(), 10000, 0.01);
        assertTrue(oldCenter.distanceTo(oldCircleArea.getOldLocation()) < 0.01);
    }

    @Test
    public void getShrinkTransitionWorks() {
        GeoPoint oldCenter = new GeoPoint(40, 50);
        CircleArea circle = new CircleArea(10000, oldCenter);
        circle.shrink(0.5);
        circle.setTime(0);
        circle.setFinalTime(2);
        circle.setShrinkTransition();
        assertEquals(10000, circle.getRadius(), 0.01);
        assertEquals(0, circle.getLocation().distanceTo(oldCenter), 0.01);

        circle.setTime(1);
        circle.setShrinkTransition();
        assertEquals(7500, circle.getRadius(), 0.01);
        assertEquals(circle.getOldLocation().distanceTo(circle.getNewLocation()) / 2, circle.getLocation().distanceTo(oldCenter), 10);

        circle.setTime(2);
        circle.setShrinkTransition();
        assertEquals(5000, circle.getRadius(), 0.01);
        assertEquals(0, circle.getOldLocation().distanceTo(circle.getNewLocation()), 0.01);
    }

    @Test
    public void getShrinkTransitionReturnsNullOnInvalidTime() {
        CircleArea circle = new CircleArea(10000, new GeoPoint(40, 50));
        circle.shrink(0.5);
        circle.setFinalTime(2);
        circle.setTime(-1);
        circle.setShrinkTransition();
        assertNull(circle.getOldLocation());
        circle.setTime(3);
        circle.setShrinkTransition();
        assertNull(circle.getOldLocation());
    }
}
