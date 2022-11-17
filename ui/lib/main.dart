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
      home: MainWrapper(),
    );
  }
}

class MainWrapper extends StatelessWidget {
  const MainWrapper({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: loadLocalToken(context),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          return Consumer<UserProvider>(
            builder: (context, value, child) {
              if (value.validated) {
                return PageHome();
              } else {
                return PageLogin();
              }
            },
          );
        }
        return Scaffold(
          body: Text("Initializing Website please wait..."),
        );
      },
    );
  }

  Future<void> loadLocalToken(BuildContext context) async {
    var userProvider = Provider.of<UserProvider>(context, listen: false);
    await userProvider.loadLocalToken();
  }
}


/*
Root Consumer is going to be responsible for switching us to login page when the JWT token becomes invalid.
We have a validate function that is going to validate the token against the server and then notify the listeners.
The Root Consumer is the listener and will get notified. And based on this it is going to Send us to homepage or login page.
 */
