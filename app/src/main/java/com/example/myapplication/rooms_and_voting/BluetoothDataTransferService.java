package com.example.myapplication.rooms_and_voting;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;

public class BluetoothDataTransferService extends Service {

    private BluetoothServerSocket serverSocket;
    private BluetoothAdapter bluetoothAdapter;
    private String appName = "com.example.myapplication";
    private static final UUID UUID_INSECURE = java.util.UUID.fromString("238c71d5-924d-4f72-af44-89b9e2cc9582");
    private BluetoothDevice blueDevice;
    private UUID deviceUUID;
    private ServerSocketConnection serverSocketConnection;
    private ClientSocketConnection clientSocketConnection;
    private SendingBetweenDevices sendingBetweenDevices;
    private ArrayList<String> theFINALANSWER = new ArrayList<>();
    private ArrayList<String> allTheSubmissions = new ArrayList<>();
    private String[] finalAnswer;

    public static final String TAG = "TAG";

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
        // TODO: the return might be incorrect //
        return Service.START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TheBinder();
    }

    private void socketsConnected(BluetoothSocket socket){
        Log.d(TAG, "socketsConnected: Connected. Transitioning to next phase.");
        sendingBetweenDevices = new SendingBetweenDevices(socket);
        sendingBetweenDevices.start();
    }

    private synchronized void startService(){
        if (clientSocketConnection != null){
            Log.d(TAG, "startService: clientsocket is NOT null");
            try {
                Log.d(TAG, "startService: Closing the pre-existing clientsocket so we can start a new one.");
                clientSocketConnection.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (serverSocketConnection == null){
            Log.d(TAG, "startService: serversocket is being started up on this side");
            serverSocketConnection = new ServerSocketConnection();
            serverSocketConnection.start();
        }
    }

    void startServiceAsClient(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startClient: Started. Device is " + device);
        clientSocketConnection = new ClientSocketConnection(device, uuid);
        clientSocketConnection.start();
    }

    void write(byte[] out) {
        Log.d(TAG, "write: Write Called.");
        try {
            sendingBetweenDevices.write(out);
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.d(TAG, "write: Connections haven't finished being established.");
        }
    }

    void addHostsAnswers(ArrayList<String> words){
        allTheSubmissions = words;
    }

    void analyzeDataForFinalAnswer(){
        Log.d(TAG, "getTheFINALANSWER: SHOW ME ALL SUBMISSIONS " + allTheSubmissions);
        ArrayList<String> alreadyCounted = new ArrayList<>();
        HashMap<String, Integer> frequencyNo = new HashMap<>();
        ArrayList<Integer> justTheNumbers = new ArrayList<>();
        for (int i = 0; i < allTheSubmissions.size() ; i++ ){
            if (!alreadyCounted.contains(allTheSubmissions.get(i))){
                int frequency = Collections.frequency(allTheSubmissions, allTheSubmissions.get(i));
                alreadyCounted.add(allTheSubmissions.get(i));
                frequencyNo.put(allTheSubmissions.get(i), frequency);
                justTheNumbers.add(frequency);
            }
        }
        int max = 0;
        for (int counter = 1; counter < justTheNumbers.size(); counter++) {
            if (justTheNumbers.get(counter) > max) {
                max = justTheNumbers.get(counter);
            }
        }
        int finalMax = max;
        frequencyNo.forEach(new BiConsumer<String, Integer>() {
            @Override
            public void accept(String s, Integer integer) {
                if (integer == finalMax){
                    Log.d(TAG, "accept: THE WEIRD FUNCTION DECIDED : " + s);
                    theFINALANSWER.add(s);
                }
            }
        });
        StringBuilder finalBigWord = new StringBuilder();
        for (String word : theFINALANSWER){
            word = word + "###";
            finalBigWord.append(word);
        }

        write(finalBigWord.toString().getBytes(Charset.defaultCharset()));
    }

    String getTheFINALANSWER(){
        Log.d(TAG, "getTheFINALANSWER: " + finalAnswer);
        return Arrays.toString(finalAnswer);
    }


    // Essentially the communicator class that's actually called a binder //

    class TheBinder extends Binder{
        BluetoothDataTransferService getBluetoothDataTransferService(){
            return BluetoothDataTransferService.this;
        }
    }

    // The connection process if user is the host //

    class ServerSocketConnection extends Thread{
        private BluetoothServerSocket serverSocket;

        ServerSocketConnection() {
            Log.d(TAG, "ServerSocketConnection: Created ServerSocket side");
            BluetoothServerSocket tempSocket = null;
            try {
                tempSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, UUID_INSECURE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = tempSocket;
        }

        @Override
        public void run() {
            Log.d(TAG, "run: Doing ServerSocket connection side stuff)");
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

    class ClientSocketConnection extends Thread{
        private BluetoothSocket socket;

        ClientSocketConnection(BluetoothDevice device, UUID uuid){
            Log.d(TAG, "ClientSocketConnection: device is... " + device);
            blueDevice = device;
            deviceUUID = uuid;
        }

        @Override
        public void run() {
            Log.d(TAG, "run: Doing clientside thread stuff");
            super.run();
            BluetoothSocket tempSocket = null;
            bluetoothAdapter.cancelDiscovery();

            try {
                tempSocket = blueDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = tempSocket;
            try {
                boolean connected = false;
                while(!connected) {
                    try {
                        if (socket != null) {
                            //TODO: this didnt work. don't do hard things when youre drunk moron //
                            socket.connect();
                        }
                        connected = true;
                    } catch (NullPointerException e){
                        Log.d(TAG, "run: fart on my dick");
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Log.d(TAG, "run: Could not connect to device with UUID_INSECURE: " + deviceUUID);
                }
            }
            socketsConnected(socket);
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
            Log.d(TAG, "run: establishing the inputstream!");
            super.run();
            byte[] buffer = new byte[1024];
            int bytes = 0;
            // listener.connectionCreated(true);
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "run: Incoming message: " + incomingMessage);
                    if(incomingMessage.contains("***")){
                        String[] submission = incomingMessage.split("\\*\\*\\*");
                        allTheSubmissions.addAll(Arrays.asList(submission));
                    } else if (incomingMessage.contains("###")) {
                        Log.d(TAG, "run: " + incomingMessage);
                        finalAnswer = incomingMessage.split("###");
                        Log.d(TAG, "run: " + Arrays.toString(finalAnswer));
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
            String outgoingMessage = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + outgoingMessage);
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
