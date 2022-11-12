// ignore_for_file: file_names

import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:ui/models/Response/BaseResponseModel.dart';
import 'package:ui/utils/Utils.dart';

class UserService {
  Future<String> login(String userName, String password) async {
    String url = Utils.AUTH_BASE_URL + Utils.AUTH_LOGIN_URL;

    String body = jsonEncode({"email": userName, "password": password});
    http.Response response = await http.post(
      Uri.parse(url),
      body: body,
      headers: {"Content-Type":"application/json"}
    );

    print(response.body);

    var baseResp = BaseResponseModel.convertResponseToBaseResponse(response);

    if (baseResp.statusCode != 200) {
      throw Exception(baseResp.msg);
    }
    return baseResp.data as String;
  }
}
