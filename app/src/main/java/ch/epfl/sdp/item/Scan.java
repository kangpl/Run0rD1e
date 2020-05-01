package ch.epfl.sdp.item;

import ch.epfl.sdp.entity.EntityType;

import ch.epfl.sdp.entity.Player;
import ch.epfl.sdp.entity.PlayerManager;
import ch.epfl.sdp.game.Game;
import ch.epfl.sdp.geometry.GeoPoint;
import ch.epfl.sdp.map.Displayable;
import ch.epfl.sdp.map.MapApi;
import ch.epfl.sdp.map.MapsActivity;
import ch.epfl.sdp.map.Renderer;

public class Scan extends TimedItem  {
    private int scanTime;
    private Renderer renderer;

    public Scan(int scanTime) {
        super(String.format("Scan (%d)", scanTime), String.format("Item that scans the entire map and reveals other players for %d seconds", scanTime), scanTime);
        this.scanTime = scanTime;
    }

    @Override
    public Item clone() {
        return new Scan(scanTime);
    }

    @Override
    public void use() {
        super.use();

        for (Player p : PlayerManager.getPlayers()) {
             p.displayOn(Game.getInstance().getMapApi());
        }
    }

    @Override
    public void stopUsing() {
        for (Player p : PlayerManager.getPlayers()) {
            p.unDisplayOn(Game.getInstance().getMapApi());
        }
    }

    public EntityType getEntityType() {
        return EntityType.SCAN;
    }



}
