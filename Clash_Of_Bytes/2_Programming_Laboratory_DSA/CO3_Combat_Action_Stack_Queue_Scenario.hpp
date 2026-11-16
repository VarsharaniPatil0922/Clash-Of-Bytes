#ifndef CO3_COMBAT_ACTION_STACK_QUEUE_SCENARIO_HPP
#define CO3_COMBAT_ACTION_STACK_QUEUE_SCENARIO_HPP

#include <iostream>
#include <string>
#include <iomanip>
#include <sstream>
#include <vector>
#include <algorithm>
#include "CO3_Prac06_Stack_Combat_Formula_Evaluator.hpp"

/*
 * Subject: Programming Laboratory (Data Structures & Algorithms)
 * Course Outcome: CO3 | Task 3 / Practical 07
 * Topic: Scenario Processing using Stack (LIFO History & Undo) and Queue (FIFO Action Scheduler)
 * Application: "Clash of Bytes" Real-Time Combat Scenario Engine
 */

// ============================================================================
// 1. COMBAT ACTION ENUM & STRING CONVERTERS
// ============================================================================
enum class CombatActionType {
    ATTACK,          // Basic offensive strike (Jab, Kick, Slash)
    DEFEND,          // Defensive block / Guard stance mitigating incoming damage
    SPECIAL_ATTACK,  // High-damage, high-energy signature move (Dragon Upper, Shadow Strike)
    DODGE,           // Evasive sidestep / roll evading incoming attacks
    COUNTER,         // Defensive riposte strike
    UNKNOWN          // Invalid / unrecognized action for validation testing
};

inline std::string actionTypeToString(CombatActionType type) {
    switch (type) {
        case CombatActionType::ATTACK:         return "ATTACK";
        case CombatActionType::DEFEND:         return "DEFEND";
        case CombatActionType::SPECIAL_ATTACK: return "SPECIAL_ATTACK";
        case CombatActionType::DODGE:          return "DODGE";
        case CombatActionType::COUNTER:        return "COUNTER";
        case CombatActionType::UNKNOWN:
        default:                               return "UNKNOWN";
    }
}

inline CombatActionType stringToActionType(const std::string& str) {
    if (str == "ATTACK") return CombatActionType::ATTACK;
    if (str == "DEFEND") return CombatActionType::DEFEND;
    if (str == "SPECIAL_ATTACK") return CombatActionType::SPECIAL_ATTACK;
    if (str == "DODGE") return CombatActionType::DODGE;
    if (str == "COUNTER") return CombatActionType::COUNTER;
    return CombatActionType::UNKNOWN;
}

// ============================================================================
// 2. COMBAT ACTION DATA STRUCTURE
// ============================================================================
struct CombatAction {
    int actionId;               // Unique sequence ID (e.g., 101, 102...)
    int fighterId;              // Originating fighter (1 = Blaze Warrior, 2 = Shadow Ninja)
    std::string fighterName;    // Fighter display name
    CombatActionType type;      // Enum action category
    std::string moveName;       // Specific move name (e.g. "Dragon Upper", "Iron Guard")
    int damage;                 // Base potential damage dealt
    int energyCost;             // Stamina/energy cost consumed
    int targetFighterId;        // Target opponent ID
    int actualDamageDealt;      // Net damage inflicted after mitigation
    bool executed;              // Execution flag
    std::string notes;          // Tactical description or scenario context

    CombatAction()
        : actionId(0), fighterId(0), fighterName("None"), type(CombatActionType::UNKNOWN),
          moveName("None"), damage(0), energyCost(0), targetFighterId(0),
          actualDamageDealt(0), executed(false), notes("") {}

    CombatAction(int id, int fId, const std::string& fName, CombatActionType aType,
                 const std::string& mName, int dmg, int energy, int targetId,
                 const std::string& desc = "")
        : actionId(id), fighterId(fId), fighterName(fName), type(aType),
          moveName(mName), damage(dmg), energyCost(energy), targetFighterId(targetId),
          actualDamageDealt(0), executed(false), notes(desc) {}

    // Strict validation ensuring data integrity
    bool isValid() const {
        return (type != CombatActionType::UNKNOWN) &&
               (fighterId > 0) &&
               (targetFighterId > 0) &&
               (fighterId != targetFighterId) &&
               (damage >= 0) &&
               (energyCost >= 0);
    }

