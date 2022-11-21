// ignore_for_file: prefer_const_literals_to_create_immutables, prefer_const_constructors, file_names, sized_box_for_whitespace

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:ui/page/Directories/PageDir.dart';
import 'package:ui/page/login/PageLogin.dart';
import 'package:ui/provider/UserProvider.dart';

class PageHome extends StatefulWidget {
  const PageHome({Key? key}) : super(key: key);

  @override
  State<PageHome> createState() => _PageHomeState();
}

class _PageHomeState extends State<PageHome> {
  @override
  void initState() {
    super.initState();
    //Validate token once we load in. If this fails we go to login page
    Provider.of<UserProvider>(context, listen: false).validateToken();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        body: DefaultTabController(
      length: 3,
      child: Column(
        children: [
          TabBar(
            labelColor: Colors.black26,
            tabs: [
              Text("Directory"),
              Text("Notty Note"),
              Text("Notty Layout"),
            ],
          ),
          Container(
            height: MediaQuery.of(context).size.height * 0.9,
            child: TabBarView(
              children: [
                PageDir(),
                Text("TEST"),
                Text("data"),
              ],
            ),
          ),
          TextButton(onPressed: logout, child: Text("LOGOUT"))
        ],
      ),
    ));
  }

  void logout() {
    Provider.of<UserProvider>(context, listen: false).logout();
  }
}
