// ignore_for_file: prefer_const_constructors, prefer_const_literals_to_create_immutables, file_names

import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class PageLogin extends StatefulWidget {
  const PageLogin({Key? key}) : super(key: key);

  @override
  State<PageLogin> createState() => _PageLoginState();
}

class _PageLoginState extends State<PageLogin> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          Text(
            "Kuku's NOTTY NOTE",
            style: GoogleFonts.bebasNeue(
                fontWeight: FontWeight.bold, fontSize: 30),
          )
        ],
      ),
    );
  }
}