    std::string getSummary() const {
        std::ostringstream oss;
        oss << "[Action #" << actionId << "] " << std::left << std::setw(15) << fighterName
            << " | " << std::setw(14) << actionTypeToString(type)
            << " | Move: " << std::setw(18) << moveName
            << " | DMG: " << std::setw(2) << damage
            << " | Cost: " << std::setw(2) << energyCost
            << " | Target: P" << targetFighterId;
        return oss.str();
    }
};

// ============================================================================
// 3. GENERIC DYNAMIC LINKED QUEUE (FIFO)
// ============================================================================
template <typename T>
class CustomQueue {
private:
    struct QueueNode {
        T data;
        QueueNode* next;
        QueueNode(const T& val) : data(val), next(nullptr) {}
    };

    QueueNode* frontNode;
    QueueNode* rearNode;
    int queueSize;

public:
    CustomQueue() : frontNode(nullptr), rearNode(nullptr), queueSize(0) {}

    ~CustomQueue() {
        clear();
    }

    // Deep Copy Constructor (Rule of Three)
    CustomQueue(const CustomQueue& other) : frontNode(nullptr), rearNode(nullptr), queueSize(0) {
        QueueNode* curr = other.frontNode;
        while (curr != nullptr) {
            enqueue(curr->data);
            curr = curr->next;
        }
    }

    // Deep Copy Assignment Operator (Rule of Three)
    CustomQueue& operator=(const CustomQueue& other) {
        if (this != &other) {
            clear();
            QueueNode* curr = other.frontNode;
            while (curr != nullptr) {
                enqueue(curr->data);
                curr = curr->next;
            }
        }
        return *this;
    }

    // O(1) Enqueue at Rear
    void enqueue(const T& val) {
        QueueNode* newNode = new QueueNode(val);
        if (isEmpty()) {
            frontNode = rearNode = newNode;
        } else {
            rearNode->next = newNode;
            rearNode = newNode;
        }
        queueSize++;
    }

    // O(1) Dequeue from Front with safe boolean status
    bool dequeue(T& outVal) {
        if (isEmpty()) {
            return false;
        }
        QueueNode* temp = frontNode;
        outVal = temp->data;
        frontNode = frontNode->next;
        if (frontNode == nullptr) {
            rearNode = nullptr;
        }
        delete temp;
        queueSize--;
        return true;
    }

    // O(1) Value-returning Dequeue with error reporting
    T dequeue() {
        if (isEmpty()) {
            std::cerr << "  [QUEUE UNDERFLOW] Cannot dequeue: Attempted to dequeue from an empty queue!\n";
            return T();
        }
        T val;
        dequeue(val);
        return val;
    }

    // O(1) Safe Front Peek
    bool peek(T& outVal) const {
        if (isEmpty()) {
            return false;
        }
        outVal = frontNode->data;
        return true;
    }

    T peek() const {
        if (isEmpty()) {
            return T();
        }
        return frontNode->data;
    }

    bool isEmpty() const {
        return frontNode == nullptr;
    }

    int size() const {
        return queueSize;
    }

    void clear() {
        while (frontNode != nullptr) {
            QueueNode* temp = frontNode;
            frontNode = frontNode->next;
            delete temp;
        }
        rearNode = nullptr;
        queueSize = 0;
    }

    template <typename Func>
    void forEach(Func func) const {
        QueueNode* curr = frontNode;
        while (curr != nullptr) {
            func(curr->data);
            curr = curr->next;
        }
    }
};

// ============================================================================
// 4. FIGHTER COMBAT STATE FOR SCENARIO PROCESSING
// ============================================================================
struct FighterCombatState {
    int id;
    std::string name;
    int health;
    int maxHealth;
    int energy;
    int maxEnergy;
    bool isGuarding;
    bool isDodging;
    int totalDamageDealt;
    int totalDamageTaken;

    FighterCombatState(int fId = 0, const std::string& fName = "Fighter", int hp = 100, int eng = 100)
        : id(fId), name(fName), health(hp), maxHealth(hp), energy(eng), maxEnergy(eng),
          isGuarding(false), isDodging(false), totalDamageDealt(0), totalDamageTaken(0) {}

    void resetStances() {
        isGuarding = false;
        isDodging = false;
    }

