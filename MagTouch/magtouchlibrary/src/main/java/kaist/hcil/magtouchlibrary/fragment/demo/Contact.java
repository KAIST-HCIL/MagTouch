package kaist.hcil.magtouchlibrary.fragment.demo;

public class Contact {
    public class DisplayType
    {
        public static final int NORMAL = 1;
        public static final int CALL = 2;
        public static final int MESSAGE = 3;
    }

    private String firstName;
    private String lastName;
    private int displayType;
    public Contact(String firstName, String lastName)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.displayType = DisplayType.NORMAL;
    }

    public String getFullName()
    {
        return firstName + " " + lastName;
    }

    public String getDisplayText()
    {
        if(displayType == DisplayType.CALL)
        {
            return "Call " + firstName;
        }
        else if(displayType == DisplayType.MESSAGE)
        {
            return "Message " + firstName;
        }
        return getFullName();
    }

    public int getDisplayType()
    {
        return displayType;
    }
    public void setDisplayType(int displayType)
    {
        this.displayType = displayType;
    }

}
