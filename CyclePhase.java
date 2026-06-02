public enum CyclePhase {
    FIRST_THREE("전3주 (1~3주차)"),
    LAST_THREE ("후3주 (4~6주차)"),
    DELOAD     ("디로딩 (7주차)");

    private final String label;

    CyclePhase(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public boolean isIncreased() {
        return this != FIRST_THREE;
    }

    public boolean isDeload() {
        return this == DELOAD;
    }

    public int firstWeekNumber() {
        if (this == FIRST_THREE) return 1;
        if (this == LAST_THREE)  return 4;
        return 7;
    }
}
