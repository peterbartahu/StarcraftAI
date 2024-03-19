package org.peterbarta.scai.bot;

import static java.util.stream.Collectors.toList;
import bwapi.BWClient;
import bwapi.Color;
import bwapi.CoordinateType;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Player;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class Javabot1 extends DefaultBWListener {
    private final BWClient bwClient;
    private final Set<Unit> claimedMineral = new HashSet<>(); //TODO remove

    public Javabot1() {
        bwClient = new BWClient(this);
    }

    public void run() {
        bwClient.startGame();
    }

    //Print some text to screen
    @Override
    public void onFrame() {
        game().drawTextScreen(20, 20, "Created: Peter Barta " + "\u00a9" + " GitHub: peterbartahu");
        //gatheringMinerals(game);
        mineWithIdleWorkers();
        spendMinerals();
        //final Player player = player();
        //trainSCV(player);
        //buildSupplyDepot(player);
    }

    private void spendMinerals() {
        //TODO supply building
        final Optional<Unit> commandCenter = findCommandCenter(); //TODO inline variable
        commandCenter.ifPresent(unit -> {
            if (!unit.isTraining()) {
                unit.train(UnitType.Terran_SCV);
            }
        });
    }

    private void mineWithIdleWorkers() {
        final List<Unit> idleWorkers = findAllIdleWorkers();
        idleWorkers.forEach(unit -> getDrawBox(unit, Color.White)); //TODO remove
        if (!idleWorkers.isEmpty()) {
            System.out.println("idleWorkers=" + idleWorkers.size());
            final Optional<Unit> commandCenter = findCommandCenter();
            System.out.println("commandCenter=" + (commandCenter.isPresent() ? "exists" : "not-exists"));
            commandCenter.ifPresent(cc -> {
                getDrawBox(cc, Color.Yellow); //TODO remove
                final List<Unit> minerals = findMinerals(commandCenter.get());
                if (!minerals.isEmpty()) {
                    System.out.println("minerals=" + minerals.size());
                }
                minerals.forEach(mineral -> getDrawBox(mineral, Color.Cyan)); //TODO remove
                minerals.stream()
                        .findAny()
                        .ifPresent(mineral -> {
                            idleWorkers.forEach(worker -> worker.gather(mineral));
                        });
            });
        }
    }

    private List<Unit> findMinerals(final Unit unit) {
        return game()
                .getMinerals()
                .stream()
                .filter(mineral -> getTileDistance(unit, mineral) < 10.0)
                .collect(toList());
    }

    private void getDrawBox(final Unit unit, final Color color) {
        final Game game = game();
        game.drawBox(CoordinateType.Map, unit.getLeft(), unit.getTop(), unit.getRight(), unit.getBottom(), color);
    }

    private Optional<Unit> findCommandCenter() {
        return findAnyUnit(player(), filterUnitType(UnitType.Terran_Command_Center));
    }

    private List<Unit> findAllIdleWorkers() {
        return player()
                .getUnits()
                .stream()
                .filter(filterUnitType(UnitType.Terran_SCV))
                .filter(Unit::isIdle)
                .collect(toList());
    }

    // Train units while we can
    private void trainSCV(final Player myAI) {
        if (myAI.supplyTotal() - myAI.supplyUsed() > 2) {
            for (final Unit commandCenter : myAI.getUnits()) {
                final UnitType unitType = commandCenter.getType();
                if (unitType.isBuilding() || !unitType.buildsWhat().isEmpty()) {
                    final UnitType toTrain = unitType.buildsWhat().get(0);
                    if (game().canMake(toTrain, commandCenter)) {
                        commandCenter.train(UnitType.Terran_SCV);
                    }
                }
            }
        }
    }

    private void gatheringMinerals(final Game game) {
        for (final Unit unit : player().getUnits()) {
            if (unit.isIdle() && unit.getType().isWorker()) {
                final Unit mineral = game.getClosestUnit(unit.getPosition(), 512, u -> u.getType().isMineralField() && !claimedMineral.contains(u));
                unit.gather(mineral);
                claimedMineral.add((mineral));
            }
        }
    }

    private void buildSupplyDepot(final Player myAI) {
        if (myAI.supplyTotal() - myAI.supplyUsed() <= 2 && myAI.minerals() >= 100) {
            final Player player = player();
            final UnitType toBuild = player.getRace().getSupplyProvider(); //Supply depot
            Unit builder = null;
            for (final Unit unit : player.getUnits()) {
                if (unit.getType().isWorker() && (unit.isIdle() || unit.isGatheringMinerals())) {
                    builder = unit;
                    break;
                }
            }
            final TilePosition buildLocation = game().getBuildLocation(toBuild, player.getStartLocation());
            if (builder != null) {
                final boolean result = builder.build(toBuild, buildLocation);
                System.out.println("buildSupplyDepot=" + result);
            }
            System.out.println("buildSupplyDepot=false");
        }
    }

    private Player player() {
        return game().self();
    }

    private Game game() {
        return bwClient.getGame();
    }

    private static double getTileDistance(final Unit unit1, final Unit unit2) {
        return unit2.getTilePosition().subtract(unit1.getTilePosition()).getLength();
    }

    private static Optional<Unit> findAnyUnit(final Player player, final Predicate<Unit> predicate) {
        return player
                .getUnits()
                .stream()
                .filter(predicate)
                .findAny();
    }

    private static Predicate<Unit> filterUnitType(final UnitType unitType) {
        return unit -> unit.getType() == unitType;
    }
}
