package kuku.advbkm.gateway.controllers;


import kuku.advbkm.gateway.models.DirectoryModel;
import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import kuku.advbkm.gateway.service.DbDirService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@CrossOrigin(value = "**") //Allow all origin, all headers, All Http methods.
@Log4j2
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
    Mono<ResponseEntity<ReqResp<String>>> createDir(@RequestBody DirectoryModel reqDir, @RequestHeader("Authorization") String authHeader) {

        //Why no safety checks? Because to be access this endpoint you have to be authenticated in the first place
        String jwtToken = authHeader.substring(7);

        return dbService.createDir(reqDir, jwtToken);
    }


    public @DeleteMapping("/{id}")
    Mono<ResponseEntity<ReqResp<Boolean>>> deleteDir(@PathVariable String id, @RequestHeader("Authorization") String header) {

        String jwtToken = header.substring(7);

        log.info("Delete directory endpoint hit with userID {} and folderID {}", jwtToken, id);

        return dbService.deleteDir(id, jwtToken);
    }

    public @GetMapping("/{id}")
    Mono<ResponseEntity<ReqResp<DirectoryModel>>> getDir(@PathVariable String id, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return dbService.getDir(id, token);
    }

    public @GetMapping("/getChildren/{parentID}")
    Flux<ResponseEntity<ReqResp<DirectoryModel>>> getDirs(@PathVariable String parentID, @RequestHeader("Authorization") String authHeader) {
        String jwtToken = authHeader.substring(7);
        log.info("Get childrenDirs with auth {} and parent ID {}", jwtToken, parentID);
        return dbService.getChildrenDirs(parentID, jwtToken);
    }


}
