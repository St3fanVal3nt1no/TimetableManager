package timetable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableGenerator {

    private static final int TRAVEL_MINUTES = 30;

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------

    // Returns filtered + preference-applied candidates without conflict resolution.
    public List<ClassSchedule> getCandidates(Timetable timetable, TimetableSystem system) {
        List<ClassSchedule> candidates = filterCandidates(timetable, system);
        return applyPreferences(candidates, timetable.getPreferences());
    }

    // Returns true if candidate conflicts with any already-scheduled class.
    public boolean conflictsWithScheduled(ClassSchedule candidate,
                                          List<ClassSchedule> scheduled,
                                          boolean allowLectureOverlap) {
        return conflictsWithAny(candidate, scheduled, allowLectureOverlap);
    }

    // Scans all pairs in a timetable and returns a warning string for each conflict found.
    public List<String> detectConflicts(List<ClassSchedule> classes, boolean allowLectureOverlap) {
        List<String> warnings = new ArrayList<>();
        for (int i = 0; i < classes.size(); i++) {
            for (int j = i + 1; j < classes.size(); j++) {
                String w = conflictWarning(classes.get(i), classes.get(j), allowLectureOverlap);
                if (w != null) warnings.add(w);
            }
        }
        return warnings;
    }

    private String conflictWarning(ClassSchedule a, ClassSchedule b, boolean allowLectureOverlap) {
        if (!a.getDay().equalsIgnoreCase(b.getDay())) return null;

        int[] rangeA = parseTimeRange(a.getTime());
        int[] rangeB = parseTimeRange(b.getTime());
        if (rangeA == null || rangeB == null) return null;

        boolean overlaps = rangeA[0] < rangeB[1] && rangeB[0] < rangeA[1];
        if (overlaps) {
            boolean bothLectures = a.getClassName().equalsIgnoreCase("Lecture")
                    && b.getClassName().equalsIgnoreCase("Lecture");
            if (allowLectureOverlap && bothLectures) return null;
            return "TIME CLASH   " + classLabel(a) + "  vs  " + classLabel(b)
                    + "  [" + a.getDay() + ": " + a.getTime() + " / " + b.getTime() + "]";
        }

        String campusA = extractCampus(a.getLocation());
        String campusB = extractCampus(b.getLocation());
        if (!campusA.isEmpty() && !campusB.isEmpty()
                && !campusA.equalsIgnoreCase(campusB)) {
            int gap = gapBetween(rangeA, rangeB);
            if (gap < TRAVEL_MINUTES) {
                return "TRAVEL       Only " + gap + " min between " + campusA + " and " + campusB
                        + "  [" + a.getDay() + ": " + a.getTime() + " / " + b.getTime() + "]";
            }
        }
        return null;
    }

    private String classLabel(ClassSchedule cs) {
        return cs.getTopic() + " " + cs.getClassName();
    }

    // -----------------------------------------------------------------------
    // Step 1 — filter by topic, campus, and semester
    //
    // Campus mixing rule (spec):
    //   City campus classes CANNOT be mixed with Bedford Park/Tonsley classes
    //   for the SAME topic. However, a student may attend City for one topic
    //   and Bedford Park/Tonsley for another.
    // -----------------------------------------------------------------------

    private List<ClassSchedule> filterCandidates(Timetable timetable, TimetableSystem system) {
        List<ClassSchedule> result = new ArrayList<>();
        List<String> selectedCampuses = timetable.getSelectedCampuses();

        for (ClassSchedule cs : system.getClasses()) {
            if (!matchesTopic(cs, timetable.getSelectedTopics())) continue;
            if (!matchesCampus(cs, selectedCampuses)) continue;
            if (!matchesSemester(cs, timetable.getSemester())) continue;

            // Campus mixing rule: for each topic, if City is selected alongside
            // Bedford Park/Tonsley, enforce separation per topic.
            if (!passesCampusMixingRule(cs, timetable.getSelectedTopics(), selectedCampuses, system)) continue;

            result.add(cs);
        }
        return result;
    }

    /**
     * Enforces the spec rule: City campus classes cannot be mixed with
     * Bedford Park/Tonsley classes for the SAME topic.
     * <p>
     * If the selected campuses include both City and at least one of
     * Bedford Park/Tonsley, we must pick one "side" per topic.
     * We resolve this by keeping only the campus group that has the most
     * available class slots for each topic (City vs BP/Tonsley).
     * If only one side is available for that topic, use it.
     */
    private boolean passesCampusMixingRule(ClassSchedule cs,
                                           List<String> selectedTopics,
                                           List<String> selectedCampuses,
                                           TimetableSystem system) {
        // Only applies when both City and BP/Tonsley campuses are selected
        boolean hasCity = false;
        boolean hasBpOrTonsley = false;
        for (String c : selectedCampuses) {
            if (c.equalsIgnoreCase("all")) return true; // no restriction in all-campus mode
            String cl = c.toLowerCase();
            if (cl.contains("city") || cl.contains("festival")) hasCity = true;
            if (cl.contains("bedford") || cl.contains("tonsley")) hasBpOrTonsley = true;
        }
        if (!hasCity || !hasBpOrTonsley) return true; // only one side selected, no conflict

        // Mixed campuses selected — check which campus group this class belongs to
        String classCampus = extractCampus(cs.getLocation()).toLowerCase();
        boolean isCity = classCampus.contains("city") || classCampus.contains("festival");
        boolean isBpOrTonsley = classCampus.contains("bedford") || classCampus.contains("tonsley");

        if (!isCity && !isBpOrTonsley) return true; // unknown campus, allow

        // For this class's topic, count how many classes exist on each side
        String topic = cs.getTopic();
        int cityCount = 0, bpCount = 0;
        for (ClassSchedule other : system.getClasses()) {
            if (!other.getTopic().equalsIgnoreCase(topic)) continue;
            String oc = extractCampus(other.getLocation()).toLowerCase();
            if (oc.contains("city") || oc.contains("festival")) cityCount++;
            else if (oc.contains("bedford") || oc.contains("tonsley")) bpCount++;
        }

        // If only one side exists for this topic, allow it
        if (cityCount == 0) return isBpOrTonsley;
        if (bpCount == 0) return isCity;

        // Both sides exist — keep only the side the user's campus preferences lean toward
        // Prefer the side that appears first in selectedCampuses
        for (String pref : selectedCampuses) {
            String pl = pref.toLowerCase();
            if (pl.contains("city") || pl.contains("festival")) return isCity;
            if (pl.contains("bedford") || pl.contains("tonsley")) return isBpOrTonsley;
        }
        return true;
    }

    private boolean matchesTopic(ClassSchedule cs, List<String> topics) {
        if (topics.isEmpty()) return true;
        for (String topic : topics) {
            if (cs.getTopic().equalsIgnoreCase(topic)) return true;
        }
        return false;
    }

    private boolean matchesCampus(ClassSchedule cs, List<String> campuses) {
        if (campuses.isEmpty()) return true;
        String classCampus = extractCampus(cs.getLocation());
        for (String campus : campuses) {
            if (campus.equalsIgnoreCase("all")) return true;
            if (classCampus.equalsIgnoreCase(campus)) return true;
        }
        return false;
    }

    private boolean matchesSemester(ClassSchedule cs, String semester) {
        if (semester == null || semester.isEmpty() || semester.equalsIgnoreCase("Both")) return true;
        return cs.getAvailability().toUpperCase().contains(semester.toUpperCase());
    }

    // -----------------------------------------------------------------------
    // Step 2 — apply preferences (in priority order, highest first)
    //
    // Spec-mandated preferences:
    //   Bedford Park              — prefer classes at Bedford Park campus
    //   Tonsley                   — prefer classes at Tonsley campus
    //   Flinders City Campus      — prefer classes at City campus
    //   All at same campus        — prefer a schedule where all classes share one campus
    //   Mornings                  — prefer classes starting before 12:00
    //   Afternoons                — prefer classes starting 12:00–17:00
    //   Monday … Friday           — prefer classes on that specific weekday
    //   Evenly spread             — prefer classes spread across as many days as possible
    //   Compact                   — prefer classes on as few days as possible
    //
    // Preferences are ordered by the user (index 0 = highest priority).
    // Each preference acts as a soft filter: if applying it would leave no
    // candidates, the filter is skipped (graceful degradation).
    // -----------------------------------------------------------------------

    private List<ClassSchedule> applyPreferences(List<ClassSchedule> candidates,
                                                 List<String> preferences) {
        List<ClassSchedule> result = new ArrayList<>(candidates);
        for (String pref : preferences) {
            String lower = pref.toLowerCase().trim();
            List<ClassSchedule> filtered = applyOnePref(result, lower);
            // Graceful degradation: keep the filtered list only if non-empty
            if (!filtered.isEmpty()) result = filtered;
        }
        return result;
    }

    private List<ClassSchedule> applyOnePref(List<ClassSchedule> classes, String lower) {
        // --- Campus preferences ---
        if (lower.equals("bedford park") || lower.equals("bedford")) {
            return filterByCampus(classes, "bedford");
        }
        if (lower.equals("tonsley")) {
            return filterByCampus(classes, "tonsley");
        }
        if (lower.equals("flinders city campus") || lower.equals("city")) {
            return filterByCampus(classes, "festival tower"); // City campus building prefix
        }
        if (lower.equals("all at same campus") || lower.equals("same campus")) {
            return filterAllSameCampus(classes);
        }

        // --- Time-of-day preferences ---
        if (lower.equals("mornings") || lower.equals("morning")) {
            return filterByStartMinutes(classes, 0, 720);      // before 12:00
        }
        if (lower.equals("afternoons") || lower.equals("afternoon")) {
            return filterByStartMinutes(classes, 720, 1020);   // 12:00–17:00
        }

        // --- Day preferences (exact day name, e.g. "mondays", "monday") ---
        String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday"};
        for (String day : days) {
            if (lower.equals(day) || lower.equals(day + "s")) {
                return filterPreferDay(classes, capitalize(day));
            }
        }

        // --- Distribution preferences ---
        if (lower.equals("evenly spread") || lower.equals("evenly spread classes across days")
                || lower.startsWith("evenly spread")) {
            return sortByEvenSpread(classes);
        }
        if (lower.equals("compact") || lower.equals("compact classes to as few days as possible")
                || lower.startsWith("compact")) {
            return sortByCompact(classes);
        }

        // --- Legacy format support: "avoid day:<Day>" / "prefer day:<Day>" ---
        if (lower.startsWith("avoid day:")) {
            return filterAvoidDay(classes, lower.substring("avoid day:".length()).trim());
        }
        if (lower.startsWith("prefer day:")) {
            return filterPreferDay(classes, lower.substring("prefer day:".length()).trim());
        }

        return classes; // unrecognised preference — no change
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    // Keeps only classes whose campus (extracted from location) contains the given keyword.
    private List<ClassSchedule> filterByCampus(List<ClassSchedule> classes, String campusKeyword) {
        List<ClassSchedule> preferred = new ArrayList<>();
        String kl = campusKeyword.toLowerCase();
        for (ClassSchedule cs : classes) {
            if (extractCampus(cs.getLocation()).toLowerCase().contains(kl)) {
                preferred.add(cs);
            }
        }
        return preferred;
    }

    // Keeps classes from the campus that appears most frequently in the candidate list.
    private List<ClassSchedule> filterAllSameCampus(List<ClassSchedule> classes) {
        Map<String, List<ClassSchedule>> byCampus = new HashMap<>();
        for (ClassSchedule cs : classes) {
            String campus = extractCampus(cs.getLocation());
            byCampus.computeIfAbsent(campus, k -> new ArrayList<>()).add(cs);
        }
        List<ClassSchedule> best = classes;
        for (List<ClassSchedule> group : byCampus.values()) {
            if (group.size() > best.size()) best = group;
        }
        return best;
    }

    private List<ClassSchedule> filterAvoidDay(List<ClassSchedule> classes, String avoidDay) {
        List<ClassSchedule> result = new ArrayList<>();
        for (ClassSchedule cs : classes) {
            if (!cs.getDay().equalsIgnoreCase(avoidDay)) result.add(cs);
        }
        return result;
    }

    private List<ClassSchedule> filterPreferDay(List<ClassSchedule> classes, String preferDay) {
        List<ClassSchedule> preferred = new ArrayList<>();
        for (ClassSchedule cs : classes) {
            if (cs.getDay().equalsIgnoreCase(preferDay)) preferred.add(cs);
        }
        return preferred.isEmpty() ? classes : preferred;
    }

    private List<ClassSchedule> filterByStartMinutes(List<ClassSchedule> classes,
                                                     int fromMinute, int toMinute) {
        List<ClassSchedule> filtered = new ArrayList<>();
        for (ClassSchedule cs : classes) {
            int[] range = parseTimeRange(cs.getTime());
            if (range != null && range[0] >= fromMinute && range[0] < toMinute) {
                filtered.add(cs);
            }
        }
        return filtered.isEmpty() ? classes : filtered;
    }

    /**
     * Sorts candidates so those on different days appear earlier (evenly spread).
     * Uses a greedy approach: among remaining candidates, pick the one on the
     * day least-represented in the already-selected set.
     */
    private List<ClassSchedule> sortByEvenSpread(List<ClassSchedule> classes) {
        List<ClassSchedule> remaining = new ArrayList<>(classes);
        List<ClassSchedule> sorted = new ArrayList<>();
        Map<String, Integer> dayCounts = new HashMap<>();

        while (!remaining.isEmpty()) {
            ClassSchedule best = null;
            int bestCount = Integer.MAX_VALUE;
            for (ClassSchedule cs : remaining) {
                int count = dayCounts.getOrDefault(cs.getDay(), 0);
                if (count < bestCount) {
                    bestCount = count;
                    best = cs;
                }
            }
            sorted.add(best);
            remaining.remove(best);
            dayCounts.merge(best.getDay(), 1, Integer::sum);
        }
        return sorted;
    }

    /**
     * Sorts candidates so those on already-used days appear earlier (compact).
     * The first candidate is kept as-is; subsequent ones prefer days already seen.
     */
    private List<ClassSchedule> sortByCompact(List<ClassSchedule> classes) {
        List<ClassSchedule> remaining = new ArrayList<>(classes);
        List<ClassSchedule> sorted = new ArrayList<>();
        Map<String, Integer> dayCounts = new HashMap<>();

        while (!remaining.isEmpty()) {
            ClassSchedule best = null;
            int bestCount = Integer.MIN_VALUE;
            for (ClassSchedule cs : remaining) {
                int count = dayCounts.getOrDefault(cs.getDay(), 0);
                if (count > bestCount) {
                    bestCount = count;
                    best = cs;
                }
            }
            sorted.add(best);
            remaining.remove(best);
            dayCounts.merge(best.getDay(), 1, Integer::sum);
        }
        return sorted;
    }

    // -----------------------------------------------------------------------
    // Step 3 — greedy conflict resolution
    // -----------------------------------------------------------------------

    private List<ClassSchedule> resolveConflicts(List<ClassSchedule> candidates,
                                                 boolean allowLectureOverlap) {
        List<ClassSchedule> scheduled = new ArrayList<>();
        for (ClassSchedule candidate : candidates) {
            if (!conflictsWithAny(candidate, scheduled, allowLectureOverlap)) {
                scheduled.add(candidate);
            }
        }
        return scheduled;
    }

    private boolean conflictsWithAny(ClassSchedule candidate,
                                     List<ClassSchedule> scheduled,
                                     boolean allowLectureOverlap) {
        for (ClassSchedule existing : scheduled) {
            if (conflicts(candidate, existing, allowLectureOverlap)) return true;
        }
        return false;
    }

    private boolean conflicts(ClassSchedule a, ClassSchedule b, boolean allowLectureOverlap) {
        if (!a.getDay().equalsIgnoreCase(b.getDay())) return false;

        int[] rangeA = parseTimeRange(a.getTime());
        int[] rangeB = parseTimeRange(b.getTime());

        if (rangeA == null || rangeB == null) return true;

        boolean overlaps = rangeA[0] < rangeB[1] && rangeB[0] < rangeA[1];

        if (overlaps) {
            boolean bothLectures = a.getClassName().equalsIgnoreCase("Lecture")
                    && b.getClassName().equalsIgnoreCase("Lecture");
            return !(allowLectureOverlap && bothLectures);
        }

        String campusA = extractCampus(a.getLocation());
        String campusB = extractCampus(b.getLocation());
        if (!campusA.isEmpty() && !campusB.isEmpty()
                && !campusA.equalsIgnoreCase(campusB)) {
            return gapBetween(rangeA, rangeB) < TRAVEL_MINUTES;
        }

        return false;
    }

    private int gapBetween(int[] rangeA, int[] rangeB) {
        if (rangeA[1] <= rangeB[0]) return rangeB[0] - rangeA[1];
        return rangeA[0] - rangeB[1];
    }

    // -----------------------------------------------------------------------
    // Field parsers
    // -----------------------------------------------------------------------

    private String extractCampus(String location) {
        if (location == null || location.isEmpty()) return "";
        int comma = location.indexOf(',');
        return comma >= 0 ? location.substring(0, comma).trim() : location.trim();
    }

    private String extractSemester(String availability) {
        String[] parts = availability.split(",");
        return parts.length >= 3 ? parts[2].trim() : "";
    }

    int[] parseTimeRange(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        String normalised = timeStr.replace('–', '-').replace('—', '-');
        String[] parts = normalised.split("-");
        if (parts.length < 2) return null;
        int start = parseTime(parts[0].trim());
        int end = parseTime(parts[parts.length - 1].trim());
        if (start < 0 || end < 0) return null;
        return new int[]{start, end};
    }

    int parseTime(String timeStr) {
        if (timeStr == null) return -1;
        String[] parts = timeStr.trim().split(":");
        if (parts.length < 2) return -1;
        try {
            return Integer.parseInt(parts[0].trim()) * 60
                    + Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
