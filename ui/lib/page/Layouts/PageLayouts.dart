import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:ui/models/BookmarkModel.dart';
import 'package:ui/models/TemplateModel.dart';

import '../../models/BookmarkSolidModel.dart';
import '../../provider/ServiceProvider.dart';
import '../../provider/UserProvider.dart';
import '../ViewBookmark/PageViewBookmark.dart';

class PageLayouts extends StatefulWidget {
  const PageLayouts({Key? key}) : super(key: key);

  @override
  State<PageLayouts> createState() => _PageLayoutsState();
}

class _PageLayoutsState extends State<PageLayouts> {
  late final serviceProvider =
      Provider.of<ServiceProvider>(context, listen: false);

  late final userProvider = Provider.of<UserProvider>(context, listen: false);
  List<TemplateModel> templates = [];
  List<BookmarkModel> bookmarks = [];
  String currentTemplate = "";

  @override
  Widget build(BuildContext context) {
    return Container(
      height: MediaQuery.of(context).size.height * 0.95,
      child: SingleChildScrollView(
        child: Column(
          children: [
            FutureBuilder(
              future: _loadTemplates(),
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.done) {
                  return Container(
                    color: Colors.grey,
                    height: MediaQuery.of(context).size.height * 0.5,
                    child: ListView(
                      children: templates.map((e) {
                        return Row(children: [
                          GestureDetector(
                            onTap: () {
                              setState(() {
                                currentTemplate = e.id;
                              });
                            },
                            child: Card(
                              child: Column(
                                children: [Text(e.name)],
                              ),
                            ),
                          ),
                          IconButton(
                              onPressed: () {
                                serviceProvider.deleteTemplate(
                                    userProvider.jwtToken!, e.id);
                              },
                              icon: Icon(Icons.delete))
                        ]);
                      }).toList(),
                    ),
                  );
                }
                return const Text("Loading dirs");
              },
            ),
            //Bookmarks loader
            FutureBuilder(
              future: _loadDirBookmarks(),
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.done) {
                  return Container(
                    color: Colors.yellow,
                    height: MediaQuery.of(context).size.height * 0.5,
                    child: ListView(
                      children: bookmarks.map((e) {
                        return Row(
                          children: [
                            GestureDetector(
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
                              ),
                            ),
                            IconButton(
                                onPressed: () {
                                  serviceProvider.deleteBookmark(
                                      userProvider.jwtToken!, e.id);
                                },
                                icon: const Icon(Icons.delete))
                          ],
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

  Future<void> _loadTemplates() async {
    templates =
        await serviceProvider.getTemplatesForUser(userProvider.jwtToken!);
  }

  Future<void> _loadDirBookmarks() async {
    bookmarks = await serviceProvider.getBookmarkListFromTempID(
        userProvider.jwtToken!, currentTemplate);
  }

  Future<TemplateModel> _getTemplate(String templateID) async {
    return await serviceProvider.getTemplateByID(
        userProvider.jwtToken!, templateID);
  }
}
