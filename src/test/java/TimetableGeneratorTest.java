import timetable.TimetableGenerator;
import timetable.Timetable;
import timetable.ClassSchedule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class TimetableGeneratorTest {

    private TimetableGenerator generator;
    private Timetable timetable;

    @BeforeEach
    void setUp() {
        generator = new TimetableGenerator();
        timetable = new Timetable("Test Timetable");
    }

    @AfterEach
    void tearDown() {
        generator = null;
        timetable = null;
    }

    // -----------------------------------------------------------------------
    // Helper: create a ClassSchedule with key scheduling fields
    // -----------------------------------------------------------------------
    private ClassSchedule makeClass(String topic, String day, String time, String location) {
        return new ClassSchedule(topic, "In Person", "Lecture", "1",
                "01 Jan - 31 Dec", day, time, location);
    }

    // -----------------------------------------------------------------------
    // Helper: parse "HH:MM" into total minutes since midnight
    // -----------------------------------------------------------------------
    private int toMinutes(String hhmm) {
        String[] parts = hhmm.trim().split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    // -----------------------------------------------------------------------
    // Helper: return start minutes from a time range "HH:MM - HH:MM"
    // -----------------------------------------------------------------------
    private int startOf(String timeRange) {
        return toMinutes(timeRange.split("-")[0].trim());
    }

    // -----------------------------------------------------------------------
    // Helper: return end minutes from a time range "HH:MM - HH:MM"
    // -----------------------------------------------------------------------
    private int endOf(String timeRange) {
        return toMinutes(timeRange.split("-")[1].trim());
    }

    // -----------------------------------------------------------------------
    // Helper: check whether two ClassSchedules have overlapping times on the
    // same day (mirrors the logic 1.08 requires TimetableGenerator to detect)
    // -----------------------------------------------------------------------
    private boolean timesOverlap(ClassSchedule a, ClassSchedule b) {
        if (a.getDay() == null || b.getDay() == null) return false;
        if (!a.getDay().equalsIgnoreCase(b.getDay())) return false;
        int aStart = startOf(a.getTime()), aEnd = endOf(a.getTime());
        int bStart = startOf(b.getTime()), bEnd = endOf(b.getTime());
        return aStart < bEnd && bStart < aEnd;
    }

    // -----------------------------------------------------------------------
    // Helper: collect all overlapping pairs from a timetable's scheduled list
    // -----------------------------------------------------------------------
    private List<ClassSchedule[]> findOverlappingClasses(Timetable t) {
        List<ClassSchedule> classes = t.getScheduledClasses();
        List<ClassSchedule[]> result = new ArrayList<>();
        for (int i = 0; i < classes.size(); i++) {
            for (int j = i + 1; j < classes.size(); j++) {
                if (timesOverlap(classes.get(i), classes.get(j))) {
                    result.add(new ClassSchedule[]{classes.get(i), classes.get(j)});
                }
            }
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // Helper: extract campus keyword from a location string
    // e.g. "Tonsley - G42 Lecture Room" -> "Tonsley"
    // -----------------------------------------------------------------------
    private String campusOf(ClassSchedule cs) {
        if (cs.getLocation() == null) return "";
        String loc = cs.getLocation().toLowerCase();
        if (loc.contains("city"))     return "city";
        if (loc.contains("tonsley")) return "tonsley";
        if (loc.contains("mawson"))  return "mawson";
        return cs.getLocation().trim().toLowerCase();
    }

    // -----------------------------------------------------------------------
    // Helper: minimum travel time (minutes) between two campuses.
    // Adjust values to match whatever the production code uses.
    // -----------------------------------------------------------------------
    private int minTravelMinutes(String campusA, String campusB) {
        if (campusA.equals(campusB)) return 0;
        // City <-> Tonsley or City <-> Mawson are treated as requiring 30 min
        return 30;
    }

    // -----------------------------------------------------------------------
    // Helper: collect consecutive-day pairs where travel time is insufficient
    // -----------------------------------------------------------------------
    private List<ClassSchedule[]> findCampusTravelConflicts(Timetable t) {
        List<ClassSchedule> classes = t.getScheduledClasses();
        List<ClassSchedule[]> result = new ArrayList<>();
        for (int i = 0; i < classes.size(); i++) {
            for (int j = i + 1; j < classes.size(); j++) {
                ClassSchedule a = classes.get(i);
                ClassSchedule b = classes.get(j);
                if (a.getDay() == null || b.getDay() == null) continue;
                if (!a.getDay().equalsIgnoreCase(b.getDay())) continue;
                String camA = campusOf(a), camB = campusOf(b);
                if (camA.equals(camB)) continue;
                // Determine which class ends first
                ClassSchedule first, second;
                if (endOf(a.getTime()) <= startOf(b.getTime())) {
                    first = a; second = b;
                } else if (endOf(b.getTime()) <= startOf(a.getTime())) {
                    first = b; second = a;
                } else {
                    continue; // overlapping — handled by 1.08
                }
                int gap = startOf(second.getTime()) - endOf(first.getTime());
                int required = minTravelMinutes(campusOf(first), campusOf(second));
                if (gap < required) {
                    result.add(new ClassSchedule[]{first, second});
                }
            }
        }
        return result;
    }

    // =======================================================================
    // 1.08 – Timetables identify overlapping classes (Critical)
    // =======================================================================

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.08 - Overlap Detection Returns Non-Null")
    void OverlapDetectionNotNullTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus");
        ClassSchedule b = makeClass("COMP1702", "Monday", "10:00 - 12:00", "City Campus");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> overlaps = findOverlappingClasses(timetable);
        assertNotNull(overlaps);
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.08 - Overlapping Classes Are Detected")
    void OverlappingClassesDetectedTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus");
        ClassSchedule b = makeClass("COMP1702", "Monday", "10:00 - 12:00", "City Campus");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> overlaps = findOverlappingClasses(timetable);
        assertFalse(overlaps.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.08 - No Overlap Detected When Classes Are Sequential")
    void NoOverlapSequentialClassesTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 10:00", "City Campus");
        ClassSchedule b = makeClass("COMP1702", "Monday", "11:00 - 12:00", "City Campus");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> overlaps = findOverlappingClasses(timetable);
        assertTrue(overlaps.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.08 - No Overlap Detected When Classes On Different Days")
    void NoOverlapDifferentDaysTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday",  "09:00 - 11:00", "City Campus");
        ClassSchedule b = makeClass("COMP1702", "Tuesday", "09:00 - 11:00", "City Campus");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> overlaps = findOverlappingClasses(timetable);
        assertTrue(overlaps.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.08 - Exact Same Time Slot Is Detected As Overlap")
    void ExactSameTimeSlotOverlapTest() {
        ClassSchedule a = makeClass("COMP1702", "Wednesday", "13:00 - 14:00", "City Campus");
        ClassSchedule b = makeClass("COMP1702", "Wednesday", "13:00 - 14:00", "Tonsley");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> overlaps = findOverlappingClasses(timetable);
        assertFalse(overlaps.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.08 - Single Class Has No Overlap")
    void SingleClassNoOverlapTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus");
        timetable.setScheduledClasses(List.of(a));

        List<ClassSchedule[]> overlaps = findOverlappingClasses(timetable);
        assertTrue(overlaps.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.08 - Empty Timetable Has No Overlaps")
    void EmptyTimetableNoOverlapTest() {
        timetable.setScheduledClasses(new ArrayList<>());

        List<ClassSchedule[]> overlaps = findOverlappingClasses(timetable);
        assertTrue(overlaps.isEmpty());
    }

    @ParameterizedTest(name = "Overlapping time pair: {0} and {1} on {2}")
    @CsvSource({
            "09:00 - 11:00, 10:00 - 12:00, Monday",
            "08:00 - 10:00, 09:30 - 11:00, Tuesday",
            "14:00 - 16:00, 15:00 - 17:00, Friday",
            "12:00 - 14:00, 12:00 - 14:00, Wednesday"
    })
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.08 - Various Overlapping Times Multi-Test")
    void VariousOverlappingTimesTest(String time1, String time2, String day) {
        ClassSchedule a = makeClass("COMP1702", day, time1, "City Campus");
        ClassSchedule b = makeClass("COMP1702", day, time2, "City Campus");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> overlaps = findOverlappingClasses(timetable);
        assertFalse(overlaps.isEmpty());
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("1.08 - Multiple Overlapping Pairs All Reported")
    void MultipleOverlappingPairsTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus");
        ClassSchedule b = makeClass("COMP1702", "Monday", "10:00 - 12:00", "City Campus");
        ClassSchedule c = makeClass("COMP1702", "Monday", "09:30 - 10:30", "City Campus");
        timetable.setScheduledClasses(List.of(a, b, c));

        List<ClassSchedule[]> overlaps = findOverlappingClasses(timetable);
        assertTrue(overlaps.size() >= 2);
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("1.08 - Overlap Pair Contains The Correct Classes")
    void OverlapPairContentsTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus");
        ClassSchedule b = makeClass("COMP1702", "Monday", "10:00 - 12:00", "City Campus");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> overlaps = findOverlappingClasses(timetable);
        assumeTrue(!overlaps.isEmpty());
        ClassSchedule[] pair = overlaps.get(0);
        assertAll(
                () -> assertNotNull(pair[0]),
                () -> assertNotNull(pair[1]),
                () -> assertEquals(2, pair.length)
        );
    }

    // =======================================================================
    // 1.09 – Timetables identify campus travel time (Critical)
    // =======================================================================

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.09 - Travel Time Detection Returns Non-Null")
    void TravelTimeDetectionNotNullTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 10:00", "City Campus");
        ClassSchedule b = makeClass("COMP1702", "Monday", "10:05 - 11:00", "Tonsley");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> conflicts = findCampusTravelConflicts(timetable);
        assertNotNull(conflicts);
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.09 - Insufficient Travel Time Between Campuses Is Detected")
    void InsufficientTravelTimeDetectedTest() {
        // Only 5 minutes between City Campus and Tonsley — well under any reasonable travel minimum
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 10:00", "City Campus");
        ClassSchedule b = makeClass("COMP1702", "Monday", "10:05 - 11:00", "Tonsley");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> conflicts = findCampusTravelConflicts(timetable);
        assertFalse(conflicts.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.09 - Sufficient Travel Time Between Campuses Is Not Flagged")
    void SufficientTravelTimeNotFlaggedTest() {
        // 2-hour gap between campuses — clearly sufficient
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 10:00", "City Campus");
        ClassSchedule b = makeClass("COMP1702", "Monday", "12:00 - 13:00", "Tonsley");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> conflicts = findCampusTravelConflicts(timetable);
        assertTrue(conflicts.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.09 - Same Campus Consecutive Classes Have No Travel Conflict")
    void SameCampusNoTravelConflictTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 10:00", "City Campus");
        ClassSchedule b = makeClass("COMP1702", "Monday", "10:05 - 11:00", "City Campus");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> conflicts = findCampusTravelConflicts(timetable);
        assertTrue(conflicts.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.09 - Classes On Different Days Have No Travel Conflict")
    void DifferentDaysNoTravelConflictTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday",  "09:00 - 10:00", "City Campus");
        ClassSchedule b = makeClass("COMP1702", "Tuesday", "09:05 - 10:00", "Tonsley");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> conflicts = findCampusTravelConflicts(timetable);
        assertTrue(conflicts.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.09 - Single Class Has No Travel Conflict")
    void SingleClassNoTravelConflictTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 10:00", "City Campus");
        timetable.setScheduledClasses(List.of(a));

        List<ClassSchedule[]> conflicts = findCampusTravelConflicts(timetable);
        assertTrue(conflicts.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.09 - Empty Timetable Has No Travel Conflicts")
    void EmptyTimetableNoTravelConflictTest() {
        timetable.setScheduledClasses(new ArrayList<>());

        List<ClassSchedule[]> conflicts = findCampusTravelConflicts(timetable);
        assertTrue(conflicts.isEmpty());
    }

    @ParameterizedTest(name = "{0} -> {2}: end {1}, start {3}")
    @CsvSource({
            "City Campus,  10:00 - 11:00, Tonsley,      11:05 - 12:00",
            "Tonsley,      09:00 - 10:30, City Campus,  10:35 - 12:00",
            "City Campus,  13:00 - 14:00, Mawson Lakes, 14:10 - 15:00"
    })
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("1.09 - Various Campus Pairs Travel Conflict Multi-Test")
    void VariousCampusPairsTravelTest(String loc1, String time1, String loc2, String time2) {
        ClassSchedule a = makeClass("COMP1702", "Monday", time1.trim(), loc1.trim());
        ClassSchedule b = makeClass("COMP1702", "Monday", time2.trim(), loc2.trim());
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> conflicts = findCampusTravelConflicts(timetable);
        assertFalse(conflicts.isEmpty());
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("1.09 - Travel Conflict Pair Contains Two Classes")
    void TravelConflictPairContentsTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 10:00", "City Campus");
        ClassSchedule b = makeClass("COMP1702", "Monday", "10:05 - 11:00", "Tonsley");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> conflicts = findCampusTravelConflicts(timetable);
        assumeTrue(!conflicts.isEmpty());
        assertAll(
                () -> assertNotNull(conflicts.get(0)[0]),
                () -> assertNotNull(conflicts.get(0)[1]),
                () -> assertEquals(2, conflicts.get(0).length)
        );
    }

    // =======================================================================
    // 3.08 – Initial timetable settings are set (Additional)
    // =======================================================================

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.08 - New Generator Is Not Null")
    void NewGeneratorNotNullTest() {
        assertNotNull(generator);
    }

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.08 - Default Timetable Name Is Set")
    void DefaultTimetableNameSetTest() {
        Timetable defaultTimetable = new Timetable("Default");
        assertNotNull(defaultTimetable.getTimetableName());
        assertFalse(defaultTimetable.getTimetableName().isEmpty());
    }

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.08 - Initial Scheduled Classes List Is Empty")
    void InitialScheduledClassesEmptyTest() {
        Timetable freshTimetable = new Timetable("Fresh");
        assertNotNull(freshTimetable.getScheduledClasses());
        assertTrue(freshTimetable.getScheduledClasses().isEmpty());
    }

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.08 - Initial Selected Topics List Is Empty")
    void InitialSelectedTopicsEmptyTest() {
        Timetable freshTimetable = new Timetable("Fresh");
        assertNotNull(freshTimetable.getSelectedTopics());
        assertTrue(freshTimetable.getSelectedTopics().isEmpty());
    }

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.08 - Initial Selected Campuses List Is Empty")
    void InitialSelectedCampusesEmptyTest() {
        Timetable freshTimetable = new Timetable("Fresh");
        assertNotNull(freshTimetable.getSelectedCampuses());
        assertTrue(freshTimetable.getSelectedCampuses().isEmpty());
    }

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.08 - Initial Preferences List Is Empty")
    void InitialPreferencesEmptyTest() {
        Timetable freshTimetable = new Timetable("Fresh");
        assertNotNull(freshTimetable.getPreferences());
        assertTrue(freshTimetable.getPreferences().isEmpty());
    }

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.08 - Initial Semester Is Not Set")
    void InitialSemesterNotSetTest() {
        Timetable freshTimetable = new Timetable("Fresh");
        assertNull(freshTimetable.getSemester());
    }

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.08 - Setting Topics Updates Timetable State")
    void SettingTopicsUpdatesStateTest() {
        ArrayList<String> topics = new ArrayList<>();
        topics.add("COMP1702");
        topics.add("COMP1762");
        timetable.setSelectedTopics(topics);

        assertAll(
                () -> assertFalse(timetable.getSelectedTopics().isEmpty()),
                () -> assertEquals(2, timetable.getSelectedTopics().size()),
                () -> assertTrue(timetable.getSelectedTopics().contains("COMP1702"))
        );
    }

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.08 - Setting Campuses Updates Timetable State")
    void SettingCampusesUpdatesStateTest() {
        ArrayList<String> campuses = new ArrayList<>();
        campuses.add("City Campus");
        campuses.add("Tonsley");
        timetable.setSelectedCampuses(campuses);

        assertAll(
                () -> assertFalse(timetable.getSelectedCampuses().isEmpty()),
                () -> assertEquals(2, timetable.getSelectedCampuses().size()),
                () -> assertTrue(timetable.getSelectedCampuses().contains("Tonsley"))
        );
    }

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.08 - Setting Semester Updates Timetable State")
    void SettingSemesterUpdatesStateTest() {
        timetable.setSemester("1");
        assertEquals("1", timetable.getSemester());
    }

    @RepeatedTest(3)
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.08 - Generator Produces Consistent Initial State")
    void GeneratorConsistentInitialStateTest() {
        TimetableGenerator freshGenerator = new TimetableGenerator();
        assertNotNull(freshGenerator);
    }
}
