package org.openjfx.model;

import org.junit.Assert;
import org.junit.Test;
import org.openjfx.model.entity.Movable;
import org.openjfx.model.entity.Player;

/*Author: Carl Manngard, Patrik Emanuelsson, Edward Karlsson, Johan Davidsson
  Responsibility:
  Used by:
  Uses:
  */

public class PlayerTest {

    private final Player player = new Player("1", 0, 0, 100, 50, 32 , 1);

    @Test
    public void createPlayerTest(){
        Assert.assertEquals(player.getHp(), 100);
        Assert.assertEquals(player.getExp(), 0);
        Assert.assertEquals(player.getId(), "1");
        Assert.assertEquals(player.getYCoord(), 0,0);
        Assert.assertEquals(player.getXCoord(), 0,0);
        Assert.assertEquals(player.getAtkRange(), 32,0);
        Assert.assertEquals(player.getAtk(), 50);
    }

    @Test   //Test of Hp methods
    public void hpTest(){
        // Test of decHp when decAmount < Hp
        player.decHp(50);
        Assert.assertEquals(player.getHp(), 50);
    }

    @Test   //Test of Exp methods
    public void expTest(){
        player.incExp(200);
        player.decExp(50);

        Assert.assertEquals(player.getExp(), 150);
    }

    @Test   //  Test of move functions
    public void moveTest(){
        final double x = player.getXCoord();
        final double y = player.getYCoord();

        player.move(Movable.Direction.UP);
        player.move(Movable.Direction.DOWN);
        player.move(Movable.Direction.LEFT);
        player.move(Movable.Direction.RIGHT);

        Assert.assertEquals(player.getXCoord(), x,0);
        Assert.assertEquals(player.getYCoord(), y,0);
    }
}