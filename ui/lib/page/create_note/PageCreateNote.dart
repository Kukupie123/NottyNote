// ignore_for_file: prefer_const_literals_to_create_immutables, prefer_const_constructors, curly_braces_in_flow_control_structures

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:ui/models/TemplateModel.dart';
import 'package:ui/provider/ServiceProvider.dart';
import 'package:ui/provider/UserProvider.dart';

class PageCreateNote extends StatefulWidget {
  final String dirID;

  const PageCreateNote(this.dirID, {Key? key}) : super(key: key);

  @override
  State<PageCreateNote> createState() => _PageCreateNoteState();
}

class _PageCreateNoteState extends State<PageCreateNote> {
  TemplateModel? currentTemplate;
  List<TemplateModel> templates = [];
  Map<String, TextEditingController> textControllers = {};
  Map<String, _ListField> textlists = {};
  TextEditingController titleController = TextEditingController();
  late final ServiceProvider serviceProvider;
  late final UserProvider userProvider;

  @override
  void initState() {
    super.initState();
    serviceProvider = Provider.of<ServiceProvider>(context, listen: false);
    userProvider = Provider.of<UserProvider>(context, listen: false);
  }

  Future<void> loadTemplates() async {
    templates =
        await serviceProvider.getTemplatesForUser(userProvider.jwtToken!);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SingleChildScrollView(
        child: Column(
          children: [
            Container(
              color: Colors.yellow,
              height: MediaQuery.of(context).size.height * 0.3,
              child: FutureBuilder(
                future: loadTemplates(),
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.done) {
                    return ListView(
                      children: templates
                          .map((e) => TextButton(
                              onPressed: () {
                                setState(() {
                                  if (currentTemplate == e) {
                                    currentTemplate = null;
                                    return;
                                  }
                                  currentTemplate = e;
                                });
                              },
                              child: Text(e.name)))
                          .toList(),
                    );
                  }
                  return Text("Loading template list");
                },
              ),
            ),
            Container(
              color: Colors.grey,
              height: MediaQuery.of(context).size.height * 0.7,
              width: double.infinity,
              child: currentTemplate == null
                  ? Text("Select a template")
                  : SingleChildScrollView(
                      child: Column(
                        children: _popFields(),
                      ),
                    ),
            )
          ],
        ),
      ),
    );
  }

  List<Widget> _popFields() {
    if (currentTemplate == null) return [];
    List<Widget> widgets = [];
    widgets.add(Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisAlignment: MainAxisAlignment.start,
      children: [
        Container(
          width: 200,
          height: 200,
          child: TextField(
            controller: titleController,
            decoration: InputDecoration(hintText: "Name of bookmark"),
          ),
        ),
      ],
    ));

    currentTemplate!.struct.forEach((key, value) {
      print("$key has type ${value.fieldType}");
      String fieldType = value.fieldType;
      bool isOptional = value.isOptional;
      switch (fieldType) {
        case "TEXT":
          if (!textControllers.containsKey(key))
            textControllers[key] = TextEditingController();
          widgets.add(Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.start,
            children: [
              Container(
                width: 200,
                height: 200,
                child: TextField(
                  controller: textControllers[key],
                  decoration: InputDecoration(hintText: key),
                ),
              ),
              Text(" Is Optional? : $isOptional")
            ],
          ));
          break;
        case "LINK":
          if (!textControllers.containsKey(key))
            textControllers[key] = TextEditingController();
          widgets.add(Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.start,
            children: [
              Container(
                width: 200,
                height: 200,
                child: TextField(
                  controller: textControllers[key],
                  decoration: InputDecoration(hintText: key),
                ),
              ),
              Text(" Is Optional? : $isOptional")
            ],
          ));
          break;
        case "LIST_TEXT":
          if (!textlists.containsKey(key))
            textlists[key] = _ListField([], TextEditingController());
          widgets.add(Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.start,
            children: [
              Text("$key : "),
              Container(
                height: 200,
                width: 200,
                child: ListView(
                  children: textlists[key]!.values.map((e) => Text(e)).toList(),
                ),
              ),
              Container(
                height: 200,
                width: 200,
                child: TextField(
                  controller: textlists[key]!.textEditingController,
                  decoration: InputDecoration(hintText: key),
                  onSubmitted: (value) {
                    setState(() {
                      textlists[key]!.values.add(value);
                    });
                  },
                ),
              )
            ],
          ));
          break;
        case "LIST_LINK":
          if (!textlists.containsKey(key))
            textlists[key] = _ListField([], TextEditingController());
          widgets.add(Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.start,
            children: [
              Text("$key : "),
              Container(
                height: 200,
                width: 200,
                child: ListView(
                  children: textlists[key]!.values.map((e) {
                    print("list element $e");
                    return Text(e);
                  }).toList(),
                ),
              ),
              Container(
                height: 200,
                width: 200,
                child: TextField(
                  controller: textlists[key]!.textEditingController,
                  decoration: InputDecoration(hintText: key),
                  onSubmitted: (value) {
                    setState(() {
                      print("ADDED $value");
                      textlists[key]!.values.add(value);
                    });
                  },
                ),
              )
            ],
          ));
          break;
      }
    });
    widgets.add(
        TextButton(onPressed: _createNote, child: Text("Create Bookmark")));
    return widgets;
  }

  Future<void> _createNote() async {
    if (currentTemplate == null) return;
    Map data = {};
    textlists.forEach((key, value) {
      data[key] = value.values;
    });
    textControllers.forEach((key, value) {
      data[key] = value.text;
    });
    ServiceProvider serviceProvider =
        Provider.of<ServiceProvider>(context, listen: false);
    UserProvider userProvider =
        Provider.of<UserProvider>(context, listen: false);

    String id = await serviceProvider.createBookmark(userProvider.jwtToken!,
        currentTemplate!.id, widget.dirID, titleController.text, data);
  }
}

class _ListField {
  List<String> values;
  TextEditingController textEditingController;

  _ListField(this.values, this.textEditingController);
}
