package com.clashofbytes.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "characters")
public class CharacterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id")
    private Long characterId;

    @Column(name = "character_name", nullable = false, unique = true, length = 50)
    private String characterName;

    @Column(name = "character_description", nullable = false, length = 255)
    private String characterDescription;

    @Column(name = "archetype", nullable = false, length = 50)
    private String archetype;

    @Column(name = "base_hp", nullable = false)
    private Double baseHp;

    @Column(name = "base_attack", nullable = false)
    private Double baseAttack;

    @Column(name = "base_defense", nullable = false)
    private Double baseDefense;

    @Column(name = "speed", nullable = false)
    private Double speed;

    public CharacterEntity() {}

    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }

    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }

    public String getCharacterDescription() { return characterDescription; }
    public void setCharacterDescription(String characterDescription) { this.characterDescription = characterDescription; }

    public String getArchetype() { return archetype; }
    public void setArchetype(String archetype) { this.archetype = archetype; }

    public Double getBaseHp() { return baseHp; }
    public void setBaseHp(Double baseHp) { this.baseHp = baseHp; }

    public Double getBaseAttack() { return baseAttack; }
    public void setBaseAttack(Double baseAttack) { this.baseAttack = baseAttack; }

    public Double getBaseDefense() { return baseDefense; }
    public void setBaseDefense(Double baseDefense) { this.baseDefense = baseDefense; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }
}
