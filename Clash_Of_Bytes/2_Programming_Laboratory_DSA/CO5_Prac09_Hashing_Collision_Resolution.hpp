#ifndef CO5_PRAC09_HASHING_COLLISION_RESOLUTION_HPP
#define CO5_PRAC09_HASHING_COLLISION_RESOLUTION_HPP

/*
 * ============================================================================
 * CLASH OF BYTES: PROGRAMMING LABORATORY (Course Code: 2310214L)
 * Practical No. 09 | Course Outcome 5 (CO5):
 * HASHING TECHNIQUES AND COLLISION HANDLING FOR FIGHTER DATA LOOKUP
 * ============================================================================
 * 
 * ACADEMIC & THEORETICAL FOUNDATIONS (CO5):
 * 1. HASH TABLE CONCEPT:
 *    - Direct-addressing mapping of arbitrary keys (Fighter ID) to table slots
 *      using a mathematical hash function h(k).
 *    - Time Complexity: Best & Average O(1) for Insert, Search, and Delete.
 *      Worst Case O(N) when severe clustering/collisions occur.
 * 
 * 2. HASH FUNCTION DESIGN:
 *    - Division (Modular) Method: h(key) = key % TABLE_SIZE
 *    - To minimize collisions, TABLE_SIZE is chosen as a prime number not 
 *      close to powers of 2.
 * 
 * 3. COLLISION RESOLUTION METHODS IMPLEMENTED:
 *    A) SEPARATE CHAINING (Closed Addressing):
 *       - Each table bucket is the head of a linked list storing all colliding keys.
 *       - Advantage: Never overflows; graceful degradation under high load factor.
 *       - Load Factor: alpha = N / M (can be > 1.0).
 * 
 *    B) LINEAR PROBING (Open Addressing):
 *       - All records stored directly inside the array.
 *       - On collision at slot h(k), linearly inspect consecutive slots:
 *         h(k, i) = (h(k) + i) % TABLE_SIZE, for i = 0, 1, 2, ...
 *       - Deleted entries marked with a tombstone (DELETED state) to preserve probe chains.
 *       - Load Factor: alpha <= 1.0 strictly.
 */

#include <iostream>
#include <iomanip>
#include <string>
#include <vector>
#include "CO4_Prac08_Searching_and_Sorting.hpp"

// ============================================================================
// METHOD 1: HASH TABLE WITH SEPARATE CHAINING
// ============================================================================
class ChainedFighterHashTable {
private:
    struct HashNode {
        int key; // Fighter ID
        FighterRecord data;
        HashNode* next;

        HashNode(int k, const FighterRecord& d) : key(k), data(d), next(nullptr) {}
    };

    int tableSize;
    int elementCount;
    std::vector<HashNode*> table;

    // Hash Function: Division Method
    int hashFunction(int key) const {
        return (key < 0 ? -key : key) % tableSize;
    }

public:
    explicit ChainedFighterHashTable(int size = 11) : tableSize(size), elementCount(0) {
        table.resize(tableSize, nullptr);
    }

    ~ChainedFighterHashTable() {
        clear();
    }

    void clear() {
        for (int i = 0; i < tableSize; ++i) {
            HashNode* curr = table[i];
            while (curr != nullptr) {
                HashNode* temp = curr;
                curr = curr->next;
                delete temp;
            }
            table[i] = nullptr;
        }
        elementCount = 0;
    }

    /*
     * Insert Fighter Record
     * Returns true on success, false if duplicate ID
     */
    bool insert(const FighterRecord& record, int& collisionsOut) {
        collisionsOut = 0;
        int index = hashFunction(record.fighterId);

        HashNode* curr = table[index];
        if (curr != nullptr) {
            collisionsOut++; // Collision occurred at this bucket
        }

        while (curr != nullptr) {
            if (curr->key == record.fighterId) {
                // Duplicate key: update record
                curr->data = record;
                return true;
            }
            curr = curr->next;
        }

        // Insert at head of bucket chain (O(1))
        HashNode* newNode = new HashNode(record.fighterId, record);
        newNode->next = table[index];
        table[index] = newNode;
        elementCount++;
        return true;
    }

    /*
     * Search Fighter Record by ID
     * Returns pointer to record or nullptr if not found.
     * Records number of probe comparisons.
     */
    const FighterRecord* search(int key, int& comparisonsOut) const {
        comparisonsOut = 0;
        int index = hashFunction(key);

        HashNode* curr = table[index];
        while (curr != nullptr) {
            comparisonsOut++;
            if (curr->key == key) {
                return &(curr->data);
            }
            curr = curr->next;
        }
        return nullptr;
    }

    /*
     * Delete Fighter Record by ID
     */
    bool remove(int key) {
        int index = hashFunction(key);
        HashNode* curr = table[index];
        HashNode* prev = nullptr;

        while (curr != nullptr) {
            if (curr->key == key) {
                if (prev == nullptr) {
                    table[index] = curr->next;
                } else {
                    prev->next = curr->next;
                }
                delete curr;
                elementCount--;
                return true;
            }
            prev = curr;
            curr = curr->next;
        }
        return false;
    }

    double getLoadFactor() const {
        return static_cast<double>(elementCount) / tableSize;
    }

    int size() const { return elementCount; }

