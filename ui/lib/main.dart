// ignore_for_file: prefer_const_constructors

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:ui/page/Home/PageHome.dart';
import 'package:ui/page/login/PageLogin.dart';
import 'package:ui/provider/ServiceProvider.dart';
import 'package:ui/provider/UserProvider.dart';

void main() {
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(
          create: (_) => UserProvider(),
        ),
        Provider(
          create: (context) => ServiceProvider(),
        )
      ],
      child: const MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        title: "Kuku's NottyNote",
        theme: ThemeData(
          primarySwatch: Colors.blue,
        ),
    );
  }
}


/*
On app start we are going to have a wrapper that is going to try to load local token
if found then it will validate it by sending it to server for validation
if validated we are going to go to homescreen

if no local found we are going to go to login screen
if validating token fails we are again going to go to login

for other requests. we are always going to first validate. If validation  fails we are going to return to login

We will need a root consumer who is going to return us to login when validation fails no matter where we are in the application
 */
