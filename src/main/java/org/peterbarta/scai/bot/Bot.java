package org.peterbarta.scai.bot;

import bwapi.BWClient;
import bwapi.DefaultBWListener;
import bwapi.Game;

public class Bot extends DefaultBWListener implements Runnable {
    private final BWClient client;

    public Bot() {
        client = new BWClient(this);
    }

    @Override
    public void run() {
        client.startGame();
    }

    protected Game game() {
        return client.getGame();
    }
}