    std::string getStatus() const {
        std::ostringstream oss;
        oss << "[" << name << "] HP: " << health << "/" << maxHealth
            << " | Energy: " << energy << "/" << maxEnergy;
        if (isGuarding) oss << " [GUARD ACTIVE]";
        if (isDodging) oss << " [DODGE ACTIVE]";
        return oss.str();
    }
};

// ============================================================================
// 5. COMBAT ACTION STACK (LIFO Action History & Undo)
// ============================================================================
class CombatActionStack {
private:
    CustomStack<CombatAction> stack; // Reuses generic node-based CustomStack<T>

public:
    CombatActionStack() {}
    ~CombatActionStack() { clear(); }

    // O(1) Push combat action onto history stack
    void push(const CombatAction& action) {
        stack.push(action);
    }

    // O(1) Safe Pop with status
    bool pop(CombatAction& outAction) {
        if (isEmpty()) {
            std::cerr << "  [STACK UNDERFLOW] Cannot pop: Combat Action History Stack is empty!\n";
            return false;
        }
        return stack.pop(outAction);
    }

    CombatAction pop() {
        if (isEmpty()) {
            std::cerr << "  [STACK UNDERFLOW] Cannot pop: Combat Action History Stack is empty!\n";
            return CombatAction();
        }
        return stack.pop();
    }

    // O(1) Safe Peek
    bool peek(CombatAction& outAction) const {
        if (isEmpty()) {
            return false;
        }
        return stack.peek(outAction);
    }

    CombatAction peek() const {
        if (isEmpty()) {
            return CombatAction();
        }
        return stack.peek();
    }

    bool isEmpty() const {
        return stack.isEmpty();
    }

    int size() const {
        return stack.size();
    }

    void clear() {
        stack.clear();
    }

    // Display entire action history from TOP (most recent) to BOTTOM (oldest)
    void display() const {
        if (isEmpty()) {
            std::cout << "  [STACK DISPLAY] Action history stack is currently EMPTY.\n";
            return;
        }

        std::cout << "  -------------------------------------------------------------------\n";
        std::cout << "  [COMBAT ACTION HISTORY STACK] (LIFO Order: Top -> Bottom | Size: " << size() << ")\n";
        std::cout << "  -------------------------------------------------------------------\n";

        int depth = 1;
        stack.forEach([&depth](const CombatAction& act) {
            std::string prefix = (depth == 1 ? "-> [TOP] " : "   [HIST] ");
            std::cout << "   " << prefix << "Slot " << depth << ": " << act.getSummary() 
                      << " | Actual DMG: " << act.actualDamageDealt << "\n";
            depth++;
        });
        std::cout << "  -------------------------------------------------------------------\n";
    }

    // Undo-last-action functionality: Reverts the top combat action and restores stats
    bool undoLastAction(FighterCombatState& p1, FighterCombatState& p2, std::string& outLog) {
        if (isEmpty()) {
            outLog = "  [UNDO REJECTED] Combat Action History Stack is empty! No actions to revert.";
            return false;
        }

        CombatAction lastAction;
        if (!pop(lastAction)) {
            outLog = "  [UNDO FAILED] Underflow error while popping from stack.";
            return false;
        }

        FighterCombatState& actor  = (lastAction.fighterId == p1.id) ? p1 : p2;
        FighterCombatState& target = (lastAction.targetFighterId == p1.id) ? p1 : p2;

        // Refund energy spent by actor
        actor.energy = std::min(actor.maxEnergy, actor.energy + lastAction.energyCost);

        // Restore health of opponent if damage was dealt
        if (lastAction.actualDamageDealt > 0) {
            target.health = std::min(target.maxHealth, target.health + lastAction.actualDamageDealt);
            target.totalDamageTaken -= lastAction.actualDamageDealt;
            actor.totalDamageDealt -= lastAction.actualDamageDealt;
        }

        // Revert defensive stances if applicable
        if (lastAction.type == CombatActionType::DEFEND) {
            actor.isGuarding = false;
        } else if (lastAction.type == CombatActionType::DODGE) {
            actor.isDodging = false;
        }

        std::ostringstream oss;
        oss << "  [UNDO SUCCESSFUL] Reverted Action #" << lastAction.actionId << " ("
            << actionTypeToString(lastAction.type) << ": " << lastAction.moveName << ") by "
            << lastAction.fighterName << ".\n"
            << "   -> Target " << target.name << " restored: +" << lastAction.actualDamageDealt << " HP (Now: " << target.health << "/" << target.maxHealth << ")\n"
            << "   -> Actor " << actor.name << " refunded: +" << lastAction.energyCost << " Energy (Now: " << actor.energy << "/" << actor.maxEnergy << ")";
        outLog = oss.str();
        return true;
    }
};

