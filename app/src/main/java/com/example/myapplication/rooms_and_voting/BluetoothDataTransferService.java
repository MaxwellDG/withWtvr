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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class BluetoothDataTransferService extends Service {

    private BluetoothAdapter bluetoothAdapter;
    private static final UUID UUID_INSECURE = java.util.UUID.fromString("238c71d5-924d-4f72-af44-89b9e2cc9582");
    private BluetoothDevice blueDevice;
    private UUID deviceUUID;
    private ServerSocketConnection serverSocketConnection;
    private ClientSocketConnection clientSocketConnection;
    private SendingBetweenDevices sendingBetweenDevices;

    private VoteCalculations voteCalculations = new VoteCalculations();
    private ArrayList<String> allTheSubmissions = new ArrayList<>();
    private boolean isStreamConnected = false;
    private boolean allAnswersTransferredToHost = false;
    private boolean isFinalAnswerTransferred = false;
    private String theFinalAnswer;

    public static final String TAG = "TAG";

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
            startService();
        } else {
            blueDevice = intent.getParcelableExtra("DEVICE_TO_CONNECT");
            deviceUUID = java.util.UUID.fromString(intent.getStringExtra("UUID"));
            startServiceAsClient(blueDevice, deviceUUID);
        }
        return Service.START_REDELIVER_INTENT;
    }


    private void socketsConnected(BluetoothSocket socket){
        sendingBetweenDevices = new SendingBetweenDevices(socket);
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

    void startServiceAsClient(BluetoothDevice device, UUID uuid){
        clientSocketConnection = new ClientSocketConnection(device, uuid);
        clientSocketConnection.start();
    }

    void write(byte[] out) {
        try {
            sendingBetweenDevices.write(out);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }





    // The connection process if user is the host //
    class ServerSocketConnection extends Thread{
        private BluetoothServerSocket serverSocket;

        ServerSocketConnection() {
            BluetoothServerSocket tempSocket = null;
            try {
                String appName = "com.example.myapplication";
                tempSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, UUID_INSECURE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = tempSocket;
        }

        @Override
        public void run() {
            super.run();
            BluetoothSocket socket = null;
            while (socket == null){
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            socketsConnected(socket);
        }

        public void cancelServerSocket(){
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }







    class ClientSocketConnection extends Thread {
        private BluetoothSocket socket;
        private Handler theHandler;

        ClientSocketConnection(BluetoothDevice device, UUID uuid) {
            blueDevice = device;
            deviceUUID = uuid;
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
                    try {
                        tempSocket[0] = blueDevice.createRfcommSocketToServiceRecord(deviceUUID);
                    } catch (IOException e) {
                        theHandler.postDelayed(this, 3000);
                    }
                    socket = tempSocket[0];
                    if (socket != null) {
                        theHandler.removeCallbacks(this);
                        try {
                            socket.connect();
                            socketsConnected(socket);
                            // the loop only doesn't callback with this possibility //
                        } catch (IOException e) {
                            e.printStackTrace();
                            theHandler.postDelayed(this, 3000);
                        }
                    } else {
                        theHandler.postDelayed(this, 3000);
                    }
                }
            };
            theHandler.post(runnable);
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
            Log.d(TAG, "run: established the input/output stream!");
            // TODO: When multiple devices are connected, might just have to add a counter to the below line before it switches to true //
            isStreamConnected = true;
            int finalAnswersSubmitted = 0;
            super.run();
            byte[] buffer = new byte[1024];
            int bytes = 0;
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "run: Incoming message: " + incomingMessage);
                    if(incomingMessage.contains("***")){
                        String[] submission = incomingMessage.split("\\*\\*\\*");
                        allTheSubmissions.addAll(Arrays.asList(submission));
                        finalAnswersSubmitted++;
                        if(finalAnswersSubmitted == 1){
                            //TODO: obv this will have to change to smth like deviceList.size()//
                            allAnswersTransferredToHost = true;
                        }
                    } else if (incomingMessage.contains("###")) {
                        Log.d(TAG, "run: " + incomingMessage);
                        String[] finalAnswer = incomingMessage.split("###");
                        theFinalAnswer = Arrays.toString(finalAnswer);
                        isFinalAnswerTransferred = true;
                        Log.d(TAG, "run: " + Arrays.toString(finalAnswer));
                    } else if (incomingMessage.equals("BRUYOUREREADY")){
                        Log.d(TAG, "run: " + incomingMessage);
                        Intent intentReady = new Intent("INCOMING_MESSAGE");
                        intentReady.putExtra("THE_MESSAGE", incomingMessage);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentReady);
                    } else {
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

        void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // Methods for confirming bluetooth data transfers have been completed //

    boolean getIsStreamConnected(){
        return isStreamConnected;
    }

    boolean getAreAllAnswersTransferredToHost(){
        return allAnswersTransferredToHost;
    }

    boolean getHasFinalAnswerBeenTransferred(){
        return this.isFinalAnswerTransferred;
    }

    void setHasFinalAnswerBeenTransferred(boolean bool){
        this.isFinalAnswerTransferred = bool;
    }

    ArrayList<String> getAllTheSubmissions(){
        return allTheSubmissions;
    }

    String getTheFinalAnswer(){
        return this.theFinalAnswer;
    }
}
