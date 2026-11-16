package CO2.OOP;




/*
 * Subject: Problem Solving Using OOP (Java)
 * Course Outcome: CO4 | Practical 04
 * Topic: Multilevel Inheritance (BerserkerWarrior extends WarriorFighter extends BaseFighter)
 */
public class BerserkerWarrior extends WarriorFighter {
    private double rageMultiplier;

    public BerserkerWarrior(String name, double hp, double atk, double shield, double rage) {
        super(name, hp, atk, shield);
        this.rageMultiplier = rage;
    }

    public double getRageMultiplier() {
        return rageMultiplier;
    }
}
