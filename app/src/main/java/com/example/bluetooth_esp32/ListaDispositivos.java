package com.example.bluetooth_esp32;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class ListaDispositivos extends ListActivity {

    private BluetoothAdapter meuBluetoothAdapter2 = null;
    static String ENDERECO_MAC = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> ArrayBluetooth = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        meuBluetoothAdapter2 = BluetoothAdapter.getDefaultAdapter();

        //getBlodedDevices lista os dispotivos pareados, suprimi a permissao de uso do bluetooth
             @SuppressLint("MissingPermission") Set<BluetoothDevice> dispositivosPareados = meuBluetoothAdapter2.getBondedDevices();

        if(dispositivosPareados.size()>0){

            for(BluetoothDevice dispositivo:dispositivosPareados){
                @SuppressLint("MissingPermission") String nomeBt = dispositivo.getName();
                String macBt = dispositivo.getAddress();
                ArrayBluetooth.add(nomeBt + "\n" + macBt);
            }
        }

        setListAdapter(ArrayBluetooth);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String informacaoGeral = ((TextView)v).getText().toString();

        //Toast.makeText(getApplicationContext(), "Info: " +informacaoGeral, Toast.LENGTH_SHORT).show();

        //Pega a variavel informacao geral e retira apenas os ultimos 17 caracterers
        String enderecoMac = informacaoGeral.substring(informacaoGeral.length()-17);

        //Toast.makeText(getApplicationContext(), "mac: " +enderecoMac, Toast.LENGTH_SHORT).show();

        Intent retornaMac = new Intent();
        retornaMac.putExtra(ENDERECO_MAC,enderecoMac);
        setResult(RESULT_OK,retornaMac);
        finish();

    }
}
