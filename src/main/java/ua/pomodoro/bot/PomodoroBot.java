package ua.pomodoro.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PomodoroBot extends TelegramLongPollingBot {
    enum TimerType{
        WORK,
        BREAK
    }

    private ConcurrentHashMap<Long, PomodoroUser> users = new ConcurrentHashMap<>();

    public static PomodoroBot S;

    public PomodoroBot(){
        S = this;
    }

    @Override
    public String getBotUsername() {
        return "PomodorikBot";
    }

    @Override
    public String getBotToken() {
        return "5331052717:AAGTFS9AAC9vTA9WiNRQnSeNwqhGUGNZw6U";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            var chatId = update.getMessage().getChatId();
            String[] cmd = update.getMessage().getText().split(" ");
            switch (cmd[0]) {
                case "/start" -> startCommand(chatId);
                case "/help" -> helpCommand(chatId);
                case "/set" -> setCommand(chatId, cmd);
                case "/chrono" -> chronoCommand(chatId, cmd[1]);
                case "/stop" -> stopCommand(chatId);
                default -> sendMessage(chatId, "Команда не розпізнана");
            }
        }
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage msg = new SendMessage(chatId.toString(), text);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public  void checkTimer() throws InterruptedException {
        while (true){
            for (Map.Entry<Long, PomodoroUser> user: users.entrySet()) {
                if(user.getValue().GetIsTimerRunning() && user.getValue().GetEndTime().compareTo(Instant.now()) < 0){
                    user.getValue().TimerEnd();
                }
            }
            Thread.sleep(1000l);
        }
    }

    private void startCommand(Long chatId) {
        //Create new user's object
        if(!users.containsKey(chatId))
            users.put(chatId, new PomodoroUser(chatId));

        sendMessage(chatId, """
                Привіт! Я бот, що допомагає керувати часом за допомогою принципа Помодоро.
                Пропиши /help, щоб отримати список команд.
                """);
    }

    private void setCommand(Long chatId, String[] cmd){
        if(cmd.length < 5){
            sendMessage(chatId, "Недостатньо аргументів!");
            return;
        }

        int[] args = new int[4];
        for (int i = 0; i < 4; i++){
            args[i] = Integer.parseInt(cmd[i +1]);
        }

        //Set user's timer
        users.get(chatId).SetTimer(args[0], args[1], args[2], args[3]);
    }

    private void chronoCommand(Long chatId, String unit){
        var user = users.get(chatId);
        if(unit.equals("хв.")){
            user.chronoUnit = ChronoUnit.MINUTES;
            sendMessage(chatId, "Хвилини встановлено одиницями виміру");
        }
        else if(unit.equals("с.")){
            user.chronoUnit = ChronoUnit.SECONDS;
            sendMessage(chatId, "Секунди встановлено одиницями виміру");
        }
        else{
            sendMessage(chatId, "Неможливий аргумент!");
        }
    }

    private void stopCommand(Long chatId){
        users.get(chatId).StopTimer();
        sendMessage(chatId, "Таймер зупинено!");
    }

    private void helpCommand(Long chatId){
        sendMessage(chatId, """
                    /set [тривалість роботи] [тривалість відпочинку] [кіл-ть повторень циклу] [множник часу роботи] - запускає новий таймер
                    /chrono [одиниця виміру часу], допустимі аргументи: хв., с. - змінює одиницю виміру часу
                    /stop - зупиняє таймер""");
    }
}
