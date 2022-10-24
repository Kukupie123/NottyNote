package util.urls;

public class URLs {
    private URLs() {
    }

    public static String DB_HOST(int port) {
        return String.format("http://localhost:%s/", port);
    }

    public static String DIR_CREATE = "api/v1/db/dir/create";

}