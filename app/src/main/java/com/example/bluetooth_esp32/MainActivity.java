package com.example.bluetooth_esp32;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    Button btnConexao, btnLed1, btnLed2, btnLed3;

    private static final int SOLICITA_ATIVACAO = 1;
    private static final int SOLICITA_CONEXAO = 2;

    MyBluetoothService.ConnectedThread connectedThread;

    boolean conexao = false;
    private static String MAC = null;


    BluetoothAdapter meuBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice meuDevice = null; //Device disposito remoto q tentara se conectar
    BluetoothSocket meuSocket = null; //Socket responsavel por trocar dados

    UUID MEU_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnConexao = (Button) findViewById(R.id.btnConexao);
        btnLed1 = (Button) findViewById(R.id.btnLed1);
        btnLed2 = (Button) findViewById(R.id.btnLed2);
        btnLed3 = (Button) findViewById(R.id.btnLed3);


        if (meuBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Seu dispositivo não possui bluetooth", Toast.LENGTH_LONG).show();

        } else if (!meuBluetoothAdapter.isEnabled()) {
            Intent ativaBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaBluetooth, SOLICITA_ATIVACAO);
        }

        btnConexao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conexao) {
                    //desconectar
                    try {
                        meuSocket.close();
                        conexao = false;
                        btnConexao.setText("Conectar");
                        Toast.makeText(getApplicationContext(), "Bluetooth foi desconectado: ", Toast.LENGTH_LONG).show();

                    } catch (IOException erro) {
                        Toast.makeText(getApplicationContext(), "Ocorreu um erro: " + erro, Toast.LENGTH_LONG).show();
                    }

                } else {
                    //conectar


                }
            }
        });

        btnLed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(conexao){
                   // MyBluetoothService.connectedThread.enviar("led1");


                }else{
                    Toast.makeText(getApplicationContext(), "Não há coneção ativa !", Toast.LENGTH_LONG).show();

                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case SOLICITA_ATIVACAO:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "O bluetooth foi ativado", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "O bluetooth não foi ativado, o app sera encerrado", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case SOLICITA_CONEXAO:
                if (resultCode == Activity.RESULT_OK) {
                    MAC = data.getExtras().getString(ListaDispositivos.ENDERECO_MAC);

                    //Toast.makeText(getApplicationContext(), "MAC FINAL:" +MAC, Toast.LENGTH_LONG).show();
                    meuDevice = meuBluetoothAdapter.getRemoteDevice(MAC);

                    try {

                        meuSocket = meuDevice.createRfcommSocketToServiceRecord(MEU_UUID);
                        meuSocket.connect();
                        conexao = true;
                        connectedThread = new MyBluetoothService.ConnectedThread(meuSocket);
                        connectedThread.start();
                        btnConexao.setText("Desconectar");

                        Toast.makeText(getApplicationContext(), "Você foi concectado com: " + MAC, Toast.LENGTH_LONG).show();


                    } catch (IOException erro) {
                        conexao = false;

                        Toast.makeText(getApplicationContext(), "Ocorreu um erro: " + erro, Toast.LENGTH_LONG).show();
                    }


                } else {
                    Toast.makeText(getApplicationContext(), "Falha ao obter o MAC", Toast.LENGTH_LONG).show();
                }
        }

    }

    public static class MyBluetoothService {
        private static final String TAG = "MY_APP_DEBUG_TAG";
        private Handler handler; // handler that gets info from Bluetooth service

        // Defines several constants used when transmitting messages between the
        // service and the UI.
        private interface MessageConstants {
            public static final int MESSAGE_READ = 0;
            public static final int MESSAGE_WRITE = 1;
            public static final int MESSAGE_TOAST = 2;

            // ... (Add other message types here as needed.)
        }

        public static class ConnectedThread extends Thread {
            private final BluetoothSocket mmSocket;
            private InputStream mmInStream;
            private OutputStream mmOutStream;
            private byte[] mmBuffer; // mmBuffer store for the stream
            private Handler handler;

            public ConnectedThread(BluetoothSocket socket) {
                //mmSocket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                // Get the input and output streams; using temp objects because
                // member streams are final.
                try {
                    tmpIn = socket.getInputStream();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when creating input stream", e);
                }
                try {
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when creating output stream", e);
                }

               mmInStream = tmpIn;
                mmOutStream = tmpOut;
                mmSocket = null;
            }

            public void run() {
                InputStream tmpIn = null;
                OutputStream tmpOut = null;
                mmInStream = tmpIn;
                mmOutStream = tmpOut;

                mmBuffer = new byte[1024];
                int numBytes; // bytes returned from read()

                // Keep listening to the InputStream until an exception occurs.
                while (true) {
                    while (true) {
                        try {
                           // Read from the InputStream.
                          numBytes = mmInStream.read(mmBuffer);
                         //Send the obtained bytes to the UI activity.
                         Message readMsg = handler.obtainMessage(
                               MessageConstants.MESSAGE_READ, numBytes, -1,
                              mmBuffer);
                           readMsg.sendToTarget();
                          } catch (IOException e) {
                            Log.d(TAG, "Input stream was disconnected", e);
                            break;
                         }
                    }
                }
                }
            }

            // Call this from the main activity to send data to the remote device.
            public void enviar(String dadosEnviar) {
                byte[] msgBuffer = dadosEnviar.getBytes();
                try {
                    mmOutStream.write(msgBuffer);

                    // Share the sent message with the UI activity.
                  //  Message writtenMsg = handler.obtainMessage(
                          //  MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                  //  writtenMsg.sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when sending data", e);

                    // Send a failure message back to the activity.
                    Message writeErrorMsg =
                            handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                    Bundle bundle = new Bundle();
                    bundle.putString("toast",
                            "Couldn't send data to the other device");
                    writeErrorMsg.setData(bundle);
                    handler.sendMessage(writeErrorMsg);
                }
            }

            // Call this method from the main activity to shut down the connection.
            // public void cancel() {
            //   try {
            //    mmSocket.close();
            //  } catch (IOException e) {
            //      Log.e(TAG, "Could not close the connect socket", e);
            //   }
            // }
        }
    }




