#ifndef CO2_PRAC05_CIRCULAR_LINKED_LIST_ARENA_BUFFS_HPP
#define CO2_PRAC05_CIRCULAR_LINKED_LIST_ARENA_BUFFS_HPP

#include <iostream>
#include <string>
#include <sstream>

/*
 * Subject: Programming Laboratory (Data Structures & Algorithms)
 * Course Outcome: CO2 | Practical 05
 * Topic: Circular Linked List for Arena Power-Ups & Fighter Buff Queue
 */

struct CombatBuff {
    int buffId;
    std::string buffName;
    std::string targetFighter;
    float magnitude;       // e.g. +20% damage, +15% speed, +25% defense
    float durationSeconds; // Remaining active duration
    std::string effectType; // "ATTACK_BOOST", "SPEED_HASTE", "SHIELD_FORTIFY", "RAGE_BURST"

    CombatBuff() 
        : buffId(0), buffName(""), targetFighter(""), magnitude(0.0f), durationSeconds(0.0f), effectType("") {}

    CombatBuff(int id, const std::string& name, const std::string& fighter, float mag, float dur, const std::string& type)
        : buffId(id), buffName(name), targetFighter(fighter), magnitude(mag), durationSeconds(dur), effectType(type) {}
};

struct CircularBuffNode {
    CombatBuff data;
    CircularBuffNode* next;

    CircularBuffNode(const CombatBuff& buff) : data(buff), next(nullptr) {}
};

class CircularBuffQueue {
private:
    CircularBuffNode* last; // In a circular queue, 'last' points to the rear node; 'last->next' is the front node
    int maxCapacity;        // Maximum active arena buff slots
    int currentCount;

public:
    explicit CircularBuffQueue(int maxSlots = 5) 
        : last(nullptr), maxCapacity(maxSlots), currentCount(0) {}

    ~CircularBuffQueue() {
        clear();
    }

    bool isFull() const {
        return currentCount >= maxCapacity;
    }

    bool isEmpty() const {
        return last == nullptr;
    }

    int getCount() const {
        return currentCount;
    }

    int getMaxCapacity() const {
        return maxCapacity;
    }

    // Enqueue new combat buff (Insert at rear in O(1))
    bool enqueueBuff(int buffId, const std::string& name, const std::string& fighter, 
                     float magnitude, float duration, const std::string& type) {
        if (isFull()) {
            std::cout << "  [ARENA BUFF QUEUE FULL] Buff #" << buffId << " (" << name 
                      << ") rejected. Max slots (" << maxCapacity << ") active.\n";
            return false;
        }

        CombatBuff newBuff(buffId, name, fighter, magnitude, duration, type);
        CircularBuffNode* newNode = new CircularBuffNode(newBuff);

        if (isEmpty()) {
            last = newNode;
            last->next = last; // Self-referential circular link
        } else {
            newNode->next = last->next; // Point to front
            last->next = newNode;       // Link old rear to new node
            last = newNode;             // Advance rear pointer
        }

        currentCount++;
        std::cout << "  [BUFF QUEUED] #" << buffId << " " << name << " -> " << fighter 
                  << " [Slots: " << currentCount << "/" << maxCapacity << "]\n";
        return true;
    }

    // Consume buff in First-Come First-Served sequence (Delete from front in O(1))
    bool consumeBuff(CombatBuff& consumed) {
        if (isEmpty()) {
            std::cout << "  [ARENA BUFF QUEUE EMPTY] No pending power-ups to consume.\n";
            return false;
        }

        CircularBuffNode* front = last->next;
        consumed = front->data;

        if (last == front) {
            // Only one buff node remaining
            delete front;
            last = nullptr;
        } else {
            last->next = front->next; // Bypass front node
            delete front;
        }

        currentCount--;
        std::cout << "  [BUFF CONSUMED] #" << consumed.buffId << " " << consumed.buffName 
                  << " applied to " << consumed.targetFighter << "! (" << currentCount << " pending)\n";
        return true;
    }

    // Rotate active buff wheel (Circular traversal: advance front pointer in O(1))
    void rotateBuffWheel() {
        if (isEmpty() || last->next == last) return;
        last = last->next; // Advances active front power-up without deleting
    }

    // Traverse and display all queued arena power-ups in circular order
    void displayActiveBuffs() const {
        if (isEmpty()) {
            std::cout << "  [ARENA BUFF QUEUE] No active combat buffs in queue.\n";
            return;
        }

        std::cout << "  --- Active Arena Combat Buffs (Circular Queue: " << currentCount << "/" << maxCapacity << ") ---\n";
        CircularBuffNode* curr = last->next; // Start from front
        int slot = 1;
        do {
            std::cout << "    Slot " << slot++ << ": [" << curr->data.buffName << "] Target: " 
                      << curr->data.targetFighter << " | Mag: +" << (curr->data.magnitude * 100.0f) 
                      << "% | Type: " << curr->data.effectType << " | Dur: " 
                      << curr->data.durationSeconds << "s\n";
            curr = curr->next;
        } while (curr != last->next);
    }

    // Get current front power-up summary for HUD rendering
    std::string getFrontSummary() const {
        if (isEmpty()) return "Buff Queue: Empty (0/" + std::to_string(maxCapacity) + ")";
        CircularBuffNode* front = last->next;
        return "[" + front->data.buffName + "] -> " + front->data.targetFighter + 
               " (+" + std::to_string((int)(front->data.magnitude * 100.0f)) + "% " + 
               front->data.effectType + ") [" + std::to_string(currentCount) + "/" + 
               std::to_string(maxCapacity) + "]";
    }

    void clear() {
        if (isEmpty()) return;

        CircularBuffNode* curr = last->next;
        while (curr != last) {
            CircularBuffNode* temp = curr;
            curr = curr->next;
            delete temp;
        }
        delete last;
        last = nullptr;
        currentCount = 0;
    }
};

#endif // CO2_PRAC05_CIRCULAR_LINKED_LIST_ARENA_BUFFS_HPP
