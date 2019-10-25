package org.openjfx.model;

import org.openjfx.model.entity.*;
import org.openjfx.model.noise.DefaultNoiseGenerator;
import org.openjfx.model.noise.NoiseGenerator;
import org.openjfx.model.entity.tile.Tile;
import org.openjfx.model.entity.tile.TileFactory;

import java.util.*;

/*Author: Carl Manngard, Patrik Emanuelsson, Edward Karlsson, Johan Davidsson
  Responsibility:
  Used by:
  Uses:
  */

public class World {
    private final TileFactory tileFactory;
    final LinkedList<LinkedList<Tile>> worldGrid;
    private final double worldVerticalSideLength;
    final double worldHorizontalSideLength;

    private final double activeDistance = 22;
    final private List<Combatant> activeEnemies = new ArrayList<>();
    final private Map<Coordinates, Combatant> inactiveEnemies = new HashMap<>();
    final private List<Chest> activeChests = new ArrayList<>();
    final private Map<Coordinates, Chest> inactiveChests = new HashMap<>();
    final private List<Combatant> players = new ArrayList<>();
    public final Player player;

    public World() {
        this(null);
    }

    /*Initiates int worldHorizontalSideLength rows for the grid matrix,
    then fills every row with int worldVerticalSideLength Tile Objects,
    the center of worldGrid matrix is set to coordinates 0,0

    Initiates inactiveEnemies and inactiveChests with a large pool of enemies/chests
     to be loaded in when their respective Coordinate coords come into activeDistance of Player
    */

    public World(NoiseGenerator noiseGenerator) {
        if (noiseGenerator != null) {
            tileFactory = new TileFactory(noiseGenerator);
        } else {
            tileFactory = new TileFactory(new DefaultNoiseGenerator());
        }
        ChestFactory chestFactory = new ChestFactory();
        EnemyFactory enemyFactory = new EnemyFactory();



        player = new Player("Player", 0.05, 0.05, 100, 20, 2, 0);
        players.add(player);

        this.worldHorizontalSideLength = 23;
        this.worldVerticalSideLength = 15;

        int spawnAreaSide = 5000;
        for (int i = 0; i < 100000; i++) {
            Combatant enemy = enemyFactory.generateEnemy(spawnAreaSide);
            inactiveEnemies.put((enemy.getCoords()), enemy);
        }

        for (int i = 0; i < 100000; i++) {
            Chest chest = chestFactory.generateChest(spawnAreaSide);
            inactiveChests.put((chest.getCoords()), chest);
        }

        double xCoord = 0 - ((worldHorizontalSideLength - 1) / 2) - 1;
        double yCoord;

        worldGrid = new LinkedList<>();
        for (int i = 0; i < worldHorizontalSideLength; i++) {
            LinkedList<Tile> worldRow = new LinkedList<>();
            xCoord++;
            yCoord = (0 - (worldVerticalSideLength - 1) / 2);
            for (int j = 0; j < worldVerticalSideLength + 1; j++) {
                worldRow.add(tileFactory.generateTile(xCoord, yCoord));
                yCoord++;
            }
            worldGrid.add(worldRow);
        }
    }


    public List<Combatant> combatantAttacks(Combatant attacker, List<Combatant> defenders) {
        List<Combatant> combatantsHit = new ArrayList<>();
        for (Combatant defender : defenders) {
            if (inSight(attacker, defender) && isEntityWithinDistance(defender, attacker, attacker.getAtkRange()) && attacker.canAttack()) {
                attacker.setAttackOnCooldown();
                combatantsHit.add(defender);
            }
        }
        return combatantsHit;
    }

    public boolean inSight(Combatant a, Entity b) {
        switch (a.getDirection()) {
            case UP:
                if (b.getYCoord() < (a.getYCoord() - 0.4)) {
                    return true;
                }
                break;
            case DOWN:
                if (b.getYCoord() > (a.getYCoord() + 0.4)) {
                    return true;
                }
                break;
            case LEFT:
                if (b.getXCoord() < (a.getXCoord() - 0.4)) {
                    return true;
                }
                break;
            case RIGHT:
                if (b.getXCoord() > (a.getXCoord() + 0.4)) {
                    return true;
                }
                break;
        }

        return false;
    }


