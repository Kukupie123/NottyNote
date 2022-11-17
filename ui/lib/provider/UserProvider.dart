// ignore_for_file: file_names

import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:http/http.dart' as http;
import 'package:ui/utils/Utils.dart';

class UserProvider with ChangeNotifier {
  String? jwtToken;
  SharedPreferences? _preferences;
  bool validated = false;

  final String _prefTokenKey = "token";

  Future<void> validateToken() async {
    const String url = URLs.AUTH_BASE_URL + URLs.AUTH_VAL_URL;
    var resp = await http
        .get(Uri.parse(url), headers: {"Authorization": "Bearer $jwtToken"});
    if (resp.statusCode != 200) {
      validated = false;
    } else {
      validated = true;
    }
    notifyListeners();
  }

  Future<void> loadLocalToken() async {
    _preferences = await SharedPreferences.getInstance();
    jwtToken = _preferences!.getString(_prefTokenKey);
    if (jwtToken == null || jwtToken!.isEmpty) {
      validated = false;
      notifyListeners();
    } else {
      await validateToken();
    }
  }

  setToken(String token) {
    _preferences!.setString(_prefTokenKey, token);
    jwtToken = _preferences!.getString(_prefTokenKey);
  }

  void logout() {
    _preferences!.remove(_prefTokenKey);
    jwtToken = null;
    validated = false;
    notifyListeners();
  }
}
