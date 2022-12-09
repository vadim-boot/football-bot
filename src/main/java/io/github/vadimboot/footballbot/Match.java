package io.github.vadimboot.footballbot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@DynamoDbBean
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Match {
    private String id;
    private LocalDate date;
    private String chatId;
    private String description;
    private Integer mainMsgId;
    private int playersLimit;
    private int keepersLimit;
    private int numberOfTeams;
    private boolean autoLimit;
    private boolean cheat;
    private boolean booking;
    private List<FootballUser> readyPlayers = new ArrayList<>();
    private List<FootballUser> readyKeepers = new ArrayList<>();
    private List<FootballUser> skipPlayers = new ArrayList<>();

    @DynamoDbPartitionKey
    @DynamoDbAttribute("pk")
    public String getId() {
        return id;
    }
}
