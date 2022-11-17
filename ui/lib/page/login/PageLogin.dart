// ignore_for_file: prefer_const_literals_to_create_immutables, prefer_const_constructors, file_names, curly_braces_in_flow_control_structures

import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:ui/page/Home/PageHome.dart';
import 'package:ui/provider/ServiceProvider.dart';
import 'package:ui/provider/UserProvider.dart';

class PageLogin extends StatefulWidget {
  const PageLogin({Key? key}) : super(key: key);

  @override
  State<PageLogin> createState() => _PageLoginState();
}

class _PageLoginState extends State<PageLogin> {
  final TextEditingController emailController = TextEditingController();
  final TextEditingController passwordController = TextEditingController();
  final TextEditingController nameController = TextEditingController();

  late final Stream<String> loginStatusStream;
  late final StreamController<String> loginStatusStreamController;

  @override
  void initState() {
    super.initState();
    loginStatusStreamController = StreamController();
    loginStatusStream = loginStatusStreamController.stream;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        mainAxisSize: MainAxisSize.max,
        children: [
          Text("Kuku's NottyNote"),
          StreamBuilder(
            builder: (context, snapshot) => Text(snapshot.data!),
            stream: loginStatusStream,
            initialData: "",
          ),
          TextField(
            controller: nameController,
            keyboardType: TextInputType.text,
            decoration: InputDecoration(
              label: Text("Name"),
            ),
          ),
          TextField(
            controller: emailController,
            keyboardType: TextInputType.emailAddress,
            decoration: InputDecoration(
              label: Text("Email"),
            ),
          ),
          TextField(
            controller: passwordController,
            obscureText: true,
            decoration: InputDecoration(label: Text("Password")),
          ),
          Row(
            mainAxisSize: MainAxisSize.max,
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              IconButton(
                onPressed: loginPressed,
                icon: Icon(Icons.login),
              ),
              TextButton(onPressed: registerPressed, child: Text("Register"))
            ],
          ),
        ],
      ),
    );
  }

  Future<void> loginPressed() async {
    var serviceProvider = Provider.of<ServiceProvider>(context, listen: false);

    if (emailController.text.isEmpty || passwordController.text.isEmpty) {
      loginStatusStreamController.add("Email and/or password field empty");
      await Future.delayed(Duration(seconds: 5));
      loginStatusStreamController.add("");
      return;
    }
    loginStatusStreamController.add("Logging In.");
    try {
      var userProvider = Provider.of<UserProvider>(context, listen: false);
      var token = await serviceProvider.userService
          .login(emailController.text, passwordController.text);
      userProvider.setToken(token);
      await userProvider.validateToken();
    } on Exception catch (e) {
      loginStatusStreamController.add(e.toString());
    }
  }

  Future<void> registerPressed() async {
    var serviceProvider = Provider.of<ServiceProvider>(context, listen: false);

    if (emailController.text.isEmpty ||
        passwordController.text.isEmpty ||
        nameController.text.isEmpty) {
      loginStatusStreamController.add("Email and/or password field empty");
      await Future.delayed(Duration(seconds: 5));
      loginStatusStreamController.add("");
      return;
    }

    loginStatusStreamController.add("Registering User");
    try {
      var success = await serviceProvider.userService.reg(
          nameController.text, emailController.text, passwordController.text);
      if (success)
        loginStatusStreamController.add("Registered Successfully");
      else
        loginStatusStreamController.add("Registration Failed");
    } on Exception catch (e) {
      loginStatusStreamController.add(e.toString());
    }
  }
}
