package org.peterbarta.scai.bot;

import bwapi.BWClient;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Player;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import java.util.HashSet;
import java.util.Set;

public class JavaBot extends DefaultBWListener {
    private final Set<Unit> claimedMineral = new HashSet<>();
    BWClient bwClient;
    Game game;
    Player player;
    boolean supplyBuild = false;
    private int supplyCap;

    private void gatheringMinerals(final Game game) {
        for (final Unit unit : game.self().getUnits()) {
            if (unit.isIdle() && unit.getType().isWorker()) {
                final Unit mineral = game.getClosestUnit(unit.getPosition(), 512,
                        u -> u.getType().isMineralField() && !claimedMineral.contains(u));
                unit.gather(mineral);
                claimedMineral.add((mineral));
            }
        }
    }

    //Print some text to screen
    @Override
    public void onFrame() {
        game.drawTextScreen(20, 20, "Created: Peter Barta " + "\u00a9" + " GitHub: peterbartahu");
        final Game game = bwClient.getGame();
        //trainSCV(player);
        //if (!buildSupplyDepot() && player.supplyTotal() - player.supplyUsed() > 2) {
        //    trainSCV(player);
        //}
        gatheringMinerals(game);
        if (!supplyBuild && game.self().minerals() >= 100) {
            System.out.println("NO SUPPLYBUILD AND MINERALS 100+");
            final TilePosition buildPosition = game.getBuildLocation(UnitType.Terran_Supply_Depot, game.self().getStartLocation());
            if (buildPosition.isValid(game)) {
                System.out.println("VALID POSITION FOUND");
                for (final Unit unit : player.getUnits()) {
                    if (unit.getType().isWorker() && (unit.isIdle() || !(unit.isCarryingMinerals()))) {
                        unit.build(UnitType.Terran_Supply_Depot);
                        supplyBuild = true;
                        break;
                    }
                }
            }
        }
        //if (game.self().supplyUsed() + 2 >= game.self().supplyTotal() && game.self().supplyTotal() > supplyCap) {
        //    game.self().getUnits().stream()
        //            .filter(u -> u.canTrain(UnitType.Terran_SCV))
        //            .findFirst()
        //            .ifPresent(u -> u.train(UnitType.Terran_SCV));
        //}
    }
    // Train units while we can
    //void trainSCV(final Player player) {
    //    if (game.self().minerals() >= 50) {
    //        System.out.println("I have 50 minels");
    //        for (final Unit unit : player.getUnits()) {
    //            if (unit.getType() == UnitType.Terran_Command_Center) {
    //                System.out.println("FIND COMMAND CENTER-----------");
    //                if (game.canMake(UnitType.Terran_SCV, unit)) {
    //                    System.out.println("I CAN MAKE 1 SCV---------");
    //                    if (!unit.isTraining()) {
    //                        System.out.println("NOW NOT TRAINING SCV SO TRAIN");
    //                        final int freeSupply = player.supplyTotal() - player.supplyUsed();
    //                        if (freeSupply > 2) {
    //                            unit.train(UnitType.Terran_SCV);
    //                            System.out.println("unit.train(UnitType.Terran_SCV) = ");
    //                        }
    //                    }
    //                }
    //            }
    //        }
    //    }
    //}
    //Let's order our workers to gather the mineral patch closest to them!
    // When a unit completes (even at the start of the game!), they trigger an onUnitComplete event.
    // We will use this to order our new units to gather minerals if they are workers.
    //@Override
    //public void onUnitComplete(final Unit unit) {
    //    if (unit.getType().isWorker()) {
    //        Unit closestMineral = null;
    //        int closestDistance = Integer.MAX_VALUE;
    //        for (final Unit mineral : game.getMinerals()) {
    //            final int distance = unit.getDistance(mineral);
    //            if (distance < closestDistance) {
    //                closestMineral = mineral;
    //                closestDistance = distance;
    //            }
    //        }
    //        // Gather the closest mineral
    //        unit.gather(closestMineral);
    //    }
    //}
    //Build supply
    //public boolean buildSupplyDepot() {
    //    final UnitType toBuild = player.getRace().getSupplyProvider(); //Supply depot
    //    Unit builder = null;
    //    if (player.supplyTotal() - player.supplyUsed() <= 2 && player.supplyTotal() <= 400) {
    //        for (final Unit unit : player.getUnits()) {
    //            if (unit.getType().isWorker() && (unit.isIdle() || unit.isGatheringMinerals())) {
    //                builder = unit;
    //                break;
    //            }
    //        }
    //    }
    //    final TilePosition buildLocation = game.getBuildLocation(toBuild, player.getStartLocation());
    //    if (builder != null) {
    //        final boolean result = builder.build(toBuild, buildLocation);
    //        System.out.println("buildSupplyDepot=" + result);
    //        return result;
    //    }
    //    System.out.println("buildSupplyDepot=false");
    //    return false;
    //}

    @Override
    public void onStart() {
        game = bwClient.getGame();
    }

    public void run() {
        bwClient = new BWClient(this);
        bwClient.startGame();
    }
}
