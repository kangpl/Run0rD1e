package ch.epfl.sdp.item;

import ch.epfl.sdp.entity.PlayerManager;

public class Coin extends Item {
    private int value;

    public Coin(int value) {
        super(String.format("Coin of value %d", value), "Medium of exchange that allows a player to buy items in shops");
        this.value = value;
    }

    @Override
    public Item clone() {
        return new Coin(value);
    }

    @Override
    public void use() {
        PlayerManager.getCurrentUser().addMoney(value);
    }

    public int getValue() {
        return value;
    }

}