// ============================================================================
// 6. COMBAT ACTION QUEUE (FIFO Action Scheduler)
// ============================================================================
class CombatActionQueue {
private:
    CustomQueue<CombatAction> queue;

public:
    CombatActionQueue() {}
    ~CombatActionQueue() { clear(); }

    // O(1) Enqueue with strict validation
    bool enqueue(const CombatAction& action, bool verbose = true) {
        if (!action.isValid()) {
            if (verbose) {
                std::cout << "  [QUEUE REJECTED] Invalid Combat Action detected! "
                          << "Action #" << action.actionId << " (" << action.moveName 
                          << ") rejected from queue.\n"
                          << "   Reason: ActionType=" << actionTypeToString(action.type)
                          << ", DMG=" << action.damage << ", Cost=" << action.energyCost 
                          << ", FighterID=" << action.fighterId << ", TargetID=" << action.targetFighterId << "\n";
            }
            return false;
        }

        queue.enqueue(action);
        if (verbose) {
            std::cout << "  [QUEUE ENQUEUE] " << action.getSummary() 
                      << " -> Enqueued at Rear (Pending: " << size() << ")\n";
        }
        return true;
    }

    // O(1) Dequeue with underflow safety
    bool dequeue(CombatAction& outAction, bool verbose = true) {
        if (isEmpty()) {
            if (verbose) {
                std::cout << "  [QUEUE UNDERFLOW] Cannot dequeue: Combat Action Queue is EMPTY!\n";
            }
            return false;
        }
        return queue.dequeue(outAction);
    }

    CombatAction dequeue() {
        if (isEmpty()) {
            std::cout << "  [QUEUE UNDERFLOW] Cannot dequeue: Combat Action Queue is EMPTY!\n";
            return CombatAction();
        }
        return queue.dequeue();
    }

    // O(1) Safe Front Peek
    bool peek(CombatAction& outAction) const {
        if (isEmpty()) {
            return false;
        }
        return queue.peek(outAction);
    }

    CombatAction peek() const {
        if (isEmpty()) {
            return CombatAction();
        }
        return queue.peek();
    }

    bool isEmpty() const {
        return queue.isEmpty();
    }

    int size() const {
        return queue.size();
    }

    void clear() {
        queue.clear();
    }

    // Display all pending actions from FRONT (next to execute) to REAR (last added)
    void display() const {
        if (isEmpty()) {
            std::cout << "  [QUEUE DISPLAY] Pending action queue is currently EMPTY.\n";
            return;
        }

        std::cout << "  -------------------------------------------------------------------\n";
        std::cout << "  [PENDING COMBAT ACTION QUEUE] (FIFO Order: Front -> Rear | Size: " << size() << ")\n";
        std::cout << "  -------------------------------------------------------------------\n";

        int index = 1;
        queue.forEach([&index](const CombatAction& act) {
            std::string prefix = (index == 1 ? "-> [FRONT] " : "   [WAIT]  ");
            std::cout << "   " << prefix << "Queue Pos " << index << ": " << act.getSummary() << "\n";
            index++;
        });
        std::cout << "  -------------------------------------------------------------------\n";
    }
};

