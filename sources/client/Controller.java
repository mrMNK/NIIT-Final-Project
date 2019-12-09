package client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import javax.sound.sampled.*;

import java.io.*;
import java.net.Socket;

enum States {
    OFF, WAIT, ROUND
}

public class Controller {
    private final String death = "☠";
    private final String enemy = "⚗";
    private final String ship = "⊱➢";
    private final String clmnMarker = "↓";
    private final String cash = "$$$";
    private float coins;
    Socket server = null;
    private States states;
    private BufferedReader in;
    private BufferedWriter out;
    private int column;
    private float currentBet;

    public void initialize(){
        this.states = States.OFF;
    }

    public void startGame() throws IOException { // метод для кнопки NEW GAME

        try {
            playMusic("sounds/bigBtn.wav");
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException exc) {
            exc.printStackTrace();
        }

        flashColumnMarker();
        flashButtons();
        prizeBtn.setText("");
        this.server = new Socket("localhost", 0001);
        this.coins = 0;
        this.column = 0;
        this.states = States.WAIT;
        in = new BufferedReader(new InputStreamReader(server.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
        setBalance();
    }

    public void playMusic(String file) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(Controller.class.getResource(file));
        Clip clip = AudioSystem.getClip();
        clip.open(audioIn);
        clip.start();
    }

    public void exitBtn() throws IOException { // метод для кнопки EXIT
        if (getState() != States.ROUND) {
            try {
                playMusic("sounds/bigBtn.wav");
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException exc) {
                exc.printStackTrace();
            }

            in.close();
            out.close();
            server.close();
            Stage stage = (Stage) exitBtn.getScene().getWindow();
            stage.close();
        }
    }

    private void send(String msg) { // метод отправки сообщений серверу
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {}
    }

    private void setColumn(int num){ // установить номер текущего столбца
        this.column = num;
    }

    private int getColumn(){ // получить номер текущего столбца
        return this.column;
    }

    private void setCurrentBet(float bet){ // установить текущую ставку
        this.currentBet = bet;
    }

    private float getCurrentBet() { // возвращает текущую ставку
        return this.currentBet;
    }

    private float getBalance(){ // возвращает баланс монет
        return this.coins;
    }

    private void setBalance() throws IOException { // запрос баланса от сервера
        send("balance");
        String balance = in.readLine();
        balanceLbl.setText(balance);
        this.coins = Float.parseFloat(balance);
    }

    private void setBalanceResponse(String str) { // изменение баланса по ответу сервера
        this.coins = Float.parseFloat(str);
        balanceLbl.setText(str);
    }

    private States getState(){ // возвращает текущее состояние
        return this.states;
    }

    private float getCoefficient(int j){ // возвращает текущий множитель приза
        float out = 0;
        switch (j){
            case (0):
                out = 1f;
                break;
            case (1):
                out = 1.29f;
                break;
            case (2):
                out = 1.72f;
                break;
            case (3):
                out = 2.29f;
                break;
            case (4):
                out = 3.06f;
                break;
            case (5):
                out = 4.08f;
                break;
            case (6):
                out = 5.45f;
                break;
            case (7):
                out = 7.26f;
                break;
        }
        return out;
    }

    private boolean checkText(String str){ // проверка введенной ставки на корректность
        if (str.isEmpty()){
            return false;
        }
        try {
            Float.parseFloat(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private float getBet(){ // возвращает значение ставки из textField типа float
        String bet = tf.getText();
        if (checkText(bet)) {
            float out = Float.parseFloat(tf.getText());
            tf.setText("");
            return out;
        }
        return 0;
    }

    public void betCoins() throws IOException{ // обработка нажатия на кнопку Bet
        if (getState() == (States.WAIT) && getBalance() > 0) {
            setCurrentBet(getBet());
            float currBet = getCurrentBet();
            if ((currBet > 0) && (currBet <= getBalance())) {
                newRound(currBet);
                tf.setText("");
            } else {
                tf.setText("Incorrect value!");
            }
        }
    }

    private void flashColumnMarker(){
        label1.setText("");
        label2.setText("");
        label3.setText("");
        label4.setText("");
        label5.setText("");
        label6.setText("");
        label7.setText("");
    }

    private void setColumnMarker(int clmn){ // устанавливает маркер "↓" над столбцом, который сейчас в игре
        flashColumnMarker();
        switch (clmn){
            case(1):
                label1.setText(clmnMarker);
                break;
            case(2):
                label2.setText(clmnMarker);
                break;
            case(3):
                label3.setText(clmnMarker);
                break;
            case(4):
                label4.setText(clmnMarker);
                break;
            case(5):
                label5.setText(clmnMarker);
                break;
            case(6):
                label6.setText(clmnMarker);
                break;
            case(7):
                label7.setText(clmnMarker);
                break;
        }
    }

    private void setColumnDeath(int clmn){ // установить символ смерти надстолбцом, где игрок наткнулся на врага
        flashColumnMarker();
        switch (clmn){
            case(1):
                label1.setText(death);
                break;
            case(2):
                label2.setText(death);
                break;
            case(3):
                label3.setText(death);
                break;
            case(4):
                label4.setText(death);
                break;
            case(5):
                label5.setText(death);
                break;
            case(6):
                label6.setText(death);
                break;
            case(7):
                label7.setText(death);
                break;
        }
    }

    private void setColumnCash(int clmn){ // установить "$" над столбцом, где игрок забрал ставку
        flashColumnMarker();
        switch (clmn){
            case(1):
                label1.setText(cash);
                break;
            case(2):
                label2.setText(cash);
                break;
            case(3):
                label3.setText(cash);
                break;
            case(4):
                label4.setText(cash);
                break;
            case(5):
                label5.setText(cash);
                break;
            case(6):
                label6.setText(cash);
                break;
            case(7):
                label7.setText(cash);
                break;
        }
    }

    private void newRound(float bet) throws IOException{ // новый раунд
        this.states = States.ROUND;

        try {
            playMusic("sounds/bigBtn.wav");
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException exc) {
            exc.printStackTrace();
        }

        setColumn(0);
        flashButtons();
        send("newRound");
        String serverMsg = in.readLine();
        if (serverMsg.equals("newRoundStart")) {
            send(Float.toString(bet)); // отправляем на сервер размер ставки
            serverMsg = in.readLine();
            if (serverMsg.equals("error")) { // если ответ - error, то обновляем баланс и меняем статус на ожидание
                setBalance();
                this.states = States.WAIT;
            } else {
                setBalanceResponse(serverMsg); // иначе - ждем пересчета баланса от сервера
                setColumn(1);
                setColumnMarker(getColumn());
                prizeBtn.setText("Get " + (getCurrentBet() * getCoefficient(getColumn() - 1)));
            }
        }
    }

    public void getPrize() throws IOException{
        if (getState().equals(States.ROUND) && ((getColumn() > 0) && (getColumn() < 8))){

            try {
                playMusic("sounds/money.wav");
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException exc) {
                exc.printStackTrace();
            }

            send("getColumn");
            send("0");
            prizeBtn.setText("");
            if (in.readLine().equals("checkLines")) {
                setColumnCash(getColumn());
                while (getColumn() < 8) {
                    send("nextLine");
                    setEnemy(Integer.parseInt(in.readLine()), getColumn());
                    setColumn(getColumn()+1);
                }
                send("stop");
            }

            if (in.readLine().equals("checkBalance")){
                setBalance();
            }
            prizeBtn.setText("");
            this.states = States.WAIT;
        }
    }

    private void setEnemy(int position, int column){ // устанавливает символ противника на игровое поле
        switch(column){
            case(1):
                switch(position){
                    case(1):
                        btn11.setText(enemy);
                        break;
                    case(2):
                        btn21.setText(enemy);
                        break;
                    case(3):
                        btn31.setText(enemy);
                        break;
                    case(4):
                        btn41.setText(enemy);
                        break;
                }
                break;
            case(2):
                switch(position){
                    case(1):
                        btn12.setText(enemy);
                        break;
                    case(2):
                        btn22.setText(enemy);
                        break;
                    case(3):
                        btn32.setText(enemy);
                        break;
                    case(4):
                        btn42.setText(enemy);
                        break;
                }
                break;
            case(3):
                switch(position){
                    case(1):
                        btn13.setText(enemy);
                        break;
                    case(2):
                        btn23.setText(enemy);
                        break;
                    case(3):
                        btn33.setText(enemy);
                        break;
                    case(4):
                        btn43.setText(enemy);
                        break;
                }
                break;
            case(4):
                switch(position){
                    case(1):
                        btn14.setText(enemy);
                        break;
                    case(2):
                        btn24.setText(enemy);
                        break;
                    case(3):
                        btn34.setText(enemy);
                        break;
                    case(4):
                        btn44.setText(enemy);
                        break;
                }
                break;
            case(5):
                switch(position){
                    case(1):
                        btn15.setText(enemy);
                        break;
                    case(2):
                        btn25.setText(enemy);
                        break;
                    case(3):
                        btn35.setText(enemy);
                        break;
                    case(4):
                        btn45.setText(enemy);
                        break;
                }
                break;
            case(6):
                switch(position){
                    case(1):
                        btn16.setText(enemy);
                        break;
                    case(2):
                        btn26.setText(enemy);
                        break;
                    case(3):
                        btn36.setText(enemy);
                        break;
                    case(4):
                        btn46.setText(enemy);
                        break;
                }
                break;
            case(7):
                switch(position){
                    case(1):
                        btn17.setText(enemy);
                        break;
                    case(2):
                        btn27.setText(enemy);
                        break;
                    case(3):
                        btn37.setText(enemy);
                        break;
                    case(4):
                        btn47.setText(enemy);
                        break;
                }
                break;
        }
    }

    private void setShip(int position, int column){ // устанавливает символ корабль игрока в выбранную клетку
        switch(column){
            case(1):
                switch(position){
                    case(1):
                        btn11.setText(ship);
                        break;
                    case(2):
                        btn21.setText(ship);
                        break;
                    case(3):
                        btn31.setText(ship);
                        break;
                    case(4):
                        btn41.setText(ship);
                        break;
                }
                break;
            case(2):
                switch(position){
                    case(1):
                        btn12.setText(ship);
                        break;
                    case(2):
                        btn22.setText(ship);
                        break;
                    case(3):
                        btn32.setText(ship);
                        break;
                    case(4):
                        btn42.setText(ship);
                        break;
                }
                break;
            case(3):
                switch(position){
                    case(1):
                        btn13.setText(ship);
                        break;
                    case(2):
                        btn23.setText(ship);
                        break;
                    case(3):
                        btn33.setText(ship);
                        break;
                    case(4):
                        btn43.setText(ship);
                        break;
                }
                break;
            case(4):
                switch(position){
                    case(1):
                        btn14.setText(ship);
                        break;
                    case(2):
                        btn24.setText(ship);
                        break;
                    case(3):
                        btn34.setText(ship);
                        break;
                    case(4):
                        btn44.setText(ship);
                        break;
                }
                break;
            case(5):
                switch(position){
                    case(1):
                        btn15.setText(ship);
                        break;
                    case(2):
                        btn25.setText(ship);
                        break;
                    case(3):
                        btn35.setText(ship);
                        break;
                    case(4):
                        btn45.setText(ship);
                        break;
                }
                break;
            case(6):
                switch(position){
                    case(1):
                        btn16.setText(ship);
                        break;
                    case(2):
                        btn26.setText(ship);
                        break;
                    case(3):
                        btn36.setText(ship);
                        break;
                    case(4):
                        btn46.setText(ship);
                        break;
                }
                break;
            case(7):
                switch(position){
                    case(1):
                        btn17.setText(ship);
                        break;
                    case(2):
                        btn27.setText(ship);
                        break;
                    case(3):
                        btn37.setText(ship);
                        break;
                    case(4):
                        btn47.setText(ship);
                        break;
                }
                break;
        }
    }

    private void setDeath(int position, int column){ // устанавливает символ корабль игрока в выбранную клетку
        switch(column){
            case(1):
                switch(position){
                    case(1):
                        btn11.setText(death);
                        break;
                    case(2):
                        btn21.setText(death);
                        break;
                    case(3):
                        btn31.setText(death);
                        break;
                    case(4):
                        btn41.setText(death);
                        break;
                }
                break;
            case(2):
                switch(position){
                    case(1):
                        btn12.setText(death);
                        break;
                    case(2):
                        btn22.setText(death);
                        break;
                    case(3):
                        btn32.setText(death);
                        break;
                    case(4):
                        btn42.setText(death);
                        break;
                }
                break;
            case(3):
                switch(position){
                    case(1):
                        btn13.setText(death);
                        break;
                    case(2):
                        btn23.setText(death);
                        break;
                    case(3):
                        btn33.setText(death);
                        break;
                    case(4):
                        btn43.setText(death);
                        break;
                }
                break;
            case(4):
                switch(position){
                    case(1):
                        btn14.setText(death);
                        break;
                    case(2):
                        btn24.setText(death);
                        break;
                    case(3):
                        btn34.setText(death);
                        break;
                    case(4):
                        btn44.setText(death);
                        break;
                }
                break;
            case(5):
                switch(position){
                    case(1):
                        btn15.setText(death);
                        break;
                    case(2):
                        btn25.setText(death);
                        break;
                    case(3):
                        btn35.setText(death);
                        break;
                    case(4):
                        btn45.setText(death);
                        break;
                }
                break;
            case(6):
                switch(position){
                    case(1):
                        btn16.setText(death);
                        break;
                    case(2):
                        btn26.setText(death);
                        break;
                    case(3):
                        btn36.setText(death);
                        break;
                    case(4):
                        btn46.setText(death);
                        break;
                }
                break;
            case(7):
                switch(position){
                    case(1):
                        btn17.setText(death);
                        break;
                    case(2):
                        btn27.setText(death);
                        break;
                    case(3):
                        btn37.setText(death);
                        break;
                    case(4):
                        btn47.setText(death);
                        break;
                }
                break;
        }
    }

    private void flashButtons(){ // очистка всего текста с кнопок на игровом поле
        btn11.setText("");
        btn12.setText("");
        btn13.setText("");
        btn14.setText("");
        btn15.setText("");
        btn16.setText("");
        btn17.setText("");

        btn21.setText("");
        btn22.setText("");
        btn23.setText("");
        btn24.setText("");
        btn25.setText("");
        btn26.setText("");
        btn27.setText("");

        btn31.setText("");
        btn32.setText("");
        btn33.setText("");
        btn34.setText("");
        btn35.setText("");
        btn36.setText("");
        btn37.setText("");

        btn41.setText("");
        btn42.setText("");
        btn43.setText("");
        btn44.setText("");
        btn45.setText("");
        btn46.setText("");
        btn47.setText("");
    }

    private void checkColumnResult(String str, int shipPosition) throws IOException{ // проверка резльтата хода игрока

        try {
            playMusic("sounds/smallBtn.wav");
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException exc) {
            exc.printStackTrace();
        }

        if (str.equals("loose")){ // сценарий, когда игрок выбрал клетку с врагом

            try {
                playMusic("sounds/bang.wav");
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException exc) {
                exc.printStackTrace();
            }

            prizeBtn.setText("");
            setDeath(shipPosition, getColumn());
            setColumnDeath(getColumn());
            while (getColumn() < 7){
                setColumn(getColumn() + 1);
                send("nextLine");
                setEnemy(Integer.parseInt(in.readLine()), getColumn());
            }
            send("stop");
            if (in.readLine().equals("checkBalance")) {
                setBalance();
                if (getBalance() == 0f){
                    try {
                        playMusic("sounds/loose.wav");
                    } catch (IOException | UnsupportedAudioFileException | LineUnavailableException exc) {
                        exc.printStackTrace();
                    }
                }
                this.states = States.WAIT;
            }
        }

        else if (str.equals("empty")){ // сценарий, когда игрок выбрал пустую от врага клетку
            setColumn(getColumn() + 1);
            if (getColumn() < 8){
                setColumnMarker(getColumn());
                prizeBtn.setText("Get " + (getCurrentBet() * getCoefficient(getColumn() - 1)));
            }
            send("enemy");
            int enemyPosition = Integer.parseInt(in.readLine());
            setEnemy(enemyPosition, getColumn()-1);
            setShip(shipPosition, getColumn()-1);
            if (getColumn() == 8 && in.readLine().equals("checkBalance")){ // выполняется, если игрок прошел всё поле

                try {
                    playMusic("sounds/money.wav");
                } catch (IOException | UnsupportedAudioFileException | LineUnavailableException exc) {
                    exc.printStackTrace();
                }

                setBalance();
                flashColumnMarker();
                prizeBtn.setText("");
                this.states = States.WAIT;
            }
        }
    }

    public void btnAA() throws IOException{ // нажатие на btn11
        if (getState().equals(States.ROUND) && (getColumn() == 1)){
            send("getColumn");
            send("1");
            checkColumnResult(in.readLine(), 1);
        }
    }

    public void btnBA() throws IOException{ // нажатие на btn21
        if (getState().equals(States.ROUND) && (getColumn() == 1)){
            send("getColumn");
            send("2");
            checkColumnResult(in.readLine(), 2);
        }
    }

    public void btnCA() throws IOException{ // нажатие на btn31
        if (getState().equals(States.ROUND) && (getColumn() == 1)){
            send("getColumn");
            send("3");
            checkColumnResult(in.readLine(), 3);
        }
    }

    public void btnDA() throws IOException{ // нажатие на btn41
        if (getState().equals(States.ROUND) && (getColumn() == 1)){
            send("getColumn");
            send("4");
            checkColumnResult(in.readLine(),4);
        }
    }

    public void btnAB() throws IOException{ // нажатие на btn12
        if (getState().equals(States.ROUND) && (getColumn() == 2)){
            send("getColumn");
            send("1");
            checkColumnResult(in.readLine(),1);
        }
    }

    public void btnBB() throws IOException{ // нажатие на btn22
        if (getState().equals(States.ROUND) && (getColumn() == 2)){
            send("getColumn");
            send("2");
            checkColumnResult(in.readLine(),2);
        }
    }

    public void btnCB() throws IOException{ // нажатие на btn32
        if (getState().equals(States.ROUND) && (getColumn() == 2)){
            send("getColumn");
            send("3");
            checkColumnResult(in.readLine(),3);
        }
    }

    public void btnDB() throws IOException{ // нажатие на btn42
        if (getState().equals(States.ROUND) && (getColumn() == 2)){
            send("getColumn");
            send("4");
            checkColumnResult(in.readLine(),4);
        }
    }

    public void btnAC() throws IOException{ // нажатие на btn13
        if (getState().equals(States.ROUND) && (getColumn() == 3)){
            send("getColumn");
            send("1");
            checkColumnResult(in.readLine(),1);
        }
    }

    public void btnBC() throws IOException{ // нажатие на btn23
        if (getState().equals(States.ROUND) && (getColumn() == 3)){
            send("getColumn");
            send("2");
            checkColumnResult(in.readLine(),2);
        }
    }

    public void btnCC() throws IOException{ // нажатие на btn33
        if (getState().equals(States.ROUND) && (getColumn() == 3)){
            send("getColumn");
            send("3");
            checkColumnResult(in.readLine(),3);
        }
    }

    public void btnDC() throws IOException{ // нажатие на btn43
        if (getState().equals(States.ROUND) && (getColumn() == 3)){
            send("getColumn");
            send("4");
            checkColumnResult(in.readLine(),4);
        }
    }

    public void btnAD() throws IOException{ // нажатие на btn14
        if (getState().equals(States.ROUND) && (getColumn() == 4)){
            send("getColumn");
            send("1");
            checkColumnResult(in.readLine(),1);
        }
    }

    public void btnBD() throws IOException{ // нажатие на btn24
        if (getState().equals(States.ROUND) && (getColumn() == 4)){
            send("getColumn");
            send("2");
            checkColumnResult(in.readLine(),2);
        }
    }

    public void btnCD() throws IOException{ // нажатие на btn34
        if (getState().equals(States.ROUND) && (getColumn() == 4)){
            send("getColumn");
            send("3");
            checkColumnResult(in.readLine(),3);
        }
    }

    public void btnDD() throws IOException{ // нажатие на btn44
        if (getState().equals(States.ROUND) && (getColumn() == 4)){
            send("getColumn");
            send("4");
            checkColumnResult(in.readLine(),4);
        }
    }

    public void btnAE() throws IOException{ // нажатие на btn15
        if (getState().equals(States.ROUND) && (getColumn() == 5)){
            send("getColumn");
            send("1");
            checkColumnResult(in.readLine(),1);
        }
    }

    public void btnBE() throws IOException{ // нажатие на btn25
        if (getState().equals(States.ROUND) && (getColumn() == 5)){
            send("getColumn");
            send("2");
            checkColumnResult(in.readLine(),2);
        }
    }

    public void btnCE() throws IOException{ // нажатие на btn35
        if (getState().equals(States.ROUND) && (getColumn() == 5)){
            send("getColumn");
            send("3");
            checkColumnResult(in.readLine(),3);
        }
    }

    public void btnDE() throws IOException{ // нажатие на btn45
        if (getState().equals(States.ROUND) && (getColumn() == 5)){
            send("getColumn");
            send("4");
            checkColumnResult(in.readLine(),4);
        }
    }

    public void btnAF() throws IOException{ // нажатие на btn16
        if (getState().equals(States.ROUND) && (getColumn() == 6)){
            send("getColumn");
            send("1");
            checkColumnResult(in.readLine(),1);
        }
    }

    public void btnBF() throws IOException{ // нажатие на btn26
        if (getState().equals(States.ROUND) && (getColumn() == 6)){
            send("getColumn");
            send("2");
            checkColumnResult(in.readLine(),2);
        }
    }

    public void btnCF() throws IOException{ // нажатие на btn36
        if (getState().equals(States.ROUND) && (getColumn() == 6)){
            send("getColumn");
            send("3");
            checkColumnResult(in.readLine(),3);
        }
    }

    public void btnDF() throws IOException{ // нажатие на btn46
        if (getState().equals(States.ROUND) && (getColumn() == 6)){
            send("getColumn");
            send("4");
            checkColumnResult(in.readLine(),4);
        }
    }

    public void btnAG() throws IOException{ // нажатие на btn17
        if (getState().equals(States.ROUND) && (getColumn() == 7)){
            send("getColumn");
            send("1");
            checkColumnResult(in.readLine(),1);
        }
    }

    public void btnBG() throws IOException{ // нажатие на btn27
        if (getState().equals(States.ROUND) && (getColumn() == 7)){
            send("getColumn");
            send("2");
            checkColumnResult(in.readLine(),2);
        }
    }

    public void btnCG() throws IOException{ // нажатие на btn36
        if (getState().equals(States.ROUND) && (getColumn() == 7)){
            send("getColumn");
            send("3");
            checkColumnResult(in.readLine(),3);
        }
    }

    public void btnDG() throws IOException{ // нажатие на btn46
        if (getState().equals(States.ROUND) && (getColumn() == 7)){
            send("getColumn");
            send("4");
            checkColumnResult(in.readLine(),4);
        }
    }

    @FXML
    Button newGameBtn;

    @FXML
    Button exitBtn;

    @FXML
    Button betBtn;

    @FXML
    Button prizeBtn;

    @FXML
    Label balanceLbl;

    @FXML
    TextField tf;

    @FXML
    GridPane gridPane;

    @FXML
    Label label1;

    @FXML
    Label label2;

    @FXML
    Label label3;

    @FXML
    Label label4;

    @FXML
    Label label5;

    @FXML
    Label label6;

    @FXML
    Label label7;

    @FXML
    Button btn11;

    @FXML
    Button btn12;

    @FXML
    Button btn13;

    @FXML
    Button btn14;

    @FXML
    Button btn15;

    @FXML
    Button btn16;

    @FXML
    Button btn17;

    @FXML
    Button btn21;

    @FXML
    Button btn22;

    @FXML
    Button btn23;

    @FXML
    Button btn24;

    @FXML
    Button btn25;

    @FXML
    Button btn26;

    @FXML
    Button btn27;

    @FXML
    Button btn31;

    @FXML
    Button btn32;

    @FXML
    Button btn33;

    @FXML
    Button btn34;

    @FXML
    Button btn35;

    @FXML
    Button btn36;

    @FXML
    Button btn37;

    @FXML
    Button btn41;

    @FXML
    Button btn42;

    @FXML
    Button btn43;

    @FXML
    Button btn44;

    @FXML
    Button btn45;

    @FXML
    Button btn46;

    @FXML
    Button btn47;
}
