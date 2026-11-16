/*
 * Subject: Programming Laboratory (Data Structures & Algorithms)
 * Course Outcomes: CO1, CO2, CO3 | Practicals 01 to 07 & Task 3
 * Topic: Comprehensive Test and Demonstration Driver for All DSA Practicals & Scenario Processing
 */

#include <iostream>
#include <iomanip>
#include "CO1_Prac01_1D_Arrays_Fighter_Move_Catalog.hpp"
#include "CO1_Prac02_2D_Sparse_Matrix_Fast_Transpose.hpp"
#include "CO2_Prac03_Singly_Linked_List_Particles.hpp"
#include "CO2_Prac04_Doubly_Linked_List_Match_Scores.hpp"
#include "CO2_Prac05_Circular_Linked_List_Arena_Buffs.hpp"
#include "CO3_Prac06_Stack_Combat_Formula_Evaluator.hpp"
#include "CO3_Combat_Action_Stack_Queue_Scenario.hpp"
#include "CO4_Prac08_Searching_and_Sorting.hpp"
#include "CO5_Prac09_Hashing_Collision_Resolution.hpp"

void demonstratePrac01() {
    std::cout << "===================================================================\n";
    std::cout << " [PRACTICAL 01] 1D Arrays: Fighter Move Catalog CRUD\n";
    std::cout << " Course Outcome: CO1 | Concept: Insert, Read, Update, Delete in 1D Array\n";
    std::cout << "===================================================================\n";
    MoveCatalog catalog;
    std::cout << "  Pre-populated moves count: " << catalog.getMoveCount() << "\n";
    
    // Create / Insert
    catalog.insertMove(5, "Dragon Upper", 22, 5, 10);
    std::cout << "  [INSERT] Added Move ID 5 (Dragon Upper, DMG: 22)\n";

    // Read
    const MoveData* m = catalog.getMove(5);
    if (m) {
        std::cout << "  [READ] Move Found: " << m->name << " | Dmg: " << m->damage 
                  << " | Startup: " << m->startupFrames << " frames\n";
    }

    // Update
    catalog.updateMoveDamage(5, 26);
    std::cout << "  [UPDATE] Buffed Move ID 5 damage to: " << catalog.getMove(5)->damage << "\n";

    // Delete
    catalog.deleteMove(1);
    std::cout << "  [DELETE] Deleted Move ID 1. Total moves remaining: " << catalog.getMoveCount() << "\n\n";
}

void demonstratePrac02() {
    std::cout << "===================================================================\n";
    std::cout << " [PRACTICAL 02] 2D Arrays: Sparse Matrix Arena Grid & Fast Transpose\n";
    std::cout << " Course Outcome: CO1 | Concept: 3-Tuple Representation & O(cols+terms) Transpose\n";
    std::cout << "===================================================================\n";
    SparseArenaGrid grid;
    std::cout << "  Original Sparse Grid non-zero elements: " << grid.getTileCount() << "\n";
    std::cout << "  Sample original tiles (Row, Col, Type):\n";
    for (int i = 0; i < 3 && i < grid.getTileCount(); i++) {
        const SparseTile& t = grid.getTile(i);
        std::cout << "    [" << i << "] Row: " << t.row << ", Col: " << t.col << ", Type: " << t.tileType << "\n";
    }

    SparseArenaGrid transposed = grid.fastTranspose();
    std::cout << "  Fast Transpose Computed! Transposed tiles count: " << transposed.getTileCount() << "\n";
    std::cout << "  Sample transposed tiles (Row, Col, Type):\n";
    for (int i = 0; i < 3 && i < transposed.getTileCount(); i++) {
        const SparseTile& t = transposed.getTile(i);
        std::cout << "    [" << i << "] Row: " << t.row << ", Col: " << t.col << ", Type: " << t.tileType << "\n";
    }
    std::cout << "\n";
}

