package managers.history;

import model.Task;

class Node {
    /* так как весь класс объявлен package-private,
    *  то нет нужды указывать модификаторы доступа для полей*/
    final Task task;
    Node prev;
    Node next;

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
