// ignore_for_file: file_names

import 'package:ui/service/DirService.dart';
import 'package:ui/service/UserSevice.dart';

class ServiceProvider {
  UserService userService = UserService();
  DirService dirService = DirService();
}