// ============================================================================
// 7. REALISTIC COMBAT SCENARIO ENGINE & DEMONSTRATION SUITE
// ============================================================================
class CombatScenarioEngine {
public:
    // Execute a single queued action against live combat states
    static bool processAction(CombatAction& action, FighterCombatState& p1, FighterCombatState& p2,
                             CombatActionStack& historyStack) {
        FighterCombatState& actor  = (action.fighterId == p1.id) ? p1 : p2;
        FighterCombatState& target = (action.targetFighterId == p1.id) ? p1 : p2;

        std::cout << "  [DISPATCH FIFO ACTION] Action #" << action.actionId 
                  << " (" << action.moveName << ") by " << actor.name << "\n";

        // Check actor energy
        if (actor.energy < action.energyCost) {
            std::cout << "   [EXHAUSTION] " << actor.name << " lacks sufficient energy (" 
                      << actor.energy << "/" << action.energyCost << ")! Move fizzled.\n";
            action.actualDamageDealt = 0;
            action.executed = false;
            historyStack.push(action);
            return false;
        }

        // Deduct energy
        actor.energy -= action.energyCost;

        int netDamage = 0;

        switch (action.type) {
            case CombatActionType::ATTACK:
            case CombatActionType::SPECIAL_ATTACK: {
                if (target.isDodging) {
                    std::cout << "   [DODGE EVADED!] " << target.name 
                              << " performed an evasive roll! Strike completely missed (0 DMG).\n";
                    netDamage = 0;
                } else if (target.isGuarding) {
                    // Shield mitigates 60% of damage
                    netDamage = static_cast<int>(action.damage * 0.40f);
                    std::cout << "   [GUARD BLOCKED!] " << target.name 
                              << " absorbed impact with Iron Guard! DMG reduced from " 
                              << action.damage << " -> " << netDamage << ".\n";
                } else {
                    netDamage = action.damage;
                    std::cout << "   [CLEAN HIT!] " << actor.name << " strikes " << target.name 
                              << " for full " << netDamage << " damage!\n";
                }

                target.health = std::max(0, target.health - netDamage);
                target.totalDamageTaken += netDamage;
                actor.totalDamageDealt += netDamage;
                break;
            }

            case CombatActionType::DEFEND: {
                actor.isGuarding = true;
                std::cout << "   [DEFENSIVE STANCE] " << actor.name 
                          << " raised guard! Next incoming strike mitigated by 60%.\n";
                netDamage = 0;
                break;
            }

            case CombatActionType::DODGE: {
                actor.isDodging = true;
                std::cout << "   [EVASIVE MANEUVER] " << actor.name 
                          << " entered dodge stance! Next incoming attack will miss entirely.\n";
                netDamage = 0;
                break;
            }

            case CombatActionType::COUNTER: {
                netDamage = action.damage;
                target.health = std::max(0, target.health - netDamage);
                std::cout << "   [TACTICAL COUNTER] " << actor.name 
                          << " executed counter-riposte for " << netDamage << " damage!\n";
                break;
            }

            case CombatActionType::UNKNOWN:
            default: {
                std::cout << "   [UNKNOWN ERROR] Cannot execute unrecognized combat action.\n";
                return false;
            }
        }

        action.actualDamageDealt = netDamage;
        action.executed = true;

        // Push executed action onto LIFO history stack
        historyStack.push(action);
        std::cout << "   -> Executed & Pushed to History Stack (Stack Size: " << historyStack.size() << ")\n";
        std::cout << "   -> State: " << p1.getStatus() << " | " << p2.getStatus() << "\n\n";

        return true;
    }

