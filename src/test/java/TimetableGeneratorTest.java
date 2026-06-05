import timetable.TimetableGenerator;
import timetable.Timetable;
import timetable.ClassSchedule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
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

    // =======================================================================
    // 1.08 – Timetables identify overlapping classes (Critical)
    // =======================================================================

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.08 - Overlap Detection Returns Non-Null")
    void OverlapDetectionNotNullTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus");
        ClassSchedule b = makeClass("ENGR1762", "Monday", "10:00 - 12:00", "City Campus");
        List<ClassSchedule> classes = List.of(a, b);
        timetable.setScheduledClasses(classes);

        List<ClassSchedule[]> overlaps = generator.findOverlappingClasses(timetable);
        assertNotNull(overlaps);
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.08 - Overlapping Classes Are Detected")
    void OverlappingClassesDetectedTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus");
        ClassSchedule b = makeClass("ENGR1762", "Monday", "10:00 - 12:00", "City Campus");
        List<ClassSchedule> classes = List.of(a, b);
        timetable.setScheduledClasses(classes);

        List<ClassSchedule[]> overlaps = generator.findOverlappingClasses(timetable);
        assertFalse(overlaps.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.08 - No Overlap Detected When Classes Are Sequential")
    void NoOverlapSequentialClassesTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 10:00", "City Campus");
        ClassSchedule b = makeClass("ENGR1762", "Monday", "11:00 - 12:00", "City Campus");
        List<ClassSchedule> classes = List.of(a, b);
        timetable.setScheduledClasses(classes);

        List<ClassSchedule[]> overlaps = generator.findOverlappingClasses(timetable);
        assertTrue(overlaps.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.08 - No Overlap Detected When Classes On Different Days")
    void NoOverlapDifferentDaysTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday",  "09:00 - 11:00", "City Campus");
        ClassSchedule b = makeClass("ENGR1762", "Tuesday", "09:00 - 11:00", "City Campus");
        List<ClassSchedule> classes = List.of(a, b);
        timetable.setScheduledClasses(classes);

        List<ClassSchedule[]> overlaps = generator.findOverlappingClasses(timetable);
        assertTrue(overlaps.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.08 - Exact Same Time Slot Is Detected As Overlap")
    void ExactSameTimeSlotOverlapTest() {
        ClassSchedule a = makeClass("COMP1702", "Wednesday", "13:00 - 14:00", "City Campus");
        ClassSchedule b = makeClass("ENGR1762", "Wednesday", "13:00 - 14:00", "Tonsley");
        List<ClassSchedule> classes = List.of(a, b);
        timetable.setScheduledClasses(classes);

        List<ClassSchedule[]> overlaps = generator.findOverlappingClasses(timetable);
        assertFalse(overlaps.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.08 - Single Class Has No Overlap")
    void SingleClassNoOverlapTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus");
        timetable.setScheduledClasses(List.of(a));

        List<ClassSchedule[]> overlaps = generator.findOverlappingClasses(timetable);
        assertTrue(overlaps.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.08 - Empty Timetable Has No Overlaps")
    void EmptyTimetableNoOverlapTest() {
        timetable.setScheduledClasses(new ArrayList<>());

        List<ClassSchedule[]> overlaps = generator.findOverlappingClasses(timetable);
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
        ClassSchedule b = makeClass("ENGR1762", day, time2, "City Campus");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> overlaps = generator.findOverlappingClasses(timetable);
        assertFalse(overlaps.isEmpty());
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("1.08 - Multiple Overlapping Pairs All Reported")
    void MultipleOverlappingPairsTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus");
        ClassSchedule b = makeClass("ENGR1762", "Monday", "10:00 - 12:00", "City Campus");
        ClassSchedule c = makeClass("COMP1002", "Monday", "09:30 - 10:30", "City Campus");
        timetable.setScheduledClasses(List.of(a, b, c));

        List<ClassSchedule[]> overlaps = generator.findOverlappingClasses(timetable);
        assertTrue(overlaps.size() >= 2);
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
        ClassSchedule b = makeClass("ENGR1762", "Monday", "10:05 - 11:00", "Tonsley");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> conflicts = generator.findCampusTravelConflicts(timetable);
        assertNotNull(conflicts);
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.09 - Insufficient Travel Time Between Campuses Is Detected")
    void InsufficientTravelTimeDetectedTest() {
        // Classes on different campuses with only 5 minutes gap — insufficient travel time
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 10:00", "City Campus");
        ClassSchedule b = makeClass("ENGR1762", "Monday", "10:05 - 11:00", "Tonsley");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> conflicts = generator.findCampusTravelConflicts(timetable);
        assertFalse(conflicts.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.09 - Sufficient Travel Time Between Campuses Is Not Flagged")
    void SufficientTravelTimeNotFlaggedTest() {
        // Classes on different campuses with ample gap
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 10:00", "City Campus");
        ClassSchedule b = makeClass("ENGR1762", "Monday", "12:00 - 13:00", "Tonsley");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> conflicts = generator.findCampusTravelConflicts(timetable);
        assertTrue(conflicts.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.09 - Same Campus Consecutive Classes Have No Travel Conflict")
    void SameCampusNoTravelConflictTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 10:00", "City Campus");
        ClassSchedule b = makeClass("ENGR1762", "Monday", "10:05 - 11:00", "City Campus");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> conflicts = generator.findCampusTravelConflicts(timetable);
        assertTrue(conflicts.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.09 - Classes On Different Days Have No Travel Conflict")
    void DifferentDaysNoTravelConflictTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday",  "09:00 - 10:00", "City Campus");
        ClassSchedule b = makeClass("ENGR1762", "Tuesday", "09:05 - 10:00", "Tonsley");
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> conflicts = generator.findCampusTravelConflicts(timetable);
        assertTrue(conflicts.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.09 - Single Class Has No Travel Conflict")
    void SingleClassNoTravelConflictTest() {
        ClassSchedule a = makeClass("COMP1702", "Monday", "09:00 - 10:00", "City Campus");
        timetable.setScheduledClasses(List.of(a));

        List<ClassSchedule[]> conflicts = generator.findCampusTravelConflicts(timetable);
        assertTrue(conflicts.isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("1.09 - Empty Timetable Has No Travel Conflicts")
    void EmptyTimetableNoTravelConflictTest() {
        timetable.setScheduledClasses(new ArrayList<>());

        List<ClassSchedule[]> conflicts = generator.findCampusTravelConflicts(timetable);
        assertTrue(conflicts.isEmpty());
    }

    @ParameterizedTest(name = "Travel conflict: {0} ending {1} then {2} starting {3}")
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
        ClassSchedule b = makeClass("ENGR1762", "Monday", time2.trim(), loc2.trim());
        timetable.setScheduledClasses(List.of(a, b));

        List<ClassSchedule[]> conflicts = generator.findCampusTravelConflicts(timetable);
        assertFalse(conflicts.isEmpty());
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
        topics.add("ENGR1762");
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
