#ifndef CO2_PRAC04_DOUBLY_LINKED_LIST_MATCH_SCORES_HPP
#define CO2_PRAC04_DOUBLY_LINKED_LIST_MATCH_SCORES_HPP

#include <string>
#include <iostream>

/*
 * Subject: Programming Laboratory (Data Structures & Algorithms)
 * Course Outcome: CO2 | Practical 04
 * Topic: Doubly Linked List Sorting & O(N1 + N2) Merging for Player Match Scores
 */
struct MatchScore {
    std::string fighterName;
    int damageDealt;
    int hitsLanded;
    int roundsWon;
};

struct DNode {
    MatchScore data;
    DNode* prev;
    DNode* next;
    DNode(const MatchScore& score) : data(score), prev(nullptr), next(nullptr) {}
};

class DoublyLinkedList {
private:
    DNode* head;
    DNode* tail;
    int count;

public:
    DoublyLinkedList() : head(nullptr), tail(nullptr), count(0) {}
    ~DoublyLinkedList() { clear(); }

    void insertEnd(const MatchScore& score) {
        DNode* newNode = new DNode(score);
        if (!head) {
            head = tail = newNode;
        } else {
            tail->next = newNode;
            newNode->prev = tail;
            tail = newNode;
        }
        count++;
    }

    // Sort by damage dealt (Descending)
    void sortDescending() {
        if (!head || !head->next) return;

        bool swapped;
        do {
            swapped = false;
            DNode* curr = head;
            while (curr->next != nullptr) {
                if (curr->data.damageDealt < curr->next->data.damageDealt) {
                    std::swap(curr->data, curr->next->data);
                    swapped = true;
                }
                curr = curr->next;
            }
        } while (swapped);
    }

    // Linear O(N1 + N2) Merge of two sorted score lists
    static DoublyLinkedList mergeSortedDescending(DoublyLinkedList& l1, DoublyLinkedList& l2) {
        DoublyLinkedList merged;
        DNode* p1 = l1.head;
        DNode* p2 = l2.head;

        while (p1 != nullptr && p2 != nullptr) {
            if (p1->data.damageDealt >= p2->data.damageDealt) {
                merged.insertEnd(p1->data);
                p1 = p1->next;
            } else {
                merged.insertEnd(p2->data);
                p2 = p2->next;
            }
        }
        while (p1 != nullptr) { merged.insertEnd(p1->data); p1 = p1->next; }
        while (p2 != nullptr) { merged.insertEnd(p2->data); p2 = p2->next; }
        return merged;
    }

    void clear() {
        DNode* curr = head;
        while (curr != nullptr) {
            DNode* temp = curr;
            curr = curr->next;
            delete temp;
        }
        head = tail = nullptr;
        count = 0;
    }

    int size() const { return count; }
    DNode* getHead() const { return head; }
};

#endif // CO2_PRAC04_DOUBLY_LINKED_LIST_MATCH_SCORES_HPP
