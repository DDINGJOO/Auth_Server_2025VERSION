package com.teambiund.bander.auth_server.controller.enums;


import com.teambiund.bander.auth_server.entity.consents_name.ConsentsTable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.teambiund.bander.auth_server.util.data.ConsentTable_init.consentMaps;
import static com.teambiund.bander.auth_server.util.data.ConsentTable_init.consentsAllMaps;

@RestController
@RequestMapping("/api/auth/enums")
public class ConsentsController {

    @GetMapping("/Consents")
    public ResponseEntity<Map<String, ConsentsTable>> getAllConsents(@RequestParam(name = "all") Boolean value
    ) {
        if (value) {
            return new ResponseEntity<>(consentsAllMaps, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(consentMaps, HttpStatus.OK);
        }
    }
}
