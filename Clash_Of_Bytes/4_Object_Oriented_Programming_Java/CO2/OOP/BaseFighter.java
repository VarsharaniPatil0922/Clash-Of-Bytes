package CO2.OOP;

/*
 * Subject: Problem Solving Using OOP (Java)
 * Course Outcome: CO3 | Practical 03
 * Topic: Class-Object, Member Variables, Methods, Overloaded Constructors, Encapsulation
 */
public abstract class BaseFighter {
    public static final double MINIMUM_DAMAGE = 5.0;

    protected String name;
    protected double health;
    protected double maxHealth;
    protected double baseAttack;
    protected static int fightersCreated = 0;

    // 1. Default Constructor
    public BaseFighter() {
        this("Fighter", 100.0, 15.0);
    }

    // 2. Parameterized Constructor
    public BaseFighter(String name, double hp, double atk) {
        this.name = name;
        this.health = hp;
        this.maxHealth = hp;
        this.baseAttack = atk;
        fightersCreated++;
    }

    // 3. Copy Constructor
    public BaseFighter(BaseFighter other) {
        this(other.name + " (Clone)", other.maxHealth, other.baseAttack);
    }

    // Polymorphic base method for runtime dynamic method dispatch
    public abstract void performSpecialMove();

    public void takeDamage(double dmg) {
        this.health = Math.max(0.0, this.health - dmg);
    }

    public void heal(double amount) {
        this.health = Math.min(this.maxHealth, this.health + amount);
    }

    public void restoreHealth() {
        this.health = this.maxHealth;
    }

    public boolean isAlive() {
        return this.health > 0.0;
    }

    // Encapsulation Getters
    public String getName() { return name; }
    public double getHealth() { return health; }
    public double getMaxHealth() { return maxHealth; }
    public double getBaseAttack() { return baseAttack; }
    public static int getFightersCreated() { return fightersCreated; }
}
