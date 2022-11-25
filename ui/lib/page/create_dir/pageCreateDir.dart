// ignore_for_file: prefer_const_constructors

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:ui/provider/ServiceProvider.dart';
import 'package:ui/provider/UserProvider.dart';

class PageCreateDir extends StatefulWidget {
  final String folderName;
  final String folderID;

  const PageCreateDir(this.folderID, this.folderName, {Key? key})
      : super(key: key);

  @override
  State<PageCreateDir> createState() => _PageCreateDirState();
}

class _PageCreateDirState extends State<PageCreateDir> {
  final StreamController statusSC = StreamController();
  late final Stream statusS;
  bool isPublic = false;
  bool _isCreating = false;
  final TextEditingController _nameController = TextEditingController();

  @override
  void initState() {
    super.initState();
    statusS = statusSC.stream;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        color: Colors.white,
        child: Column(
          children: [
            Text("Going to be under ${widget.folderName}"),
            TextField(
              controller: _nameController,
              decoration: InputDecoration(hintText: "Folder Name"),
            ),
            Row(
              children: [
                Text("Is Public ? "),
                Checkbox(
                    value: isPublic,
                    onChanged: (e) {
                      setState(() {
                        isPublic = e!;
                      });
                    })
              ],
            ),
            TextButton(onPressed: _createDir, child: Text("Create directory"))
          ],
        ),
      ),
    );
  }

  Future<void> _createDir() async {
    if (_isCreating) return;
    _isCreating = true;
    if (_nameController.text.isEmpty) return;
    var serviceProvider = Provider.of<ServiceProvider>(context, listen: false);
    var userService = Provider.of<UserProvider>(context, listen: false);
    String id = await serviceProvider.createDir(
        userService.jwtToken!, _nameController.text, isPublic, widget.folderID);
    _isCreating = false;
  }
}
