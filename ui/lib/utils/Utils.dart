// ignore_for_file: constant_identifier_names, file_names

class URLs {
  static const AUTH_BASE_URL = "http://localhost:8080/api/v1/gate/auth";
  static const DIR_BASE_URL = "http://localhost:8080/api/v1/gate/dir";

  static const AUTH_LOGIN_URL = "/login";
  static const AUTH_REG_URL = "/reg";

  static DIR_GET_DIRS(String parentID) {
    return "/" + parentID;
  }
}
