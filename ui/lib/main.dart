// ignore_for_file: prefer_const_constructors

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
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
        home: const Wrapper());
  }
}

class Wrapper extends StatefulWidget {
  const Wrapper({Key? key}) : super(key: key);

  @override
  State<Wrapper> createState() => _WrapperState();
}

class _WrapperState extends State<Wrapper> {
  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: setupData(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          String? token =
              Provider.of<UserProvider>(context, listen: false).jwtToken;
          if (token == null || token.isEmpty) return PageLogin();
          return Text("Welcome home your token is $token");
        }
        return Text("Loading");
      },
    );
  }

  Future<void> setupData() async {
    await Provider.of<UserProvider>(context, listen: false)
        .initializeProvider();
    return;
  }
}