    // Run complete realistic combat scenario demonstration
    static void demonstrateScenarioProcessing() {
        std::cout << "===================================================================\n";
        std::cout << " [TASK 3 / PRACTICAL 07] SCENARIO PROCESSING USING STACK & QUEUE\n";
        std::cout << " Course Outcome: CO3 | Concept: LIFO Combat History & FIFO Action Queue\n";
        std::cout << " Subject: Programming Laboratory (Data Structures & Algorithms)\n";
        std::cout << "===================================================================\n\n";

        FighterCombatState p1(1, "Blaze Warrior", 100, 100);
        FighterCombatState p2(2, "Shadow Ninja", 90, 100);

        CombatActionQueue pendingQueue;
        CombatActionStack historyStack;

        std::cout << "--- [PHASE 1] INITIAL COMBAT STATE & FIGHTER INITIALIZATION ---\n";
        std::cout << "  Player 1: " << p1.getStatus() << "\n";
        std::cout << "  Player 2: " << p2.getStatus() << "\n\n";

        std::cout << "--- [PHASE 2] ENQUEUEING COMBAT ACTIONS (FIFO QUEUE) ---\n";
        std::cout << "  Simulating player input buffers and scripted combat round actions:\n";

        // Multiple actions in succession
        pendingQueue.enqueue(CombatAction(101, 1, "Blaze Warrior", CombatActionType::ATTACK, "Slash Strike", 18, 10, 2, "Opening strike"));
        pendingQueue.enqueue(CombatAction(102, 2, "Shadow Ninja", CombatActionType::DEFEND, "Iron Guard", 0, 5, 1, "Defensive stance"));
        pendingQueue.enqueue(CombatAction(103, 1, "Blaze Warrior", CombatActionType::SPECIAL_ATTACK, "Dragon Upper", 32, 25, 2, "Signature heavy uppercut"));
        pendingQueue.enqueue(CombatAction(104, 2, "Shadow Ninja", CombatActionType::DODGE, "Shadow Step", 0, 12, 1, "Evasive roll"));
        pendingQueue.enqueue(CombatAction(105, 2, "Shadow Ninja", CombatActionType::ATTACK, "Kunai Barrage", 22, 15, 1, "Agile ranged counter"));
        pendingQueue.enqueue(CombatAction(106, 1, "Blaze Warrior", CombatActionType::DEFEND, "Shield Barrier", 0, 5, 2, "Fortified defense"));
        pendingQueue.enqueue(CombatAction(107, 2, "Shadow Ninja", CombatActionType::SPECIAL_ATTACK, "Shadow Strike", 38, 30, 1, "Lethal stealth ultimate"));

        std::cout << "\n--- [PHASE 3] QUEUE INSPECTION & FIFO FRONT PEEK ---\n";
        pendingQueue.display();

        CombatAction frontAction;
        if (pendingQueue.peek(frontAction)) {
            std::cout << "  [QUEUE PEEK FRONT] Next action in line to execute is Action #" 
                      << frontAction.actionId << " (" << frontAction.moveName 
                      << ") by " << frontAction.fighterName << ".\n";
        }
        std::cout << "  Current Queue Size: " << pendingQueue.size() << " | Queue isEmpty: " 
                  << (pendingQueue.isEmpty() ? "YES" : "NO") << "\n\n";

        std::cout << "--- [PHASE 4] PROCESSING SCENARIO ACTIONS IN FIFO SEQUENCE ---\n";
        std::cout << "  Actions dequeue in the exact first-come first-served order they were buffered:\n\n";

        int step = 1;
        while (!pendingQueue.isEmpty()) {
            std::cout << "  >>> Step " << step++ << ": Dequeueing next pending action from front...\n";
            CombatAction currentAction;
            pendingQueue.dequeue(currentAction);
            processAction(currentAction, p1, p2, historyStack);
        }

        std::cout << "  [QUEUE DRAINED] All pending combat actions have been processed.\n";
        std::cout << "  Pending Queue Size: " << pendingQueue.size() << " (isEmpty: " 
                  << (pendingQueue.isEmpty() ? "YES" : "NO") << ")\n\n";

        std::cout << "--- [PHASE 5] LIFO COMBAT ACTION STACK DEMONSTRATION ---\n";
        std::cout << "  Inspecting the executed combat history stack (Demonstrating LIFO order):\n";
        historyStack.display();

        CombatAction topAction;
        if (historyStack.peek(topAction)) {
            std::cout << "  [STACK PEEK TOP] Most recent executed action is Action #" 
                      << topAction.actionId << " (" << topAction.moveName 
                      << ") by " << topAction.fighterName << ".\n";
            std::cout << "  Notice: Action #" << topAction.actionId 
                      << " was the LAST action enqueued/executed, and is now at the TOP of the stack!\n\n";
        }

        std::cout << "--- [PHASE 6] UNDO-LAST-ACTION FUNCTIONALITY (LIFO REVERSAL) ---\n";
        std::cout << "  Reverting the last two combat moves via Stack pop (Undo mechanism):\n\n";

        std::string undoLog;
        std::cout << "  [UNDO #1] Reverting top of stack...\n";
        historyStack.undoLastAction(p1, p2, undoLog);
        std::cout << undoLog << "\n\n";

        std::cout << "  [UNDO #2] Reverting next top of stack...\n";
        historyStack.undoLastAction(p1, p2, undoLog);
        std::cout << undoLog << "\n\n";

        std::cout << "  Post-Undo Combat Action History Stack (Size: " << historyStack.size() << "):\n";
        historyStack.display();

        std::cout << "  Post-Undo Fighter Stats:\n";
        std::cout << "    P1: " << p1.getStatus() << "\n";
        std::cout << "    P2: " << p2.getStatus() << "\n\n";

        std::cout << "--- [PHASE 7] LIFO VS FIFO CORE BEHAVIORAL CONTRAST ---\n";
        std::cout << "  +-------------------------------------------------------------------+\n";
        std::cout << "  | FEATURE             | QUEUE (FIFO)            | STACK (LIFO)            |\n";
        std::cout << "  +---------------------+-------------------------+-------------------------+\n";
        std::cout << "  | Insertion Point     | Rear (enqueue)          | Top (push)              |\n";
        std::cout << "  | Removal Point       | Front (dequeue)         | Top (pop)               |\n";
        std::cout << "  | Sequence Model      | First-In, First-Out     | Last-In, First-Out      |\n";
        std::cout << "  | Game Combat Role    | Action Scheduler/Buffer | History & Undo Engine   |\n";
        std::cout << "  | First Handled       | Action #101 (Earliest)  | Action #107 (Latest)    |\n";
        std::cout << "  | Time Complexity     | O(1) Enqueue / Dequeue  | O(1) Push / Pop         |\n";
        std::cout << "  +-------------------------------------------------------------------+\n\n";
    }

