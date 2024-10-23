package exceptions;

public class LocationNotFoundException extends Exception {
    public LocationNotFoundException(String locationName) {
        super(STR."Location not found \{locationName}");
    }
}
