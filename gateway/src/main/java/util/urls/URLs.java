package util.urls;

public class URLs {
    private URLs() {
    }

    public static String DB_HOST(int port) {
        return "http://localhost:8000/api/v1/db/";
    }

    public static String DIR_CREATE = "dir/create";

    public static final String TEMP_CREATE = "template/create";
    public static final String BKM_CREATE = "bookmark/create";

    public static String BKMS_FROM_DIR(String dirID) {
        return "bookmark/dir/" + dirID;
    }

    public static String DIR_GET_DELETE(String id) {
        return "dir/" + id;
    }

    public static String DIR_DELETE_GET(String id) {
        return "dir/" + id;
    }

    public static String DIR_GET_CHILDREN(String parentID) {
        return "dir/getChildren/" + parentID;
    }

    public static String BKM_DELETE_GET(String id) {
        return "bookmark/" + id;
    }


    public static String TEMP_DELETE_GET(String id) {
        return "template/" + id;
    }

}