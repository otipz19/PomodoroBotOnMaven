package ua.pomodoro.bot;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class PomodoroUser {
    private Long chatId;
    private boolean isTimerRunning;
    private PomodoroBot.TimerType timerType;
    private Instant endTime;
    private int workTimeDuration;
    private int breakTimeDuration;
    private int repeatsLeft;
    private int multiplier;
    public ChronoUnit chronoUnit;

    public boolean GetIsTimerRunning() { return isTimerRunning; }
    public Instant GetEndTime() { return endTime; }

    public PomodoroUser(long chatId){
        this.chatId = chatId;
        chronoUnit  = ChronoUnit.MINUTES;
    }

    public void SetTimer(int workTimeDuration, int breakTimeDuration, int repeats, int multiplier){
        if(workTimeDuration < 1 || breakTimeDuration < 1 || repeats < 1 || multiplier < 1){
            PomodoroBot.S.sendMessage(chatId, "Неможливі аргументи!");
        }

        this.workTimeDuration = workTimeDuration;
        this.breakTimeDuration = breakTimeDuration;
        this.repeatsLeft = repeats;
        this.multiplier = multiplier;
        isTimerRunning = true;
        timerType = PomodoroBot.TimerType.WORK;
        endTime = Instant.now().plus(workTimeDuration, chronoUnit);

        NotifyUser(true);
    }

    public void TimerEnd(){
        if(timerType == PomodoroBot.TimerType.BREAK){
            repeatsLeft--;
            if(repeatsLeft == 0){
                isTimerRunning = false;
                NotifyUser(false);
                return;
            }
            timerType = PomodoroBot.TimerType.WORK;
            workTimeDuration *= multiplier;
            endTime = Instant.now().plus(workTimeDuration, chronoUnit);
        }
        else{
            timerType = PomodoroBot.TimerType.BREAK;
            endTime = Instant.now().plus(breakTimeDuration, chronoUnit);
        }

        NotifyUser(false);
    }

    public void StopTimer(){
        isTimerRunning = false;
    }

    private void NotifyUser(boolean justStarted){
        String msg = new String();

        String timeUnit = chronoUnit == ChronoUnit.MINUTES ? "хв." : "с.";

        if(justStarted){
            msg = "Розпочато новий цикл роботи-відпочинку!\nТривалість періоду роботи: " + workTimeDuration + timeUnit + "\nТривалість періоду відпочинку: " + breakTimeDuration + timeUnit;
        }
        else if(!isTimerRunning){
            msg = "Вітаю! Помодоро завершено.\nВи можете задати новий цикл роботи-відпочинку використавши команду /set";
        }
        else if(timerType == PomodoroBot.TimerType.WORK){
            msg = "Пройшло " + breakTimeDuration + timeUnit + " Час відпочинку завершено!\nНаступний період триватиме " + workTimeDuration + timeUnit + "\nДо роботи!";
        }
        else{
            msg = "Пройшло " + workTimeDuration + timeUnit + " Час роботи завершено!\nНаступний період триватиме " + breakTimeDuration + timeUnit + "\nГарного відпочинку!";
        }

        PomodoroBot.S.sendMessage(chatId, msg);
    }
}
