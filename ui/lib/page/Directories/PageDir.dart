// ignore_for_file: prefer_const_literals_to_create_immutables, file_names, prefer_const_constructors

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:ui/models/BookmarkModel.dart';
import 'package:ui/models/BookmarkSolidModel.dart';
import 'package:ui/models/DirectoryModel.dart';
import 'package:ui/models/TemplateModel.dart';
import 'package:ui/page/ViewBookmark/PageViewBookmark.dart';
import 'package:ui/page/create_dir/pageCreateDir.dart';
import 'package:ui/page/create_layout/PageCreatelayout.dart';
import 'package:ui/page/create_note/PageCreateNote.dart';
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
  String currentFolderName = "ROOT";

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: MediaQuery.of(context).size.height * 0.95,
      child: SingleChildScrollView(
        child: Column(
          children: [
            Text("Current Dir : $currentFolderName"),
            Row(
              mainAxisSize: MainAxisSize.max,
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                TextButton(
                    onPressed: () {
                      showDialog(
                        context: context,
                        builder: (context) =>
                            PageCreateDir(currentDirID, currentFolderName),
                      );
                    },
                    child: Text("Create new Directory")),
                TextButton(
                    onPressed: () {
                      showDialog(
                        context: context,
                        builder: (context) => PageCreateNote(currentDirID),
                      );
                    },
                    child: Text("Create new Note Note")),
                TextButton(onPressed: () {
                  showDialog(
                    context: context,
                    builder: (context) => PageCreateLayout(),
                  );
                }, child: Text("Create Notty Layout"))
              ],
            ),
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
                                currentFolderName = e.name;
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
                        return GestureDetector(
                            onTap: () async {
                              TemplateModel template =
                                  await _getTemplate(e.templateID);
                              var bookmarkSolid =
                                  BookmarkSolidModel(template, e);
                              Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder: (context) => PageViewBookmark(
                                        bookmark: bookmarkSolid),
                                  ));
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
                return const Text("Loading Bookmarks");
              },
            ),
          ],
        ),
      ),
    );
  }

  Future<TemplateModel> _getTemplate(String templateID) async {
    var serviceProvider = Provider.of<ServiceProvider>(context, listen: false);

    var userProvider = Provider.of<UserProvider>(context, listen: false);

    return await serviceProvider.getTemplateByID(
        userProvider.jwtToken!, templateID);
  }

  Future<void> loadRootDirs() async {
    var serviceProvider = Provider.of<ServiceProvider>(context, listen: false);

    var userProvider = Provider.of<UserProvider>(context, listen: false);

    serviceProvider.getTemplateByID(userProvider.jwtToken!, "*");

    dirs = await serviceProvider.getChildrenDirs(
        userProvider.jwtToken!, currentDirID);
  }

  Future<void> loadDirBookmarks() async {
    var serviceProvider = Provider.of<ServiceProvider>(context, listen: false);

    var userProvider = Provider.of<UserProvider>(context, listen: false);

    bookmarks = await serviceProvider.getBookmarkListFromDirID(
        userProvider.jwtToken!, currentDirID);
  }
}
