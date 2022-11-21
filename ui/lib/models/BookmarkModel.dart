// ignore_for_file: file_names

class BookmarkModel {
  late final String id;
  late final String creatorID;
  late final String templateID;
  late final String dirID;
  late final String name;
  late final Map<String, dynamic> data;

  BookmarkModel(this.id, this.creatorID, this.templateID, this.dirID, this.name,
      this.data);
}
