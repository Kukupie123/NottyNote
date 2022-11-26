// ignore_for_file: file_names

import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:ui/models/BookmarkModel.dart';
import 'package:ui/models/TemplateField.dart';
import 'package:ui/models/TemplateModel.dart';

import '../models/DirectoryModel.dart';
import '../models/Response/BaseResponseModel.dart';
import '../utils/Utils.dart';

class ServiceProvider {
  //AUTH SERVICE------------------------
  Future<String> login(String email, String password) async {
    String url = URLs.AUTH_BASE_URL + URLs.AUTH_LOGIN_URL;

    String body = jsonEncode({"email": email, "password": password});
    http.Response response = await http.post(Uri.parse(url),
        body: body, headers: {"Content-Type": "application/json"});
    var baseResp = BaseResponseModel.convertResponseToBaseResponse(response);

    if (baseResp.statusCode != 200) {
      throw Exception(baseResp.msg);
    }
    return baseResp.data;
  }

  Future<bool> reg(String name, String email, String password) async {
    String url = URLs.AUTH_BASE_URL + URLs.AUTH_REG_URL;

    String body =
        jsonEncode({"email": email, "password": password, "name": name});
    http.Response resp = await http.post(Uri.parse(url),
        body: body, headers: {"Content-Type": "application/json"});

    var baseResp = BaseResponseModel.convertResponseToBaseResponse(resp);

    if (baseResp.statusCode != 200) {
      throw Exception(baseResp.msg);
    }
    return baseResp.data as bool;
  }

  //DIR SERVICE-------------------------------

  ///Parses response body of type {data,msg} and returns a DirModel
  DirModel _createDirFromRespBody(String responseBody) {
    var data = jsonDecode(responseBody)['data'];
    String id = data['id'];
    String creatorID = data['creatorID'];
    String name = data['name'];
    String parentID = data['parent'];
    //IMPORTANT : List<String> will not work. Be sure to store list as List<dynamic>. Then iterate over them and then cast them appropriately.
    List<dynamic> childrenIDs = data['children'];
    List<dynamic> bookmarkIDs = data['bookmarks'];

    List<String> castedChildren = [];
    List<String> castedBookmarks = [];

    for (dynamic s in childrenIDs) {
      String a = s as String;
      castedChildren.add(a);
    }

    for (dynamic s in bookmarkIDs) {
      String a = s as String;
      castedBookmarks.add(a);
    }

    return DirModel(
        id, creatorID, name, parentID, castedChildren, castedBookmarks);
  }

  List<DirModel> _createDirsFromRespBody(String respBody) {
    List<dynamic> dataList = jsonDecode(respBody);
    List<String> dataListString = [];
    for (Map s in dataList) {
      dataListString.add(jsonEncode(s));
    }
    List<DirModel> dirs = [];
    for (String s in dataListString) {
      var a = _createDirFromRespBody(s);
      dirs.add(a);
    }
    return dirs;
  }

  Future<bool> deleteDir(String jwtToken, String dirID) async {
    String url = "http://localhost:8080/api/v1/gate/dir/$dirID";
    var resp = await http
        .delete(Uri.parse(url), headers: {"Authorization": "Bearer $jwtToken"});
    return jsonDecode(resp.body)['data'];
  }

  Future<String> createDir(
      String jwtToken, String dirName, bool isPublic, String parentID) async {
    String url = "http://localhost:8080/api/v1/gate/dir/create";
    if (parentID == "*") parentID = "";
    var body = jsonEncode({
      "name": dirName,
      "isPublic": isPublic,
      "parent": parentID,
    });
    var resp = await http.post(Uri.parse(url),
        headers: {
          "Authorization": "Bearer $jwtToken",
          "Content-Type": "application/json"
        },
        body: body);
    if (resp.statusCode == 200) return resp.body;
    throw Exception(
        jsonDecode(resp.body)['msg'] + "status code ${resp.statusCode}");
  }

  Future<DirModel> getDir(String jwtToken, String dirID) async {
    //GET http://localhost:8080/api/v1/gate/dir/{{id}}
    String url = "http://localhost:8080/api/v1/gate/dir/$dirID";
    var resp = await http
        .get(Uri.parse(url), headers: {"Authorization": "Bearer $jwtToken"});
    if (resp.statusCode != 200) {
      throw Exception("Something went wrong");
    }
    return _createDirFromRespBody(resp.body);
  }

  /// Get list of DirModel who are the children of parentID
  Future<List<DirModel>> getChildrenDirs(
      String jwtToken, String parentID) async {
    String url = URLs.DIR_BASE_URL + URLs.DIR_GET_DIRS(parentID);
    var resp = await http.get(
      Uri.parse(url),
      headers: {"Authorization": "Bearer $jwtToken"},
    );
    return _createDirsFromRespBody(resp.body);
  }

  //BOOKMARK SERVICE---------------------
  BookmarkModel _createBookmarkFromRespBody(String respBody) {
    var data = jsonDecode(respBody)['data'];
    return BookmarkModel(data['id'], data['creatorID'], data['templateID'],
        data['dirID'], data['name'], data['data']);
  }

  List<BookmarkModel> _createBookmarksFromRespBody(String respBody) {
    List<BookmarkModel> bookmarks = [];
    List<dynamic> dataList = jsonDecode(respBody);
    List<String> dataListString = [];
    for (dynamic d in dataList) {
      dataListString.add(jsonEncode(d));
    }
    for (String data in dataListString) {
      bookmarks.add(_createBookmarkFromRespBody(data));
    }
    print(bookmarks.toString());
    return bookmarks;
  }