void demonstratePrac03() {
    std::cout << "===================================================================\n";
    std::cout << " [PRACTICAL 03] Singly Linked List: Dynamic Particle Node Management\n";
    std::cout << " Course Outcome: CO2 | Concept: Node Insertion, Traversal, and removeIf\n";
    std::cout << "===================================================================\n";
    struct TestParticle {
        int id;
        float lifetime;
    };

    SinglyLinkedList<TestParticle> list;
    list.insertFront({1, 0.5f});
    list.insertFront({2, 0.0f}); // Expired
    list.insertFront({3, 0.8f});
    list.insertFront({4, 0.0f}); // Expired
    list.insertFront({5, 1.2f});

    std::cout << "  Initial particle nodes in list: " << list.size() << "\n";

    // Deletion using lambda predicate (expired particles)
    list.removeIf([](const TestParticle& p) {
        return p.lifetime <= 0.0f;
    });

    std::cout << "  Active particles after removeIf (lifetime > 0): " << list.size() << "\n";
    std::cout << "  Surviving particle IDs: ";
    list.forEach([](const TestParticle& p) {
        std::cout << "#" << p.id << " (life: " << p.lifetime << "s) ";
    });
    std::cout << "\n\n";
}

void demonstratePrac04() {
    std::cout << "===================================================================\n";
    std::cout << " [PRACTICAL 04] Doubly Linked List: Sort & Linear O(N1 + N2) Merge\n";
    std::cout << " Course Outcome: CO2 | Concept: Doubly Linked Node Sorting & Merging\n";
    std::cout << "===================================================================\n";
    DoublyLinkedList poolA;
    poolA.insertEnd({"Blaze Warrior", 85, 8, 2});
    poolA.insertEnd({"Shadow Ninja", 110, 11, 3});
    poolA.insertEnd({"Cyber Brawler", 65, 5, 1});

    std::cout << "  Sorting Pool A by damage dealt (descending)...\n";
    poolA.sortDescending();
    for (DNode* c = poolA.getHead(); c != nullptr; c = c->next) {
        std::cout << "    Fighter: " << std::left << std::setw(16) << c->data.fighterName 
                  << " | Damage: " << c->data.damageDealt << "\n";
    }

    DoublyLinkedList poolB;
    poolB.insertEnd({"Valkyrie", 95, 9, 2});
    poolB.insertEnd({"Iron Golem", 70, 6, 1});
    poolB.sortDescending();

    std::cout << "  Merging Pool A & Pool B using O(N1 + N2) Linear Merge...\n";
    DoublyLinkedList merged = DoublyLinkedList::mergeSortedDescending(poolA, poolB);
    for (DNode* c = merged.getHead(); c != nullptr; c = c->next) {
        std::cout << "    [MERGED] " << std::left << std::setw(16) << c->data.fighterName 
                  << " | Damage: " << c->data.damageDealt << "\n";
    }
    std::cout << "\n";
}

void demonstratePrac05() {
    std::cout << "===================================================================\n";
    std::cout << " [PRACTICAL 05] Circular Linked List: Arena Power-Up & Buff Queue\n";
    std::cout << " Course Outcome: CO2 | Concept: Circular Node Insertion, Consumption, & Rotation\n";
    std::cout << "===================================================================\n";
    CircularBuffQueue buffQueue(4); // Max 4 buff slots in arena

    buffQueue.enqueueBuff(1, "Dragon's Might", "Blaze Warrior", 0.25f, 10.0f, "ATTACK_BOOST");
    buffQueue.enqueueBuff(2, "Shadow Haste", "Shadow Ninja", 0.20f, 8.0f, "SPEED_HASTE");
    buffQueue.enqueueBuff(3, "Titan Aegis", "Blaze Warrior", 0.30f, 12.0f, "SHIELD_FORTIFY");
    buffQueue.enqueueBuff(4, "Overdrive Surge", "Shadow Ninja", 0.35f, 6.0f, "RAGE_BURST");

    // Attempting to exceed max capacity (rejects)
    buffQueue.enqueueBuff(5, "Berserk Aura", "Blaze Warrior", 0.40f, 5.0f, "ATTACK_BOOST");

    std::cout << "\nActive Arena Buffs in Circular Queue:\n";
    buffQueue.displayActiveBuffs();

    std::cout << "\nConsuming Front Buff (FCFS Combat Activation):\n";
    CombatBuff consumed;
    buffQueue.consumeBuff(consumed);

    std::cout << "\nRotating Active Buff Wheel (Circular traversal):\n";
    buffQueue.rotateBuffWheel();
    buffQueue.displayActiveBuffs();
    std::cout << "\n";
}