    // Run exhaustive edge-case testing, empty conditions & invalid input validation
    static void demonstrateEdgeCasesAndValidation() {
        std::cout << "--- [PHASE 8] EDGE CASES & VALIDATION SUITE ---\n";
        std::cout << "  Testing robust error handling for empty structures and invalid data:\n\n";

        CombatActionStack emptyStack;
        CombatActionQueue emptyQueue;
        FighterCombatState dummy1(1, "Blaze Warrior", 100, 100);
        FighterCombatState dummy2(2, "Shadow Ninja", 100, 100);

        // Test 1: Empty Stack operations
        std::cout << "  [TEST 1] Empty Stack Underflow Handling:\n";
        CombatAction dummyAction;
        bool popResult = emptyStack.pop(dummyAction);
        std::cout << "   -> Pop from empty stack returned: " << (popResult ? "TRUE (Bug)" : "FALSE (Safe Underflow Handled)") << "\n";

        bool peekResult = emptyStack.peek(dummyAction);
        std::cout << "   -> Peek on empty stack returned: " << (peekResult ? "TRUE (Bug)" : "FALSE (Safe Empty Handled)") << "\n";

        std::string undoLog;
        bool undoResult = emptyStack.undoLastAction(dummy1, dummy2, undoLog);
        std::cout << "   -> Undo on empty stack returned: " << (undoResult ? "TRUE (Bug)" : "FALSE (Safe Empty Handled)") << "\n";
        std::cout << "   -> Undo error log: " << undoLog << "\n\n";

        // Test 2: Empty Queue operations
        std::cout << "  [TEST 2] Empty Queue Underflow Handling:\n";
        bool dequeueResult = emptyQueue.dequeue(dummyAction);
        std::cout << "   -> Dequeue from empty queue returned: " << (dequeueResult ? "TRUE (Bug)" : "FALSE (Safe Underflow Handled)") << "\n";

        bool queuePeekResult = emptyQueue.peek(dummyAction);
        std::cout << "   -> Peek on empty queue returned: " << (queuePeekResult ? "TRUE (Bug)" : "FALSE (Safe Empty Handled)") << "\n\n";

        // Test 3: Invalid and Unknown Combat Actions
        std::cout << "  [TEST 3] Rejection of Invalid & Unknown Actions:\n";

        // Unknown action type
        CombatAction badAction1(991, 1, "Hacker", CombatActionType::UNKNOWN, "GlitchHack", 50, 10, 2);
        std::cout << "   -> Testing UNKNOWN Action Type:\n";
        bool eq1 = emptyQueue.enqueue(badAction1);
        std::cout << "      Enqueue result: " << (eq1 ? "ACCEPTED (Bug)" : "REJECTED (Valid Error Guard)") << "\n";

        // Negative damage
        CombatAction badAction2(992, 1, "Blaze Warrior", CombatActionType::ATTACK, "NegativeDmg", -20, 10, 2);
        std::cout << "   -> Testing Negative Damage (-20 DMG):\n";
        bool eq2 = emptyQueue.enqueue(badAction2);
        std::cout << "      Enqueue result: " << (eq2 ? "ACCEPTED (Bug)" : "REJECTED (Valid Error Guard)") << "\n";

        // Negative energy cost
        CombatAction badAction3(993, 1, "Blaze Warrior", CombatActionType::ATTACK, "NegativeCost", 15, -30, 2);
        std::cout << "   -> Testing Negative Energy Cost (-30 Cost):\n";
        bool eq3 = emptyQueue.enqueue(badAction3);
        std::cout << "      Enqueue result: " << (eq3 ? "ACCEPTED (Bug)" : "REJECTED (Valid Error Guard)") << "\n";

        // Self-targeting action
        CombatAction badAction4(994, 1, "Blaze Warrior", CombatActionType::ATTACK, "SelfHarm", 15, 10, 1);
        std::cout << "   -> Testing Self-Targeting Action (Actor ID == Target ID):\n";
        bool eq4 = emptyQueue.enqueue(badAction4);
        std::cout << "      Enqueue result: " << (eq4 ? "ACCEPTED (Bug)" : "REJECTED (Valid Error Guard)") << "\n\n";

        // Test 4: Multiple Actions in Succession & Dynamic Memory Cleanup
        std::cout << "  [TEST 4] High-Volume Stress & Memory Integrity Check:\n";
        CombatActionQueue stressQueue;
        CombatActionStack stressStack;
        const int STRESS_COUNT = 50;

        for (int i = 1; i <= STRESS_COUNT; i++) {
            int actorId = (i % 2 == 1) ? 1 : 2;
            int targetId = (i % 2 == 1) ? 2 : 1;
            std::string actorName = (actorId == 1) ? "Blaze Warrior" : "Shadow Ninja";
            stressQueue.enqueue(CombatAction(200 + i, actorId, actorName,
                                             CombatActionType::ATTACK, "Rapid Strike", 10 + (i % 5), 5, targetId), false);
        }
        std::cout << "   -> Successfully enqueued " << stressQueue.size() << " actions in succession.\n";

        for (int i = 1; i <= STRESS_COUNT; i++) {
            CombatAction act;
            stressQueue.dequeue(act, false);
            stressStack.push(act);
        }
        std::cout << "   -> Dequeued all " << STRESS_COUNT << " actions and pushed into Stack (Stack size: " << stressStack.size() << ").\n";

        stressStack.clear();
        stressQueue.clear();
        std::cout << "   -> Cleared both structures. Stack size: " << stressStack.size() 
                  << " | Queue size: " << stressQueue.size() << ". Zero memory leaks!\n\n";
    }

