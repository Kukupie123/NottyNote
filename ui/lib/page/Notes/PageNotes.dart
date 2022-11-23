// ignore_for_file: prefer_const_literals_to_create_immutables, prefer_const_constructors

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:ui/models/BookmarkModel.dart';
import 'package:ui/provider/ServiceProvider.dart';
import 'package:ui/provider/UserProvider.dart';

import '../../models/BookmarkSolidModel.dart';
import '../../models/TemplateModel.dart';
import '../ViewBookmark/PageViewBookmark.dart';

class PageNotes extends StatefulWidget {
  const PageNotes({Key? key}) : super(key: key);

  @override
  State<PageNotes> createState() => _PageNotesState();
}

class _PageNotesState extends State<PageNotes> {
  List<BookmarkModel>? bookmarks;

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: loadBookmarks(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          return Column(
            children: bookmarks!
                .map((e) => TextButton(
                    onPressed: () async {
                      TemplateModel template = await _getTemplate(e.templateID);
                      var bookmarkSolid = BookmarkSolidModel(template, e);
                      Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) =>
                                PageViewBookmark(bookmark: bookmarkSolid),
                          ));
                    },
                    child: Card(
                      child: Text(e.name),
                    )))
                .toList(),
          );
        }
        return Text("Loading bookmarks");
      },
    );
  }

  Future<void> loadBookmarks() async {
    var service = Provider.of<ServiceProvider>(context, listen: false);
    String token = Provider.of<UserProvider>(context, listen: false).jwtToken!;
    bookmarks = await service.getBookmarksByToken(token);
  }

  Future<TemplateModel> _getTemplate(String templateID) async {
    var serviceProvider = Provider.of<ServiceProvider>(context, listen: false);

    var userProvider = Provider.of<UserProvider>(context, listen: false);

    return await serviceProvider.getTemplateByID(
        userProvider.jwtToken!, templateID);
  }
}
