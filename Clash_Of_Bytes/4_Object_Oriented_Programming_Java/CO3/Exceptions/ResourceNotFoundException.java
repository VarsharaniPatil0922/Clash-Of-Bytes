package CO3.Exceptions;

/**
 * Course Outcome: CO3 | Practical 03: Exception Handling
 * 
 * Thrown when an external file, executable (e.g. clash_of_bytes.exe), or
 * configuration resource is missing or inaccessible.
 */
public class ResourceNotFoundException extends ClashOfBytesException {

    private static final long serialVersionUID = 1L;
    private final String resourcePath;

    public ResourceNotFoundException(String resourcePath, String resourceDescription) {
        super("ERR_RESOURCE_NOT_FOUND",
              String.format("Required game resource '%s' (%s) was not found.", 
                            resourcePath, resourceDescription),
              "Path=" + resourcePath);
        this.resourcePath = resourcePath;
    }

    public ResourceNotFoundException(String message) {
        super("ERR_RESOURCE_NOT_FOUND", message);
        this.resourcePath = "UNKNOWN";
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
