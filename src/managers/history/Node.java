package managers.history;

import model.Task;

public class Node {

    protected final Task task;
    protected Node prev;
    protected Node next;

    public Node(Node prev, Task task, Node next) {
        this.task = task;
        this.prev = prev;
        this.next = next;
    }

    @Override
    public String toString() {
        return String.format("task=%s, next=%s",
                this.task,
                this.next);
    }

}
