// ignore_for_file: file_names

import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:ui/models/BookmarkModel.dart';

import '../models/DirectoryModel.dart';
import '../models/Response/BaseResponseModel.dart';
import '../utils/Utils.dart';

class ServiceProvider {
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

  /// Get list of DirModel who are the children of parentID
  Future<List<DirModel>> getChildrenDirs(
      String jwtToken, String parentID) async {
    String url = URLs.DIR_BASE_URL + URLs.DIR_GET_DIRS(parentID);
    var resp = await http.get(
      Uri.parse(url),
      headers: {"Authorization": "Bearer $jwtToken"},
    );

    //var baseResp = BaseResponseModel.convertResponseToBaseResponse(resp);
    List<dynamic> data = jsonDecode(resp
        .body); //List<dynamic>  [{data: "", msg: ""}]. Probably because I return a flux

    List<dynamic> rawDirs = [];

    for (dynamic d in data) {
      rawDirs.add(d['data']);
    }

    List<DirModel> dirs = [];

    for (dynamic s in rawDirs) {
      //IMPORTANT : List<String> will not work. Be sure to store list as List<dynamic>. Then iterate over them and then cast them appropriately.
      String id = s['id'];
      String creatorID = s['creatorID'];
      String name = s['name'];
      String parentID = s['parent'];
      List<dynamic> childrenIDs = s['children'];
      List<dynamic> bookmarkIDs = s['bookmarks'];

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

      var a = DirModel(
          id, creatorID, name, parentID, castedChildren, castedBookmarks);
      dirs.add(a);
    }

    return dirs;
  }

  Future<List<BookmarkModel>> getBookmarkFromDirID(
      String jwtToken, String dirID) async {
    String url = "http://localhost:8080/api/v1/gate/bookmark/dir/$dirID";
    var resp = await http
        .get(Uri.parse(url), headers: {"Authorization": "Bearer  $jwtToken"});

    if (resp.statusCode != 200) throw Exception("Status code ${resp.statusCode}");

    List<dynamic> body =
        jsonDecode(resp.body); //Body is going to return an array of {data,msg}
    List<dynamic> rawData = [];
    List<BookmarkModel> bookmarks = [];
    for (dynamic d in body) {
      rawData.add(d['data']);
    }
    print(rawData.toString());

    for (dynamic data in rawData) {
      bookmarks.add(BookmarkModel(data['id'], data['creatorID'],
          data['templateID'], data['dirID'], data['name'], data['data']));
    }

    print("----------------------");
    print(bookmarks.toString());
    return bookmarks;
  }
}
