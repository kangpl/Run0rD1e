package ch.epfl.sdp.items;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.epfl.sdp.entity.Player;
import ch.epfl.sdp.entity.PlayerManager;
import ch.epfl.sdp.game.Game;
import ch.epfl.sdp.game.GameThread;
import ch.epfl.sdp.item.Scan;
import ch.epfl.sdp.item.Shield;
import ch.epfl.sdp.item.Shrinker;
import ch.epfl.sdp.item.TimedItem;
import ch.epfl.sdp.map.MockMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimedItemTest {
    public final int countTime = 2;
    Player user;

    @Before
    public void setup() {
        Game.getInstance().setMapApi(new MockMap());
        Player player = new Player("","");
        PlayerManager.getInstance().setCurrentUser(player);
        user = PlayerManager.getInstance().getCurrentUser();
    }

    @After
    public void teardown(){
        PlayerManager.getInstance().clear();
    }

    @Test
    public void timedItemCounterIsDecrementedBy1WhenUpdated(){
        TimedItem timedItem = new Shield(countTime) {};

        for(int i = countTime*GameThread.FPS; i > 0; --i) {
            assertEquals(timedItem.getRemainingTime(), i/ GameThread.FPS);
            System.out.println(Math.floorDiv(i,GameThread.FPS) + " i =  " + i  +  " " +  timedItem.getRemainingTime());
            timedItem.update();
            assertEquals(timedItem.getRemainingTime(), (i-1)/GameThread.FPS);
        }
    }

    @Test
    public void timedItemIsDeletedFromTheListWhenTimeIsFinished(){
        TimedItem timedItem = new Shield(countTime) {};
        Game.getInstance().addToUpdateList(timedItem);

        for(int i = countTime*GameThread.FPS; i > 0; --i) {
            timedItem.update();
        }

//        timedItem.update();
        //assertFalse(Game.getInstance().updatablesContains(timedItem));
    }

    @Test
    public void scanGetsUpdated(){
        MockMap map = new MockMap();
        Game.getInstance().setMapApi(map);
        PlayerManager.getInstance().addPlayer(user);
        Scan scan = new Scan(countTime);
        scan.useOn(user);

        while (scan.getRemainingTime() > 0){
            assertFalse(map.getDisplayables().isEmpty());
            scan.update();
        }

        // getRemainingTime is in seconds so we still have some frames
        for(int i = GameThread.FPS; i > 0; --i){
            assertFalse(map.getDisplayables().isEmpty());
            scan.update();
        }

        assertTrue(map.getDisplayables().isEmpty());
    }

    @Test
    public void shieldSetsShieldedWhenUpdated(){
        assertFalse(user.isShielded());
        Shield shield = new Shield(countTime);
        shield.useOn(user);

        while (shield.getRemainingTime() > 0){
            assertTrue(user.isShielded());
            shield.update();
        }

        // getRemainingTime is in seconds so we still have some frames
        for(int i = GameThread.FPS; i > 0; --i){
            assertTrue(user.isShielded());
            shield.update();
        }

        assertFalse(user.isShielded());
    }

    @Test
    public void shrinkerChangesAOERadiusBack(){
        Double originalRadius = user.getAoeRadius();
        int removeAoeRadius = 10;
        Shrinker shrinker = new Shrinker(countTime, removeAoeRadius);
        shrinker.useOn(user);

        while (shrinker.getRemainingTime() > 0){
            assertTrue(user.getAoeRadius() == originalRadius - removeAoeRadius);
            shrinker.update();
        }

        // getRemainingTime is in seconds so we still have some frames
        for(int i = GameThread.FPS; i > 0; --i){
            assertTrue(user.getAoeRadius() == originalRadius - removeAoeRadius);
            shrinker.update();
        }

        assertTrue(user.getAoeRadius() == originalRadius);
    }
}