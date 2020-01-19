package com.example.myapplication.rooms_and_voting;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class BluetoothDataTransferService extends Service {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice blueDevice;
    private static final String[] UUIDS_FOR_CLIENTS = {"238c71d5-924d-4f72-af44-89b9e2cc9582", "b44d57dd-b28d-4917-aabe-e48b8ab6157a",
            "8a736e75-6963-4ed0-88b6-77afb46750ff", "aa3fdcc1-aac0-4a62-b05f-41543934b2f7",
            "baa489c4-d079-4e52-8878-1f88714724b3", "197cb4f5-31a3-49e0-a485-5c319f7615a2",
            "f319d442-4cae-49d2-b0a2-60900a9156dd", "d115dd7c-c5d2-466b-b1c4-8b54ce9fecc4"};
    private ArrayList<BluetoothDevice> allTheDevices;
    private ServerSocketConnection serverSocketConnection;
    private ClientSocketConnection clientSocketConnection;

    private ArrayList<String> allTheSubmissions = new ArrayList<>();

    private boolean allAnswersTransferredToHost = false;
    private boolean isFinalAnswerTransferred = false;
    private boolean isAllConnectionsEstablished = false;

    private String theFinalAnswer;
    private boolean isHost = false;

    private int counterConnectionsMade = 0;
    private int counterFinalAnswersSubmitted = 0;
    private ArrayList<SendingBetweenDevices> allDevicesCommunicator = new ArrayList<>();


    // Essentially the communicator class that's actually called a binder //
    class TheBinder extends Binder{
        BluetoothDataTransferService getBluetoothDataTransferService(){
            return BluetoothDataTransferService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TheBinder();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        int typeOfUser = intent.getIntExtra("SERVICE_ID", 0);
        if (typeOfUser == 1){
            isHost = true;
            allTheDevices = intent.getParcelableArrayListExtra("DEVICE_LIST");
            startService();
        } else {
            blueDevice = intent.getParcelableExtra("DEVICE_TO_CONNECT");
            startServiceAsClient(blueDevice);
        }
        return Service.START_REDELIVER_INTENT;
    }


    private void socketsConnected(BluetoothSocket socket){
        SendingBetweenDevices sendingBetweenDevices = new SendingBetweenDevices(socket);
        allDevicesCommunicator.add(sendingBetweenDevices);
        sendingBetweenDevices.start();
    }

    private synchronized void startService(){
        if (clientSocketConnection != null){
            try {
                clientSocketConnection.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (serverSocketConnection == null){
            serverSocketConnection = new ServerSocketConnection();
            serverSocketConnection.start();
        }
    }

    void startServiceAsClient(BluetoothDevice device){
        clientSocketConnection = new ClientSocketConnection(device);
        clientSocketConnection.start();
    }

    void write(byte[] out) {
        try {
            for(SendingBetweenDevices aDevice : allDevicesCommunicator) {
                aDevice.write(out);
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    ArrayList<SendingBetweenDevices> getAllConnections(){
        return allDevicesCommunicator;
    }

    ClientSocketConnection getClientSocketConnection(){
        return clientSocketConnection;
    }





    // The connection process if user is the host //
    class ServerSocketConnection extends Thread{
        private BluetoothServerSocket serverSocket;
        private BluetoothSocket workingSocket;
        private int counterOfConnectionsMade = 0;

        ServerSocketConnection() {
        }

        @Override
        public void run() {
            super.run();
            while (counterOfConnectionsMade < allTheDevices.size()){
                workingSocket = null;
                String appName = "com.example.myapplication";
                try {
                    for (int i = 0; i < 1; i++) {
                        serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, UUID.fromString(UUIDS_FOR_CLIENTS[i]));
                        try {
                            workingSocket = serverSocket.accept();
                            if (workingSocket != null) {
                                socketsConnected(workingSocket);
                                cancelServerSocket(serverSocket);
                                counterOfConnectionsMade++;
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void cancelServerSocket(BluetoothServerSocket socket){
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }







    class ClientSocketConnection extends Thread {
        private BluetoothSocket socket;
        private Handler theHandler;

        ClientSocketConnection(BluetoothDevice device) {
            blueDevice = device;
            theHandler = new Handler();
        }

        @Override
        public void run() {
            super.run();
            final BluetoothSocket[] tempSocket = {null};
            bluetoothAdapter.cancelDiscovery();

            // a loop to continue trying to search/connect for when the host joins the CreateDestination lobby //
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 1; i++) {
                        try {
                            tempSocket[0] = blueDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(UUIDS_FOR_CLIENTS[i]));
                            socket = tempSocket[0];
                            if (socket != null) {
                                theHandler.removeCallbacks(this);
                                try {
                                    socket.connect();
                                    socketsConnected(socket);
                                    break;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    theHandler.postDelayed(this, 2000);
                                }
                            } else {
                                theHandler.postDelayed(this, 2000);
                            }
                        } catch (IOException e) {
                            theHandler.postDelayed(this, 2000);
                        }
                    }
                    }
                };
            theHandler.post(runnable);
            }


        void cancelClientSocket(){
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }







    class SendingBetweenDevices extends Thread {
        private InputStream inputStream;
        private OutputStream outputStream;


        private SendingBetweenDevices(BluetoothSocket socket){
            InputStream tempImp = null;
            OutputStream tempOut = null;
            try {
                tempImp = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                tempOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = tempImp;
            outputStream = tempOut;
        }

        @Override
        public void run() {
            super.run();
            counterConnectionsMade++;

            if (isHost && counterConnectionsMade >= allTheDevices.size()){
                isAllConnectionsEstablished = true;
            }

            byte[] buffer = new byte[1024];
            int bytes = 0;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    if(incomingMessage.contains("***")){
                        // messageType: sending final answers //
                        String[] submission = incomingMessage.split("\\*\\*\\*");
                        allTheSubmissions.addAll(Arrays.asList(submission));
                        counterFinalAnswersSubmitted++;
                        if(counterFinalAnswersSubmitted >= allTheDevices.size()){
                            allAnswersTransferredToHost = true;
                        }
                    } else if (incomingMessage.contains("###")) {
                        // messageType: the actual final answer //
                        String[] finalAnswer = incomingMessage.split("###");
                        theFinalAnswer = Arrays.toString(finalAnswer);
                        isFinalAnswerTransferred = true;
                    } else if (incomingMessage.contains("!!!")){
                        // messageType: settings and notification of connection established //
                        Intent intentReady = new Intent("INCOMING_MESSAGE");
                        String[] outGoing = incomingMessage.split("!!!");
                        String outGoingMessage = Arrays.toString(outGoing);
                        intentReady.putExtra("THE_SETTINGS_AND_NOTIFICATION", outGoingMessage);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentReady);
                    } else if (incomingMessage.equals("BRUWEREADY")) {
                        // messageType: master notifying slaves that ActualVoting.class will begin //
                        Intent intentReady = new Intent("INCOMING_MESSAGE");
                        intentReady.putExtra("THE_MESSAGE", incomingMessage);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentReady);
                    } else {
                        // messageType: sending an option for CreateDestinations.class //
                        if(isHost){
                            // Master receiving incoming messages and resending them to all the slaves //
                            BluetoothDataTransferService.this.write(incomingMessage.getBytes(Charset.defaultCharset()));
                        }
                        Intent intent = new Intent("INCOMING_MESSAGE");
                        intent.putExtra("THE_MESSAGE", incomingMessage);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        void cancelConnection(){
            try {
                inputStream.close();
                outputStream.close();
                if(clientSocketConnection != null){
                    clientSocketConnection.socket.close();
                } else if (serverSocketConnection.serverSocket != null){
                    serverSocketConnection.serverSocket.close();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // Methods for confirming bluetooth data transfers have been completed //

    boolean getAreAllConnectionsEstablished(){
        return isAllConnectionsEstablished;
    }

    boolean getAreAllAnswersTransferredToHost(){
        return allAnswersTransferredToHost;
    }

    boolean getHasFinalAnswerBeenTransferred(){
        return this.isFinalAnswerTransferred;
    }

    void setHasFinalAnswerBeenTransferred(){
        this.isFinalAnswerTransferred = true;
    }

    ArrayList<String> getAllTheSubmissions(){
        return allTheSubmissions;
    }

    String getTheFinalAnswer(){
        return this.theFinalAnswer;
    }
}
