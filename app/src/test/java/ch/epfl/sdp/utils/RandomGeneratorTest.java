package ch.epfl.sdp.utils;

import org.junit.Before;
import org.junit.Test;

import ch.epfl.sdp.entity.Enemy;
import ch.epfl.sdp.entity.Player;
import ch.epfl.sdp.entity.ShelterArea;
import ch.epfl.sdp.geometry.GeoPoint;
import ch.epfl.sdp.item.Healthpack;
import ch.epfl.sdp.item.Scan;
import ch.epfl.sdp.item.Shield;
import ch.epfl.sdp.item.Shrinker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RandomGeneratorTest {
    private RandomGenerator randGen;

    @Before
    public void setup(){
        randGen = new RandomGenerator();
    }

    @Test
    public void randomGeoPointTest() {
        GeoPoint g = randGen.randomGeoPoint();
        GeoPoint f = new GeoPoint(0,0);
        assertFalse(g.getLongitude() == f.getLongitude());
        assertFalse(g.getLatitude() == f.getLatitude());
    }

    @Test
    public void randomHealthPackTest() {
        Healthpack h = randGen.randomHealthPack();
        assertTrue(h.getValue() >=25 && h.getValue() <= 50);
    }

    @Test
    public void randomShieldTest() {
        Shield s = randGen.randomShield();
        assertTrue(s.getRemainingTime() >= 20);
        assertTrue(s.getRemainingTime() <= 30);
    }

    @Test
    public void randomShrinker() {
        for(int i = 0; i < 100; ++i){
            Shrinker s = randGen.randomShrinker();
            assertTrue(s.getRemainingTime() >= -1);
            assertTrue(s.getRemainingTime() <= 5);
            assertTrue(s.getShrinkingRadius() >= -1);
            assertTrue(s.getShrinkingRadius() <= 5);
        }
    }

    @Test
    public void randomScan() {
        Scan s = randGen.randomScan();
        assertTrue(s.getRemainingTime() <= 1);
        assertTrue(s.getRemainingTime() >= 0);
    }


    @Test
    public void randomCoinTest() {
        for (int i = 0; i<4; i++) {
            assertTrue(randGen.randomCoin(randGen.randomGeoPoint()).getValue() < 30);
        }
    }



    @Test
    public void randomShelterAreaTest() {
        GeoPoint g = randGen.randomGeoPoint();
        ShelterArea s = randGen.randomShelterArea(g);
        assertTrue(s.getAoeRadius() <= 70 && s.getAoeRadius() >= 60);
    }

}
