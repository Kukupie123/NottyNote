// ignore_for_file: file_names

import 'dart:convert';

import 'package:http/http.dart';

class BaseResponseModel {
  String? msg;
  dynamic data;
  int statusCode = 500;

  BaseResponseModel(
      {required this.msg, required this.data, required this.statusCode});

  static BaseResponseModel convertResponseToBaseResponse(Response response) {
    int statusCode = response.statusCode;
    Map parsedBody = jsonDecode(response.body);
    String msg = parsedBody['msg'];
    dynamic data = parsedBody['data'];

    return BaseResponseModel(msg: msg, data: data, statusCode: statusCode);
  }
}
