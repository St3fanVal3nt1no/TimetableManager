import timetable.TimetableSystem;
import timetable.Timetable;
import timetable.ClassSchedule;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class TimetableSystemTest {

    private TimetableSystem system;

    @BeforeEach
    void setUp() {
        system = new TimetableSystem();
    }

    @AfterEach
    void tearDown() {
        system = null;
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------
    private ClassSchedule makeClass(String topic, String day, String time, String location) {
        return new ClassSchedule(topic, "In Person", "Lecture", "1",
                "01 Jan - 31 Dec", day, time, location);
    }

    // =======================================================================
    // 3.02 – Timetables can be created (Critical)
    // =======================================================================

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.02 - Timetable Can Be Added To System")
    void TimetableAddedToSystemTest() {
        Timetable t = new Timetable("My Timetable");
        system.addTimetable(t);
        assertFalse(system.getTimetables().isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.02 - Added Timetable Is Retrievable")
    void AddedTimetableRetrievableTest() {
        Timetable t = new Timetable("My Timetable");
        system.addTimetable(t);
        assertEquals("My Timetable", system.getTimetables().get(0).getTimetableName());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.02 - Multiple Timetables Can Be Added")
    void MultipleTimetablesAddedTest() {
        system.addTimetable(new Timetable("T1"));
        system.addTimetable(new Timetable("T2"));
        system.addTimetable(new Timetable("T3"));
        assertEquals(3, system.getTimetables().size());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.02 - Initial Timetable List Is Empty")
    void InitialTimetableListEmptyTest() {
        assertTrue(system.getTimetables().isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.02 - Initial Class List Is Empty")
    void InitialClassListEmptyTest() {
        assertTrue(system.getClasses().isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.02 - Timetable Count Increments After Add")
    void TimetableCountIncrementsTest() {
        int before = system.getTimetables().size();
        system.addTimetable(new Timetable("New"));
        assertEquals(before + 1, system.getTimetables().size());
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("3.02 - Timetable List Is Not Null After Init")
    void TimetableListNotNullTest() {
        assertNotNull(system.getTimetables());
    }

    // =======================================================================
    // 3.03 – Timetables can be deleted (Critical)
    // =======================================================================

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.03 - Timetable Can Be Removed From System")
    void TimetableRemovedFromSystemTest() {
        Timetable t = new Timetable("ToDelete");
        system.addTimetable(t);
        system.removeTimetable(t);
        assertTrue(system.getTimetables().isEmpty());
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.03 - Correct Timetable Is Removed")
    void CorrectTimetableRemovedTest() {
        Timetable t1 = new Timetable("T1");
        Timetable t2 = new Timetable("T2");
        system.addTimetable(t1);
        system.addTimetable(t2);
        system.removeTimetable(t1);
        assertAll(
                () -> assertEquals(1, system.getTimetables().size()),
                () -> assertEquals("T2", system.getTimetables().get(0).getTimetableName())
        );
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("3.03 - Timetable Count Decrements After Remove")
    void TimetableCountDecrementsTest() {
        Timetable t = new Timetable("T");
        system.addTimetable(t);
        int before = system.getTimetables().size();
        system.removeTimetable(t);
        assertEquals(before - 1, system.getTimetables().size());
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("3.03 - Removing Non-Existent Timetable Does Not Throw")
    void RemoveNonExistentNoThrowTest() {
        Timetable t = new Timetable("Ghost");
        assertDoesNotThrow(() -> system.removeTimetable(t));
    }

    // =======================================================================
    // 3.04 – Classes can be removed from the system (Core)
    // =======================================================================

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("3.04 - Class Can Be Added To System")
    void ClassAddedToSystemTest() {
        ClassSchedule cs = makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus");
        system.addClass(cs);
        assertFalse(system.getClasses().isEmpty());
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("3.04 - Class Can Be Removed From System")
    void ClassRemovedFromSystemTest() {
        ClassSchedule cs = makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus");
        system.addClass(cs);
        system.clearClasses();
        assertTrue(system.getClasses().isEmpty());
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("3.04 - Class Count Increments After Add")
    void ClassCountIncrementsTest() {
        int before = system.getClasses().size();
        system.addClass(makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus"));
        assertEquals(before + 1, system.getClasses().size());
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("3.04 - Multiple Classes Can Be Added")
    void MultipleClassesAddedTest() {
        system.addClass(makeClass("COMP1702", "Monday",  "09:00 - 11:00", "City Campus"));
        system.addClass(makeClass("COMP1762", "Tuesday", "10:00 - 12:00", "Tonsley"));
        assertEquals(2, system.getClasses().size());
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("3.04 - Class List Is Not Null")
    void ClassListNotNullTest() {
        assertNotNull(system.getClasses());
    }

    // =======================================================================
    // 2.01 – Topics are shown (Core)
    // =======================================================================

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.01 - Topics Extracted From Imported Classes")
    void TopicsExtractedTest() {
        system.addClass(makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus"));
        system.addClass(makeClass("COMP1762", "Tuesday", "10:00 - 12:00", "Tonsley"));
        List<String> topics = system.getAvailableTopics();
        assertAll(
                () -> assertNotNull(topics),
                () -> assertFalse(topics.isEmpty())
        );
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.01 - Duplicate Topics Not Listed Twice")
    void DuplicateTopicsNotListedTest() {
        system.addClass(makeClass("COMP1702", "Monday",    "09:00 - 11:00", "City Campus"));
        system.addClass(makeClass("COMP1702", "Wednesday", "13:00 - 15:00", "Tonsley"));
        List<String> topics = system.getAvailableTopics();
        long count = topics.stream().filter("COMP1702"::equals).count();
        assertEquals(1, count);
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.01 - No Topics When No Classes Imported")
    void NoTopicsWhenNoClassesTest() {
        List<String> topics = system.getAvailableTopics();
        assertTrue(topics.isEmpty());
    }

    // =======================================================================
    // 2.02 – Campuses are shown (Core)
    // =======================================================================

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.02 - Campuses Extracted From Imported Classes")
    void CampusesExtractedTest() {
        system.addClass(makeClass("COMP1702", "Monday", "09:00 - 11:00", "City Campus"));
        system.addClass(makeClass("COMP1762", "Tuesday", "10:00 - 12:00", "Tonsley"));
        List<String> campuses = system.getAvailableCampuses();
        assertAll(
                () -> assertNotNull(campuses),
                () -> assertFalse(campuses.isEmpty())
        );
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.02 - Duplicate Campuses Not Listed Twice")
    void DuplicateCampusesNotListedTest() {
        system.addClass(makeClass("COMP1702", "Monday",    "09:00 - 11:00", "City Campus"));
        system.addClass(makeClass("COMP1762", "Wednesday", "13:00 - 15:00", "City Campus"));
        List<String> campuses = system.getAvailableCampuses();
        long count = campuses.stream().filter("City Campus"::equals).count();
        assertEquals(1, count);
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.02 - No Campuses When No Classes Imported")
    void NoCampusesWhenNoClassesTest() {
        List<String> campuses = system.getAvailableCampuses();
        assertTrue(campuses.isEmpty());
    }

    // =======================================================================
    // 2.03 – Topics can be selected (Core)
    // =======================================================================

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.03 - Selected Topics Stored On Timetable")
    void SelectedTopicsStoredTest() {
        Timetable t = new Timetable("T");
        ArrayList<String> topics = new ArrayList<>();
        topics.add("COMP1702");
        t.setSelectedTopics(topics);
        assertTrue(t.getSelectedTopics().contains("COMP1702"));
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.03 - Multiple Topics Can Be Selected")
    void MultipleTopicsSelectedTest() {
        Timetable t = new Timetable("T");
        ArrayList<String> topics = new ArrayList<>();
        topics.add("COMP1702");
        topics.add("COMP1762");
        t.setSelectedTopics(topics);
        assertEquals(2, t.getSelectedTopics().size());
    }

    // =======================================================================
    // 2.04 – Campuses can be selected (Core)
    // =======================================================================

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.04 - Selected Campuses Stored On Timetable")
    void SelectedCampusesStoredTest() {
        Timetable t = new Timetable("T");
        ArrayList<String> campuses = new ArrayList<>();
        campuses.add("City Campus");
        t.setSelectedCampuses(campuses);
        assertTrue(t.getSelectedCampuses().contains("City Campus"));
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.04 - Multiple Campuses Can Be Selected")
    void MultipleCampusesSelectedTest() {
        Timetable t = new Timetable("T");
        ArrayList<String> campuses = new ArrayList<>();
        campuses.add("City Campus");
        campuses.add("Tonsley");
        t.setSelectedCampuses(campuses);
        assertEquals(2, t.getSelectedCampuses().size());
    }
}
