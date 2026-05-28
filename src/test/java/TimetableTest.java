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
public class TimetableTest {
    private Timetable timetable;

    @BeforeEach
    void setUp() {
        timetable = new Timetable();
    }

    @AfterEach
    void tearDown() {
        timetable = null;
    }

    @Test
    @Tag("Critical")
    @Tag("Bright")
    @DisplayName("Default Constructor Test")
    void DefaultContsturctorTest() {
        assertNotNull(timetable);}


    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.05 -Named Timetable Creation Test")
    void CreateNamedTimetableTest() {
        assertNotNull(new Timetable("My Schedule"));
    }

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.05 - Timetable Name Setter Test")
    void TimetableSetterTest() {
        Timetable named = new Timetable("Semester 2 Plan");
        assertEquals("Semester 2 Plan", named.getTimetableName());}

    // -----------------------------------------------------------------------
    // setTimetableName / getTimetableName – "Timetables can be named"
    // -----------------------------------------------------------------------

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.05 - Timetable Name Update Test")
    void SetterNameUpdateTest() {
        timetable.setTimetableName("Engineering Timetable");
        assertEquals("Engineering Timetable", timetable.getTimetableName());}

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.05-Multiple Name Updates Test")
    void TableMultiNameTest() {
        timetable.setTimetableName("Schedule 1.0");
        timetable.setTimetableName("Schedule 2.0");
        timetable.setTimetableName("Schedule FINAL");
        assertEquals("Schedule FINAL", timetable.getTimetableName());}

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.05 - Timetable Blank Name Test")
    void TimetableBlankNameTest() {
        assertDoesNotThrow(() -> timetable.setTimetableName(""));
        assertEquals("", timetable.getTimetableName());}

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.07 - Null Name No Error Test")
    void NullNameTest() {
        assertDoesNotThrow(() -> timetable.setTimetableName(null));
    }

    @Test
    @Tag("Additional")
    @Tag("Bright")
    @DisplayName("3.07 - Long Timetable Name Test")
    void LongNameTest() {
        String longName = "A".repeat(500);
        timetable.setTimetableName(longName);
        assertEquals(500, timetable.getTimetableName().length());}

    @Test
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.05 - Timetable Name toString Test")
    void toStringTest() {
        timetable.setTimetableName("CS Plan");
        assertTrue(timetable.toString().contains("CS Plan"));}

    @ParameterizedTest(name = "Name ''{0}'' is stored correctly")
    @ValueSource(strings = {"My Plan", "S1 2025", "Comp Sci Schedule", "A", "123 456"})
    @Tag("Core")
    @Tag("Bright")
    @DisplayName("2.05 - Unusual Name Multi-Test")
    void UnusualNameTest(String name) {
        timetable.setTimetableName(name);
        assertEquals(name, timetable.getTimetableName());}
}
