// ignore_for_file: prefer_const_constructors

import 'package:flutter/material.dart';
import 'package:frontend/Pages/Login/PageLogin.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        backgroundColor: Color(0xff0a2141),
        scaffoldBackgroundColor: Color(0xff0a2141),
        primaryColor:  Color(0xff0a2141),
      ),
      home: PageLogin()
    );
  }
}