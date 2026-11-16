package CO2.OOP;




/*
 * Subject: Problem Solving Using OOP (Java)
 * Course Outcome: CO4 | Practical 04
 * Topic: Multiple Inheritance in Java (PaladinFighter implements ShieldBearer, ElementalAttacker)
 * Note: Resolves Diamond Problem in Java by inheriting multiple interface contracts.
 */
public class PaladinFighter extends BaseFighter 
        implements ShieldBearer, ElementalAttacker {
    
    private String affinity;
    private double faithArmor;

    public PaladinFighter(String name, double hp, double atk, String affinity, double faithArmor) {
        super(name, hp, atk);
        this.affinity = affinity;
        this.faithArmor = faithArmor;
    }

    // ShieldBearer Interface Implementation
    @Override
    public double blockIncomingDamage(double incomingDamage) {
        double mitigated = Math.max(0.0, incomingDamage - faithArmor);
        System.out.printf("  [MULTIPLE INHERITANCE] %s blocks damage: %.1f -> %.1f (Faith Armor: %.1f)%n",
                this.name, incomingDamage, mitigated, faithArmor);
        return mitigated;
    }

    @Override
    public void deployAegisBarrier() {
        System.out.printf("  [MULTIPLE INHERITANCE] %s deploys Aegis Holy Barrier!%n", this.name);
    }

    // ElementalAttacker Interface Implementation
    @Override
    public double castElementalStrike(String element, double baseAttack) {
        double elementalDamage = baseAttack * 1.6;
        System.out.printf("  [MULTIPLE INHERITANCE] %s channels %s Elemental Strike for %.1f damage!%n",
                this.name, element, elementalDamage);
        return elementalDamage;
    }

    @Override
    public String getElementalAffinity() {
        return affinity;
    }

    public double getFaithArmor() {
        return faithArmor;
    }

    @Override
    public void performSpecialMove() {
        System.out.printf("  [POLYMORPHIC MOVE] %s casts Radiant Holy Judgment Beam!%n", this.name);
    }
}
