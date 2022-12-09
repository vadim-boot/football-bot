package io.github.vadimboot.footballbot;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MatchController {

    private MatchServiceListDB matchServiceListDB;

    @Autowired
    public MatchController(MatchServiceListDB matchServiceListDB) {
        this.matchServiceListDB = matchServiceListDB;
    }

    @GetMapping("/list")
    public List<Match> getMatchList() {
        return matchServiceListDB.getMatches();
    }

    @PostMapping("/add")
    public ResponseEntity add(@RequestBody Match match) {
        matchServiceListDB.add(match);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/update")
    public Match update(@RequestBody Match updateMatch) {
        Match match = matchServiceListDB.findById(updateMatch.getId());
        match.setDescription(updateMatch.getDescription());
        match.setCheat(updateMatch.isCheat());
        if (updateMatch.getPlayersLimit() > 0) {
            match.setPlayersLimit(updateMatch.getPlayersLimit());
        }
        if (updateMatch.getKeepersLimit() > 0) {
            match.setKeepersLimit(updateMatch.getKeepersLimit());
        }
        if (updateMatch.getNumberOfTeams() > 0) {
            match.setNumberOfTeams(updateMatch.getNumberOfTeams());
        }
        return match;
    }

    @GetMapping("/")
    public ResponseEntity defaultResponse() {
        return ResponseEntity.ok().build();
    }
}