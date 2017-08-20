package boggle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;

public class Boggle {

    private static final List<String[]> DICE_FACES;
    private static final Set<String> DICTIONARY_WORDS;
    private static final Set<String> FOUND_WORDS;
    private static JFrame boggleFrame;
    private static JPanel dicePanel;
    private static JButton[][] diceBtns;
    private static JPanel rightPanel;
    private static JPanel wordsPanel;
    private static JTextPane wordsTextPane;
    private static JScrollPane wordsScrollPane;
    private static JPanel rightBottomPanel;
    private static JPanel scrambleBtnPanel;
    private static JButton scrambleBtn;
    private static JPanel timerPanel;
    private static JLabel timerLabel;
    private static JPanel bottomPanel;
    private static JPanel entryPanel;
    private static TitledBorder entryTitle;
    private static JLabel currentWordLabel;
    private static JButton submitWordButton;
    private static JPanel scorePanel;
    private static JLabel scoreLabel;
    private static Timer timer;

    static {
        DICE_FACES = getDiceFaces();
        DICTIONARY_WORDS = getDictionaryWords();
        FOUND_WORDS = new HashSet<>();
    }

    public static void main(String[] args) {
        // Set LockAndFeel to be less ugly
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Boggle.class.getName()).log(Level.SEVERE, null, ex);
        }

        // --Start--
        boggleFrame = new JFrame("Boggle");
        boggleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        boggleFrame.setLayout(new BorderLayout());

        // Dice panel
        dicePanel = new JPanel(new GridLayout(4, 4));
        dicePanel.setPreferredSize(new Dimension(400, 400));
        dicePanel.setBorder(BorderFactory.createTitledBorder("Dice"));

        // Dice buttons
        diceBtns = new JButton[4][4];
        for (JButton[] diceBtn : diceBtns) {
            for (int j = 0; j < diceBtns[0].length; j++) {
                diceBtn[j] = new JButton(DICE_FACES.iterator().next()[(int) (Math.random() * 6)]);
                diceBtn[j].setFocusable(false);
                diceBtn[j].setFont(new Font(diceBtn[j].getFont().getName(), Font.PLAIN, 36));
                diceBtn[j].addActionListener((ActionEvent e) -> {
                    JButton btn = (JButton) e.getSource();
                    btn.setEnabled(false);
                    currentWordLabel.setText(currentWordLabel.getText() + btn.getText());
                });
                dicePanel.add(diceBtn[j]);
            }
        }
        boggleFrame.add(dicePanel, BorderLayout.CENTER);

        // --Start-- Right panel
        rightPanel = new JPanel(new BorderLayout());
        // Words panel
        wordsPanel = new JPanel(new BorderLayout());
        wordsPanel.setBorder(BorderFactory.createTitledBorder("Words Found"));
        wordsTextPane = new JTextPane();
        wordsTextPane.setEditable(false);
        wordsScrollPane = new JScrollPane(wordsTextPane);
        wordsPanel.add(wordsScrollPane, BorderLayout.CENTER);
        rightPanel.add(wordsPanel, BorderLayout.CENTER);

        // --Start-- RightBottom panel
        rightBottomPanel = new JPanel(new GridLayout(2, 1));
        rightBottomPanel.setPreferredSize(new Dimension(300, 180));

        // ScrambleBtn panel
        scrambleBtnPanel = new JPanel();
        scrambleBtnPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scrambleBtn = new JButton("Scramble");
        scrambleBtn.setPreferredSize(new Dimension(290, 70));
        scrambleBtn.setFocusable(false);
        scrambleBtn.addActionListener((ActionEvent e) -> {
            currentWordLabel.setText("");
            for (int i = 0; i < diceBtns.length; i++) {
                for (int j = 0; j < diceBtns[0].length; j++) {
                    diceBtns[i][j].setText(DICE_FACES.get(i * diceBtns.length + j)[(int) (Math.random() * 6)]);
                    diceBtns[i][j].setEnabled(true);
                }
            }
        });
        scrambleBtnPanel.add(scrambleBtn);
        rightBottomPanel.add(scrambleBtnPanel);

        // Timer panel
        timerPanel = new JPanel();
        timerPanel.setBorder(BorderFactory.createTitledBorder("Time Remaining"));
        timerLabel = new JLabel("3:00");
        timerLabel.setFont(new Font("Dialog", Font.PLAIN, 36));
        timerPanel.add(timerLabel);
        rightBottomPanel.add(timerPanel);
        // --End-- RightBottom panel

        rightPanel.add(rightBottomPanel, BorderLayout.PAGE_END);
        boggleFrame.add(rightPanel, BorderLayout.LINE_END);
        // --End-- Right panel

        // --Start-- Bottom panel
        bottomPanel = new JPanel(new BorderLayout());
        // --Start-- Entry panel
        entryPanel = new JPanel(new BorderLayout());
        entryPanel.setPreferredSize(new Dimension(400, 80));
        entryTitle = BorderFactory.createTitledBorder("Current Word");
        entryPanel.setBorder(entryTitle);

        // CurrentWord label
        currentWordLabel = new JLabel();
        currentWordLabel.setFont(new Font("Dialog", Font.PLAIN, 36));
        currentWordLabel.setHorizontalAlignment(JLabel.CENTER);
        currentWordLabel.setVerticalAlignment(JLabel.CENTER);
        entryPanel.add(currentWordLabel, BorderLayout.CENTER);

        // SubmitWord button
        submitWordButton = new JButton("Submit");
        submitWordButton.setFocusable(false);
        submitWordButton.setPreferredSize(new Dimension(100, 47));
        submitWordButton.addActionListener((ActionEvent e) -> {
            if (!currentWordLabel.getText().isEmpty()) {
                String currentWord = currentWordLabel.getText();
                currentWordLabel.setText("");
                for (JButton[] diceBtnArr : diceBtns) {
                    for (int j = 0; j < diceBtns[0].length; j++) {
                        diceBtnArr[j].setEnabled(true);
                    }
                }

                if (DICTIONARY_WORDS.contains(currentWord) && FOUND_WORDS.add(currentWord)) {
                    wordsTextPane.setText(wordsTextPane.getText() + currentWord + "\n");
                    
                    if (currentWord.length() <= 4) {
                        scoreLabel.setText(addIntToStringInt(scoreLabel.getText(), 1));
                    } else {
                        switch (currentWord.length()) {
                            case 5:
                                scoreLabel.setText(addIntToStringInt(scoreLabel.getText(), 2));
                                break;
                            case 6:
                                scoreLabel.setText(addIntToStringInt(scoreLabel.getText(), 3));
                                break;
                            case 7:
                                scoreLabel.setText(addIntToStringInt(scoreLabel.getText(), 5));
                                break;
                            default:
                                scoreLabel.setText(addIntToStringInt(scoreLabel.getText(), 11));
                                break;
                        }
                    }
                }
            }
        });
        entryPanel.add(submitWordButton, BorderLayout.LINE_END);
        bottomPanel.add(entryPanel, BorderLayout.CENTER);
        // --End-- Entry panel

        // --Start-- Score panel
        scorePanel = new JPanel(new BorderLayout());
        scorePanel.setPreferredSize(new Dimension(190, 80));
        scorePanel.setBorder(BorderFactory.createTitledBorder("Score"));
        scoreLabel = new JLabel("0");
        scoreLabel.setFont(new Font("Dialog", Font.PLAIN, 36));
        scoreLabel.setHorizontalAlignment(JLabel.CENTER);
        scoreLabel.setVerticalAlignment(JLabel.CENTER);
        scorePanel.add(scoreLabel, BorderLayout.CENTER);
        bottomPanel.add(scorePanel, BorderLayout.LINE_END);
        // --End-- Score panel
        
        boggleFrame.add(bottomPanel, BorderLayout.PAGE_END);
        // --End-- Bottom panel

        boggleFrame.setSize(720, 520);
        boggleFrame.setLocationRelativeTo(null);

        timer = new Timer(1000, new ActionListener() {
            int timeRemaining = 180;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeRemaining == 0) {
                    for (JButton[] diceRow : diceBtns) {
                        for (JButton die : diceRow) {
                            die.setEnabled(false);
                        }
                    }
                    scrambleBtn.setEnabled(false);
                    System.out.println("DONE");
                    ((Timer) e.getSource()).stop();
                } else {
                    timeRemaining--;
                    timerLabel.setText(String.format("%d:%02d", timeRemaining / 60, timeRemaining % 60));
                }
            }
        });
        boggleFrame.setVisible(true);
        // --End--
        timer.start();
    }

    private static List<String[]> getDiceFaces() {
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(Boggle.class.getResourceAsStream("/resources/BoggleData.txt"))).lines()) {
            return stream.map(s -> s.split(" ")).collect(Collectors.toList());
        }
    }

    private static Set<String> getDictionaryWords() {
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(Boggle.class.getResourceAsStream("/resources/Dictionary.txt"))).lines()) {
            return stream.map(w -> w.toUpperCase()).collect(Collectors.toCollection(HashSet::new));
        }
    }

    private static String addIntToStringInt(String strN, int n) {
        return Integer.toString(Integer.parseInt(strN) + n);
    }
}
