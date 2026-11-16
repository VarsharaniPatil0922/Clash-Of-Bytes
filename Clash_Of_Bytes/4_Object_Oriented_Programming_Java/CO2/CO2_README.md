# CO2 Implementations

| Concept | File | Implementation | Why Used |
|---------|------|----------------|----------|
| Lambda | `Main/CombatManager.java` | Implements the `MoveCalculator` functional interface inline for dynamic damage formulas. | To write concise, anonymous functional blocks for calculating attack damage without separate classes. |
| Single inheritance | `CO2/OOP/WarriorFighter.java` | `WarriorFighter` extends `BaseFighter`. | To inherit common attributes (HP, attack) while adding specific traits like `shieldDefense`. |
| Multilevel inheritance | `CO2/OOP/BerserkerWarrior.java` | `BerserkerWarrior` extends `WarriorFighter` (which extends `BaseFighter`). | To create specialized deeper hierarchies, adding a `rageMultiplier` to a warrior. |
| Hierarchical inheritance | `CO2/OOP/NinjaFighter.java` & `CyberBrawler.java` | Multiple distinct classes extend the same `BaseFighter` parent class. | To branch out different distinct character archetypes from one common base. |
| Multiple inheritance through interfaces | `CO2/OOP/PaladinFighter.java` | `PaladinFighter` implements both `ShieldBearer` and `ElementalAttacker`. | Java forbids multiple class inheritance, so interfaces allow the Paladin to gain multiple distinct abilities safely. |
| Hybrid inheritance | `CO2/OOP/HybridGladiator.java` | Extends `BerserkerWarrior` and implements two interfaces (`ShieldBearer`, `ElementalAttacker`). | To combine a deep class hierarchy with multiple interface contracts. |
| Compile-time polymorphism | `CO2/OOP/WarriorFighter.java` | The `attack()` method is overloaded with different parameters (basic, multiplier, tactical). | To provide flexible versions of the same action depending on the available input data. |
| Runtime polymorphism | `Main/CombatManager.java` | Parent reference `BaseFighter` calls `performSpecialMove()`, which executes the subclass's overridden version. | To allow dynamic, object-specific behavior while treating all fighters under a uniform interface. |
| Abstract class | `CO2/OOP/BaseFighter.java` | Declares abstract method `performSpecialMove()`. | To serve as a blueprint that cannot be instantiated directly, forcing children to define special moves. |
| Interface | `CO2/OOP/ShieldBearer.java` | Defines method signatures like `blockIncomingDamage()`. | To guarantee that specific classes implement a defined set of abilities. |
| static | `CO2/OOP/BaseFighter.java` | `fightersCreated` tracks total fighters instantiated. | To maintain class-level shared data independent of specific object instances. |
| final | `CO2/OOP/BaseFighter.java` | `MINIMUM_DAMAGE` is declared `final`. | To define an immutable constant that cannot be altered during execution. |
| super | `CO2/OOP/WarriorFighter.java` | `super(name, hp, atk)` calls the parent constructor. | To correctly initialize inherited fields managed by the superclass. |
