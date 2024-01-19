package com.example.midnightwerewolfmasterspeaking;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private EditText timeToSpeak, timeEndGame;
    private TextView cronometer;
    private Button btnStart, btnStop;
    private ArrayList<String> textList = new ArrayList<>();
    private TextToSpeech textToSpeech;
    private Handler handler;
    private int currentIndex = 0;
    private CountDownTimer countDownTimer;
    private Runnable myRunnable;
    long durationMessageInMillis;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.getDefault());
                }
            }
        });

        cronometer = findViewById(R.id.txt_crono);
        timeEndGame = findViewById(R.id.edt_minutes);
        timeToSpeak = findViewById(R.id.edt_seconds);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);

        // Inicia recurso para rodar a cada intervalo de tempo
        handler = new Handler();

        // Adiciona as falas
        textList.add("Todos fechem os olhos. Werewolfs abram os olhos e se reconheçam, se estiver sozinho você pode olhar uma carta do centro");
        textList.add("Minion abra os olhos, Werewolfs façam sinal de positivo. Minion após identificar os werewolfs feche os olhos");
        textList.add("Meinsons abram os olhos e se reconheçam, após se reconhecerem fechem os olhos");
        textList.add("Sier abra os olhos você pode olhar a carta de uma pessoa qualquer ou olhar duas cartas do centro. Após a ação feche os olhos.");
        textList.add("Róber faça sua ação, você pode trocar sua carta com outra pessoa e olhar o personagem que recebeu. Após a ação feche os olhos.");
        textList.add("Troubloumaiker faça sua ação, você pode trocar a carta de duas pessoas, você não pode olhar as cartas. Após a ação feche os olhos.");
        textList.add("Todos abram os olhos");

//         Inicia o botão start
        btnStart.setOnClickListener(v -> {
            startGame();
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopGame();
            }
        });

    }

    private void stopGame() {
        currentIndex = 0;
        handler.removeCallbacks(myRunnable);
        btnStart.setVisibility(View.VISIBLE);
        cronometer.setText("00:00");
        countDownTimer.cancel();
    }

    void startGame() {
        btnStart.setVisibility(View.INVISIBLE);
        int seconds = Integer.parseInt(timeToSpeak.getText().toString());

        int minutes = Integer.parseInt(timeEndGame.getText().toString());
        String textInicialCrono = minutes + ":00";

        cronometer.setText(textInicialCrono);

        // Instanciando um Runnable para poder cancelar ele depois
        myRunnable = new Runnable() {
            @Override
            public void run() {
                if(currentIndex < textList.size()) {
                    String message = textList.get(currentIndex);
                    speakMessage(message);
                    durationMessageInMillis = durationMenssageInMillis(message);
                    currentIndex++;
                    // Agendar o próximo discurso após a duração da mensagem atual
                    handler.postDelayed(this, (durationMessageInMillis) + (seconds * 1000L));
                }
            }
        };

        // Agendamento inicial para iniciar o processo
        handler.postDelayed(myRunnable, 0);

        startCountDown(this, minutes);
    }

    private void speakMessage(String message) {
        Log.d(TAG, "entrei no speakMessage");
        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);

    }

    private void startCountDown(Context context, int minutes) {
        long miliseconds = minutes * 60 * 1000L;

        int textColor = ContextCompat.getColor(context, R.color.red);
        cronometer.setTextColor(textColor);

        countDownTimer = new CountDownTimer(miliseconds, 1000) {

            public void onTick(long millisUntilFinished) {
                updateCountDownText(millisUntilFinished);
            }

            public void onFinish() {

                cronometer.setText("done!");
                speakMessage("O tempo de jogo acabou, decidam a votação.");
            }
        }.start();

    }

    private void updateCountDownText(Long milisUntilFinished) {
        int minutes = (int) (milisUntilFinished / 1000 / 60);
        int seconds = (int) (milisUntilFinished / 1000 % 60);

        @SuppressLint("DefaultLocale")
        String timeLeft = String.format("%02d:%02d", minutes, seconds);

        cronometer.setText(timeLeft);

    }

    private long durationMenssageInMillis(String message) {
        int words = message.length();
        int wordsPerMinute = 900; //Eu percebi que uma frase de length = 100 durou 7 segundos,
        // então por regra de 3 em 1 minuto dá pra ler um length de 900. Dividindo por 900 encontramos o tempo em
        // segundos para ler nossa frase

        return (long) ((words / (double) wordsPerMinute) * 60 * 1000);
    }
}