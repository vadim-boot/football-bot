package io.github.vadimboot.footballbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

@SpringBootApplication
public class FootballBotApplication implements CommandLineRunner {

    @Autowired
    MatchServiceListDB matchServiceListDB;

    @Autowired
    FootballBot footballBot;

    public static void main(String[] args) {
        SpringApplication.run(FootballBotApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        matchServiceListDB.add(Match.builder()
                .description("Супер голсование!")
                .playersLimit(21)
                .keepersLimit(3)
                .id("9999")
                .chatId("310312880")
                .numberOfTeams(3)
                .cheat(false)
                .readyKeepers(new ArrayList<>())
                .readyPlayers(new ArrayList<>())
                .skipPlayers(new ArrayList<>())
                .build());

        matchServiceListDB.add(Match.builder()
                .description("Среда, Тедже, 7 декабря, 18.00-20.00")
                .playersLimit(21)
                .keepersLimit(3)
                .id("1")
                .chatId("-1001686876091")
                .numberOfTeams(3)
                .cheat(true)
                .readyKeepers(new ArrayList<>())
                .readyPlayers(new ArrayList<>())
                .skipPlayers(new ArrayList<>())
                .build());

        matchServiceListDB.add(Match.builder()
                .description("Воскресенье, Соли, 11 декабря, 10.00-12.00")
                .playersLimit(21)
                .keepersLimit(3)
                .id("2")
                .chatId("-1001686876091")
                .cheat(false)
                .numberOfTeams(3)
                .readyKeepers(new ArrayList<>())
                .readyPlayers(new ArrayList<>())
                .skipPlayers(new ArrayList<>())
                .build());

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(footballBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
