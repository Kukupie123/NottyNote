//This is similar to bookmark model but here the data's type will be defined as well and will be stored in solidData
import 'package:ui/models/BookmarkModel.dart';
import 'package:ui/models/BookmarkSolidField.dart';
import 'package:ui/models/TemplateModel.dart';

class BookmarkSolidModel extends BookmarkModel {
  final Map<String, BookmarkSolidField> solidData = {};

  BookmarkSolidModel(TemplateModel templateModel, BookmarkModel bookmarkModel)
      : super(
            bookmarkModel.id,
            bookmarkModel.creatorID,
            bookmarkModel.templateID,
            bookmarkModel.dirID,
            bookmarkModel.name,
            bookmarkModel.data) {
    //Iterate over the template struct
    templateModel.struct.forEach((fieldName, templateField) {
      print("fieldName is $fieldName and type is ${templateField.fieldType}");
      var value = data[fieldName];
      String type = templateField.fieldType;

      switch (type) {
        case "TEXT":
          value = value as String;
          break;
        case "LIST_TEXT":
          value = value as List<dynamic>;
          for (int i = 0; i < value.length; i++) {
            value[i] = value[i] as String;
          }
          break;
        case "LINK":
          value = value as String;
          break;
        case "LIST_LINK":
          value = value as List<dynamic>;
          for (int i = 0; i < value.length; i++) {
            value[i] = value[i] as String;
          }
          break;
      }

      solidData[fieldName] =
          BookmarkSolidField(type, templateField.isOptional, value);
    });
  }
}
