#ifndef CO3_PRAC06_STACK_COMBAT_FORMULA_EVALUATOR_HPP
#define CO3_PRAC06_STACK_COMBAT_FORMULA_EVALUATOR_HPP

#include <iostream>
#include <string>
#include <vector>
#include <sstream>
#include <cctype>
#include <cmath>
#include <map>

/*
 * Subject: Programming Laboratory (Data Structures & Algorithms)
 * Course Outcome: CO3 | Practical 06
 * Topic: Stack Data Structure for Combat Damage Formula Conversions & Evaluation
 */

// Custom Generic Stack implementation using dynamically allocated nodes
template <typename T>
class CustomStack {
private:
    struct Node {
        T data;
        Node* next;
        Node(const T& val, Node* nxt = nullptr) : data(val), next(nxt) {}
    };

    Node* topNode;
    int stackSize;

public:
    CustomStack() : topNode(nullptr), stackSize(0) {}

    ~CustomStack() {
        clear();
    }

    // Copy Constructor (Deep Copy preserving LIFO order)
    CustomStack(const CustomStack& other) : topNode(nullptr), stackSize(0) {
        if (other.topNode == nullptr) return;
        // Collect nodes from other into temporary buffer to preserve top-to-bottom order
        std::vector<T> items;
        Node* curr = other.topNode;
        while (curr != nullptr) {
            items.push_back(curr->data);
            curr = curr->next;
        }
        for (int i = (int)items.size() - 1; i >= 0; i--) {
            push(items[i]);
        }
    }

    // Copy Assignment Operator (Deep Copy with self-assignment protection)
    CustomStack& operator=(const CustomStack& other) {
        if (this != &other) {
            clear();
            if (other.topNode != nullptr) {
                std::vector<T> items;
                Node* curr = other.topNode;
                while (curr != nullptr) {
                    items.push_back(curr->data);
                    curr = curr->next;
                }
                for (int i = (int)items.size() - 1; i >= 0; i--) {
                    push(items[i]);
                }
            }
        }
        return *this;
    }

    void push(const T& val) {
        topNode = new Node(val, topNode);
        stackSize++;
    }

    T pop() {
        if (isEmpty()) {
            std::cerr << "  [STACK ERROR] Underflow: Attempted to pop from empty stack!\n";
            return T();
        }
        Node* temp = topNode;
        T val = temp->data;
        topNode = topNode->next;
        delete temp;
        stackSize--;
        return val;
    }

    // Safe pop returning boolean success status
    bool pop(T& outVal) {
        if (isEmpty()) {
            return false;
        }
        Node* temp = topNode;
        outVal = temp->data;
        topNode = topNode->next;
        delete temp;
        stackSize--;
        return true;
    }

    T peek() const {
        if (isEmpty()) {
            return T();
        }
        return topNode->data;
    }

    // Safe peek returning boolean success status
    bool peek(T& outVal) const {
        if (isEmpty()) {
            return false;
        }
        outVal = topNode->data;
        return true;
    }

    bool isEmpty() const {
        return topNode == nullptr;
    }

    int size() const {
        return stackSize;
    }

    void clear() {
        while (!isEmpty()) {
            Node* temp = topNode;
            topNode = topNode->next;
            delete temp;
        }
        stackSize = 0;
    }

    // Traversal from top to bottom
    template <typename Func>
    void forEach(Func func) const {
        Node* curr = topNode;
        while (curr != nullptr) {
            func(curr->data);
            curr = curr->next;
        }
    }
};

class CombatFormulaStackEngine {
private:
    static bool isOperator(const std::string& token) {
        return (token == "+" || token == "-" || token == "*" || token == "/" || token == "^");
    }

    static bool isOperator(char c) {
        return (c == '+' || c == '-' || c == '*' || c == '/' || c == '^');
    }

