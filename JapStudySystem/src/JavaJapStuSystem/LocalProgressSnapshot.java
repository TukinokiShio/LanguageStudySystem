package LanguageStudySystem.JavaJapStuSystem;

/** Pure counts for the local dictionary's lifetime progress bar. */
final class LocalProgressSnapshot {
    final long liveEntries;
    final long historicalMastered;
    final long activeMastered;
    final long pendingEnrich;
    final long total;
    final long mastered;
    final long known;
    final long strange;

    private LocalProgressSnapshot(long liveEntries, long historicalMastered,
                                  long activeMastered, long known, long pendingEnrich) {
        this.liveEntries = liveEntries;
        this.historicalMastered = historicalMastered;
        this.activeMastered = activeMastered;
        this.pendingEnrich = pendingEnrich;
        this.total = liveEntries + historicalMastered;
        this.mastered = activeMastered + historicalMastered;
        this.known = known;
        this.strange = liveEntries - activeMastered - known;
    }

    static LocalProgressSnapshot calculate(JapStuJFrame.JaNode listHead,
                                           int historicalMastered) {
        long live = 0L;
        long activeMastered = 0L;
        long known = 0L;
        long pending = 0L;
        JapStuJFrame.JaNode node = listHead == null ? null : listHead.next;
        while (node != null) {
            live++;
            if (node.masteryState == 2) activeMastered++;
            else if (node.masteryState == 1) known++;
            if (node.enrichState != 1) pending++;
            node = node.next;
        }
        return calculateCounts(live, historicalMastered, activeMastered, known, pending);
    }

    static LocalProgressSnapshot calculateCounts(long liveEntries, long historicalMastered,
                                                 long activeMastered, long activeKnown,
                                                 long pendingEnrich) {
        long live = Math.max(0L, liveEntries);
        long history = Math.max(0L, historicalMastered);
        long activeMasteredCount = Math.min(live, Math.max(0L, activeMastered));
        long knownCount = Math.min(live - activeMasteredCount, Math.max(0L, activeKnown));
        long pendingCount = Math.min(live, Math.max(0L, pendingEnrich));
        return new LocalProgressSnapshot(live, history, activeMasteredCount,
                knownCount, pendingCount);
    }
}
