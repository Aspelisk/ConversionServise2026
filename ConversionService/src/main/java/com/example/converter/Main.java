import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Main extends Application {

    private Label statusLabel;
    private ProgressBar progressBar;
    private File selectedFile;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MP3 to WAV Converter");

        // Создаем элементы интерфейса
        Button selectButton = new Button("1. Выбрать MP3 файл");
        Button convertButton = new Button("2. Конвертировать в WAV");
        statusLabel = new Label("Статус: Ожидание файла...");
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setVisible(false); // Скрыт по умолчанию

        // Логика кнопки выбора файла
        selectButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Открыть MP3 файл");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("MP3 файлы", "*.mp3")
            );

            selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                statusLabel.setText("Выбран: " + selectedFile.getName());
                convertButton.setDisable(false);
            } else {
                statusLabel.setText("Файл не выбран");
                convertButton.setDisable(true);
            }
        });

        // Логика кнопки конвертации
        convertButton.setOnAction(e -> {
            if (selectedFile != null) {
                convertButton.setDisable(true);
                selectButton.setDisable(true);
                progressBar.setVisible(true);

                // ИСПРАВЛЕНО: устанавливаем -1 для бесконечной анимации
                progressBar.setProgress(-1);

                // Запускаем конвертацию в отдельном потоке
                new Thread(() -> {
                    try {
                        convertMp3ToWav(selectedFile);
                        statusLabel.setText("Готово! Файл сохранен рядом с оригиналом.");
                    } catch (Exception ex) {
                        statusLabel.setText("Ошибка: " + ex.getMessage());
                        ex.printStackTrace();
                    } finally {
                        // ИСПРАВЛЕНО: возвращаем прогресс в 0
                        progressBar.setProgress(0);
                        progressBar.setVisible(false);
                        convertButton.setDisable(false);
                        selectButton.setDisable(false);
                    }
                }).start();
            }
        });

        convertButton.setDisable(true);

        // Собираем интерфейс
        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");
        root.getChildren().addAll(selectButton, convertButton, progressBar, statusLabel);

        Scene scene = new Scene(root, 400, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Метод конвертации
     */
    private void convertMp3ToWav(File mp3File) throws UnsupportedAudioFileException, IOException {
        // 1. Получаем путь для нового файла (заменяем .mp3 на .wav)
        String newPath = mp3File.getAbsolutePath().replace(".mp3", ".wav");
        File wavFile = new File(newPath);

        // 2. Открываем входной поток.
        // Библиотека MP3SPI позволит прочитать MP3
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(mp3File);

        // 3. Записываем в файл формата WAV
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavFile);

        audioInputStream.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}