    /*For each Combatant damaged in List<Combatant> hit, HP of damaged is decreased by Combatant attacker's
      atk value, if Combatant damaged's HP falls under 0 it is removed from world*/

    public void attackHit(Combatant attacker, List<Combatant> hit) {
        for (Combatant damaged : hit) {
            damaged.decHp(attacker.getAtk() - damaged.getDef());
            if (damaged.getHp() <= 0) {
                activeEnemies.remove(damaged);
            }

        }


    }
    /*Calculates the absolute distance between entity a and b using the pythagorean theorem and
     returns the corresponding double value */

    private double distance(Entity a, Entity b) {
        double xDist = Math.abs(a.getXCoord() - b.getXCoord());
        double yDist = Math.abs(a.getYCoord() - b.getYCoord());
        return Math.sqrt((yDist * yDist) + (xDist * xDist));
    }
    private boolean isPathFree(Combatant c1, Combatant c2){
        final ArrayList<Combatant> cs = new ArrayList<>();
        cs.add(c2);
        return isPathFree(c1, cs);
    }

    /*
     * This method checks if the path an entity is obstructed by either a tile or an opposing Combatant
     * First it measures which tiles are ahead of the entity, if they are solid it returns false
     * After that it goes through a list of all opposing Combatants and runs the isEntityInPath method */

    public boolean isPathFree(Combatant c, List<Combatant> e){

        double checkX1 = (worldHorizontalSideLength - 1)/2;
        double checkY1 = (worldVerticalSideLength - 1)/2;

        Tile center = worldGrid.get((int) checkX1).get((int) checkY1);

        checkX1 += (c.getXCoord() - center.getXCoord());
        checkY1 += (c.getYCoord() - center.getYCoord());

        double checkX2 = checkX1;
        double checkY2 = checkY1;

        final double s = c.getMoveSpeed() - 0.05;

        switch(c.getDirection()) {
            case UP:
                checkX1 += 0.05;
                checkX2 += 0.9;
                checkY1 -= s;
                checkY2 -= s;
                break;
            case DOWN:
                checkX2 += 0.9;
                checkY1 += 0.9 + s; // -0.05 to combat a potential rounding error
                checkY2 += 0.9 + s;
                break;
            case RIGHT:
                checkX1 += 0.9 + s;
                checkX2 += 0.9 + s;
                checkY2 += 0.9;
                break;
            case LEFT:
                checkX1 -= s;
                checkX2 -= s;
                checkY2 += 0.9;
                break;
        }

        if(checkX1 < 0 || checkX2 < 0 || checkY1 < 0 || checkY2 < 0 || checkX1 > 21 || checkX2 > 21 || checkY1 > 13 || checkY2 > 13){
            return false;
        }

        if (worldGrid.get((int) checkX1).get((int)checkY1).getISSolid()){
            return false;
        }
        else if (worldGrid.get((int) checkX2).get((int) checkY2).getISSolid()){
            return false;
        }

        for(Combatant en : e){

            if(isEntityInPath(c, en)){
                if(distance(c, en) < 1.1){
                    return false;
                }
            }
        }

        return true;

    }

    /*
     *    A method that takes in two Entities and checks if the target entity is in the path of the requester.
     */

