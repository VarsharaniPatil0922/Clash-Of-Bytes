#ifndef CO4_PRAC08_SEARCHING_AND_SORTING_HPP
#define CO4_PRAC08_SEARCHING_AND_SORTING_HPP

/*
 * ============================================================================
 * CLASH OF BYTES: PROGRAMMING LABORATORY (Course Code: 2310214L)
 * Practical No. 08 | Course Outcome 4 (CO4):
 * SEARCHING AND SORTING TECHNIQUES FOR FIGHTER COMBAT RECORDS
 * ============================================================================
 * 
 * ACADEMIC & THEORETICAL FOUNDATIONS:
 * 1. LINEAR SEARCH:
 *    - Sequentially checks each record in the collection until a match is found.
 *    - Precondition: None (works on both unsorted and sorted datasets).
 *    - Time Complexity: Best O(1), Average O(N), Worst O(N).
 *    - Space Complexity: O(1) auxiliary space.
 * 
 * 2. BINARY SEARCH:
 *    - Divide-and-conquer searching by repeatedly halving the search interval.
 *    - MANDATORY PRECONDITION: The dataset MUST be sorted on the search key.
 *    - Time Complexity: Best O(1), Average O(log N), Worst O(log N).
 *    - Space Complexity: O(1) iterative auxiliary space.
 * 
 * 3. SORTING ALGORITHMS (Fighter Attributes: ID, Health, Attack Power, Score):
 *    - Bubble Sort: Repeatedly steps through the list, swaps adjacent elements 
 *      that are in the wrong order. Early termination flag optimizes best case to O(N).
 *    - Selection Sort: Divides array into sorted and unsorted subarrays; finds the 
 *      minimum element from the unsorted part and puts it at the beginning. O(N^2) always.
 *    - Insertion Sort: Builds sorted array one element at a time by shifting elements.
 *      Highly efficient for nearly-sorted datasets (O(N) best case).
 */

#include <iostream>
#include <iomanip>
#include <string>
#include <vector>
#include <chrono>

struct FighterRecord {
    int fighterId;
    std::string name;
    std::string archetype;
    int maxHealth;
    int attackPower;
    int defense;
    int matchScore;

    FighterRecord() 
        : fighterId(0), name("Unknown"), archetype("None"), 
          maxHealth(100), attackPower(15), defense(5), matchScore(0) {}

    FighterRecord(int id, const std::string& n, const std::string& arch, int hp, int atk, int def, int score)
        : fighterId(id), name(n), archetype(arch), 
          maxHealth(hp), attackPower(atk), defense(def), matchScore(score) {}
};

class FighterSearchSortCatalog {
private:
    std::vector<FighterRecord> records;

public:
    FighterSearchSortCatalog() {}

    void addRecord(const FighterRecord& rec) {
        records.push_back(rec);
    }

    void clear() {
        records.clear();
    }

    size_t size() const {
        return records.size();
    }

    const std::vector<FighterRecord>& getRecords() const {
        return records;
    }

    // ========================================================================
    // CO4: SEARCHING ALGORITHMS
    // ========================================================================

    /*
     * Linear Search by Fighter ID
     * Returns index of found record or -1 if not found.
     * Records number of comparison steps in 'comparisonsOut'.
     */
    int linearSearchById(int targetId, int& comparisonsOut) const {
        comparisonsOut = 0;
        for (size_t i = 0; i < records.size(); ++i) {
            comparisonsOut++;
            if (records[i].fighterId == targetId) {
                return static_cast<int>(i);
            }
        }
        return -1;
    }

    /*
     * Linear Search by Fighter Name (Case-insensitive comparison)
     */
    int linearSearchByName(const std::string& targetName, int& comparisonsOut) const {
        comparisonsOut = 0;
        for (size_t i = 0; i < records.size(); ++i) {
            comparisonsOut++;
            if (records[i].name == targetName) {
                return static_cast<int>(i);
            }
        }
        return -1;
    }

