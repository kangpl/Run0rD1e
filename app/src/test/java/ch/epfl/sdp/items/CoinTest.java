package ch.epfl.sdp.items;

import org.junit.Test;

import java.util.ArrayList;

import ch.epfl.sdp.entity.Player;
import ch.epfl.sdp.entity.PlayerManager;
import ch.epfl.sdp.game.Game;
import ch.epfl.sdp.geometry.GeoPoint;
import ch.epfl.sdp.item.Coin;
import ch.epfl.sdp.item.Item;
import ch.epfl.sdp.map.MockMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoinTest {

    private Player originalPlayer = PlayerManager.getInstance().getCurrentUser();

    private Player player;
    private GeoPoint l;
    private MockMap mockMap;

    @Test
    public void clonedCoinHasSameValue(){
        Coin dime = new Coin(10, new GeoPoint(10,10));
        Item clonedDime = dime.clone();
        assertTrue(dime.getValue() == ((Coin)clonedDime).getValue());
    }

    @Test
    public void userBankIncreasesWhenCoinUsed(){
        Player broke = new Player(20.0, 20.0, 100, "amroa", "amro.abdrabo@gmail.com");
        broke.removeMoney(broke.getMoney());
        PlayerManager.getInstance().setCurrentUser(broke);
        Coin dime  = new Coin(10, new GeoPoint(10,10));
        dime.useOn(broke);
        assertTrue(broke.getMoney() == dime.getValue());
    }

    @Test
    public void generateRandomCoinsTest() {
        ArrayList<Coin> gen = Coin.generateCoinsAroundLocation(new GeoPoint(10, 10),5);
        assertTrue(gen.size() == 5);
    }

    @Test
    public void coinDisappearsWhenPicked() {
        PlayerManager.getInstance().removeAll(); // Just to be sure that there are no players
        l = new GeoPoint(10,10);
        player = new Player("testPlayer","testPlayer@gmail.com");
        player.setLocation(l);
        PlayerManager.getInstance().setCurrentUser(player);
        mockMap = new MockMap();
        Game.getInstance().setMapApi(mockMap);
        Game.getInstance().setRenderer(mockMap);
        Game.getInstance().clearGame();
        Coin c = new Coin(10, l);
        Game.getInstance().addToUpdateList(c);
        Game.getInstance().addToDisplayList(c);
        assertTrue(Game.getInstance().updatablesContains(c));
        assertTrue(Game.getInstance().displayablesContains(c));
        assertFalse(c.isTaken());
        Game.getInstance().update();
        assertTrue(c.isTaken());
        PlayerManager.getInstance().removeAll();
    }

    @Test
    public void getLocationTest() {
        GeoPoint g = new GeoPoint(10,10);
        Coin c = new Coin(10, g);
        assertTrue(c.getLocation().equals(g));
    }


}