    private boolean isEntityInPath(Combatant requester, Entity target){
        if(inSight(requester,target)) {
            switch (requester.getDirection()) {
                case UP:
                    if ((requester.getCoords().getXCoord() - 0.9) < target.getCoords().getXCoord() && target.getCoords().getXCoord() < (requester.getCoords().getXCoord() + 0.9) && (requester.getCoords().getYCoord() > target.getCoords().getYCoord())) {
                        return true;
                    }
                    break;
                case DOWN:
                    if ((requester.getCoords().getXCoord() - 0.9) < target.getCoords().getXCoord() && target.getCoords().getXCoord() < (requester.getCoords().getXCoord() + 0.9) && (requester.getCoords().getYCoord() < target.getCoords().getYCoord())) {
                        return true;
                    }
                    break;
                case LEFT:
                    if ((requester.getCoords().getYCoord() - 0.9) < target.getCoords().getYCoord() && target.getCoords().getYCoord() < (requester.getCoords().getYCoord() + 0.9) && (requester.getCoords().getXCoord() > target.getCoords().getXCoord())) {
                        return true;
                    }
                    break;
                case RIGHT:
                    if ((requester.getCoords().getYCoord() - 0.9) < target.getCoords().getYCoord() && target.getCoords().getYCoord() < (requester.getCoords().getYCoord() + 0.9) && (requester.getCoords().getXCoord() < target.getCoords().getXCoord())) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }


    /*Checks if the absolute distance between Entity requester and Combatant target.
    If less or equal to double range, returns true. If higher than double range, returns false  */

    public boolean isEntityWithinDistance(Entity target, Combatant requester, double range) {
        return distance(requester, target) <= range;
    }

    /*
     * This method runs the checkifEntitiesInactive and checkIfEntitiesActive methods.
     * Then it checks if any enemies are close enough to see and move towards the player, if they are and the pathIsFree method returns true it will move towards the player
     * If not it will attempt to move in another random direction, if pathIsFree method returns false it will simply not move
     */

    public void moveMobs() {
        checkIfEntitiesInactive();
        checkIfEntitiesActive();
        //Int to use for future random mob movement
        //int rand = (int)Math.ceil(Math.random() * 2);
        for (Combatant combatant : activeEnemies) {
            double enemyDetectDistance = 7;
            if (isEntityWithinDistance(player, combatant, enemyDetectDistance)) {
                if (player.getXCoord() + 0.9 < combatant.getXCoord()) {
                    combatant.setDirection(Movable.Direction.LEFT);
                    if(isPathFree(combatant, player)) {
                        combatant.move(Movable.Direction.LEFT);
                    }
                } else if (player.getXCoord() - 0.9 > combatant.getXCoord()) {
                    combatant.setDirection(Movable.Direction.RIGHT);
                    if(isPathFree(combatant, player)) {
                        combatant.move(Movable.Direction.RIGHT);
                    }
                }
                if (player.getYCoord() + 0.9 < combatant.getYCoord()) {
                    combatant.setDirection(Movable.Direction.UP);
                    if(isPathFree(combatant, player)) {
                        combatant.move(Movable.Direction.UP);
                    }
                } else if (player.getYCoord() - 0.9 > combatant.getYCoord()) {
                    combatant.setDirection(Movable.Direction.DOWN);
                    if(isPathFree(combatant, player)) {
                        combatant.move(Movable.Direction.DOWN);
                    }
                }
            }
            //  If mobs are not within distance the mobs shall move freely.
            else {
                int rand = (int) Math.ceil(Math.random() * 5);
                switch (rand) {
                    case 1:
                        combatant.setDirection(Movable.Direction.DOWN);
                        if(isPathFree(combatant, player)) {
                            combatant.move(Movable.Direction.DOWN);
                        }
                        break;
                    case 2:
                        combatant.setDirection(Movable.Direction.UP);
                        if(isPathFree(combatant, player)) {
                            combatant.move(Movable.Direction.UP);
                        }
                        break;
                    case 3:
                        combatant.setDirection(Movable.Direction.LEFT);
                        if(isPathFree(combatant, player)) {
                            combatant.move(Movable.Direction.LEFT);
                        }
                        break;
                    case 4:
                        combatant.setDirection(Movable.Direction.RIGHT);
                        if(isPathFree(combatant, player)) {
                            combatant.move(Movable.Direction.RIGHT);
                        }
                        break;
                    case 5:
                        break;
                }
            }
        }


    }


    /*Checks if entities in inactiveChests and inactiveEnemies have come into viewport,
    If they have come into viewport they are removed from inactiveChests or inactiveEnemies Map
    and added to activeChests or activeEnemies List*/

    private void checkIfEntitiesInactive() {
        List<Combatant> newlyInactiveEnemies = new ArrayList<>();
        for (Combatant combatant : activeEnemies) {
            if (!isEntityWithinDistance(combatant, player, activeDistance)) {
                newlyInactiveEnemies.add(combatant);
            }
        }
        for (Combatant combatant : newlyInactiveEnemies) {
            activeEnemies.remove(combatant);
            inactiveEnemies.put(combatant.getCoords(), combatant);
        }
        List<Chest> newlyInactiveChests = new ArrayList<>();
        for (Chest chest : activeChests) {
            if (!isEntityWithinDistance(chest, player, activeDistance)) {
                newlyInactiveChests.add(chest);
            }
        }
        for (Chest chest : newlyInactiveChests) {
            activeChests.remove(chest);
            inactiveChests.put(chest.getCoords(), chest);
        }
    }

    /*Checks if entities in activeChests and activeEnemies still are in viewport,
    If they are no longer in viewport they are stored in inactiveChests or inactiveEnemies Map and
    removed from activeChests or activeEnemies List*/

        private void checkIfEntitiesActive() {
            List<Combatant> newlyActive = new ArrayList<>();
            for (Combatant combatant : inactiveEnemies.values()) {
                if (isEntityWithinDistance(combatant, player, activeDistance)) {
                    activeEnemies.add(combatant);
                    newlyActive.add(combatant);
                }
            }
            for (Combatant combatant : newlyActive) {
                inactiveEnemies.remove(combatant.getCoords());
            }
            List<Chest> newlyActive2 = new ArrayList<>();
            for (Chest chest : inactiveChests.values()) {
                if (isEntityWithinDistance(chest, player, activeDistance)) {
                    activeChests.add(chest);
                    newlyActive2.add(chest);
                }
            }
            for (Chest chest : newlyActive2) {
                inactiveChests.remove(chest.getCoords());
            }
        }

        void updateWorldGrid() {
            final double playerXCoord = Math.round(this.player.getXCoord()); // To fix rounding error
            final double playerYCoord = Math.round(this.player.getYCoord()); // To fix rounding error

            final double maxYViewport = playerYCoord + (worldVerticalSideLength - 1) / 2;
            final double minYViewport = playerYCoord - (worldVerticalSideLength - 1) / 2;
            final double maxXViewport = playerXCoord + (worldHorizontalSideLength - 1) / 2;
            final double minXViewport = playerXCoord - (worldHorizontalSideLength - 1) / 2;

            final boolean shouldUpdateTiles =
                    Math.round(this.player.getPrevYCoord()) != playerYCoord
                 || Math.round(this.player.getPrevXCoord()) != playerXCoord;

            if(shouldUpdateTiles){
                switch (this.player.getDirection()) {
                    case UP:
                        for (LinkedList<Tile> column : worldGrid) {
                            column.removeLast();
                            column.addFirst(tileFactory.generateTile(column.getFirst().getXCoord(), minYViewport));
                        }
                        break;
                    case DOWN:
                        for (LinkedList<Tile> column : worldGrid) {
                            column.removeFirst();
                            column.addLast(tileFactory.generateTile(column.getFirst().getXCoord(), maxYViewport));
                        }
                        break;
                    case LEFT:
                        worldGrid.removeLast();
                        LinkedList<Tile> newFirstColumn = new LinkedList<>();
                        for (int y = (int) minYViewport; y <= (int) maxYViewport; y++) {
                            newFirstColumn.addLast(tileFactory.generateTile(minXViewport, y));
                        }
                        worldGrid.addFirst(newFirstColumn);
                        break;
                    case RIGHT:
                        worldGrid.removeFirst();
                        LinkedList<Tile> newLastColumn = new LinkedList<>();
                        for (int y = (int) minYViewport; y <= (int) maxYViewport; y++) {
                            newLastColumn.addLast(tileFactory.generateTile(maxXViewport, y));
                        }
                        worldGrid.addLast(newLastColumn);
                        break;
                }
            }
        }

        public List<Combatant> getPlayers () {
            return players;
        }

        public List<Combatant> getActiveEnemies () {
            return activeEnemies;
        }

        public List<Chest> getActiveChests () {
            return activeChests;
        }

        public Player getPlayer () {
            return player;
        }

        public LinkedList<LinkedList<Tile>> getWorldGrid () {
            return worldGrid;
        }

    }

