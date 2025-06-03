import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class KeycloakAvailabilityChecker {

    private static final String KEYCLOAK_URL = "http://localhost:8080";
    private static final int WAIT_TIME = 5000; // 5 seconds
    private static final int MAX_ATTEMPTS = 60;

    public static void main(String[] args) {
        int attempt = 0;
        boolean isAvailable = false;

        System.out.println("Waiting for the Keycloak instance to become available...");

        while (attempt < MAX_ATTEMPTS) {
            try {
                URL url = new URI(KEYCLOAK_URL).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(WAIT_TIME);
                connection.setReadTimeout(WAIT_TIME);
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    System.out.println("Keycloak is available!");
                    isAvailable = true;
                    break;
                } else {
                    System.out.println("Keycloak is not yet available. Attempt " + (attempt + 1) + " of " + MAX_ATTEMPTS + "...");
                }
            } catch (Exception e) {
                System.out.println("Keycloak is not yet available. Attempt " + (attempt + 1) + " of " + MAX_ATTEMPTS + "...");
            }

            attempt++;
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (!isAvailable) {
            System.out.println("Keycloak is still not available after " + MAX_ATTEMPTS + " attempts. Exiting program.");
            System.exit(1);
        }

        // Further actions after Keycloak is available
        System.out.println("Performing further actions...");
    }
}