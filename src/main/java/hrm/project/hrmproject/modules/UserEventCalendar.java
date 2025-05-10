package hrm.project.hrmproject.modules;

public class UserEventCalendar {
    private User user; // Foreign key reference to User
    private Event event; // Foreign key reference to Event

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