    static int getPrecedence(char op) {
        if (op == '^') return 3;
        if (op == '*' || op == '/') return 2;
        if (op == '+' || op == '-') return 1;
        return 0;
    }

public:
    // a) Prefix Combat Formula to Infix Expression (Scanning right-to-left with Stack)
    static std::string prefixToInfix(const std::string& prefixFormula) {
        CustomStack<std::string> stack;
        std::vector<std::string> tokens;
        std::stringstream ss(prefixFormula);
        std::string token;

        while (ss >> token) {
            tokens.push_back(token);
        }

        // Scan tokens from right to left
        for (int i = (int)tokens.size() - 1; i >= 0; i--) {
            const std::string& current = tokens[i];

            if (isOperator(current)) {
                if (stack.size() < 2) return "INVALID_FORMULA";
                std::string op1 = stack.pop();
                std::string op2 = stack.pop();
                std::string combined = "(" + op1 + " " + current + " " + op2 + ")";
                stack.push(combined);
            } else {
                stack.push(current); // Operand: ATK, RAGE, COMBO, DEF, numbers
            }
        }

        return stack.isEmpty() ? "" : stack.pop();
    }

    // b) Infix Expression to Postfix Expression (Using Operator Stack)
    static std::string infixToPostfix(const std::string& infixFormula) {
        CustomStack<char> opStack;
        std::string postfix = "";
        std::string currentToken = "";

        auto flushToken = [&]() {
            if (!currentToken.empty()) {
                if (!postfix.empty() && postfix.back() != ' ') postfix += " ";
                postfix += currentToken;
                currentToken.clear();
            }
        };

        for (size_t i = 0; i < infixFormula.length(); i++) {
            char c = infixFormula[i];

            if (isspace(c)) {
                flushToken();
                continue;
            }

            if (isalnum(c) || c == '_' || c == '.') {
                currentToken += c;
            } else {
                flushToken();

                if (c == '(') {
                    opStack.push(c);
                } else if (c == ')') {
                    while (!opStack.isEmpty() && opStack.peek() != '(') {
                        if (!postfix.empty() && postfix.back() != ' ') postfix += " ";
                        postfix += opStack.pop();
                    }
                    if (!opStack.isEmpty() && opStack.peek() == '(') {
                        opStack.pop(); // Discard '('
                    }
                } else if (isOperator(c)) {
                    while (!opStack.isEmpty() && opStack.peek() != '(' &&
                           (getPrecedence(opStack.peek()) > getPrecedence(c) ||
                            (getPrecedence(opStack.peek()) == getPrecedence(c) && c != '^'))) {
                        if (!postfix.empty() && postfix.back() != ' ') postfix += " ";
                        postfix += opStack.pop();
                    }
                    opStack.push(c);
                }
            }
        }
        flushToken();

        while (!opStack.isEmpty()) {
            if (!postfix.empty() && postfix.back() != ' ') postfix += " ";
            postfix += opStack.pop();
        }

        return postfix;
    }

    // c) Postfix Evaluation using Operand Stack and Live Fighter Stats
    static double evaluatePostfix(const std::string& postfixFormula, 
                                  const std::map<std::string, double>& fighterStats) {
        CustomStack<double> valStack;
        std::stringstream ss(postfixFormula);
        std::string token;

        while (ss >> token) {
            if (isOperator(token)) {
                if (valStack.size() < 2) return 0.0;
                double val2 = valStack.pop();
                double val1 = valStack.pop();

                if (token == "+") valStack.push(val1 + val2);
                else if (token == "-") valStack.push(val1 - val2);
                else if (token == "*") valStack.push(val1 * val2);
                else if (token == "/") valStack.push(val2 != 0.0 ? val1 / val2 : 0.0);
                else if (token == "^") valStack.push(std::pow(val1, val2));
            } else {
                // Check if operand is a fighter battle stat
                auto it = fighterStats.find(token);
                if (it != fighterStats.end()) {
                    valStack.push(it->second);
                } else {
                    try {
                        double num = std::stod(token);
                        valStack.push(num);
                    } catch (...) {
                        valStack.push(0.0);
                    }
                }
            }
        }

        return valStack.isEmpty() ? 0.0 : valStack.pop();
    }

    // Complete Pipeline: Evaluates damage directly from Prefix formula
    static double calculateCombatDamage(const std::string& prefixFormula,
                                        double atk, double rage, double combo, double def,
                                        std::string& outInfix, std::string& outPostfix) {
        outInfix = prefixToInfix(prefixFormula);
        outPostfix = infixToPostfix(outInfix);

        std::map<std::string, double> stats = {
            {"ATK", atk},
            {"RAGE", rage},
            {"COMBO", combo},
            {"DEF", def}
        };

        return evaluatePostfix(outPostfix, stats);
    }
};

#endif // CO3_PRAC06_STACK_COMBAT_FORMULA_EVALUATOR_HPP
