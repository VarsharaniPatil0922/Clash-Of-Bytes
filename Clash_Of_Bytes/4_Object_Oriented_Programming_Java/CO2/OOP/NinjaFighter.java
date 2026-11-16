package CO2.OOP;




/*
 * Subject: Problem Solving Using OOP (Java)
 * Course Outcome: CO4 | Practical 04
 * Topic: Hierarchical Inheritance - Branch A (NinjaFighter extends BaseFighter)
 */
public class NinjaFighter extends BaseFighter {
    private int stealthDuration;

    public NinjaFighter(String name, double hp, double atk, int stealth) {
        super(name, hp, atk);
        this.stealthDuration = stealth;
    }

    @Override
    public void performSpecialMove() {
        System.out.printf("  [POLYMORPHIC MOVE] %s executes Shadow Clone Strike! (Vanishes for %d seconds)%n", this.name, this.stealthDuration);
    }

    public int getStealthDuration() {
        return stealthDuration;
    }
}