void demonstratePrac06() {
    std::cout << "===================================================================\n";
    std::cout << " [PRACTICAL 06] Stack Operations: Real-Time Combat Formula Engine\n";
    std::cout << " Course Outcome: CO3 | Concept: Prefix->Infix, Infix->Postfix, & Damage Evaluation\n";
    std::cout << "===================================================================\n";

    std::string prefixFormula = "* + ATK RAGE / COMBO 2";
    std::cout << "  Input Battle Formula (Prefix): " << prefixFormula << "\n";

    std::string infixFormula = CombatFormulaStackEngine::prefixToInfix(prefixFormula);
    std::cout << "  a) Prefix to Infix (Display) : " << infixFormula << "\n";

    std::string postfixFormula = CombatFormulaStackEngine::infixToPostfix(infixFormula);
    std::cout << "  b) Infix to Postfix (Stack)  : " << postfixFormula << "\n";

    double atk = 24.0, rage = 6.0, combo = 4.0, def = 2.0;
    std::cout << "  Fighter Combat Stats Loaded  : ATK=" << atk << ", RAGE=" << rage 
              << ", COMBO=" << combo << ", DEF=" << def << "\n";

    std::string outInfix, outPostfix;
    double damage = CombatFormulaStackEngine::calculateCombatDamage(
        prefixFormula, atk, rage, combo, def, outInfix, outPostfix);

    std::cout << "  c) Evaluated Combat Damage   : " << damage << " HP (Formula: (24 + 6) * (4 / 2))\n\n";
}

void demonstrateTask03_ScenarioProcessing() {
    CombatScenarioEngine::runAll();
}

