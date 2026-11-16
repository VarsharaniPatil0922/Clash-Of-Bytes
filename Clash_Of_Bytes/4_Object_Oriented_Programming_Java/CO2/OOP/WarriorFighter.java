package CO2.OOP;




/*
 * Subject: Problem Solving Using OOP (Java)
 * Course Outcome: CO4 | Practical 04
 * Topic: Single Inheritance (WarriorFighter extends BaseFighter)
 */
public class WarriorFighter extends BaseFighter {
    protected double shieldDefense;

    public WarriorFighter(String name, double hp, double atk, double shield) {
        super(name, hp, atk); // Calling superclass constructor
        this.shieldDefense = shield;
    }

    @Override
    public void performSpecialMove() {
        System.out.printf("  [POLYMORPHIC MOVE] %s unleashes Titan Shield Bash! (Absorbs damage and stuns opponent)%n", this.name);
    }

    public double getShieldDefense() {
        return shieldDefense;
    }

    // Compile-time polymorphism using method overloading
    public void attack() {
        System.out.printf("  [BASIC ATTACK] %s swings their weapon for %.1f damage!%n", this.name, this.baseAttack);
    }

    public void attack(double multiplier) {
        double damage = this.baseAttack * multiplier;
        System.out.printf("  [POWER ATTACK] %s swings their weapon with %.1fx force for %.1f damage!%n", this.name, multiplier, damage);
    }

    public void attack(double attackPower, double targetDefense) {
        double damage = Math.max(MINIMUM_DAMAGE, attackPower - targetDefense);
        System.out.printf("  [TACTICAL ATTACK] %s bypasses %.1f defense to deal %.1f damage!%n", this.name, targetDefense, damage);
    }
}
