package CO3.Collections;

import CO2.OOP.BaseFighter;
import CO2.OOP.WarriorFighter;
import CO2.OOP.NinjaFighter;
import CO2.OOP.CyberBrawler;
import CO2.OOP.PaladinFighter;
import CO2.OOP.BerserkerWarrior;
import CO2.OOP.HybridGladiator;
import CO3.Exceptions.InvalidFighterSelectionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Course Outcome: CO3 | Practical 03: Collections Framework
 * 
 * Central registry managing all fighter prototypes using:
 * - LinkedHashMap<String, BaseFighter> : O(1) keyed lookup preserving registration order.
 * - List<BaseFighter> (ArrayList)     : Fast indexed access matching Swing JComboBox indices.
 * - Set<String> (LinkedHashSet)       : Set of unique fighter archetypes without duplicates.
 */
public final class FighterRosterRegistry {

    private static final FighterRosterRegistry DEFAULT_INSTANCE = new FighterRosterRegistry();

    private final Map<String, BaseFighter> fighterMap;
    private final List<BaseFighter> fighterList;
    private final Set<String> archetypeSet;

    public FighterRosterRegistry() {
        this.fighterMap = new LinkedHashMap<>();
        this.fighterList = new ArrayList<>();
        this.archetypeSet = new LinkedHashSet<>();
        loadDefaultRoster();
    }

    public static FighterRosterRegistry getDefaultRegistry() {
        return DEFAULT_INSTANCE;
    }

    private void loadDefaultRoster() {
        registerFighter(new WarriorFighter("Ironclad Warrior", 115.0, 18.0, 6.0));
        registerFighter(new NinjaFighter("Shadow Ninja", 95.0, 16.0, 4));
        registerFighter(new CyberBrawler("Cyber Brawler", 110.0, 19.0, 3));
        registerFighter(new PaladinFighter("Aura Paladin", 120.0, 17.0, "Holy Radiance", 7.5));
        registerFighter(new BerserkerWarrior("Blaze Berserker", 130.0, 22.0, 4.0, 2.0));
        registerFighter(new HybridGladiator("Titan Gladiator", 140.0, 24.0, 8.0, 2.2, "Colosseum of Bytes"));
    }

    /**
     * Registers a new fighter into the collections.
     */
    public synchronized void registerFighter(BaseFighter fighter) {
        if (fighter == null || fighter.getName() == null || fighter.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot register null or nameless fighter into registry.");
        }
        fighterMap.put(fighter.getName(), fighter);
        // Avoid duplicate entries in list if re-registered with same name
        int existingIdx = -1;
        for (int i = 0; i < fighterList.size(); i++) {
            if (fighterList.get(i).getName().equalsIgnoreCase(fighter.getName())) {
                existingIdx = i;
                break;
            }
        }
        if (existingIdx >= 0) {
            fighterList.set(existingIdx, fighter);
        } else {
            fighterList.add(fighter);
        }
        archetypeSet.add(fighter.getClass().getSimpleName());
    }

    /**
     * Safely retrieves a fighter by 0-based index.
     * Throws InvalidFighterSelectionException if index is invalid.
     */
    public synchronized BaseFighter getFighter(int index) throws InvalidFighterSelectionException {
        if (index < 0 || index >= fighterList.size()) {
            throw new InvalidFighterSelectionException(index, fighterList.size());
        }
        return fighterList.get(index);
    }

    /**
     * Safely retrieves a fighter by name from the Map.
     * Throws InvalidFighterSelectionException if name is not registered.
     */
    public synchronized BaseFighter getFighterByName(String name) throws InvalidFighterSelectionException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidFighterSelectionException("<empty>", "Fighter name query cannot be empty or null.");
        }
        BaseFighter fighter = fighterMap.get(name);
        if (fighter == null) {
            throw new InvalidFighterSelectionException(name, "No fighter registered under name '" + name + "'.");
        }
        return fighter;
    }

    public synchronized boolean containsFighter(String name) {
        return name != null && fighterMap.containsKey(name);
    }

    public synchronized List<BaseFighter> getAllFighters() {
        return Collections.unmodifiableList(new ArrayList<>(fighterList));
    }

    public synchronized String[] getFighterNames() {
        String[] names = new String[fighterList.size()];
        for (int i = 0; i < fighterList.size(); i++) {
            names[i] = fighterList.get(i).getName();
        }
        return names;
    }

    public synchronized int getFighterCount() {
        return fighterList.size();
    }

    public synchronized Set<String> getUniqueArchetypes() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(archetypeSet));
    }

    /**
     * Returns a new List of fighters sorted descending by Health using Comparator.
     */
    public synchronized List<BaseFighter> getFightersSortedByHealth() {
        List<BaseFighter> sorted = new ArrayList<>(fighterList);
        sorted.sort(Comparator.comparingDouble(BaseFighter::getHealth).reversed());
        return sorted;
    }

    /**
     * Returns a new List of fighters sorted descending by Attack using Comparator.
     */
    public synchronized List<BaseFighter> getFightersSortedByAttack() {
        List<BaseFighter> sorted = new ArrayList<>(fighterList);
        sorted.sort(Comparator.comparingDouble(BaseFighter::getBaseAttack).reversed());
        return sorted;
    }

    /**
     * Validates pair selection for match startup.
     */
    public synchronized void validateFighterPair(int p1Index, int p2Index, boolean disallowMirror) 
            throws InvalidFighterSelectionException {
        if (p1Index < 0 || p1Index >= fighterList.size()) {
            throw new InvalidFighterSelectionException(p1Index, fighterList.size());
        }
        if (p2Index < 0 || p2Index >= fighterList.size()) {
            throw new InvalidFighterSelectionException(p2Index, fighterList.size());
        }
        if (disallowMirror && p1Index == p2Index) {
            String duplicateName = fighterList.get(p1Index).getName();
            throw new InvalidFighterSelectionException(duplicateName, 
                "Mirror match rejected: Both Player 1 and Player 2 selected '" + duplicateName + 
                "'. Unique fighter selection is enforced.");
        }
    }
}
