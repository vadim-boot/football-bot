package io.github.vadimboot.footballbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatchServiceBooking {

    MatchServiceListDB matchServiceListDB;

//    private final static List<Long> KNOWN_KEEPERS_ID = Arrays.asList(1652759709L   //Алексей Янковский
//            , 1861334005L                                                   //Евгений Швецов
////            , 1009900103L                                                   //Женек Anonimous Userovich
//            , 2071450412L);                                                 //D.Khamidullin

    private final static String MSG_TEMPLATE = "\n\nОснова:\n"
            + "%s\n\n"
            + "Киперы:\n"
            + "%s\n\n"
            + "Полевые в резерве:\n"
            + "%s\n\n"
            + "Киперы в резерве:\n"
            + "%s\n\n"
            + "Пропускаю:\n"
            + "%s";

    private Map<Integer, FootballUser> cheaters = new HashMap<>();

    @Autowired
    public MatchServiceBooking(MatchServiceListDB matchServiceListDB) {
        this.matchServiceListDB = matchServiceListDB;
        initCheaters();
    }

    public void initCheaters() {

        FootballUser vadim = new FootballUser();
        vadim.setId(310312880L);
        vadim.setUserName("vadim34k");
        vadim.setFirstName("Vadim");
        vadim.setLastName("M");

        FootballUser alexey = new FootballUser();
        alexey.setId(270631903L);
        alexey.setUserName("mcaley");
        alexey.setFirstName("Alexey");
        alexey.setLastName("McAley");

        FootballUser konstantin = new FootballUser();
        konstantin.setId(209965884L);
        konstantin.setUserName("klozbinev87");
        konstantin.setFirstName("Konstantin");
        konstantin.setLastName("Lozbinev");

        cheaters.put(6, vadim);
        cheaters.put(11, alexey);
        cheaters.put(15, konstantin);
    }

    private int calcPlayersLimit(Match match) {
        int diff = 0;
        if (2 == match.getNumberOfTeams()) {
            diff = switch (Math.min(match.getKeepersLimit(), match.getReadyKeepers().size())) {
                case 0 -> 0;
                case 1 -> 1;
                default -> 2;
            };
        } else if (3 == match.getNumberOfTeams()) {
            diff = switch (Math.min(match.getKeepersLimit(), match.getReadyKeepers().size())) {
                case 0 -> 0;
                case 1 -> 1;
                default -> 3;
            };
        } else if (4 == match.getNumberOfTeams()) {
            diff = switch (Math.min(match.getKeepersLimit(), match.getReadyKeepers().size())) {
                case 0 -> 0;
                case 1 -> 1;
                case 2 -> 2;
                case 3 -> 3;
                default -> 4;
            };
        }
        return match.getPlayersLimit() - diff;
    }

    public void handlePlayCallback(Match match, FootballUser user) {
        if (match.isBooking()) {
            addUser(match.getReadyPlayers(), user);
            if (match.isCheat()) {
                int cheaterIndex = match.getReadyPlayers().size();
                if (cheaters.containsKey(cheaterIndex)) {
                    addUser(match.getReadyPlayers(), cheaters.get(cheaterIndex));
                    removeUser(match.getSkipPlayers(), cheaters.get(cheaterIndex));
                    cheaters.remove(cheaterIndex);
                }
            }
            removeUser(match.getSkipPlayers(), user);
            removeUser(match.getReadyKeepers(), user);
        }
    }

    public void handleSkipCallback(Match match, FootballUser user) {
        if (match.isBooking()) {
            removeUser(match.getReadyKeepers(), user);
            removeUser(match.getReadyPlayers(), user);
            addUser(match.getSkipPlayers(), user);
        }
    }

    public void handleKeeperCallback(Match match, FootballUser user) {
        if (match.isBooking()) {
            removeUser(match.getSkipPlayers(), user);
            removeUser(match.getReadyPlayers(), user);
            addUser(match.getReadyKeepers(), user);
        }
    }

    private void addUser(List<FootballUser> users, FootballUser newUser) {
        if (users.stream().noneMatch(u -> u.getId().equals(newUser.getId()) ||
                u.getUserName().equals(newUser.getUserName()))) {
            users.add(newUser);
        }
    }

    private void removeUser(List<FootballUser> users, FootballUser user) {
        users.removeIf(u -> u.getId().equals(user.getId()) || u.getUserName().equals(user.getUserName()));
    }

    private String userToString(FootballUser user) {
        String firstName = user.getFirstName();
        String lastName = user.getLastName() == null ? "" : user.getLastName();
        String userName = user.getUserName() == null ? "()" : " (@" + user.getUserName() + ")";
        String s = firstName + " " + lastName + " " + userName;
        return s.trim();
    }

    private String arrayUserToString(List<FootballUser> users) {
        if (users == null || users.size() == 0) {
            return "----";
        }
        int i = 1;
        StringBuilder sb = new StringBuilder();
        for (FootballUser user : users) {
            sb.append(i + ". " + userToString(user));
            sb.append("\n");
            i++;
        }
        return sb.toString().trim();
    }

    public String getCurrentMsg(Match match) {
        int playerLimit = match.isAutoLimit() ? calcPlayersLimit(match) : match.getPlayersLimit();

        List<FootballUser> playersInGame = match.getReadyPlayers().subList(0,
                Math.min(playerLimit, match.getReadyPlayers().size()));

        List<FootballUser> keepersInGame = match.getReadyKeepers().subList(0,
                Math.min(match.getKeepersLimit(), match.getReadyKeepers().size()));

        List<FootballUser> playersInReserve = match.getReadyPlayers().size() > playerLimit ?
                match.getReadyPlayers().subList(playerLimit, match.getReadyPlayers().size()) :
                Collections.emptyList();

        List<FootballUser> keepersInReserve = match.getReadyKeepers().size() > match.getKeepersLimit() ?
                match.getReadyKeepers().subList(match.getKeepersLimit(), match.getReadyKeepers().size()) :
                Collections.emptyList();

        return match.getDescription() + String.format(MSG_TEMPLATE,
                arrayUserToString(playersInGame),
                arrayUserToString(keepersInGame),
                arrayUserToString(playersInReserve),
                arrayUserToString(keepersInReserve),
                arrayUserToString(match.getSkipPlayers()));
    }
}
