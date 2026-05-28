package timetable;

import java.util.ArrayList;
import java.util.List;

public class TimetableSystem {

    private List<ClassSchedule> classes;
    private List<Timetable> timetables;

    public TimetableSystem() {
        classes = new ArrayList<>();
        timetables = new ArrayList<>();
    }

    // --- Class management ---

    public List<ClassSchedule> getClasses() { return classes; }

    public void setClasses(List<ClassSchedule> classes) { this.classes = classes; }

    public void addClass(ClassSchedule cs) { classes.add(cs); }

    public void clearClasses() { classes.clear(); }

    public List<ClassSchedule> getClassesByTopic(String topic) {
        List<ClassSchedule> result = new ArrayList<>();
        for (ClassSchedule cs : classes) {
            if (cs.getTopic().toLowerCase().contains(topic.toLowerCase())) {
                result.add(cs);
            }
        }
        return result;
    }

    public List<ClassSchedule> getClassesByCampus(String campus) {
        List<ClassSchedule> result = new ArrayList<>();
        for (ClassSchedule cs : classes) {
            if (cs.getAvailability().toLowerCase().contains(campus.toLowerCase())) {
                result.add(cs);
            }
        }
        return result;
    }

    public List<String> getAvailableTopics() {
        List<String> topics = new ArrayList<>();
        for (ClassSchedule cs : classes) {
            if (!topics.contains(cs.getTopic())) {
                topics.add(cs.getTopic());
            }
        }
        return topics;
    }

    public List<String> getAvailableCampuses() {
        List<String> campuses = new ArrayList<>();
        for (ClassSchedule cs : classes) {
            String campus = extractCampusFromLocation(cs.getLocation());
            if (!campus.isEmpty() && !campuses.contains(campus)) {
                campuses.add(campus);
            }
        }
        return campuses;
    }

    private String extractCampusFromLocation(String location) {
        if (location == null || location.isEmpty()) return "";
        int comma = location.indexOf(',');
        return comma >= 0 ? location.substring(0, comma).trim() : location.trim();
    }

    public List<String> getAvailableSemesters() {
        List<String> semesters = new ArrayList<>();
        for (ClassSchedule cs : classes) {
            String[] parts = cs.getAvailability().split(",");
            if (parts.length >= 3) {
                String semester = parts[2].trim();
                if (!semesters.contains(semester)) {
                    semesters.add(semester);
                }
            }
        }
        return semesters;
    }

    // --- Timetable management ---

    public List<Timetable> getTimetables() { return timetables; }

    public void addTimetable(Timetable timetable) { timetables.add(timetable); }

    public void removeTimetable(Timetable timetable) { timetables.remove(timetable); }

    public Timetable getTimetableByName(String name) {
        for (Timetable t : timetables) {
            if (t.getTimetableName().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "TimetableSystem{classes=" + classes.size() + ", timetables=" + timetables.size() + "}";
    }
}

