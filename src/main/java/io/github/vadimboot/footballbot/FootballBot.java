package io.github.vadimboot.footballbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.toIntExact;

@Component
public class FootballBot extends TelegramLongPollingBot {

    private final Long VADIM_CHAT_ID = 310312880L;
    @Value("#{systemEnvironment['FOOTBALL_BOT_TOKEN']}")
    private String botToken;

    private InlineKeyboardMarkup markupInline = null;
    private MatchServiceListDB matchServiceListDB;
    private MatchServiceBooking matchBooking;
    private MatchServiceDynamoDB matchServiceDynamoDB;

    @Autowired
    public FootballBot(MatchServiceListDB matchServiceListDB,
                       MatchServiceBooking matchServiceBooking,
                       MatchServiceDynamoDB matchServiceDynamoDB) {
        this.matchServiceListDB = matchServiceListDB;
        this.matchBooking = matchServiceBooking;
        this.matchServiceDynamoDB = matchServiceDynamoDB;
    }

    @Override
    public String getBotUsername() {
        return "MersinFootballBot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getChatId().equals(VADIM_CHAT_ID)) {
            if (update.getMessage().getText().equals("/list")) {
                SendMessage message = new SendMessage();
                message.setChatId(VADIM_CHAT_ID);
                message.setText(matchServiceListDB.getMatches().stream()
                        .map(m -> "id = " + m.getId()
                                + "\ndesc = " + m.getDescription()
                                + "\nbooking = " + m.isBooking())
                        .collect(Collectors.joining("\n\n")));
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (update.getMessage().getText().contains("/start")) {

                String msgText = update.getMessage().getText();
                Match match = matchServiceListDB.findById(msgText.replace("/start", ""));
                if (match == null) {
                    return;
                }

                match.setBooking(true);
                SendMessage message = new SendMessage();
                message.setChatId(match.getChatId());
                message.setText(matchBooking.getCurrentMsg(match));
                message.setReplyMarkup(getInlineKeyboardMarkup());
                try {
                    Integer message_id = execute(message).getMessageId();
                    match.setMainMsgId(message_id);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (update.getMessage().getText().contains("/refresh")) {
                String msgText = update.getMessage().getText();
                Match match = matchServiceListDB.findById(msgText.replace("/refresh", ""));
                if (match == null) {
                    return;
                }

                EditMessageText new_message = new EditMessageText();
                new_message.setChatId(match.getChatId());
                new_message.setMessageId(toIntExact(match.getMainMsgId()));
                new_message.setText(matchBooking.getCurrentMsg(match));
                new_message.setReplyMarkup(getInlineKeyboardMarkup());

                try {
                    execute(new_message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (update.getMessage().getText().contains("/save")) {
                String msgText = update.getMessage().getText();
                matchServiceDynamoDB.saveToDB(msgText.replace("/save", ""));
            } else if (update.getMessage().getText().contains("/load")) {
                String msgText = update.getMessage().getText();
                matchServiceDynamoDB.loadFromDB(msgText.replace("/load", ""));
            } else if (update.getMessage().getText().contains("/delete")) {
                String msgText = update.getMessage().getText();
                matchServiceListDB.deleteById(msgText.replace("/delete", ""));
            } else if (update.getMessage().getText().contains("/pause")) {
                String msgText = update.getMessage().getText();
                Match match = matchServiceListDB.findById(msgText.replace("/pause", ""));
                match.setBooking(!match.isBooking());
            }
        } else if (update.hasCallbackQuery()) {

            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            Integer msgId = update.getCallbackQuery().getMessage().getMessageId();

            Match match = matchServiceListDB.findMatchByChatAndMsg(chatId, msgId);

            if (match == null) {
                return;
            }

            String call_data = update.getCallbackQuery().getData();

            User user = update.getCallbackQuery().getFrom();

            if (call_data.equals("play")) {
                matchBooking.handlePlayCallback(match, new FootballUser(user));
            } else if (call_data.equals("skip")) {
                matchBooking.handleSkipCallback(match, new FootballUser(user));
            } else if (call_data.equals("keeper")) {
                matchBooking.handleKeeperCallback(match, new FootballUser(user));
            }

            EditMessageText new_message = new EditMessageText();
            new_message.setChatId(match.getChatId());
            new_message.setMessageId(toIntExact(match.getMainMsgId()));
            new_message.setText(matchBooking.getCurrentMsg(match));
            new_message.setReplyMarkup(getInlineKeyboardMarkup());

            try {
                if (!update.getCallbackQuery().getMessage().getText().trim().equalsIgnoreCase(new_message.getText().trim())) {
                    execute(new_message);
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private InlineKeyboardMarkup getInlineKeyboardMarkup() {
        if (markupInline != null) {
            return markupInline;
        }

        markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> firstRowInline = new ArrayList<>();
        InlineKeyboardButton buttonPlay = new InlineKeyboardButton();
        buttonPlay.setText("Играю");
        buttonPlay.setCallbackData("play");
        firstRowInline.add(buttonPlay);

        InlineKeyboardButton buttonSkip = new InlineKeyboardButton();
        buttonSkip.setText("Пропускаю");
        buttonSkip.setCallbackData("skip");
        firstRowInline.add(buttonSkip);

        rowsInline.add(firstRowInline);

        List<InlineKeyboardButton> secondRowInline = new ArrayList<>();

        InlineKeyboardButton buttonKeeper = new InlineKeyboardButton();
        buttonKeeper.setText("На ворота");
        buttonKeeper.setCallbackData("keeper");
        secondRowInline.add(buttonKeeper);

        rowsInline.add(secondRowInline);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }
}