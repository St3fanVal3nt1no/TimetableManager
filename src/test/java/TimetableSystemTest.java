import timetable.TimetableSystem;
import timetable.Timetable;
import timetable.ClassSchedule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    private ClassSchedule makeClass(String topic, String availability, String day, String location) {
        return new ClassSchedule(topic, availability, "Lecture", "1",
                "01 Jan", day, "09:00 - 10:00", location);
    }


    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("Default Constructor Test")
    void DefaultContstructorTest() {
        assertNotNull(system);
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("2.05 - Add Timetable Test")
    void AddTimetableTest() {
        system.addTimetable(new Timetable("Plan A"));
        assertEquals(1, system.getTimetables().size());}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("2.05 - Timetable Name Getter Test")
    void TimetableGetterTest() {
        Timetable t = new Timetable("My Schedule");
        system.addTimetable(t);
        Timetable found = system.getTimetableByName("My Schedule");
        assertAll(() -> assertNotNull(found),() -> assertEquals("My Schedule", found.getTimetableName()));}

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("2.05- Timetable Invalid Name Test")
    void GetterInvalidNameTest() {
        system.addTimetable(new Timetable("Plan A"));
        assertNull(system.getTimetableByName("Plan B"));}

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.05 - Timetable Getter Case Sensitivity Test")
    void GetterCaseSensitivityTest() {
        system.addTimetable(new Timetable("Engineering Plan"));
        assertAll(
                () -> assertNotNull(system.getTimetableByName("engineering plan")),
                () -> assertNotNull(system.getTimetableByName("ENGINEERING PLAN")),
                () -> assertNotNull(system.getTimetableByName("Engineering Plan")));}

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.07 - Timetable Get Null Name Test")
    void GetterNullNameTest() {
        system.addTimetable(new Timetable("Plan A"));
        Timetable result = system.getTimetableByName(null);
        assertNull(result);}

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("Timetable Getter Empty String Test")
    void EmptyStringTest() {
        system.addTimetable(new Timetable("Plan A"));
        assertNull(system.getTimetableByName(""));}

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.07 - Class Getter Empty List Test")
    void NoClassesGetterTest() {
        assertDoesNotThrow(() -> {
            List<ClassSchedule> result = system.getClassesByTopic(null);
            assertTrue(result.isEmpty());});}

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.07 - Class Getter Null Test")
    void GetNullClassTest() {
        system.addClass(makeClass("COMP1702", "A", "Monday", "Loc"));
        assertThrows(NullPointerException.class, () -> system.getClassesByTopic(null));}

    @ParameterizedTest(name = "Timetable named ''{0}'' is retrievable by name")
    @ValueSource(strings = {"Alpha", "Beta Plan", "S2 Engineering", "123", "My Timetable"})
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.05 - Named Timetable Retrieval Multi-Test")
    void MultiNameGetterTest(String name) {
        system.addTimetable(new Timetable(name));
        assumeTrue(system.getTimetables().size() == 1);
        assertNotNull(system.getTimetableByName(name));}
}