// ignore_for_file: file_names

import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:ui/models/Response/BaseResponseModel.dart';
import 'package:ui/utils/Utils.dart';

class UserService {
  Future<String> login(String email, String password) async {
    String url = URLs.AUTH_BASE_URL + URLs.AUTH_LOGIN_URL;

    String body = jsonEncode({"email": email, "password": password});
    http.Response response = await http.post(Uri.parse(url),
        body: body, headers: {"Content-Type": "application/json"});
    var baseResp = BaseResponseModel.convertResponseToBaseResponse(response);

    if (baseResp.statusCode != 200) {
      throw Exception(baseResp.msg);
    }
    return baseResp.data as String;
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
}
