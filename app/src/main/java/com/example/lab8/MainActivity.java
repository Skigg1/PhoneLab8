package com.example.lab8;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText editRus, editMath, editInform, editSocial, editChemistry,
            editPhysics, editEng, editGeo;
    private Button buttonCalculate, buttonClear;
    private ProgressBar progressBar;
    private TextView textResults, textError;

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final String apiUrl = "https://nti.urfu.ru/ege-calc/json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        setupClickListeners();
    }

    private void initViews() {
        editRus = findViewById(R.id.editRus);
        editMath = findViewById(R.id.editMath);
        editInform = findViewById(R.id.editInform);
        editSocial = findViewById(R.id.editSocial);
        editChemistry = findViewById(R.id.editChemistry);
        editPhysics = findViewById(R.id.editPhysics);
        editEng = findViewById(R.id.editEng);
        editGeo = findViewById(R.id.editGeo);

        buttonCalculate = findViewById(R.id.buttonCalculate);
        buttonClear = findViewById(R.id.buttonClear);

        progressBar = findViewById(R.id.progressBar);
        textResults = findViewById(R.id.textResults);
        textError = findViewById(R.id.textError);
    }

    private void setupClickListeners() {
        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    calculateEgeResults();
                }
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllFields();
            }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;

        String rusText = editRus.getText().toString();
        String mathText = editMath.getText().toString();

        if (rusText.isEmpty()) {
            editRus.setError("Это поле обязательно для заполнения");
            isValid = false;
        } else {
            editRus.setError(null);
        }

        if (mathText.isEmpty()) {
            editMath.setError("Это поле обязательно для заполнения");
            isValid = false;
        } else {
            editMath.setError(null);
        }

        EditText[] fields = {
                editRus, editMath, editInform, editSocial,
                editChemistry, editPhysics, editEng, editGeo
        };

        for (EditText field : fields) {
            String text = field.getText().toString();

            if (!text.isEmpty()) {
                try {
                    int score = Integer.parseInt(text);
                    if (score < 0 || score > 100) {
                        field.setError("Баллы должны быть от 0 до 100");
                        isValid = false;
                    } else {
                        field.setError(null);
                    }
                } catch (NumberFormatException e) {
                    field.setError("Введите число");
                    isValid = false;
                }
            }
        }

        return isValid;
    }

    private int getScoreOrZero(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void calculateEgeResults() {

        progressBar.setVisibility(View.VISIBLE);
        textResults.setVisibility(View.GONE);
        textError.setVisibility(View.GONE);

        EgeRequest requestData = new EgeRequest(
                getScoreOrZero(editMath.getText().toString()),
                getScoreOrZero(editRus.getText().toString()),
                getScoreOrZero(editInform.getText().toString()),
                getScoreOrZero(editSocial.getText().toString()),
                getScoreOrZero(editChemistry.getText().toString()),
                getScoreOrZero(editPhysics.getText().toString()),
                getScoreOrZero(editEng.getText().toString()),
                getScoreOrZero(editGeo.getText().toString())
        );

        String json = gson.toJson(requestData);
        System.out.println("Отправляемый JSON: " + json);

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, json);

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        textError.setVisibility(View.VISIBLE);
                        textError.setText("Ошибка сети: " + e.getMessage());
                        Toast.makeText(
                                MainActivity.this,
                                "Ошибка подключения к серверу",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body() != null ?
                        response.body().string() : null;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful()) {
                            System.out.println("Полученный ответ: " + responseBody);
                            showDirectionsFromResponse(responseBody);
                        } else {
                            textError.setVisibility(View.VISIBLE);
                            textError.setText("Ошибка сервера: " + response.code());
                            Toast.makeText(
                                    MainActivity.this,
                                    "Ошибка сервера: " + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });
            }
        });
    }

    private void showDirectionsFromResponse(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            textResults.setVisibility(View.VISIBLE);
            textResults.setText("😔 Нет подходящих направлений");
            return;
        }

        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(responseBody);

            List<String> directions = new ArrayList<>();

            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonElement item = jsonArray.get(i);
                    if (item.isJsonObject()) {
                        JsonObject jsonObject = item.getAsJsonObject();

                        String directionName = getFieldValue(jsonObject,
                                new String[]{"name", "speciality", "specialty", "title",
                                        "direction", "program", "specialty_name"});

                        if (!directionName.isEmpty()) {
                            directions.add(directionName);
                        }
                    }
                }
            }

            if (directions.isEmpty()) {
                textResults.setVisibility(View.VISIBLE);
                textResults.setText("По вашим баллам нет подходящих направлений\n\n" + "Ответ сервера:\n" + responseBody);
            } else {
                StringBuilder result = new StringBuilder("🎓 Подходящие направления:\n\n");
                for (String direction : directions) {
                    result.append(direction).append("\n");
                }
                textResults.setVisibility(View.VISIBLE);
                textResults.setText(result.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            textResults.setVisibility(View.VISIBLE);
            textResults.setText("Ошибка обработки ответа\n\n" + "Полученный ответ:\n" + responseBody);
        }
    }

    private String getFieldValue(JsonObject jsonObject, String[] fieldNames) {
        for (String fieldName : fieldNames) {
            if (jsonObject.has(fieldName)) {
                return jsonObject.get(fieldName).getAsString();
            }
        }
        return "";
    }

    private void clearAllFields() {

        editRus.setText("");
        editMath.setText("");
        editInform.setText("");
        editSocial.setText("");
        editChemistry.setText("");
        editPhysics.setText("");
        editEng.setText("");
        editGeo.setText("");

        editRus.setError(null);
        editMath.setError(null);
        editInform.setError(null);
        editSocial.setError(null);
        editChemistry.setError(null);
        editPhysics.setError(null);
        editEng.setError(null);
        editGeo.setError(null);

        textResults.setVisibility(View.GONE);
        textError.setVisibility(View.GONE);
    }
}