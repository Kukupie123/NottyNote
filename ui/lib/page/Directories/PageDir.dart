// ignore_for_file: prefer_const_literals_to_create_immutables

import 'package:flutter/material.dart';

class PageDir extends StatefulWidget {
  const PageDir({Key? key}) : super(key: key);

  @override
  State<PageDir> createState() => _PageDirState();
}

class _PageDirState extends State<PageDir> {
  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Column(
        children: [
          ListView(
            children: [],
          ),
          ListView(
            children: [],
          )
        ],
      ),
    );
  }
}