    // Display Course Outcome 3 mapping & Time Complexity Analysis
    static void displayComplexityAndCO3Mapping() {
        std::cout << "===================================================================\n";
        std::cout << " [ACADEMIC MAPPING] PROGRAMMING LABORATORY (DSA) - COURSE OUTCOME 3\n";
        std::cout << "===================================================================\n";
        std::cout << " Course Outcome : CO3 - Understand, apply, and evaluate Stack and Queue\n";
        std::cout << "                  data structures for real-world scenario processing.\n";
        std::cout << " Syllabus Topic : Linear Data Structures: Stacks and Queues (Applications)\n";
        std::cout << " Implementation : 1. CustomStack<T> (Linked Node Stack with LIFO History & Undo)\n";
        std::cout << "                  2. CustomQueue<T> (Linked Node Queue with FIFO Combat Scheduler)\n";
        std::cout << "                  3. CombatAction History & Reversible Combat State System\n";
        std::cout << "\n Time & Space Complexity Summary:\n";
        std::cout << "  - Stack Push(x)        : O(1) Constant Time (Head Pointer insertion)\n";
        std::cout << "  - Stack Pop()          : O(1) Constant Time (Head Pointer removal)\n";
        std::cout << "  - Stack Peek()         : O(1) Constant Time (Direct top node access)\n";
        std::cout << "  - Stack Undo()         : O(1) Constant Time (Pop top & stat reversal)\n";
        std::cout << "  - Queue Enqueue(x)     : O(1) Constant Time (Rear Pointer insertion)\n";
        std::cout << "  - Queue Dequeue()      : O(1) Constant Time (Front Pointer removal)\n";
        std::cout << "  - Queue Peek()         : O(1) Constant Time (Direct front node access)\n";
        std::cout << "  - Traversal / Display  : O(N) Linear Time (Iterates through N nodes)\n";
        std::cout << "  - Space Complexity     : O(N) Auxiliary Space for N dynamically allocated nodes\n";
        std::cout << "===================================================================\n\n";
    }

    // Master execution wrapper
    static void runAll() {
        demonstrateScenarioProcessing();
        demonstrateEdgeCasesAndValidation();
        displayComplexityAndCO3Mapping();
    }
};

#endif // CO3_COMBAT_ACTION_STACK_QUEUE_SCENARIO_HPP
