package ch.epfl.sdp.items;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import ch.epfl.sdp.entity.Player;
import ch.epfl.sdp.entity.PlayerManager;
import ch.epfl.sdp.game.Game;
import ch.epfl.sdp.geometry.GeoPoint;
import ch.epfl.sdp.item.Coin;
import ch.epfl.sdp.item.Healthpack;
import ch.epfl.sdp.item.Scan;
import ch.epfl.sdp.item.Shield;
import ch.epfl.sdp.item.Shrinker;
import ch.epfl.sdp.map.MockMap;
import ch.epfl.sdp.utils.RandomGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ItemsTest {
    private Player player;
    private GeoPoint A;
    private Healthpack healthpack;
    private int healthAmount;
    private Shield shield;
    private int shieldTime;
    private Shrinker shrinker;
    private Scan scan;
    private int scanTime;

    @Before
    public void setup(){
        RandomGenerator r = new RandomGenerator();
        Game.getInstance().setMapApi(new MockMap());
        player = new Player("Test Name", "test@email.com");
        PlayerManager.getInstance().addPlayer(player);
        PlayerManager.getInstance().setCurrentUser(player);
        A = new GeoPoint(6.14308, 46.21023);

        shieldTime = 40;
        healthAmount = 60;
        scanTime = 50;

        healthpack = new Healthpack(healthAmount);
        shield = new Shield(shieldTime);
        shrinker = new Shrinker( 40, 10);
        scan = new Scan(scanTime);
    }

    @After
    public void teardown(){
        PlayerManager.getInstance().clear();
        Game.getInstance().destroyGame();
    }

    @Test
    public void healthpackTest() {
        Healthpack healthpack = new Healthpack(1);

        PlayerManager.getInstance().getCurrentUser().setHealthPoints(10);
        healthpack.useOn(PlayerManager.getInstance().getCurrentUser());
        assertTrue(PlayerManager.getInstance().getCurrentUser().getHealthPoints() == 11);
    }

    @Test
    public void ifItemNotInInventoryNothingHappens() {
        Player player = new Player(6.149290, 46.212470, 50,
                "Skyris", "test@email.com"); //player position is in Geneva
        player.getInventory().removeItem(new Healthpack(0).getName());
        assertEquals(0, player.getInventory().getItems().size());
    }

    @Test
    public void scanTest() {
        assertTrue(scanTime == scan.getValue());
    }


    @Test
    public void shieldTest() {
        assertEquals(40, shield.getRemainingTime(), 0);
        assertTrue(shieldTime == shield.getValue());
    }

    @Test
    public void shrinkerTest() {
        assertEquals(40, shrinker.getRemainingTime(), 0);
        assertEquals(10, shrinker.getShrinkingRadius(), 0);
        assertTrue(shrinker.getShrinkingRadius() == shrinker.getValue());
    }


    @Test
    public void increaseHealth() {
        PlayerManager.getInstance().getCurrentUser().setHealthPoints(30);
        PlayerManager.getInstance().setCurrentUser(PlayerManager.getInstance().getCurrentUser());
        healthpack.useOn(PlayerManager.getInstance().getCurrentUser());
        assertEquals(90, PlayerManager.getInstance().getCurrentUser().getHealthPoints(), 0);
        healthpack.useOn(PlayerManager.getInstance().getCurrentUser());
        assertEquals(100, PlayerManager.getInstance().getCurrentUser().getHealthPoints(), 0);
        assertTrue(healthAmount == healthpack.getValue());
    }

    @Test
    public void coinTest() {
        PlayerManager.getInstance().getCurrentUser().removeMoney(PlayerManager.getInstance().getCurrentUser().getMoney());
        Coin c = new Coin(5, new GeoPoint(10,10));
        assertTrue(5 == c.getValue());
        c.useOn(PlayerManager.getInstance().getCurrentUser());
        assertTrue(PlayerManager.getInstance().getCurrentUser().getMoney() == 5);
    }
}