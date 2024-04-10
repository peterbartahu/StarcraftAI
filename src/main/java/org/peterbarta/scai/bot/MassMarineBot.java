package org.peterbarta.scai.bot;

import static java.util.stream.Collectors.toList;
import bwapi.Color;
import bwapi.CoordinateType;
import bwapi.Game;
import bwapi.Player;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Value;

public class MassMarineBot extends Bot {
    @Value("${author}")
    String author;

    @Override
    public void onFrame() {
        showAuthor();
        gatherResources();
        spendResources();
    }

    private void spendResources() {
        buildSupplyCommand();
        trainWorkerCommand();
        buildBarracksCommand();
        trainMarineCommand();
    }

    private void trainMarineCommand() {
        final Optional<Unit> baracks = findBaracks();
        baracks.ifPresent(unit -> {
            if (!unit.isTraining()) {
                unit.train(UnitType.Terran_Marine);
            }
        });
    }

    private void buildBarracksCommand() {
        getAvailableBuilder().ifPresent(worker -> {
            final Optional<TilePosition> position = findBuildPosition(worker.getTilePosition(), UnitType.Terran_Barracks);
            position.ifPresent(p -> {
                if (worker.canBuild(UnitType.Terran_Barracks, p)) {
                    System.out.println("time to build barracks");
                    worker.build(UnitType.Terran_Barracks, p);
                }
            });
        });
    }

    private void buildSupplyCommand() {
        final int supplyTotal = player().supplyTotal();
        final int supplyUsed = player().supplyUsed();
        final int supplyBuildTheshold = 4;
        if (supplyTotal - supplyUsed <= supplyBuildTheshold) {
            getAvailableBuilder().ifPresent(worker -> {
                final Optional<TilePosition> position = findBuildPosition(worker.getTilePosition(), UnitType.Terran_Supply_Depot);
                position.ifPresent(p -> {
                    if (worker.canBuild(UnitType.Terran_Supply_Depot, p)) {
                        System.out.printf("time to build supply (%s/%s)%n", supplyUsed, supplyTotal);
                        worker.build(UnitType.Terran_Supply_Depot, p);
                    }
                });
            });
        }
    }

    private Optional<TilePosition> findBuildPosition(final TilePosition origin, final UnitType unitType) {
        final TilePosition position = game().getBuildLocation(unitType, origin);
        if (position.isValid(game())) {
            return Optional.of(position);
        }
        return Optional.empty();
    }

    private Optional<Unit> getAvailableBuilder() {
        return findAnyUnit(player(), filterUnitType(UnitType.Terran_SCV));
    }

    private void trainWorkerCommand() {
        final Optional<Unit> commandCenter = findCommandCenter(); //TODO inline variable
        commandCenter.ifPresent(unit -> {
            if (!unit.isTraining() && getAllScvs(player()) < 16) {
                unit.train(UnitType.Terran_SCV);
            }
        });
    }

    private int getAllScvs(final Player player) {
        return player.allUnitCount(UnitType.Terran_SCV);
    }

    private void gatherResources() {
        final List<Unit> idleWorkers = findAllIdleWorkers();
        idleWorkers.forEach(unit -> getDrawBox(unit, Color.White)); //TODO remove
        if (!idleWorkers.isEmpty()) {
            System.out.println("time to send idle worker to gather resources");
            System.out.println("\tidleWorkers=" + idleWorkers.size());
            final Optional<Unit> commandCenter = findCommandCenter();
            System.out.println("\tcommandCenter=" + (commandCenter.isPresent() ? "exists" : "not-exists"));
            commandCenter.ifPresent(cc -> {
                getDrawBox(cc, Color.Yellow); //TODO remove
                final List<Unit> minerals = findMinerals(commandCenter.get());
                if (!minerals.isEmpty()) {
                    System.out.println("\tminerals=" + minerals.size());
                    minerals.forEach(mineral -> getDrawBox(mineral, Color.Cyan)); //TODO remove
                    idleWorkers.forEach(worker -> {
                        Collections.shuffle(minerals);
                        worker.gather(minerals.get(0));
                    });
                }
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

    private Optional<Unit> findBaracks() {
        return findAnyUnit(player(), filterUnitType(UnitType.Terran_Barracks));
    }

    private void showAuthor() {
        game().drawTextScreen(20, 20, author);
    }

    private List<Unit> findAllIdleWorkers() {
        return player()
                .getUnits()
                .stream()
                .filter(filterUnitType(UnitType.Terran_SCV))
                .filter(Unit::isIdle)
                .collect(toList());
    }

    private Player player() {
        return game().self();
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
