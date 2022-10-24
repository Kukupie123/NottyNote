package kuku.advbkm.gateway.controllers;


import kuku.advbkm.gateway.models.DirectoryModel;
import kuku.advbkm.gateway.models.ReqResp.ReqResp;
import kuku.advbkm.gateway.service.DbDirService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    Mono<ResponseEntity<ReqResp<String>>> createDir(@RequestBody DirectoryModel reqDir, @RequestHeader("Authorization") String authHeader) {

        //Why no safety checks? Because to be access this endpoint you have to be authenticated in the first place
        String jwtToken = authHeader.substring(7);

        //DEBUG
        System.out.println(String.format("CreateDir request with body %s and userID", reqDir, jwtToken));

        return dbService.createDir(reqDir, jwtToken);
    }
}
