#ifndef CO1_PRAC01_1D_ARRAYS_FIGHTER_MOVE_CATALOG_HPP
#define CO1_PRAC01_1D_ARRAYS_FIGHTER_MOVE_CATALOG_HPP

#include <string>

/*
 * Subject: Programming Laboratory (Data Structures & Algorithms)
 * Course Outcome: CO1 | Practical 01
 * Topic: 1D Arrays CRUD (Create, Read, Update, Delete) for Fighter Move Catalog
 */
struct MoveData {
    int moveId;
    std::string name;
    int damage;
    int startupFrames;
    int recoveryFrames;
};

class MoveCatalog {
private:
    static const int MAX_MOVES = 16;
    MoveData moves[MAX_MOVES];
    int count;

public:
    MoveCatalog() : count(0) {
        // Pre-populate standard fighting game moves (CRUD read/insert)
        insertMove(1, "Jab Punch", 10, 4, 8);
        insertMove(2, "Heavy Kick", 16, 7, 12);
        insertMove(3, "Special Art", 28, 12, 20);
        insertMove(4, "Sweep Kick", 14, 6, 14);
    }

    bool insertMove(int id, const std::string& name, int dmg, int startup, int recovery) {
        if (count >= MAX_MOVES) return false;
        moves[count++] = {id, name, dmg, startup, recovery};
        return true;
    }

    bool updateMoveDamage(int id, int newDmg) {
        for (int i = 0; i < count; i++) {
            if (moves[i].moveId == id) {
                moves[i].damage = newDmg;
                return true;
            }
        }
        return false;
    }

    bool deleteMove(int id) {
        int idx = -1;
        for (int i = 0; i < count; i++) {
            if (moves[i].moveId == id) { idx = i; break; }
        }
        if (idx == -1) return false;
        for (int i = idx; i < count - 1; i++) {
            moves[i] = moves[i + 1];
        }
        count--;
        return true;
    }

    const MoveData* getMove(int id) const {
        for (int i = 0; i < count; i++) {
            if (moves[i].moveId == id) return &moves[i];
        }
        return nullptr;
    }

    int getMoveCount() const { return count; }
};

#endif // CO1_PRAC01_1D_ARRAYS_FIGHTER_MOVE_CATALOG_HPP