    void displayTable() const {
        std::cout << "\n------------------------------------------------------------------------------------\n";
        std::cout << " HASH TABLE WITH SEPARATE CHAINING (Buckets: " << tableSize 
                  << ", Elements: " << elementCount << ", Load Factor: " << std::fixed << std::setprecision(2) << getLoadFactor() << ")\n";
        std::cout << "------------------------------------------------------------------------------------\n";
        for (int i = 0; i < tableSize; ++i) {
            std::cout << " Bucket [" << std::setw(2) << i << "]: ";
            HashNode* curr = table[i];
            if (!curr) {
                std::cout << "[ EMPTY ]\n";
                continue;
            }
            while (curr != nullptr) {
                std::cout << "-> (ID:" << curr->key << " | " << curr->data.name 
                          << " | HP:" << curr->data.maxHealth << ") ";
                curr = curr->next;
            }
            std::cout << "-> NULL\n";
        }
        std::cout << "------------------------------------------------------------------------------------\n";
    }
};

// ============================================================================
// METHOD 2: HASH TABLE WITH OPEN ADDRESSING (LINEAR PROBING)
// ============================================================================
class LinearProbingHashTable {
private:
    enum class SlotState {
        EMPTY,
        OCCUPIED,
        DELETED // Tombstone for deleted slots
    };

    struct Slot {
        FighterRecord data;
        SlotState state;
        Slot() : state(SlotState::EMPTY) {}
    };

    int tableSize;
    int elementCount;
    std::vector<Slot> table;

    int hashFunction(int key) const {
        return (key < 0 ? -key : key) % tableSize;
    }

public:
    explicit LinearProbingHashTable(int size = 13) : tableSize(size), elementCount(0) {
        table.resize(tableSize);
    }

    bool insert(const FighterRecord& record, int& probesOut) {
        probesOut = 0;
        if (elementCount >= tableSize) {
            std::cerr << "  [ERROR] Hash Table is full! Insertion failed.\n";
            return false;
        }

        int hashVal = hashFunction(record.fighterId);
        int firstTombstone = -1;

        for (int i = 0; i < tableSize; ++i) {
            probesOut++;
            int idx = (hashVal + i) % tableSize;

            if (table[idx].state == SlotState::OCCUPIED) {
                if (table[idx].data.fighterId == record.fighterId) {
                    // Update existing key
                    table[idx].data = record;
                    return true;
                }
            } else if (table[idx].state == SlotState::DELETED) {
                if (firstTombstone == -1) firstTombstone = idx;
            } else if (table[idx].state == SlotState::EMPTY) {
                int targetIdx = (firstTombstone != -1) ? firstTombstone : idx;
                table[targetIdx].data = record;
                table[targetIdx].state = SlotState::OCCUPIED;
                elementCount++;
                return true;
            }
        }

        if (firstTombstone != -1) {
            table[firstTombstone].data = record;
            table[firstTombstone].state = SlotState::OCCUPIED;
            elementCount++;
            return true;
        }

        return false;
    }

    const FighterRecord* search(int key, int& probesOut) const {
        probesOut = 0;
        int hashVal = hashFunction(key);

        for (int i = 0; i < tableSize; ++i) {
            probesOut++;
            int idx = (hashVal + i) % tableSize;

            if (table[idx].state == SlotState::EMPTY) {
                return nullptr; // Terminate search at empty slot
            }
            if (table[idx].state == SlotState::OCCUPIED && table[idx].data.fighterId == key) {
                return &(table[idx].data);
            }
        }
        return nullptr;
    }

    bool remove(int key) {
        int hashVal = hashFunction(key);
        for (int i = 0; i < tableSize; ++i) {
            int idx = (hashVal + i) % tableSize;
            if (table[idx].state == SlotState::EMPTY) {
                return false;
            }
            if (table[idx].state == SlotState::OCCUPIED && table[idx].data.fighterId == key) {
                table[idx].state = SlotState::DELETED; // Mark tombstone
                elementCount--;
                return true;
            }
        }
        return false;
    }

    double getLoadFactor() const {
        return static_cast<double>(elementCount) / tableSize;
    }

    void displayTable() const {
        std::cout << "\n------------------------------------------------------------------------------------\n";
        std::cout << " HASH TABLE WITH OPEN ADDRESSING (LINEAR PROBING)\n";
        std::cout << " Capacity: " << tableSize << " | Elements: " << elementCount 
                  << " | Load Factor: " << std::fixed << std::setprecision(2) << getLoadFactor() << "\n";
        std::cout << "------------------------------------------------------------------------------------\n";
        std::cout << std::left << std::setw(8) << "Slot" << std::setw(12) << "State" 
                  << std::setw(12) << "FighterID" << std::setw(18) << "Name" << "Stats\n";
        std::cout << "------------------------------------------------------------------------------------\n";
        for (int i = 0; i < tableSize; ++i) {
            std::cout << " [" << std::setw(4) << i << "] ";
            if (table[i].state == SlotState::EMPTY) {
                std::cout << std::setw(12) << "EMPTY" << "--\n";
            } else if (table[i].state == SlotState::DELETED) {
                std::cout << std::setw(12) << "DELETED" << "<Tombstone>\n";
            } else {
                std::cout << std::setw(12) << "OCCUPIED" 
                          << std::setw(12) << table[i].data.fighterId 
                          << std::setw(18) << table[i].data.name 
                          << "HP:" << table[i].data.maxHealth << " ATK:" << table[i].data.attackPower << "\n";
            }
        }
        std::cout << "------------------------------------------------------------------------------------\n";
    }
};

#endif // CO5_PRAC09_HASHING_COLLISION_RESOLUTION_HPP