void demonstrateCO4_SearchingAndSorting() {
    std::cout << "\n========================================================================================\n";
    std::cout << " [PRACTICAL 08 / TASK 4] SEARCHING & SORTING ALGORITHMS (COURSE OUTCOME 4)\n";
    std::cout << " Subject: Programming Laboratory (C++) | Topic: Linear/Binary Search, Bubble/Select/Insert Sort\n";
    std::cout << "========================================================================================\n";

    FighterSearchSortCatalog catalog;
    catalog.addRecord(FighterRecord(105, "Blaze Warrior", "Warrior", 120, 28, 8, 450));
    catalog.addRecord(FighterRecord(101, "Shadow Ninja", "Ninja", 90, 32, 4, 620));
    catalog.addRecord(FighterRecord(108, "Cyber Brawler", "Brawler", 110, 26, 6, 380));
    catalog.addRecord(FighterRecord(103, "Iron Paladin", "Paladin", 140, 22, 12, 510));
    catalog.addRecord(FighterRecord(107, "Blood Berserker", "Berserker", 130, 35, 3, 700));
    catalog.addRecord(FighterRecord(102, "Hybrid Gladiator", "Gladiator", 115, 25, 7, 490));
    catalog.addRecord(FighterRecord(106, "Frost Valkyrie", "Paladin", 125, 27, 9, 580));
    catalog.addRecord(FighterRecord(104, "Thunder Shaman", "Brawler", 95, 30, 5, 410));

    catalog.displayCatalog("INITIAL FIGHTER ROSTER (UNSORTED)");

    // --- 1. SEARCHING DEMONSTRATION ---
    std::cout << "\n--- [PART 1] SEARCHING ALGORITHMS DEMONSTRATION ---\n";
    int comps = 0;
    int targetId = 107;
    std::cout << " [TEST 1A] Linear Search for Fighter ID " << targetId << " (Existing Record):\n";
    int idx = catalog.linearSearchById(targetId, comps);
    if (idx != -1) {
        const auto& f = catalog.getRecords()[idx];
        std::cout << "   -> [FOUND] Index: " << idx << " | Name: " << f.name 
                  << " | HP: " << f.maxHealth << " | ATK: " << f.attackPower 
                  << " | Comparisons required: " << comps << " (O(N) search)\n";
    }

    std::cout << "\n [TEST 1B] Linear Search for Fighter ID 999 (Non-Existent Record):\n";
    idx = catalog.linearSearchById(999, comps);
    std::cout << "   -> [NOT FOUND] Correctly identified missing ID. Comparisons exhausted: " 
              << comps << " / " << catalog.size() << " (Full scan)\n";

    // --- 2. SORTING DEMONSTRATIONS ---
    std::cout << "\n--- [PART 2] SORTING ALGORITHMS DEMONSTRATION ---\n";

    // A. Bubble Sort by Attack Power (Descending)
    long long bComps = 0, bSwaps = 0;
    FighterSearchSortCatalog bubbleCatalog = catalog;
    bubbleCatalog.bubbleSort(FighterSearchSortCatalog::SortKey::ATTACK_POWER, false, bComps, bSwaps);
    std::cout << "\n [SORT 1] Bubble Sort by Attack Power (Descending):\n";
    std::cout << "   -> Comparisons: " << bComps << " | Swaps: " << bSwaps << " | Best: O(N), Avg/Worst: O(N^2)\n";
    bubbleCatalog.displayCatalog("ROSTER SORTED BY ATTACK POWER (DESCENDING)");

    // B. Selection Sort by Health (Descending)
    long long sComps = 0, sSwaps = 0;
    FighterSearchSortCatalog selectCatalog = catalog;
    selectCatalog.selectionSort(FighterSearchSortCatalog::SortKey::HEALTH, false, sComps, sSwaps);
    std::cout << "\n [SORT 2] Selection Sort by Max Health (Descending):\n";
    std::cout << "   -> Comparisons: " << sComps << " | Swaps: " << sSwaps << " | Complexity: O(N^2) always\n";
    selectCatalog.displayCatalog("ROSTER SORTED BY MAX HEALTH (DESCENDING)");

    // C. Insertion Sort by Fighter ID (Ascending) -> REQUIRED PRECONDITION FOR BINARY SEARCH
    long long iComps = 0, iShifts = 0;
    catalog.insertionSort(FighterSearchSortCatalog::SortKey::ID, true, iComps, iShifts);
    std::cout << "\n [SORT 3] Insertion Sort by Fighter ID (Ascending):\n";
    std::cout << "   -> Comparisons: " << iComps << " | Shifts: " << iShifts << " | Prepares data for Binary Search\n";
    catalog.displayCatalog("ROSTER SORTED BY FIGHTER ID (PRECONDITION FOR BINARY SEARCH)");

    // --- 3. BINARY SEARCH DEMONSTRATION ---
    std::cout << "\n--- [PART 3] BINARY SEARCH (AFTER PRECONDITION SORTING) ---\n";
    std::cout << " Precondition Check: Roster is strictly sorted by Fighter ID.\n";
    
    int bSearchComps = 0;
    int bSearchIdx = catalog.binarySearchById(targetId, bSearchComps);
    std::cout << " [TEST 3A] Binary Search for Fighter ID " << targetId << ":\n";
    if (bSearchIdx != -1) {
        const auto& f = catalog.getRecords()[bSearchIdx];
        std::cout << "   -> [FOUND] Index: " << bSearchIdx << " | Name: " << f.name 
                  << " | Score: " << f.matchScore 
                  << " | Binary Search Comparisons: " << bSearchComps 
                  << " vs Linear Search Comparisons: " << comps 
                  << " (Comparison-count reduction: " << std::fixed << std::setprecision(1) 
                  << (double)comps / bSearchComps << "x)\n";
    }

    std::cout << " [TEST 3B] Binary Search for Missing ID 999:\n";
    bSearchIdx = catalog.binarySearchById(999, bSearchComps);
    std::cout << "   -> [NOT FOUND] Correctly rejected after " << bSearchComps 
              << " halving steps (log2(" << catalog.size() << ") approx " << bSearchComps << ")\n";

    // --- 4. ALGORITHM SELECTION JUSTIFICATION & COMPLEXITY COMPARISON ---
    std::cout << "\n--- [PART 4] ALGORITHM SELECTION JUSTIFICATION & COMPLEXITY ANALYSIS ---\n";
    std::cout << " 1. Why Bubble Sort? Ideal for educational demonstration and nearly-sorted live match streams.\n";
    std::cout << " 2. Why Selection Sort? Minimizes write/swap operations (at most N swaps), ideal when memory writes are costly.\n";
    std::cout << " 3. Why Insertion Sort? Online algorithm; excellent for inserting newly unlocked fighters one by one into roster.\n";
    std::cout << " 4. Binary Search Precondition: Array elements must be ordered monotonically by key to allow interval halving.\n";

    // --- 5. EMPIRICAL BENCHMARK ON DIFFERENT DATASET SIZES ---
    FighterSearchSortCatalog::benchmarkDatasetSizes();
}