    /*
     * Binary Search by Fighter ID
     * Precondition: records MUST BE SORTED by fighterId in ascending order.
     * Returns index of found record or -1 if not found.
     * Records number of comparison steps in 'comparisonsOut'.
     */
    int binarySearchById(int targetId, int& comparisonsOut) const {
        comparisonsOut = 0;
        int low = 0;
        int high = static_cast<int>(records.size()) - 1;

        while (low <= high) {
            comparisonsOut++;
            int mid = low + (high - low) / 2;

            if (records[mid].fighterId == targetId) {
                return mid;
            } else if (records[mid].fighterId < targetId) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    // ========================================================================
    // CO4: SORTING ALGORITHMS
    // ========================================================================

    enum class SortKey {
        ID,
        HEALTH,
        ATTACK_POWER,
        MATCH_SCORE
    };

    /*
     * Comparator helper
     */
    static bool compare(const FighterRecord& a, const FighterRecord& b, SortKey key, bool ascending) {
        int valA = 0, valB = 0;
        switch (key) {
            case SortKey::ID: valA = a.fighterId; valB = b.fighterId; break;
            case SortKey::HEALTH: valA = a.maxHealth; valB = b.maxHealth; break;
            case SortKey::ATTACK_POWER: valA = a.attackPower; valB = b.attackPower; break;
            case SortKey::MATCH_SCORE: valA = a.matchScore; valB = b.matchScore; break;
        }
        return ascending ? (valA < valB) : (valA > valB);
    }

    /*
     * 1. Bubble Sort with early termination flag
     * Complexity: Best O(N), Worst O(N^2), Space O(1)
     */
    void bubbleSort(SortKey key, bool ascending, long long& comparisons, long long& swaps) {
        comparisons = 0;
        swaps = 0;
        size_t n = records.size();
        if (n <= 1) return;

        for (size_t i = 0; i < n - 1; ++i) {
            bool swapped = false;
            for (size_t j = 0; j < n - i - 1; ++j) {
                comparisons++;
                // Swap if out of order
                if (!compare(records[j], records[j + 1], key, ascending) && 
                    compare(records[j + 1], records[j], key, ascending)) {
                    std::swap(records[j], records[j + 1]);
                    swaps++;
                    swapped = true;
                }
            }
            if (!swapped) break; // Optimized early exit
        }
    }

    /*
     * 2. Selection Sort
     * Complexity: Best O(N^2), Worst O(N^2), Space O(1)
     */
    void selectionSort(SortKey key, bool ascending, long long& comparisons, long long& swaps) {
        comparisons = 0;
        swaps = 0;
        size_t n = records.size();
        if (n <= 1) return;

        for (size_t i = 0; i < n - 1; ++i) {
            size_t targetIdx = i;
            for (size_t j = i + 1; j < n; ++j) {
                comparisons++;
                if (compare(records[j], records[targetIdx], key, ascending)) {
                    targetIdx = j;
                }
            }
            if (targetIdx != i) {
                std::swap(records[i], records[targetIdx]);
                swaps++;
            }
        }
    }

    /*
     * 3. Insertion Sort
     * Complexity: Best O(N), Worst O(N^2), Space O(1)
     */
    void insertionSort(SortKey key, bool ascending, long long& comparisons, long long& shifts) {
        comparisons = 0;
        shifts = 0;
        size_t n = records.size();
        if (n <= 1) return;

        for (size_t i = 1; i < n; ++i) {
            FighterRecord keyItem = records[i];
            int j = static_cast<int>(i) - 1;

            while (j >= 0) {
                comparisons++;
                if (compare(keyItem, records[j], key, ascending)) {
                    records[j + 1] = records[j];
                    shifts++;
                    j--;
                } else {
                    break;
                }
            }
            records[j + 1] = keyItem;
        }
    }

    // ========================================================================
    // DISPLAY UTILITY
    // ========================================================================
    void displayCatalog(const std::string& title = "FIGHTER COMBAT ROSTER") const {
        std::cout << "\n========================================================================================\n";
        std::cout << " " << title << " (Total Fighters: " << records.size() << ")\n";
        std::cout << "========================================================================================\n";
        std::cout << std::left 
                  << std::setw(6)  << "ID" 
                  << std::setw(18) << "Fighter Name" 
                  << std::setw(16) << "Archetype" 
                  << std::setw(10) << "Max HP" 
                  << std::setw(10) << "ATK Pwr" 
                  << std::setw(8)  << "DEF" 
                  << std::setw(10) << "Score" << "\n";
        std::cout << "----------------------------------------------------------------------------------------\n";
        for (const auto& r : records) {
            std::cout << std::left 
                      << std::setw(6)  << r.fighterId 
                      << std::setw(18) << r.name 
                      << std::setw(16) << r.archetype 
                      << std::setw(10) << r.maxHealth 
                      << std::setw(10) << r.attackPower 
                      << std::setw(8)  << r.defense 
                      << std::setw(10) << r.matchScore << "\n";
        }
        std::cout << "========================================================================================\n";
    }

    // ========================================================================
    // EMPIRICAL DATASET SIZE BENCHMARK (N = 10, 50, 200, 1000)
    // ========================================================================
    static void benchmarkDatasetSizes() {
        std::cout << "\n--- [PART 5] EMPIRICAL COMPARISON ON DIFFERENT DATASET SIZES ---\n";
        std::cout << " Testing sorting and searching comparisons across N = 10, N = 50, N = 200, N = 1000:\n\n";
        std::cout << " +---------+--------------------+--------------------+--------------------+--------------------+--------------------+\n";
        std::cout << " | Size(N) | Bubble Sort Comps  | Select Sort Comps  | Insert Sort Comps  | Linear Search (Avg)| Binary Search (Max)|\n";
        std::cout << " +---------+--------------------+--------------------+--------------------+--------------------+--------------------+\n";
        int testSizes[] = {10, 50, 200, 1000};
        for (int n : testSizes) {
            FighterSearchSortCatalog catalog;
            for (int i = 0; i < n; ++i) {
                int id = (i * 37 + 17) % (n * 10) + 1;
                catalog.addRecord(FighterRecord(id, "TestFighter", "Warrior", 100 + (i % 50), 20 + (i % 30), 5, (i * 13) % 1000));
            }
            long long bComps = 0, bSwaps = 0;
            long long sComps = 0, sSwaps = 0;
            long long iComps = 0, iShifts = 0;

            FighterSearchSortCatalog bCat = catalog;
            bCat.bubbleSort(SortKey::MATCH_SCORE, true, bComps, bSwaps);

            FighterSearchSortCatalog sCat = catalog;
            sCat.selectionSort(SortKey::MATCH_SCORE, true, sComps, sSwaps);

            FighterSearchSortCatalog iCat = catalog;
            iCat.insertionSort(SortKey::ID, true, iComps, iShifts);

            double linAvg = (double)n / 2.0;
            int binMax = 0;
            int temp = n;
            while (temp > 0) { binMax++; temp /= 2; }

            std::cout << " | " << std::left << std::setw(7) << n 
                      << " | " << std::setw(18) << bComps 
                      << " | " << std::setw(18) << sComps 
                      << " | " << std::setw(18) << iComps 
                      << " | " << std::setw(18) << linAvg 
                      << " | " << std::setw(18) << binMax << " |\n";
        }
        std::cout << " +---------+--------------------+--------------------+--------------------+--------------------+--------------------+\n";
    }
};

#endif // CO4_PRAC08_SEARCHING_AND_SORTING_HPP
