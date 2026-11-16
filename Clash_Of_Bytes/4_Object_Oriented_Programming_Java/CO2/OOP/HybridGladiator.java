package CO2.OOP;




/*
 * Subject: Problem Solving Using OOP (Java)
 * Course Outcome: CO4 | Practical 04
 * Topic: Hybrid Inheritance in Java (Combines Multilevel Class Inheritance + Multiple Interfaces)
 */
public class HybridGladiator extends BerserkerWarrior 
        implements ShieldBearer, ElementalAttacker {
    
    private String arenaOrigin;

    public HybridGladiator(String name, double hp, double atk, double shield, double rage, String origin) {
        super(name, hp, atk, shield, rage);
        this.arenaOrigin = origin;
    }

    @Override
    public double blockIncomingDamage(double incomingDamage) {
        double absorbed = Math.max(0.0, incomingDamage - (shieldDefense * 1.35));
        System.out.printf("  [HYBRID INHERITANCE] %s deflects attack with Titan Shield (Remains: %.1f dmg)%n",
                this.name, absorbed);
        return absorbed;
    }

    @Override
    public void deployAegisBarrier() {
        System.out.printf("  [HYBRID INHERITANCE] %s raises Unyielding Fortress Barrier from %s!%n",
                this.name, arenaOrigin);
    }

    @Override
    public double castElementalStrike(String element, double baseAttack) {
        double burstDamage = (baseAttack * getRageMultiplier()) + 15.0;
        System.out.printf("  [HYBRID INHERITANCE] %s executes Hybrid Elemental Rampage [%s] for %.1f damage!%n",
                this.name, element, burstDamage);
        return burstDamage;
    }

    @Override
    public String getElementalAffinity() {
        return "Chaos-Magma";
    }

    public String getArenaOrigin() {
        return arenaOrigin;
    }
}
