// ignore_for_file: sized_box_for_whitespace, prefer_const_constructors

import 'package:flutter/material.dart';
import 'package:ui/models/BookmarkSolidModel.dart';

class PageViewBookmark extends StatelessWidget {
  final BookmarkSolidModel bookmark;

  const PageViewBookmark({Key? key, required this.bookmark}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          Text(bookmark.name),
          Container(
            height: MediaQuery.of(context).size.height * 0.9,
            child: ListView(
              children: _populateList(),
            ),
          )
        ],
      ),
    );
  }

  List<Widget> _populateList() {
    List<Widget> widgets = [];
    bookmark.solidData.forEach((fieldName, fieldData) {
      String type = fieldData.fieldType;
      dynamic value = fieldData.value;

      switch (type) {
        case "TEXT":
          value = value as String;
          widgets.add(_simpleRow(fieldName, value));
          break;
        case "LIST_TEXT":
          value = value as List<dynamic>;
          for (int i = 0; i < value.length; i++) {
            value[i] = value[i] as String;
          }
          widgets.addAll(_simpleRows(fieldName, value));
          break;
        case "LINK":
          value = value as String;
          widgets.add(_simpleRow(fieldName, value));
          break;
        case "LIST_LINK":
          value = value as List<dynamic>;
          for (int i = 0; i < value.length; i++) {
            value[i] = value[i] as String;
          }
          widgets.addAll(_simpleRows(fieldName, value));
          break;
      }
    });
    return widgets;
  }

  Widget _simpleRow(String label, String value) {
    return Row(
      children: [
        Padding(
          padding: EdgeInsets.all(10),
          child: Text("$label : "),
        ),
        Padding(
          padding: EdgeInsets.all(10),
          child: Text(" $value"),
        ),
      ],
    );
  }

  List<Widget> _simpleRows(String label, List<dynamic> values) {
    List<String> newValues = [];
    for (dynamic d in values) {
      newValues.add(d.toString());
    }
    return newValues
        .map((e) => Row(
              children: [
                Padding(
                  padding: EdgeInsets.all(10),
                  child: Text("$label : "),
                ),
                Padding(
                  padding: EdgeInsets.all(10),
                  child: Text(" $e"),
                ),
              ],
            ))
        .toList();
  }
}
