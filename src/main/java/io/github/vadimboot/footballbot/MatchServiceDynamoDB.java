package io.github.vadimboot.footballbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;

@Service
public class MatchServiceDynamoDB {

    private MatchServiceListDB matchServiceListDB;

    @Autowired
    public MatchServiceDynamoDB(MatchServiceListDB matchServiceListDB) {
        this.matchServiceListDB = matchServiceListDB;
    }

    private DynamoDbEnhancedClient getEnhancedClient() {
        Region region = Region.US_EAST_1;
        DynamoDbClient ddb = DynamoDbClient.builder()
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .region(region)
                .build();

        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();
    }

    public void saveToDB(String id) {
        Match match = matchServiceListDB.findById(id);
        if (match == null) {
            return;
        }

        DynamoDbEnhancedClient enhancedClient = getEnhancedClient();
        try {
            DynamoDbTable<Match> matchTable = enhancedClient.table("Football", TableSchema.fromBean(Match.class));
            matchTable.putItem(match);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
    }

    public void loadFromDB(String id) {
        DynamoDbEnhancedClient enhancedClient = getEnhancedClient();
        try {
            DynamoDbTable<Match> table = enhancedClient.table("Football", TableSchema.fromBean(Match.class));
            Key key = Key.builder()
                    .partitionValue(id)
                    .build();
            Match matchFromDB = table.getItem(r -> r.key(key));

            Optional<Match> optional = matchServiceListDB.getMatches().stream()
                    .filter(m -> m.getId().equals(matchFromDB.getId()))
                    .findAny();

            if (optional.isPresent()) {
                Match existingMatch = optional.get();
                existingMatch.setReadyPlayers(matchFromDB.getReadyPlayers());
                existingMatch.setReadyKeepers(matchFromDB.getReadyKeepers());
                existingMatch.setSkipPlayers(matchFromDB.getSkipPlayers());
            } else {
                matchServiceListDB.add(matchFromDB);
            }
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
    }
}
