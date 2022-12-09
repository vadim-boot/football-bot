package io.github.vadimboot.footballbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.User;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FootballUser {
    private Long id;
    private String firstName;
    private String lastName;
    private String userName;

    public FootballUser(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.userName = user.getUserName() != null ? user.getUserName() : "'";
    }
}
