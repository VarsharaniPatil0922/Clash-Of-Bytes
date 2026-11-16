package model;

/**
 * Model class representing a selectable Fighter character.
 */
public class Character {
    private int characterId;
    private String characterName;
    private String characterDescription;
    private String archetype;
    private double baseHp;
    private double baseAttack;
    private double baseDefense;
    private double speed;

    public Character() {}

    public Character(int characterId, String characterName, String characterDescription,
                     String archetype, double baseHp, double baseAttack, double baseDefense, double speed) {
        this.characterId = characterId;
        this.characterName = characterName;
        this.characterDescription = characterDescription;
        this.archetype = archetype;
        this.baseHp = baseHp;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.speed = speed;
    }

    public int getCharacterId() { return characterId; }
    public void setCharacterId(int characterId) { this.characterId = characterId; }

    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }

    public String getCharacterDescription() { return characterDescription; }
    public void setCharacterDescription(String characterDescription) { this.characterDescription = characterDescription; }

    public String getArchetype() { return archetype; }
    public void setArchetype(String archetype) { this.archetype = archetype; }

    public double getBaseHp() { return baseHp; }
    public void setBaseHp(double baseHp) { this.baseHp = baseHp; }

    public double getBaseAttack() { return baseAttack; }
    public void setBaseAttack(double baseAttack) { this.baseAttack = baseAttack; }

    public double getBaseDefense() { return baseDefense; }
    public void setBaseDefense(double baseDefense) { this.baseDefense = baseDefense; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    @Override
    public String toString() {
        return characterName + " (" + archetype + ")";
    }
}
