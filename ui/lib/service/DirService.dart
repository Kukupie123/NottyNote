import 'package:http/http.dart' as http;
import 'package:ui/models/DirectoryModel.dart';
import 'package:ui/models/Response/BaseResponseModel.dart';
import 'package:ui/utils/Utils.dart';

class DirService {
  Future<List<DirModel>> getUserDirs(String jwtToken, String parentID) async {
    String url = URLs.DIR_BASE_URL + URLs.DIR_GET_DIRS(parentID);
    var resp = await http.get(
      Uri.parse(url),
      headers: {"Authorization": jwtToken},
    );
    var baseResp = BaseResponseModel.convertResponseToBaseResponse(resp);
    print("${baseResp.data.runtimeType} is the type of baseResp");
    return baseResp.data as List<DirModel>;
  }
}
