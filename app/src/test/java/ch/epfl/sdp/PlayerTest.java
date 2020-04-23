package ch.epfl.sdp;

import org.junit.Before;
import org.junit.Test;

import ch.epfl.sdp.entity.EntityType;
import ch.epfl.sdp.entity.Player;
import ch.epfl.sdp.entity.PlayerManager;
import ch.epfl.sdp.game.Game;
import ch.epfl.sdp.item.Healthpack;
import ch.epfl.sdp.map.Displayable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class PlayerTest {
    private Player player1; //player position is in Geneva
    private Game game;

    @Before
    public void setup(){
        game = new Game();
        PlayerManager playerManager = new PlayerManager();
        player1 = new Player(6.149290, 46.212470, 50, "Skyris", "test@email.com");
        PlayerManager.setUser(player1);
    }

    @Test
    public void otherMethodTest() {
        assertTrue(player1.isAlive());
        assertEquals("Skyris", player1.getUsername());
        assertEquals(0, player1.getSpeed(), 0.001);
        assertEquals(0, player1.getTimeTraveled(), 0.001);
        assertEquals(0, player1.getGeneralScore());
        assertEquals(0, player1.getDistanceTraveled(), 0.001);
        assertEquals("test@email.com", player1.getEmail());
    }

    @Test
    public void healthPackUseTest() {
        Healthpack healthpack = new Healthpack(1);

        PlayerManager.getUser().setHealthPoints(10);
        healthpack.use();

        assertTrue(PlayerManager.getUser().getHealthPoints() == 11);
    }

    @Test
    public void getEntityTypeReturnsUser() {
        Displayable currentPlayer = new Player(0,0,0,"temp", "fake");
        assertEquals(EntityType.USER, currentPlayer.getEntityType());
    }

    @Test
    public void scoreIncreasesOnDisplacementWithTime() throws InterruptedException {
        assertEquals(0, player1.generalScore);
        assertEquals(0, player1.currentGameScore);
        game.initGame();
        Thread.sleep(13000);
        assertEquals(10, player1.currentGameScore);
        player1.distanceTraveled += 5000;
        Thread.sleep(13000);
        assertEquals(30, player1.currentGameScore);
        game.destroyGame();
        assertEquals(80, player1.generalScore);
        assertEquals(0, player1.currentGameScore);
    }
}