  Future<bool> deleteBookmark(String jwtToken, String id) async {
    String url = "http://localhost:8080/api/v1/gate/bookmark/$id";
    var resp = await http
        .delete(Uri.parse(url), headers: {"Authorization": "Bearer $jwtToken"});
    return jsonDecode(resp.body)['data'];
  }

  Future<BookmarkModel> getBookmarkByID(
      String jwtToken, String bookmarkID) async {
    String url = "http://localhost:8080/api/v1/gate/bookmark/$bookmarkID";
    var resp = await http
        .get(Uri.parse(url), headers: {"Authorization": "Bearer  $jwtToken"});
    return _createBookmarkFromRespBody(resp.body);
  }

  Future<String> createBookmark(String jwtToken, String templateID,
      String dirID, String bookmarkName, Map data) async {
    if (dirID == "*" || dirID.isEmpty) throw Exception("Dir ID is * or empty");
    Map body = {};
    body['templateID'] = templateID;
    body['dirID'] = dirID;
    body['name'] = bookmarkName;
    body['data'] = data;

    String encodedBody = jsonEncode(body);
    String url = "http://localhost:8080/api/v1/gate/bookmark/create";
    var resp = await http.post(Uri.parse(url),
        headers: {
          "Authorization": "Bearer $jwtToken",
          "Content-Type": "application/json"
        },
        body: encodedBody);

    print("sc ${resp.statusCode} and body ${resp.body}");

    return resp.body;
  }

  Future<List<BookmarkModel>> getBookmarksByToken(String jwtToken) async {
    String url = "http://localhost:8080/api/v1/gate/bookmark/getall/all";
    var resp = await http
        .get(Uri.parse(url), headers: {"Authorization": "Bearer  $jwtToken"});

    return _createBookmarksFromRespBody(resp.body);
  }

  Future<List<BookmarkModel>> getBookmarkListFromDirID(
      String jwtToken, String dirID) async {
    String url = "http://localhost:8080/api/v1/gate/bookmark/dir/$dirID";
    var resp = await http
        .get(Uri.parse(url), headers: {"Authorization": "Bearer  $jwtToken"});

    if (resp.statusCode != 200) {
      throw Exception("Status code ${resp.statusCode}");
    }
    return _createBookmarksFromRespBody(resp.body);
  }

  Future<List<BookmarkModel>> getBookmarkListFromTempID(
      String jwtToken, String tempID) async {
    String url = "http://localhost:8080/api/v1/gate/bookmark/temp/$tempID";
    var resp = await http
        .get(Uri.parse(url), headers: {"Authorization": "Bearer $jwtToken"});

    return _createBookmarksFromRespBody(resp.body);
  }

  //TEMPLATE SERVICE-------------

  TemplateModel _createTemplateFromRespBody(String body) {
    Map<String, dynamic> data = jsonDecode(body)['data'];
    //data['bookmarks'] is of type list<dynamic> and we can't cast it to list<String> so we need to integrate the dynamic list and cast the values ourselves
    List<String> bookmarks = [];
    for (dynamic c in data['bookmarks']) {
      bookmarks.add(c);
    }
    //data['struct'] is of type jsonMap so we need to manually parse it
    Map<dynamic, dynamic> rawStruct = data['struct'];
    Map<String, TemplateField> struct = {};
    rawStruct.forEach((key, value) {
      String fieldName = key;
      var templateField = TemplateField(value['fieldType'], value["optional"]);
      struct[fieldName] = templateField;
    });
    TemplateModel template = TemplateModel(
        data['id'], data['name'], data['creatorID'], bookmarks, struct);
    return template;
  }

  List<TemplateModel> _createTemplatesFromRespBody(String body) {
    List<dynamic> bodies = jsonDecode(body);
    List<String> stringBodies = [];
    for (dynamic d in bodies) {
      stringBodies.add(jsonEncode(d));
    }
    List<TemplateModel> models = [];
    for (String s in stringBodies) {
      models.add(_createTemplateFromRespBody(s));
    }
    return models;
  }

  Future<bool> deleteTemplate(String jwtToken, String tempID) async {
    print("DELETEING TEMPLATE $tempID");
    String url = "http://localhost:8080/api/v1/gate/temp/$tempID";
    var resp = await http
        .delete(Uri.parse(url), headers: {"Authorization": "Bearer $jwtToken"});
    return jsonDecode(resp.body)['data'];
  }

  Future<String> createTemplate(
      String jwtToken, String templateName, Map struct) async {
    String url = "http://localhost:8080/api/v1/gate/temp/create";
    Map body = {"name": templateName};
    body['struct'] = struct;
    String encodedBody = jsonEncode(body);
    print(encodedBody);
    var resp = await http.post(Uri.parse(url),
        headers: {
          "Authorization": "Bearer $jwtToken",
          "Content-Type": "application/json"
        },
        body: encodedBody);

    print("SC : ${resp.statusCode} and body : ${resp.body}");
    return jsonDecode(resp.body)['data'];
  }

  Future<TemplateModel> getTemplateByID(
      String jwtToken, String templateID) async {
    String url = "http://localhost:8080/api/v1/gate/temp/$templateID";
    var resp = await http
        .get(Uri.parse(url), headers: {"Authorization": "Bearer $jwtToken"});
    if (resp.statusCode != 200) {
      throw Exception("Something went wrong");
    }
    return _createTemplateFromRespBody(resp.body);
  }

  Future<List<TemplateModel>> getTemplatesForUser(String token) async {
    String url = "http://localhost:8080/api/v1/gate/temp/getall/all";
    var resp = await http
        .get(Uri.parse(url), headers: {"Authorization": "Bearer $token"});
    return _createTemplatesFromRespBody(resp.body);
  }
}
