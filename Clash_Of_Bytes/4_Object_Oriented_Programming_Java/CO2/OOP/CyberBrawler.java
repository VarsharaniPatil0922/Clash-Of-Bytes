package CO2.OOP;




/*
 * Subject: Problem Solving Using OOP (Java)
 * Course Outcome: CO4 | Practical 04
 * Topic: Hierarchical Inheritance - Branch B (CyberBrawler extends BaseFighter)
 */
public class CyberBrawler extends BaseFighter {
    private int cyberCoreEnergy;

    public CyberBrawler(String name, double hp, double atk, int energy) {
        super(name, hp, atk);
        this.cyberCoreEnergy = energy;
    }

    @Override
    public void performSpecialMove() {
        System.out.printf("  [POLYMORPHIC MOVE] %s fires Cyber Plasma Overdrive Beam! (Energy: %d)%n", this.name, this.cyberCoreEnergy);
    }

    public int getCyberCoreEnergy() {
        return cyberCoreEnergy;
    }
}
