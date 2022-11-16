// ignore_for_file: file_names

import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:shared_preferences/shared_preferences.dart';

class UserProvider with ChangeNotifier {
  String? jwtToken;
  SharedPreferences? _preferences;

  final String _prefTokenKey = "token";

  Future<void> initializeProvider() async {
    _preferences = await SharedPreferences.getInstance();
    jwtToken = _preferences!.getString(_prefTokenKey);
    notifyListeners();
  }

  void setToken(String token) {
    _preferences!.setString(_prefTokenKey, token);
    jwtToken = _preferences!.getString(token);
    notifyListeners();
  }

  void logout() {
    _preferences!.remove(_prefTokenKey);
    jwtToken = null;
    notifyListeners();
  }
}
