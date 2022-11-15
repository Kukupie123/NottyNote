// ignore_for_file: prefer_const_literals_to_create_immutables, prefer_const_constructors

import 'package:flutter/material.dart';
import 'package:ui/page/Directories/PageDir.dart';

class PageHome extends StatefulWidget {
  const PageHome({Key? key}) : super(key: key);

  @override
  State<PageHome> createState() => _PageHomeState();
}

class _PageHomeState extends State<PageHome>
    with SingleTickerProviderStateMixin {
  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 3,
      child: Scaffold(
        backgroundColor: Colors.black54,
        body: Column(
          children: [
            TabBar(
              tabs: [
                Text("Directory"),
                Text("Notty Note"),
                Text("Notty Layout"),
              ],
            ),
            SingleChildScrollView(
              child: Column(
                children: [
                  TabBarView(
                    children: [
                      PageDir(),
                      Text("Note view"),
                      Text("Layout view")
                    ],
                  )
                ],
              ),
            )
          ],
        ),
      ),
    );
  }
}
