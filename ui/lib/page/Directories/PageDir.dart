// ignore_for_file: prefer_const_literals_to_create_immutables, file_names

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:ui/models/DirectoryModel.dart';
import 'package:ui/provider/ServiceProvider.dart';
import 'package:ui/provider/UserProvider.dart';
import 'package:ui/service/DirService.dart';

class PageDir extends StatefulWidget {
  const PageDir({Key? key}) : super(key: key);

  @override
  State<PageDir> createState() => _PageDirState();
}

class _PageDirState extends State<PageDir> {
  List<DirModel> dirs = [];
  String currentDirID = "";

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      child: Column(
        children: [
          //Directory loader
          FutureBuilder(
            future: loadRootDirs(),
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.done) {
                return ListView(
                    children: dirs
                        .map((e) =>
                            TextButton(onPressed: () {}, child: Text(e.name)))
                        .toList());
              }
              return const Text("Loading dirs");
            },
          )
        ],
      ),
    );
  }

  Future<void> loadRootDirs() async {
    var serviceProvider = Provider.of<ServiceProvider>(context, listen: false);

    var userProvider = Provider.of<UserProvider>(context, listen: false);

    dirs = await serviceProvider.dirService
        .getUserDirs(userProvider.jwtToken!, "*");
    return;
  }

  Future<void> loadDirBookmarks() async {}
}
