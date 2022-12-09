package io.github.vadimboot.footballbot;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MatchServiceListDB {
    private List<Match> matches = new ArrayList<>();

    public Match findMatchByChatAndMsg(String chatId, Integer msgId) {
        Optional<Match> optional = matches.stream()
                .filter(m -> m.getMainMsgId() != null)
                .filter(m ->
                        m.getChatId().equals(chatId) &&
                                m.getMainMsgId().equals(msgId))
                .findFirst();

        return optional.orElse(null);
    }

    public Match findById(String id) {
        Optional<Match> optional = matches.stream()
                .filter(m -> m.getId().equals(id))
                .findAny();

        return optional.orElse(null);
    }

    public void deleteById(String id) {
        matches.removeIf(m -> m.getId().equals(id));
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void add(Match match) {
        if (matches.stream().noneMatch(m -> m.getId().equals(match.getId()))) {
            matches.add(match);
        } else {
            System.out.println("Ошибка при добавлении матча: С таким ID уже есть.");
        }
    }
}
