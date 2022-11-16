// ignore_for_file: file_names

class BookmarkModel {
  final String id;
  final String creatorID;
  final String templateID;
  final String dirID;
  final String name;
  final Map<String, dynamic> data;

  BookmarkModel(this.id, this.creatorID, this.templateID, this.dirID, this.name,
      this.data);
}
