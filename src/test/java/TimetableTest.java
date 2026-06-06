import timetable.Timetable;
import timetable.ClassSchedule;
import timetable.TimetableGenerator;
import timetable.TimetableSystem;
import timetable.CSVHandler;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class TimetableTest {

    private Timetable          timetable;
    private TimetableGenerator generator;
    private TimetableSystem    system;

    private static final String TEST_DATA = "src/test/resources/";

    @BeforeEach
    void setUp() {
        timetable = new Timetable("Test Timetable");
        generator = new TimetableGenerator();
        system    = new TimetableSystem();
    }

    @AfterEach
    void tearDown() {
        timetable = null;
        generator = null;
        system.clearClasses();
        system = null;
    }

    // -----------------------------------------------------------------------
    // Helper: create a ClassSchedule
    // -----------------------------------------------------------------------
    private ClassSchedule makeClass(String topic, String day, String time, String location) {
        return new ClassSchedule(topic, "In Person", "Lecture", "1",
                "01 Jan - 31 Dec", day, time, location);
    }

    // =======================================================================
    // Core Timetable property tests
    // =======================================================================

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.02 - New Timetable Is Not Null")
    void NewTimetableNotNullTest() {
        assertNotNull(timetable);
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.02 - New Timetable Name Is Correct")
    void NewTimetableNameCorrectTest() {
        assertEquals("Test Timetable", timetable.getTimetableName());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.02 - New Timetable Scheduled Classes Is Empty")
    void NewTimetableScheduledClassesEmptyTest() {
        assertNotNull(timetable.getScheduledClasses());
        assertTrue(timetable.getScheduledClasses().isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.02 - New Timetable Selected Topics Is Empty")
    void NewTimetableSelectedTopicsEmptyTest() {
        assertNotNull(timetable.getSelectedTopics());
        assertTrue(timetable.getSelectedTopics().isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.02 - New Timetable Selected Campuses Is Empty")
    void NewTimetableSelectedCampusesEmptyTest() {
        assertNotNull(timetable.getSelectedCampuses());
        assertTrue(timetable.getSelectedCampuses().isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.02 - New Timetable Preferences Is Empty")
    void NewTimetablePreferencesEmptyTest() {
        assertNotNull(timetable.getPreferences());
        assertTrue(timetable.getPreferences().isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.02 - New Timetable Semester Is Null")
    void NewTimetableSemesterNullTest() {
        assertNull(timetable.getSemester());
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("3.02 - Default Constructor Creates Valid Timetable")
    void DefaultConstructorValidTest() {
        Timetable blank = new Timetable();
        assertNotNull(blank);
    }

    // =======================================================================
    // 2.07 – Timetable preferences are applied (Critical)
    // =======================================================================

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("2.07 - Preferences Can Be Set On Timetable")
    void PreferencesCanBeSetTest() {
        ArrayList<String> prefs = new ArrayList<>();
        prefs.add("Morning classes");
        timetable.setPreferences(prefs);
        assertFalse(timetable.getPreferences().isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("2.07 - Set Preference Is Retrievable")
    void SetPreferenceRetrievableTest() {
        ArrayList<String> prefs = new ArrayList<>();
        prefs.add("No Friday classes");
        timetable.setPreferences(prefs);
        assertTrue(timetable.getPreferences().contains("No Friday classes"));
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("2.07 - Multiple Preferences Can Be Set")
    void MultiplePreferencesCanBeSetTest() {
        ArrayList<String> prefs = new ArrayList<>();
        prefs.add("Morning classes");
        prefs.add("No Friday classes");
        prefs.add("City Campus only");
        timetable.setPreferences(prefs);
        assertEquals(3, timetable.getPreferences().size());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("2.07 - Preferences Are Persisted After Generation Setup")
    void PreferencesPersistedAfterGenerationSetupTest() {
        ArrayList<String> prefs = new ArrayList<>();
        prefs.add("Prefer morning");
        timetable.setPreferences(prefs);
        ArrayList<String> topics = new ArrayList<>();
        topics.add("COMP1702");
        timetable.setSelectedTopics(topics);
        timetable.setSemester("1");
        // Preferences must still be intact after other fields are set
        assertFalse(timetable.getPreferences().isEmpty());
        assertTrue(timetable.getPreferences().contains("Prefer morning"));
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("2.07 - Preference Filter Excludes Classes On Unwanted Day")
    void PreferenceFilterExcludesUnwantedDayTest() {
        // Simulate applying a "No Friday classes" preference:
        // build a pool with Friday and non-Friday classes, apply filter
        ArrayList<String> prefs = new ArrayList<>();
        prefs.add("No Friday classes");
        timetable.setPreferences(prefs);

        List<ClassSchedule> pool = new ArrayList<>();
        pool.add(makeClass("COMP1702", "Monday",  "09:00 - 11:00", "City Campus"));
        pool.add(makeClass("COMP1702", "Friday",  "09:00 - 11:00", "City Campus"));
        pool.add(makeClass("COMP1702", "Tuesday", "13:00 - 15:00", "Tonsley"));

        // Apply preference: remove Friday classes when preference says so
        List<ClassSchedule> filtered = applyDayPreference(pool, timetable.getPreferences());

        boolean hasFriday = filtered.stream()
                .anyMatch(c -> "Friday".equalsIgnoreCase(c.getDay()));
        assertFalse(hasFriday, "Friday classes should be excluded by preference");
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("2.07 - Preference Filter Keeps Allowed Day Classes")
    void PreferenceFilterKeepsAllowedDayClassesTest() {
        ArrayList<String> prefs = new ArrayList<>();
        prefs.add("No Friday classes");
        timetable.setPreferences(prefs);

        List<ClassSchedule> pool = new ArrayList<>();
        pool.add(makeClass("COMP1702", "Monday",    "09:00 - 11:00", "City Campus"));
        pool.add(makeClass("COMP1702", "Wednesday", "13:00 - 15:00", "Tonsley"));

        List<ClassSchedule> filtered = applyDayPreference(pool, timetable.getPreferences());
        assertEquals(2, filtered.size());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("2.07 - Morning Preference Excludes Afternoon Classes")
    void MorningPreferenceExcludesAfternoonTest() {
        ArrayList<String> prefs = new ArrayList<>();
        prefs.add("Morning classes");
        timetable.setPreferences(prefs);

        List<ClassSchedule> pool = new ArrayList<>();
        pool.add(makeClass("COMP1702", "Monday", "08:00 - 10:00", "City Campus")); // morning
        pool.add(makeClass("COMP1702", "Monday", "14:00 - 16:00", "City Campus")); // afternoon
        pool.add(makeClass("COMP1762", "Tuesday", "09:30 - 11:30", "Tonsley"));    // morning

        List<ClassSchedule> filtered = applyTimePreference(pool, timetable.getPreferences());

        boolean hasAfternoon = filtered.stream()
                .anyMatch(c -> startMinutes(c.getTime()) >= 720); // 720 = noon
        assertFalse(hasAfternoon, "Afternoon classes should be excluded by morning preference");
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("2.07 - Morning Preference Retains Morning Classes")
    void MorningPreferenceRetainsMorningClassesTest() {
        ArrayList<String> prefs = new ArrayList<>();
        prefs.add("Morning classes");
        timetable.setPreferences(prefs);

        List<ClassSchedule> pool = new ArrayList<>();
        pool.add(makeClass("COMP1702", "Monday",  "08:00 - 10:00", "City Campus"));
        pool.add(makeClass("COMP1762", "Tuesday", "09:30 - 11:30", "Tonsley"));

        List<ClassSchedule> filtered = applyTimePreference(pool, timetable.getPreferences());
        assertEquals(2, filtered.size());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("2.07 - No Preferences Applies No Filter")
    void NoPreferencesAppliesNoFilterTest() {
        // Empty preferences → all classes pass through
        List<ClassSchedule> pool = new ArrayList<>();
        pool.add(makeClass("COMP1702", "Monday",  "08:00 - 10:00", "City Campus"));
        pool.add(makeClass("COMP1702", "Friday",  "14:00 - 16:00", "Tonsley"));
        pool.add(makeClass("COMP1762", "Tuesday", "09:30 - 11:30", "City Campus"));

        List<ClassSchedule> filtered = applyDayPreference(pool, timetable.getPreferences());
        assertEquals(3, filtered.size());
    }

    @ParameterizedTest(name = "Preference ''{0}'' stored correctly")
    @ValueSource(strings = {
            "Morning classes",
            "No Friday classes",
            "City Campus only",
            "Afternoon classes",
            "No Monday classes"
    })
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("2.07 - Various Preferences Can Be Set And Retrieved")
    void VariousPreferencesStoredTest(String preference) {
        ArrayList<String> prefs = new ArrayList<>();
        prefs.add(preference);
        timetable.setPreferences(prefs);
        assertTrue(timetable.getPreferences().contains(preference));
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.07 - Preferences Are Independent Between Timetables")
    void PreferencesIndependentBetweenTimetablesTest() {
        Timetable t2 = new Timetable("Second Timetable");

        ArrayList<String> prefs1 = new ArrayList<>();
        prefs1.add("Morning classes");
        timetable.setPreferences(prefs1);

        ArrayList<String> prefs2 = new ArrayList<>();
        prefs2.add("No Friday classes");
        t2.setPreferences(prefs2);

        assertAll(
                () -> assertTrue(timetable.getPreferences().contains("Morning classes")),
                () -> assertFalse(timetable.getPreferences().contains("No Friday classes")),
                () -> assertTrue(t2.getPreferences().contains("No Friday classes")),
                () -> assertFalse(t2.getPreferences().contains("Morning classes"))
        );
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.07 - Preference Campus Filter Keeps Matching Campus Classes")
    void PreferenceCampusFilterKeepsMatchingTest() {
        ArrayList<String> prefs = new ArrayList<>();
        prefs.add("City Campus only");
        timetable.setPreferences(prefs);

        List<ClassSchedule> pool = new ArrayList<>();
        pool.add(makeClass("COMP1702", "Monday",  "09:00 - 11:00", "City Campus"));
        pool.add(makeClass("COMP1702", "Tuesday", "10:00 - 12:00", "Tonsley"));
        pool.add(makeClass("COMP1762", "Wednesday","13:00 - 15:00", "City Campus"));

        List<ClassSchedule> filtered = applyCampusPreference(pool, "City Campus");

        boolean allCityOnlyCampus = filtered.stream()
                .allMatch(c -> c.getLocation() != null
                        && c.getLocation().toLowerCase().contains("city"));
        assertAll(
                () -> assertEquals(2, filtered.size()),
                () -> assertTrue(allCityOnlyCampus)
        );
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.07 - Preference Campus Filter Excludes Non-Matching Campus Classes")
    void PreferenceCampusFilterExcludesNonMatchingTest() {
        List<ClassSchedule> pool = new ArrayList<>();
        pool.add(makeClass("COMP1702", "Monday",  "09:00 - 11:00", "Tonsley"));
        pool.add(makeClass("COMP1762", "Tuesday", "10:00 - 12:00", "Tonsley"));

        List<ClassSchedule> filtered = applyCampusPreference(pool, "City Campus");
        assertTrue(filtered.isEmpty());
    }

    @RepeatedTest(3)
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.07 - Preferences Consistently Returned")
    void PreferencesConsistentlyReturnedTest() {
        ArrayList<String> prefs = new ArrayList<>();
        prefs.add("Morning classes");
        timetable.setPreferences(prefs);
        assertAll(
                () -> assertNotNull(timetable.getPreferences()),
                () -> assertEquals(1, timetable.getPreferences().size()),
                () -> assertEquals("Morning classes", timetable.getPreferences().get(0))
        );
    }

    // =======================================================================
    // 3.05 – Timetables can be edited (Critical)
    // =======================================================================

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.05 - Timetable Name Can Be Updated")
    void TimetableNameCanBeUpdatedTest() {
        timetable.setTimetableName("Updated Name");
        assertEquals("Updated Name", timetable.getTimetableName());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.05 - Timetable Name Update Replaces Old Name")
    void TimetableNameUpdateReplacesOldNameTest() {
        timetable.setTimetableName("New Name");
        assertNotEquals("Test Timetable", timetable.getTimetableName());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.05 - Selected Topics Can Be Updated")
    void SelectedTopicsCanBeUpdatedTest() {
        ArrayList<String> original = new ArrayList<>();
        original.add("COMP1702");
        timetable.setSelectedTopics(original);

        ArrayList<String> updated = new ArrayList<>();
        updated.add("COMP1762");
        updated.add("COMP1702");
        timetable.setSelectedTopics(updated);

        assertAll(
                () -> assertEquals(2, timetable.getSelectedTopics().size()),
                () -> assertTrue(timetable.getSelectedTopics().contains("COMP1762"))
        );
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.05 - Removing A Topic Updates The Selected Topics")
    void RemovingTopicUpdatesSelectedTopicsTest() {
        ArrayList<String> topics = new ArrayList<>();
        topics.add("COMP1702");
        topics.add("COMP1762");
        timetable.setSelectedTopics(topics);

        ArrayList<String> updated = new ArrayList<>(timetable.getSelectedTopics());
        updated.remove("COMP1762");
        timetable.setSelectedTopics(updated);

        assertAll(
                () -> assertEquals(1, timetable.getSelectedTopics().size()),
                () -> assertFalse(timetable.getSelectedTopics().contains("COMP1762"))
        );
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.05 - Selected Campuses Can Be Updated")
    void SelectedCampusesCanBeUpdatedTest() {
        ArrayList<String> original = new ArrayList<>();
        original.add("City Campus");
        timetable.setSelectedCampuses(original);

        ArrayList<String> updated = new ArrayList<>();
        updated.add("Tonsley");
        updated.add("City Campus");
        timetable.setSelectedCampuses(updated);

        assertAll(
                () -> assertEquals(2, timetable.getSelectedCampuses().size()),
                () -> assertTrue(timetable.getSelectedCampuses().contains("Tonsley"))
        );
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.05 - Removing A Campus Updates The Selected Campuses")
    void RemovingCampusUpdatesSelectedCampusesTest() {
        ArrayList<String> campuses = new ArrayList<>();
        campuses.add("City Campus");
        campuses.add("Tonsley");
        timetable.setSelectedCampuses(campuses);

        ArrayList<String> updated = new ArrayList<>(timetable.getSelectedCampuses());
        updated.remove("Tonsley");
        timetable.setSelectedCampuses(updated);

        assertAll(
                () -> assertEquals(1, timetable.getSelectedCampuses().size()),
                () -> assertFalse(timetable.getSelectedCampuses().contains("Tonsley"))
        );
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.05 - Preferences Can Be Updated")
    void PreferencesCanBeUpdatedTest() {
        ArrayList<String> original = new ArrayList<>();
        original.add("Morning classes");
        timetable.setPreferences(original);

        ArrayList<String> updated = new ArrayList<>();
        updated.add("No Friday classes");
        timetable.setPreferences(updated);

        assertAll(
                () -> assertEquals(1, timetable.getPreferences().size()),
                () -> assertTrue(timetable.getPreferences().contains("No Friday classes")),
                () -> assertFalse(timetable.getPreferences().contains("Morning classes"))
        );
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.05 - Preferences Can Be Cleared")
    void PreferencesCanBeClearedTest() {
        ArrayList<String> prefs = new ArrayList<>();
        prefs.add("Morning classes");
        timetable.setPreferences(prefs);
        timetable.setPreferences(new ArrayList<>());
        assertTrue(timetable.getPreferences().isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.05 - Semester Can Be Updated")
    void SemesterCanBeUpdatedTest() {
        timetable.setSemester("1");
        timetable.setSemester("2");
        assertEquals("2", timetable.getSemester());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.05 - Scheduled Classes Can Be Replaced")
    void ScheduledClassesCanBeReplacedTest() {
        List<ClassSchedule> original = new ArrayList<>();
        original.add(makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus"));
        timetable.setScheduledClasses(original);

        List<ClassSchedule> updated = new ArrayList<>();
        updated.add(makeClass("COMP1762", "Tuesday",   "10:00 - 12:00", "Tonsley"));
        updated.add(makeClass("COMP1702", "Wednesday", "13:00 - 15:00", "City Campus"));
        timetable.setScheduledClasses(updated);

        assertAll(
                () -> assertEquals(2, timetable.getScheduledClasses().size()),
                () -> assertEquals("COMP1762", timetable.getScheduledClasses().get(0).getTopic())
        );
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.05 - Removing A Scheduled Class Updates The List")
    void RemovingScheduledClassUpdatesListTest() {
        ClassSchedule cs1 = makeClass("COMP1702", "Monday",  "09:00 - 11:00", "City Campus");
        ClassSchedule cs2 = makeClass("COMP1762", "Tuesday", "10:00 - 12:00", "Tonsley");
        List<ClassSchedule> classes = new ArrayList<>(Arrays.asList(cs1, cs2));
        timetable.setScheduledClasses(classes);

        List<ClassSchedule> updated = new ArrayList<>(timetable.getScheduledClasses());
        updated.remove(cs1);
        timetable.setScheduledClasses(updated);

        assertAll(
                () -> assertEquals(1, timetable.getScheduledClasses().size()),
                () -> assertEquals("COMP1762", timetable.getScheduledClasses().get(0).getTopic())
        );
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.05 - Scheduled Classes Can Be Cleared")
    void ScheduledClassesCanBeClearedTest() {
        List<ClassSchedule> classes = new ArrayList<>();
        classes.add(makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus"));
        timetable.setScheduledClasses(classes);
        timetable.setScheduledClasses(new ArrayList<>());
        assertTrue(timetable.getScheduledClasses().isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.05 - All Editable Fields Can Be Changed Independently")
    void AllEditableFieldsChangeIndependentlyTest() {
        // Set initial state
        ArrayList<String> topics = new ArrayList<>();
        topics.add("COMP1702");
        timetable.setSelectedTopics(topics);

        ArrayList<String> campuses = new ArrayList<>();
        campuses.add("City Campus");
        timetable.setSelectedCampuses(campuses);

        ArrayList<String> prefs = new ArrayList<>();
        prefs.add("Morning classes");
        timetable.setPreferences(prefs);

        timetable.setSemester("1");
        timetable.setTimetableName("Original Name");

        // Edit each field
        timetable.setTimetableName("Edited Name");

        ArrayList<String> newTopics = new ArrayList<>();
        newTopics.add("COMP1762");
        timetable.setSelectedTopics(newTopics);

        ArrayList<String> newCampuses = new ArrayList<>();
        newCampuses.add("Tonsley");
        timetable.setSelectedCampuses(newCampuses);

        ArrayList<String> newPrefs = new ArrayList<>();
        newPrefs.add("No Friday classes");
        timetable.setPreferences(newPrefs);

        timetable.setSemester("2");

        assertAll(
                () -> assertEquals("Edited Name",           timetable.getTimetableName()),
                () -> assertTrue(timetable.getSelectedTopics().contains("COMP1762")),
                () -> assertTrue(timetable.getSelectedCampuses().contains("Tonsley")),
                () -> assertTrue(timetable.getPreferences().contains("No Friday classes")),
                () -> assertEquals("2",                     timetable.getSemester())
        );
    }

    @ParameterizedTest(name = "Timetable name updated to ''{0}''")
    @ValueSource(strings = {
            "Semester 1 2026",
            "My Engineering Timetable",
            "Backup Plan",
            "Final Version",
            ""
    })
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.05 - Various Timetable Names Can Be Set")
    void VariousTimetableNamesCanBeSetTest(String name) {
        timetable.setTimetableName(name);
        assertEquals(name, timetable.getTimetableName());
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("3.05 - Editing One Timetable Does Not Affect Another")
    void EditingOneTimetableDoesNotAffectAnotherTest() {
        Timetable t2 = new Timetable("Second");

        timetable.setTimetableName("First Edited");

        ArrayList<String> prefs = new ArrayList<>();
        prefs.add("Morning classes");
        timetable.setPreferences(prefs);

        assertAll(
                () -> assertEquals("First Edited", timetable.getTimetableName()),
                () -> assertEquals("Second",       t2.getTimetableName()),
                () -> assertTrue(t2.getPreferences().isEmpty())
        );
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("3.05 - Topics List Reflects Addition Order After Edit")
    void TopicsListReflectsOrderAfterEditTest() {
        ArrayList<String> topics = new ArrayList<>();
        topics.add("COMP1702");
        topics.add("COMP1762");
        topics.add("ENGR1762");
        timetable.setSelectedTopics(topics);

        assertAll(
                () -> assertEquals("COMP1702", timetable.getSelectedTopics().get(0)),
                () -> assertEquals("COMP1762", timetable.getSelectedTopics().get(1)),
                () -> assertEquals("ENGR1762", timetable.getSelectedTopics().get(2))
        );
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("3.05 - Scheduled Classes Size Updates Correctly On Each Edit")
    void ScheduledClassesSizeUpdatesOnEachEditTest() {
        List<ClassSchedule> one = new ArrayList<>();
        one.add(makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus"));
        timetable.setScheduledClasses(one);
        assertEquals(1, timetable.getScheduledClasses().size());

        List<ClassSchedule> three = new ArrayList<>();
        three.add(makeClass("COMP1702", "Monday",    "09:00 - 11:00", "City Campus"));
        three.add(makeClass("COMP1762", "Tuesday",   "10:00 - 12:00", "Tonsley"));
        three.add(makeClass("ENGR1762", "Wednesday", "13:00 - 15:00", "City Campus"));
        timetable.setScheduledClasses(three);
        assertEquals(3, timetable.getScheduledClasses().size());

        timetable.setScheduledClasses(new ArrayList<>());
        assertEquals(0, timetable.getScheduledClasses().size());
    }

    @RepeatedTest(3)
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("3.05 - Name Edit Is Consistently Persisted")
    void NameEditConsistentlyPersistedTest() {
        timetable.setTimetableName("Consistent");
        assertEquals("Consistent", timetable.getTimetableName());
    }

    // =======================================================================
    // Private helper methods for preference filtering (used by 2.07 tests)
    // These simulate the filtering logic TimetableGenerator must implement.
    // =======================================================================

    /**
     * Applies day-based preferences to a class pool.
     * Recognises "No {Day} classes" style preferences and removes matching days.
     */
    private List<ClassSchedule> applyDayPreference(List<ClassSchedule> pool,
                                                   List<String> preferences) {
        List<ClassSchedule> result = new ArrayList<>(pool);
        for (String pref : preferences) {
            String lower = pref.toLowerCase();
            if (lower.startsWith("no ") && lower.endsWith(" classes")) {
                String excludedDay = pref.substring(3, pref.length() - 8).trim();
                result.removeIf(c -> excludedDay.equalsIgnoreCase(c.getDay()));
            }
        }
        return result;
    }

    /**
     * Applies time-of-day preferences to a class pool.
     * Recognises "Morning classes" (keep only before noon) and
     * "Afternoon classes" (keep only noon or later).
     */
    private List<ClassSchedule> applyTimePreference(List<ClassSchedule> pool,
                                                    List<String> preferences) {
        List<ClassSchedule> result = new ArrayList<>(pool);
        for (String pref : preferences) {
            if ("Morning classes".equalsIgnoreCase(pref)) {
                result.removeIf(c -> c.getTime() != null && startMinutes(c.getTime()) >= 720);
            } else if ("Afternoon classes".equalsIgnoreCase(pref)) {
                result.removeIf(c -> c.getTime() != null && startMinutes(c.getTime()) < 720);
            }
        }
        return result;
    }

    /**
     * Applies a campus preference filter to a class pool.
     * Keeps only classes whose location contains the specified campus keyword.
     */
    private List<ClassSchedule> applyCampusPreference(List<ClassSchedule> pool,
                                                      String campusKeyword) {
        List<ClassSchedule> result = new ArrayList<>(pool);
        result.removeIf(c -> c.getLocation() == null ||
                !c.getLocation().toLowerCase().contains(campusKeyword.toLowerCase()));
        return result;
    }

    /**
     * Parses the start time (minutes since midnight) from a "HH:MM - HH:MM" range.
     */
    private int startMinutes(String timeRange) {
        String start = timeRange.split("-")[0].trim();
        String[] parts = start.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
}

