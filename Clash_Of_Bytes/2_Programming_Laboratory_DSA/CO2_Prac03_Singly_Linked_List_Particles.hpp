#ifndef CO2_PRAC03_SINGLY_LINKED_LIST_PARTICLES_HPP
#define CO2_PRAC03_SINGLY_LINKED_LIST_PARTICLES_HPP

#include <iostream>

/*
 * Subject: Programming Laboratory (Data Structures & Algorithms)
 * Course Outcome: CO2 | Practical 03
 * Topic: Singly Linked List with Dynamic O(1) Head Insertion & Deletion for Combat Particles
 */
template <typename T>
struct Node {
    T data;
    Node<T>* next;
    Node(const T& val) : data(val), next(nullptr) {}
};

template <typename T>
class SinglyLinkedList {
private:
    Node<T>* head;
    size_t count;

public:
    SinglyLinkedList() : head(nullptr), count(0) {}
    ~SinglyLinkedList() { clear(); }

    // O(1) Insertion at Front
    void insertFront(const T& val) {
        Node<T>* newNode = new Node<T>(val);
        newNode->next = head;
        head = newNode;
        count++;
    }

    // Traversal and lambda execution
    template <typename Func>
    void forEach(Func func) {
        Node<T>* curr = head;
        while (curr != nullptr) {
            func(curr->data);
            curr = curr->next;
        }
    }

    // Deletion of expired particle nodes
    template <typename Predicate>
    void removeIf(Predicate pred) {
        Node<T>* curr = head;
        Node<T>* prev = nullptr;

        while (curr != nullptr) {
            if (pred(curr->data)) {
                Node<T>* toDelete = curr;
                if (prev == nullptr) {
                    head = curr->next;
                } else {
                    prev->next = curr->next;
                }
                curr = curr->next;
                delete toDelete;
                count--;
            } else {
                prev = curr;
                curr = curr->next;
            }
        }
    }

    void clear() {
        while (head != nullptr) {
            Node<T>* temp = head;
            head = head->next;
            delete temp;
        }
        count = 0;
    }

    size_t size() const { return count; }
    bool isEmpty() const { return count == 0; }
};

#endif // CO2_PRAC03_SINGLY_LINKED_LIST_PARTICLES_HPP
