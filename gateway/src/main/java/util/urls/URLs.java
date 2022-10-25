package util.urls;

public class URLs {
    private URLs() {
    }

    public static String DB_HOST(int port) {
        return "http://localhost:8000/api/v1/db/";
    }

    public static String DIR_CREATE = "dir/create";

    public static String DIR_DELETE(String id) {
        return "dir/" + id;
    }


}