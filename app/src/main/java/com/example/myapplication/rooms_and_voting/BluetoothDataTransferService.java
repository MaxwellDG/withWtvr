package com.example.myapplication.rooms_and_voting;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothDataTransferService {

    private BluetoothServerSocket serverSocket;
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private String appName = "com.example.myapplication";
    private static final UUID UUID = java.util.UUID.fromString("238c71d5-924d-4f72-af44-89b9e2cc9582");
    private BluetoothDevice blueDevice;
    private UUID deviceUUID;
    private ServerSocketConnection serverSocketConnection;
    private ClientSocketConnection clientSocketConnection;
    private SendingBetweenDevices sendingBetweenDevices;

    public static final String TAG = "TAG";

    public BluetoothDataTransferService(Context context) {
        Log.d(TAG, "BluetoothDataTransferService: Created Service");
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
        startService();
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
        Log.d(TAG, "startClient: Started.");
        clientSocketConnection = new ClientSocketConnection(device, uuid);
        clientSocketConnection.start();
    }

    void write(byte[] out) {
        Log.d(TAG, "write: Write Called.");
        sendingBetweenDevices.write(out);
    }

    class ServerSocketConnection extends Thread{
        private BluetoothServerSocket serverSocket;

        ServerSocketConnection() {
            Log.d(TAG, "ServerSocketConnection: Created ServerSocket side");
            BluetoothServerSocket tempSocket = null;
            try {
                tempSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, UUID);
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
            // this part brings it all together //
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
            Log.d(TAG, "ClientSocketConnection: Created ClientSocket side");
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
                socket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Log.d(TAG, "run: Could not connect to device with UUID: " + deviceUUID);
                }
            }
            // this part brings it all together //
            socketsConnected(socket);
        }
    }

    class SendingBetweenDevices extends Thread {
        private BluetoothSocket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        private SendingBetweenDevices(BluetoothSocket socket){
            Log.d(TAG, "SendingBetweenDevices: Actually reached the point where we'll start sending data across!");
            this.socket = socket;
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
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "run: Incoming message: " + incomingMessage);
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
