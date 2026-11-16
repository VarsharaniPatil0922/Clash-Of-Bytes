package CO3.Collections;

import CO3.Exceptions.CombatValidationException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Course Outcome: CO3 | Practical 03: Collections Framework
 * 
 * Manages combat turns using a Queue data structure (FIFO - First-In-First-Out).
 * Backed by java.util.ArrayDeque, ensuring zero unnecessary allocations,
 * deterministic sequential execution, and fast O(1) enqueue/dequeue operations.
 */
public class CombatActionQueue {

    private final Queue<CombatAction> actionQueue;

    public CombatActionQueue() {
        // ArrayDeque is significantly faster and more cache-friendly than LinkedList
        this.actionQueue = new ArrayDeque<>();
    }

    /**
     * Enqueues an action to the back of the queue.
     */
    public synchronized void enqueueAction(CombatAction action) throws CombatValidationException {
        if (action == null) {
            throw new CombatValidationException("Cannot enqueue a null combat action!");
        }
        actionQueue.offer(action);
    }

    /**
     * Retrieves and removes the next action from the front of the FIFO queue.
     * Throws CombatValidationException if queue is empty.
     */
    public synchronized CombatAction dequeueNextAction() throws CombatValidationException {
        if (actionQueue.isEmpty()) {
            throw new CombatValidationException("Combat action queue underflow: No pending actions remaining to execute!");
        }
        return actionQueue.poll();
    }

    /**
     * Peeks at the next action without removing it.
     */
    public synchronized CombatAction peekNextAction() throws CombatValidationException {
        if (actionQueue.isEmpty()) {
            throw new CombatValidationException("Combat action queue underflow: Cannot peek into an empty action queue!");
        }
        return actionQueue.peek();
    }

    public synchronized int size() {
        return actionQueue.size();
    }

    public synchronized boolean isEmpty() {
        return actionQueue.isEmpty();
    }

    public synchronized void clear() {
        actionQueue.clear();
    }

    public synchronized List<CombatAction> toList() {
        return new ArrayList<>(actionQueue);
    }
}
