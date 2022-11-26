// ignore_for_file: prefer_const_literals_to_create_immutables, prefer_const_constructors';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:ui/provider/ServiceProvider.dart';
import 'package:ui/provider/UserProvider.dart';

class PageCreateLayout extends StatefulWidget {
  const PageCreateLayout({Key? key}) : super(key: key);

  @override
  State<PageCreateLayout> createState() => _PageCreateLayoutState();
}

class _PageCreateLayoutState extends State<PageCreateLayout> {
  Map<String, _FieldType> structs = {};
  String type = "TEXT";
  TextEditingController titleController = TextEditingController();
  TextEditingController fieldNameController = TextEditingController();
  bool _isOptional = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SingleChildScrollView(
        child: Column(
          children: [
            SizedBox(
              height: 200,
              width: 200,
              child: TextField(
                controller: titleController,
                decoration:
                    const InputDecoration(hintText: "Name of the layout"),
              ),
            ),
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                SizedBox(
                  height: 200,
                  width: 200,
                  child: TextField(
                    controller: fieldNameController,
                    decoration: const InputDecoration(hintText: "Field Name"),
                  ),
                ),
                DropdownButton(
                  value: type,
                  items: [
                    const DropdownMenuItem(
                      value: "TEXT",
                      child: Text("TEXT"),
                    ),
                    const DropdownMenuItem(
                      value: "LINK",
                      child: Text("LINK"),
                    ),
                    const DropdownMenuItem(
                      value: "LIST_TEXT",
                      child: Text("LIST Text"),
                    ),
                    const DropdownMenuItem(
                      value: "LIST_LINK",
                      child: Text("LIST Link"),
                    ),
                  ],
                  onChanged: (value) {
                    setState(() {
                      type = value!;
                    });
                  },
                ),
                Checkbox(
                  value: _isOptional,
                  onChanged: (value) {
                    setState(() {
                      _isOptional = value!;
                    });
                  },
                ),
                const Text("Is Optional?"),
                TextButton(
                    onPressed: () {
                      if (fieldNameController.text.isEmpty) return;
                      setState(() {
                        structs[fieldNameController.text] =
                            _FieldType(type, _isOptional);
                      });
                    },
                    child: const Text("Add Field"))
              ],
            ),
            SizedBox(
              height: 500,
              child: ListView(
                children: _popStructs(),
              ),
            ),
            TextButton(onPressed: _createLayout, child: const Text("Create"))
          ],
        ),
      ),
    );
  }

  Future<void> _createLayout() async {
    if (titleController.text.isEmpty) return;

    Map<String, dynamic> structMap = {};

    structs.forEach((key, value) {
      structMap[key] = {
        "fieldType": value.fieldType,
        "isOptional": value.isOptional
      };
    });

    var sp = Provider.of<ServiceProvider>(context, listen: false);
    var up = Provider.of<UserProvider>(context, listen: false);
    await sp.createTemplate(up.jwtToken!, titleController.text, structMap);
  }

  List<Widget> _popStructs() {
    List<Widget> widgets = [];
    structs.forEach((key, value) {
      var a = Row(children: [
        Text("$key :  "),
        Text("Type : ${value.fieldType}"),
      ]);
      widgets.add(a);
    });
    return widgets;
  }
}

class _FieldType {
  String fieldType;
  bool isOptional;

  _FieldType(this.fieldType, this.isOptional);
}
