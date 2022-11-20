import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:ui/models/DirectoryModel.dart';
import 'package:ui/utils/Utils.dart';

class DirService {
  Future<List<DirModel>> getUserDirs(String jwtToken, String parentID) async {
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
}
