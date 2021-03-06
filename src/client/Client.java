package client;

import route.Route;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;

public class Client {

    private static Scanner scanner = new Scanner(System.in);
    private static DatagramSocket ds;
    private static InetAddress serverAddress;
    private static String clientName = "localhost";
    private static int clientPort = 4321;
    private static Object[] inputArr;
    private static Object command;
    private static Object argument;


    public static void main(String[] args) throws Exception {
        serverAddress = InetAddress.getByName(clientName);
        //Создание сокета для отправки команд
        ds = new DatagramSocket(clientPort);

        loadAutoSave();

        while (true) {
            processInput();
            sendRequest();
            getResult();
        }
    }

    private static void loadAutoSave() throws IOException {
        System.out.println("do you want to recover last data?[y/n]");
        String command = scanner.nextLine();

        if (command.equalsIgnoreCase("y")) {
            command = "load_auto_save";


            byte[] requestArr;

            //Создание потока вывода
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            baos.flush();
            oos.flush();

            //Запись команды и аргумента в этот поток
            oos.writeObject(new Object[]{command});
            oos.flush();
            oos.close();
            requestArr = baos.toByteArray();

            //Упаковка команды в датаграмму
            DatagramPacket dp = new DatagramPacket(requestArr, requestArr.length, serverAddress, 1234);

            //Отправка на сервер в порт 1234
            ds.send(dp);
            getResult();
        }
    }

    private static void processInput() {
        inputArr = scanner.nextLine().split(" ");
        command = inputArr[0];
        if (inputArr.length > 1) {
            argument = inputArr[1];
            if (command.equals("update")) {
                Route routeForUpdating = new Route();
                routeForUpdating.setId(Integer.parseInt((String) inputArr[1]));
                argument = routeForUpdating;
            }
        }
        if (command.equals("add")) {
            argument = new Route();
        }
    }

    private static void sendRequest() throws IOException {

        byte[] requestArr;

        //Создание потока вывода
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        baos.flush();
        oos.flush();

        //Запись команды и аргумента в этот поток
        oos.writeObject(new Object[]{command, argument});
        oos.flush();
        oos.close();
        requestArr = baos.toByteArray();

        //Упаковка команды в датаграмму
        DatagramPacket dp = new DatagramPacket(requestArr, requestArr.length, serverAddress, 1234);
        //Отправка на сервер в порт 1234
        ds.send(dp);
        System.out.println(Arrays.toString(inputArr) + " sent to server at: " + serverAddress);

    }

    private static void getResult() throws IOException {

        //Создание пакета для приема ответа от сервера
        byte[] resultArr = new byte[4096];
        DatagramPacket resultPacket = new DatagramPacket(resultArr, resultArr.length, InetAddress.getByName("localhost"), 0);
        ds.receive(resultPacket);

        //Распаковка полученного ответа от сервера из датаграммы
        String resultString = new String(resultPacket.getData(), 0, resultPacket.getLength());
        System.out.println(resultString);

        if (resultString.equals("exit")) {
            ds.close();
            System.exit(0);
        }
    }

}