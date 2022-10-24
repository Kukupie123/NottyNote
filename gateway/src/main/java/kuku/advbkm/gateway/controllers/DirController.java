package kuku.advbkm.gateway.controllers;


import kuku.advbkm.gateway.models.ReqRespBodies.RequestDirCreate;
import kuku.advbkm.gateway.models.ReqRespModel.ReqResp;
import kuku.advbkm.gateway.service.DbDirService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController()
@RequestMapping("/api/v1/gate/dir/")
public class DirController {

    private final DbDirService dbService;

    public DirController(DbDirService dbService) {
        this.dbService = dbService;
    }

    /**
     * Returns the ID of the new folder
     */
    public @PostMapping("/create")
    Mono<ResponseEntity<ReqResp<String>>> createDir(@RequestBody RequestDirCreate reqDir) {
        return dbService.createDir(reqDir.getName(), reqDir.isPublic(), reqDir.getParent());
    }
}
