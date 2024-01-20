package org.peterbarta.scai;

import bwapi.BWClient;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Player;
import bwapi.Unit;
import bwapi.UnitType;

public class Main extends DefaultBWListener {
    BWClient bwClient;
    Game game;

    Player myAI;

    @Override
    public void onStart() {
        game = bwClient.getGame();
    }

    //Print some text to screen
    @Override
    public void onFrame() {
        game.drawTextScreen(20, 20, "Created: Peter Barta " + "\u00a9" + " GitHub: peterbartahu");
        myAI = game.self();
        game.drawTextScreen(20, 30, myAI.getName() + " has " + myAI.minerals() + " minerals");

        // Train SCV-s
        trainSCV(myAI);
    }


    // Train units while we can
    void trainSCV(Player myAI) {
        for (Unit commandCenter : myAI.getUnits()) {
            UnitType unitType = commandCenter.getType();
            if (unitType.isBuilding() || !unitType.buildsWhat().isEmpty()) {
                UnitType toTrain = unitType.buildsWhat().get(0);
                if (game.canMake(toTrain, commandCenter)) {
                    commandCenter.train(UnitType.Terran_SCV);
                }
            }
        }
    }

    //Let's order our workers to gather the mineral patch closest to them!
    // When a unit completes (even at the start of the game!), they trigger an onUnitComplete event.
    // We will use this to order our new units to gather minerals if they are workers.
    public void onUnitComplete(Unit unit) {
        if (unit.getType().isWorker()) {
            Unit closestMineral = null;
            int closestDistance = Integer.MAX_VALUE;
            for (Unit mineral : game.getMinerals()) {
                int distance = unit.getDistance(mineral);
                if (distance < closestDistance) {
                    closestMineral = mineral;
                    closestDistance = distance;
                }
            }
            // Gather the closest mineral
            unit.gather(closestMineral);
        }
    }


    void run() {
        bwClient = new BWClient(this);
        bwClient.startGame();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}