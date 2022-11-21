import 'TemplateField.dart';

class TemplateModel {
  String id;
  String name;
  String creatorID;
  List<String> bookmarks;
  Map<String, TemplateField> struct; //name of field  : fieldStruct
  TemplateModel(
      this.id, this.name, this.creatorID, this.bookmarks, this.struct);
}