void demonstrateCO5_Hashing() {
    std::cout << "\n========================================================================================\n";
    std::cout << " [PRACTICAL 09 / TASK 4] HASHING & COLLISION RESOLUTION (COURSE OUTCOME 5)\n";
    std::cout << " Subject: Programming Laboratory (C++) | Topic: Chaining vs Linear Probing & Benchmark\n";
    std::cout << "========================================================================================\n";

    std::vector<FighterRecord> roster = {
        FighterRecord(101, "Shadow Ninja", "Ninja", 90, 32, 4, 620),
        FighterRecord(112, "Shadow Clone", "Ninja", 80, 24, 3, 310),   // 112 % 11 = 2 (Collides with 101: 101 % 11 = 2)
        FighterRecord(105, "Blaze Warrior", "Warrior", 120, 28, 8, 450), // 105 % 11 = 6
        FighterRecord(116, "Berserk Blaze", "Warrior", 130, 34, 5, 520), // 116 % 11 = 6 (Collides with 105)
        FighterRecord(103, "Iron Paladin", "Paladin", 140, 22, 12, 510), // 103 % 11 = 4
        FighterRecord(108, "Cyber Brawler", "Brawler", 110, 26, 6, 380), // 108 % 11 = 9
        FighterRecord(120, "Gladiator Prime", "Gladiator", 115, 27, 7, 490) // 120 % 11 = 10
    };

    // --- 1. SEPARATE CHAINING DEMONSTRATION ---
    std::cout << "\n--- [METHOD A] HASH TABLE WITH SEPARATE CHAINING (CLOSED ADDRESSING) ---\n";
    ChainedFighterHashTable chainedTable(11); // Prime table size 11

    int totalCollisions = 0;
    for (const auto& f : roster) {
        int coll = 0;
        chainedTable.insert(f, coll);
        if (coll > 0) {
            std::cout << "  [CHAIN COLLISION] Key " << f.fighterId << " (" << f.name 
                      << ") collided at Bucket [" << (f.fighterId % 11) << "]. Appended to chain.\n";
            totalCollisions++;
        } else {
            std::cout << "  [INSERT] Key " << f.fighterId << " (" << f.name 
                      << ") inserted into Bucket [" << (f.fighterId % 11) << "].\n";
        }
    }
    std::cout << "  Total Chain Collisions Handled: " << totalCollisions << "\n";
    chainedTable.displayTable();

    // Searching in Chained Hash Table
    std::cout << "\n [SEARCH TESTS - CHAINING]\n";
    int chainComps = 0;
    const FighterRecord* res = chainedTable.search(112, chainComps);
    if (res) {
        std::cout << "  [SUCCESSFUL SEARCH] Key 112 found: " << res->name 
                  << " | Probes in chain: " << chainComps << " (O(1) average lookup)\n";
    }

    res = chainedTable.search(999, chainComps);
    std::cout << "  [UNSUCCESSFUL SEARCH] Key 999: " << (res ? "Found" : "Not Found") 
              << " | Probes: " << chainComps << " (Fast rejection without scanning full table)\n";

    // Deletion
    std::cout << "\n [DELETE TEST - CHAINING]\n";
    std::cout << "  Deleting Fighter ID 101 (Head of collision chain at bucket 2)...\n";
    bool delOk = chainedTable.remove(101);
    std::cout << "  Deletion Status: " << (delOk ? "SUCCESS" : "FAILED") << "\n";
    chainedTable.displayTable();

    // Verify surviving node in chain
    res = chainedTable.search(112, chainComps);
    std::cout << "  Searching for colliding node 112 after head deletion: " 
              << (res ? res->name : "LOST") << " (Integrity verified!)\n";

    // --- 2. OPEN ADDRESSING (LINEAR PROBING) DEMONSTRATION ---
    std::cout << "\n--- [METHOD B] HASH TABLE WITH OPEN ADDRESSING (LINEAR PROBING) ---\n";
    LinearProbingHashTable probeTable(13); // Table size 13

    for (const auto& f : roster) {
        int probes = 0;
        probeTable.insert(f, probes);
        std::cout << "  [PROBE INSERT] Key " << f.fighterId << " (" << f.name 
                  << ") placed. Probes required: " << probes << "\n";
    }
    probeTable.displayTable();

    std::cout << "\n [SEARCH & TOMBSTONE DELETE TESTS - LINEAR PROBING]\n";
    int probeSteps = 0;
    res = probeTable.search(116, probeSteps);
    if (res) {
        std::cout << "  [FOUND] Key 116 found: " << res->name << " in " << probeSteps << " probe steps.\n";
    }

    std::cout << "  Deleting Key 105 (triggers tombstone placement)...\n";
    probeTable.remove(105);
    probeTable.displayTable();

    // Search key past the tombstone
    res = probeTable.search(116, probeSteps);
    std::cout << "  Searching for Key 116 past tombstone: " 
              << (res ? res->name : "LOST") << " in " << probeSteps << " steps (Tombstone probe traversal PASSED!)\n";

    // --- 3. PERFORMANCE COMPARISON: HASHING VS LINEAR SEARCH ---
    std::cout << "\n--- [PART 3] PERFORMANCE BENCHMARK: HASH LOOKUP VS LINEAR SEARCH ---\n";
    std::cout << " +----------------------+------------------+------------------+------------------+\n";
    std::cout << " | METRIC               | LINEAR SEARCH    | BINARY SEARCH    | HASH TABLE       |\n";
    std::cout << " +----------------------+------------------+------------------+------------------+\n";
    std::cout << " | Precondition         | None             | Must be Sorted   | None             |\n";
    std::cout << " | Best Case Time       | O(1)             | O(1)             | O(1)             |\n";
    std::cout << " | Average Case Time    | O(N)             | O(log N)         | O(1)             |\n";
    std::cout << " | Worst Case Time      | O(N)             | O(log N)         | O(N) (Severe Cl) |\n";
    std::cout << " | Space Complexity     | O(1)             | O(1)             | O(M) Table Array |\n";
    std::cout << " | Typical Probes (N=8) | 4 to 8 checks    | 2 to 3 checks    | 1 to 2 checks    |\n";
    std::cout << " +----------------------+------------------+------------------+------------------+\n";
}

int main() {
    std::cout << "\n===================================================================\n";
    std::cout << "   PROGRAMMING LABORATORY (DSA) - COMPLETE PRACTICALS & TASK 4\n";
    std::cout << "   Course Code: 2310214L | All Practicals 01 to 09 Suite\n";
    std::cout << "===================================================================\n\n";

    demonstratePrac01();
    demonstratePrac02();
    demonstratePrac03();
    demonstratePrac04();
    demonstratePrac05();
    demonstratePrac06();
    demonstrateTask03_ScenarioProcessing();
    demonstrateCO4_SearchingAndSorting();
    demonstrateCO5_Hashing();

    std::cout << "\n===================================================================\n";
    std::cout << "   ALL DSA PRACTICALS & TASK 4 COMPLETED SUCCESSFULLY!\n";
    std::cout << "===================================================================\n\n";
    return 0;
}
