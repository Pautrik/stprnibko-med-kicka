package org.openjfx.model;


//  This is the Player class, with variables related to the player . Obs: Should also inherit Interfaces in the future
public class Player extends Combatant {
    private int exp = 0;
    private Item[] inventory = new Item[4];
    private int selectedItem = 0;
    private double prevXCoord;
    private double prevYCoord;

    public int getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
        resetStats();
        itemBonus(inventory[selectedItem -1]);
    }

    public Player(String id, double xCoord, double yCoord, int hitpoints, int attack, float atkRange, int defense) {
        super(id, xCoord, yCoord, hitpoints, attack, atkRange, defense);
        this.moveSpeed = 1.0/3.0;
    }

    public String getId() {
        return id;
    }


    public int getHp() {
        return hp;
    }

    public int getAtk(){
        return atk;
    }

    public double getAtkRange() {
        return atkRange;
    }

    public void decHp(int decAmount) {

        hp = hp - decAmount;

    }

    public void incHp(int incAmount){

        hp = hp + incAmount;

    }

    public int getExp() {
        return exp;
    }

    public void incExp(int incAmount){
        exp = exp + incAmount;
    }

    public void decExp(int decAmount){
        exp = exp - decAmount;
    }

    public Item[] getInventory() {
        Item [] inventoryCopy = new Item[4];
        System.arraycopy(inventory, 0, inventoryCopy, 0, inventory.length);
        return inventoryCopy;
    }

    public void setItem(Item item, int index) {

        this.inventory[index] = item;
    }

    public void itemBonus(Item item) {

        if(item instanceof Weapon){
            this.atk += ((Weapon) item).getAtk();
        }
        else if(item instanceof Armor){
            this.def += ((Armor) item).getDef();
        }

    }

    public boolean canAttack(){
        if (attackCooldownTicker > 0){
            return true;
        }
        attackCooldownTicker++;
        return false;
    }

    private void setPrevCoords(double x, double y) {
        this.prevXCoord = x;
        this.prevYCoord = y;
    }

    @Override
    public void move(Direction direction) {
        setPrevCoords(this.getXcoord(), this.getYcoord());
        super.move(direction);
    }

    public double getPrevXCoord() {
        return this.prevXCoord;
    }

    public double getPrevYCoord() {
        return this.prevYCoord;
    }

    public void resetStats(){
        this.atk = 20;
        this.def = 0;
    }
}


