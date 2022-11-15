class DirModel {
  final String id;
  final String creatorID;
  final String name;
  final String parentID;
  final List<String> childrenIDs;
  final List<String> bookmarkIDs;

  DirModel(this.id, this.creatorID, this.name, this.parentID, this.childrenIDs,
      this.bookmarkIDs);
}
