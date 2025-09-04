package util;

public enum CsvField {
    ID(0),
    TYPE(1),
    TITLE(2),
    STATUS(3),
    DESCRIPTION(4),
    EPIC_ID(5),
    DURATION(6),
    START_TIME(7);

    private final int index;

    CsvField(int index) {
        this.index = index;
    }

    public int get() {
        return index;
    }
}
