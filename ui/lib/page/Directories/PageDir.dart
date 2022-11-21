// ignore_for_file: prefer_const_literals_to_create_immutables, file_names

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:ui/models/BookmarkModel.dart';
import 'package:ui/models/DirectoryModel.dart';
import 'package:ui/provider/ServiceProvider.dart';
import 'package:ui/provider/UserProvider.dart';

class PageDir extends StatefulWidget {
  const PageDir({Key? key}) : super(key: key);

  @override
  State<PageDir> createState() => _PageDirState();
}

class _PageDirState extends State<PageDir> {
  List<DirModel> dirs = [];
  List<BookmarkModel> bookmarks = [];
  String currentDirID = "*";

  @override
  Widget build(BuildContext context) {
    print("Current dir is $currentDirID");
    return SizedBox(
      height: MediaQuery.of(context).size.height * 0.95,
      child: SingleChildScrollView(
        child: Column(
          children: [
            //Directory loader
            FutureBuilder(
              future: loadRootDirs(),
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.done) {
                  return Container(
                    color: Colors.grey,
                    height: MediaQuery.of(context).size.height * 0.5,
                    child: ListView(
                      children: dirs.map((e) {
                        return GestureDetector(
                            onTap: () {
                              setState(() {
                                currentDirID = e.id;
                              });
                            },
                            child: Card(
                              child: Column(
                                children: [Text(e.name)],
                              ),
                            ));
                      }).toList(),
                    ),
                  );
                }
                return const Text("Loading dirs");
              },
            ),
            //Bookmarks loader
            FutureBuilder(
              future: loadDirBookmarks(),
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.done) {
                  return Container(
                    color: Colors.yellow,
                    height: MediaQuery.of(context).size.height * 0.5,
                    child: ListView(
                      children: bookmarks.map((e) {
                        return Card(
                          child: Column(
                            children: [Text(e.name)],
                          ),
                        );
                      }).toList(),
                    ),
                  );
                }
                return const Text("Loading Bookmarks");
              },
            ),
          ],
        ),
      ),
    );
  }

  Future<void> loadRootDirs() async {
    var serviceProvider = Provider.of<ServiceProvider>(context, listen: false);

    var userProvider = Provider.of<UserProvider>(context, listen: false);

    serviceProvider.getTemplateByID(userProvider.jwtToken!, "63764aaf2f397909b7526a18");

    dirs = await serviceProvider.getChildrenDirs(
        userProvider.jwtToken!, currentDirID);
  }

  Future<void> loadDirBookmarks() async {
    var serviceProvider = Provider.of<ServiceProvider>(context, listen: false);

    var userProvider = Provider.of<UserProvider>(context, listen: false);

    bookmarks = await serviceProvider.getBookmarkFromDirID(
        userProvider.jwtToken!, currentDirID);
  }
}
