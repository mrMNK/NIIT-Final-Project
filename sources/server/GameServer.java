package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

class Compound extends Thread {
    private Socket client = null;
    private float coins = 0;
    private int[][] field = new int[4][7];
    private BufferedReader in;
    private BufferedWriter out;

    public Compound(Socket socket) throws IOException {
        this.client = socket;
        this.coins = 1000;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start();
    }

    @Override
    public void run() {
        String word;
        try {
            while (true) {
                word = in.readLine();

                switch (word) {
                    case ("balance"): // обработка запроса клиента о балансе
                        send(Float.toString(getCoins()));
                        break;
                    case ("newRound"): // старт нового раунда
                        send("newRoundStart");
                        float bet = Float.parseFloat(in.readLine());
                        int clmn = 0;

                        if (checkBalance(bet)){
                            setCoins(bet * (-1)); // пересчитываем баланс (вычитаем из баланса ставку)
                            generate(); // заполняем игровое поле врагами
                            send(Float.toString(getCoins())); // отправляем клинту обновленный баланс

                            while (clmn < 7){
                                String clientMsg = in.readLine(); // ждем ответа клиента
                                if (clientMsg.equals("getColumn")) {
                                    int position = Integer.parseInt(in.readLine());
                                    /*
                                    получаем номер выбранной игроком клетки или информацию о том, что
                                    игрок забирает ставку
                                    */

                                    if (position == 0){ // игрок забирает ставку
                                        send("checkLines");
                                        String clMsg = in.readLine(); // получаем запрос от клиента
                                        int tmp = clmn;
                                        while (clMsg.equals("nextLine") && (tmp < 7)){ // поочередно отправляем игроку
                                            send(Integer.toString(getFieldColumn(tmp))); // местоположение всех врагов
                                            clMsg = in.readLine();
                                            tmp++;
                                        }
                                        break;
                                    }
                                    else if (position == getFieldColumn(clmn)){ // игрок "попал" на врага
                                        send("loose");
                                        String clMsg = in.readLine(); // получаем запрос от клиента
                                        while (clMsg.equals("nextLine") && (clmn < 6)){ // поочередно отправляем игроку
                                            clmn++;                                     // местоположение всех врагов
                                            send(Integer.toString(getFieldColumn(clmn)));
                                            clMsg = in.readLine();
                                        }
                                        bet = 0;
                                        break;
                                    }
                                    else if ((position > 0) && (position < 5)){ // игрок выбрал пустую клетку
                                        send("empty");
                                        String clMsg = in.readLine();
                                        if (clMsg.equals("enemy")){
                                            send(Integer.toString(getFieldColumn(clmn))); // отправить позицию врага
                                            clmn++;
                                        }else{
                                            break;
                                        }
                                    } else{ // игрок жульничает
                                        break;
                                    }
                                } else{
                                    break;
                                }

                            }
                        } else{
                            send("error");
                            break;
                        }
                        bet = bet * getCoefficient(clmn);
                        setCoins(bet);
                        send("checkBalance");
                        break;
                }
            }
        } catch (IOException e) {
        }
    }

    private void setCoins(float num){ // метод изменяет баланс монет
        this.coins += num;
    }

    private float getCoins(){ // возвращает текущий баланс монет на счету
        return this.coins;
    }

    private void setEnemyToField(int i, int j){ // установить врага в ячейку [i][j]
        this.field[i][j] = 1;
    }

    private int getFieldColumn(int j) { // возвращает местополодение врага в заданном столбце
        for (int i = 0; i < 4; i++){
            if (this.field[i][j] == 1){
                return i + 1;
            }
        }
        return 0;
    }

    private void flashField(){ // заполняет массив нулями
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 7; j++){
                this.field[i][j] = 0;
            }
        }
    }

    private void send(String msg) { // метод отправки сообщений клиенту
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {}
    }

    private boolean checkBalance(float bet) { // проверяет, что ставка меньше или равна балансу монет
        if (bet <= getCoins()){
            return true;
        }
        return false;
    }

    private void generate(){ // генерация игрового поля
        flashField();
        FieldGenerator fg = new FieldGenerator();
        for (int j = 0; j < 7; j++){
            setEnemyToField(fg.generateLine(), j);
        }
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

}

public class GameServer {
    public static void main(String[] args) throws IOException {
        System.out.println("Start server");

        ServerSocket server = null;

        try {
            server = new ServerSocket(0001);
        } catch (IOException e) {
            System.out.println("Error connection to port: 0001");
            System.exit(-1);
        }

        try {
            while (true) {
                System.out.print("Waiting for connection" + "\n");
                Socket soc = server.accept();
                try {
                    System.out.println("Client connected" + "\n");
                    new Compound(soc);
                } catch (IOException e) {
                    System.out.println("Connection error" + "\n");
                    System.exit(-1);
                }
            }
        } finally {
            server.close();
        }
    }
}