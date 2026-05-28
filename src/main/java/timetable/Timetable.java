package timetable;

import java.util.ArrayList;
import java.util.List;

public class Timetable {

    private String timetableName;
    private String semester;
    private boolean allowLectureOverlap;
    private List<String> selectedTopics;
    private List<String> selectedCampuses;
    private List<String> preferences;
    private List<ClassSchedule> scheduledClasses;

    public Timetable() {
        semester = "";
        allowLectureOverlap = false;
        selectedTopics = new ArrayList<>();
        selectedCampuses = new ArrayList<>();
        preferences = new ArrayList<>();
        scheduledClasses = new ArrayList<>();
    }

    public Timetable(String timetableName) {
        this();
        this.timetableName = timetableName;
    }

    public String getTimetableName()             { return timetableName; }
    public String getSemester()                  { return semester; }
    public boolean isAllowLectureOverlap()       { return allowLectureOverlap; }
    public List<String> getSelectedTopics()      { return selectedTopics; }
    public List<String> getSelectedCampuses()    { return selectedCampuses; }
    public List<String> getPreferences()         { return preferences; }
    public List<ClassSchedule> getScheduledClasses() { return scheduledClasses; }

    public void setTimetableName(String timetableName)         { this.timetableName = timetableName; }
    public void setSemester(String semester)                   { this.semester = semester; }
    public void setAllowLectureOverlap(boolean allow)          { this.allowLectureOverlap = allow; }
    public void setSelectedTopics(List<String> selectedTopics) { this.selectedTopics = selectedTopics; }
    public void setSelectedCampuses(List<String> selectedCampuses) { this.selectedCampuses = selectedCampuses; }
    public void setPreferences(List<String> preferences)       { this.preferences = preferences; }
    public void setScheduledClasses(List<ClassSchedule> scheduledClasses) { this.scheduledClasses = scheduledClasses; }

    @Override
    public String toString() {
        return "Timetable: " + timetableName + "\n" +
                "Semester: " + semester + "\n" +
                "Topics: " + String.join(", ", selectedTopics) + "\n" +
                "Campuses: " + String.join(", ", selectedCampuses) + "\n" +
                "Lecture overlap: " + (allowLectureOverlap ? "allowed" : "not allowed") + "\n" +
                "Preferences: " + String.join(", ", preferences) + "\n" +
                "Scheduled Classes: " + scheduledClasses.size();
    }
}

