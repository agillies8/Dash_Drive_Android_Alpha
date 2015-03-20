package com.redbear.Simple_Dash;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by agillies on 1/27/2015.
 *
 *
 *  // DRCommandTypeAllStop = '0',
 // DRCommandTypeSetName = '1',
 //DRCommandTypeDirectDrive = '2',
 //DRCommandTypeGyroDrive = '3',
 //DRCommandTypeSetEyes = '4',
 //DRCommandTypeRequestSignals = '6',
 //DRCommandTypeAutoMode = '7',
 */
public class packetBuilder {


private final static String TAG = Chat.class.getSimpleName();


    private static final int MAX_NAME_LENGTH = 9;


    public packetBuilder(){

    };

    public byte[] returnAllStopTx(){
        //DRCommandTypeAllStop = '0',
        String command = "0";
        byte[] b1 = command.getBytes(Charset.forName("UTF-8")); //convert string command to bytes
        byte[] tx = new byte[14]; // initiate 14 byte tx packet
        tx[0] = b1[0];  //set packet 1 to cmd type

        // this loop translates to string for logging purposes
        final StringBuilder stringBuilder = new StringBuilder(tx.length);
        for (byte byteChar : tx) {
            stringBuilder.append(String.format("%02X ", byteChar));
        }
        Log.d(TAG, String.format("All Stop being Sent: %s", stringBuilder.toString()));

        return tx;
    }

    public byte[] returnTypeSetNameTx(byte robotType, byte robotColor, byte codeVersion, String name ) {
// [type "1" - 1]  [robot Type - 0-255 - 1] [robot color - 0-255 - 1] [code version - 0-255 - 1] [name - string - 10, terminated with a null character]
        //DRColorUndefined = 0, 0
        //      DRBlue, 1
        //    DRRedRobot, 2
        //  DRGreenRobot, 3
        //DRYellowRobot, 4
        //DRBlackRobot, 5
        //DROrangeRobot, 6

        // DRCommandTypeSetName = '1',
        String command = "1";

        byte[] b1 = command.getBytes(Charset.forName("UTF-8")); //convert string command to bytes
        byte[] nameBytes = name.getBytes(Charset.forName("UTF-8"));

        if (name.length() > MAX_NAME_LENGTH) {
            Log.e(TAG, String.format("robot name length too long"));
            nameBytes = Arrays.copyOfRange(nameBytes, 0, MAX_NAME_LENGTH);
        }

        byte[] tx = new byte[14]; // initiate 14 byte tx packet
        tx[0] = b1[0];  //set packet 1 to cmd type
        tx[1] = robotType;  //set up pow and dir packets
        tx[2] = robotColor;
        tx[3] = codeVersion;
        tx[4] = nameBytes[0];
        tx[5] = nameBytes[1];
        tx[6] = nameBytes[2];
        tx[7] = nameBytes[3];
        tx[8] = nameBytes[4];
        tx[9] = nameBytes[5];
        tx[10] = nameBytes[6];
        tx[11] = nameBytes[7];
        tx[12] = nameBytes[8];

        // this loop translates to string for logging purposes
        final StringBuilder stringBuilder = new StringBuilder(tx.length);
        for (byte byteChar : tx) {
            stringBuilder.append(String.format("%02X ", byteChar));
        }
        Log.d(TAG, String.format("robot properties set: %s", stringBuilder.toString()));

        return tx;
    }

    public byte[] returnDirectDriveTx(int leftMotor, int rightMotor){

// [type "2" -1]  [mtrA1 - 0-255 - 1] [mtrA2 - 0-255 - 1] [mtrB1 - 0-255 - 1] [mtrB2 - 0-255 - 1]
        //DRCommandTypeDirectDrive = '2',

        String command = "2";
        byte[] b1 = command.getBytes(Charset.forName("UTF-8")); //convert string command to bytes

        byte mtrA1;
        byte mtrA2;
        byte mtrB1;
        byte mtrB2;

        if (leftMotor >= 0) {
             mtrA1 = (byte)(leftMotor);
             mtrA2 = 0;
        } else {
             mtrA1 = 0;
             mtrA2 = (byte)(-1*leftMotor);
        }

        if (rightMotor >= 0) {
             mtrB1 = (byte)(rightMotor );
             mtrB2 = 0;
        } else {
             mtrB1 = 0;
             mtrB2 = (byte)(-1*rightMotor );
        }

        byte[] tx = new byte[14]; // initiate 14 byte tx packet
        tx[0] = b1[0];  //set packet 1 to cmd type
        tx[1] = mtrA1;
        tx[2] = mtrA2;
        tx[3] = mtrB1;
        tx[4] = mtrB2;

        Log.d(TAG, String.format("Left byte right byte  %d  %d",mtrA1, mtrB1 ));

        // this loop translates to string for logging purposes
        final StringBuilder stringBuilder = new StringBuilder(tx.length);
        for (byte byteChar : tx) {
            stringBuilder.append(String.format("%02X ", byteChar));
        }
        Log.d(TAG, String.format("Direct Drive Data being Sent: %s", stringBuilder.toString()));

        return tx;
    }

    public byte[] returnGyroDriveTx(int power, int direction){

        //DRCommandTypeSetEyes = '4',
        //DRCommandTypeRequestSignals = '6',
        //DRCommandTypeAutoMode = '7',

        //DRCommandTypeGyroDrive = '3',
        String command = "3";
        short power_h = (short) power;  //short is 2 bytes, expecting 2 bytes for power, 2 for direction +/- 400 turn rate
        short direction_h = (short) direction;//direction;

        byte[] b1 = command.getBytes(Charset.forName("UTF-8")); //convert string command to bytes

        byte[] powerB = shortToBytes(power_h);
        byte[] directionB = shortToBytes(direction_h);
        byte[] tx = new byte[14]; // initiate 14 byte tx packet
        tx[0] = b1[0];  //set packet 1 to cmd type
        tx[1] = powerB[0];  //set up pow and dir packets
        tx[2] = powerB[1];
        tx[3] = directionB[0];
        tx[4] = directionB[1];

        // this loop translates to string for logging purposes
        final StringBuilder stringBuilder = new StringBuilder(tx.length);
        for (byte byteChar : tx) {
            stringBuilder.append(String.format("%02X ", byteChar));
        }
        Log.d(TAG, String.format("Gyro Drive Data being Sent: %s", stringBuilder.toString()));

        return tx;
    }

    public byte[] returnSetEyesTx(byte red, byte green, byte blue){


        //DRCommandTypeRequestSignals = '6',
        //DRCommandTypeAutoMode = '7',

//    [type "4" -1]  [red - 0-255 - 1] [green - 0-255 - 1] [blue - 0-255 - 1]
        //DRCommandTypeSetEyes = '4',
        String command = "4";
        byte[] b1 = command.getBytes(Charset.forName("UTF-8")); //convert string command to bytes


        byte[] tx = new byte[14]; // initiate 14 byte tx packet
        tx[0] = b1[0];  //set packet 1 to cmd type
        tx[1] = red;  //set up pow and dir packets
        tx[2] = green;
        tx[3] = blue;

        // this loop translates to string for logging purposes
        final StringBuilder stringBuilder = new StringBuilder(tx.length);
        for (byte byteChar : tx) {
            stringBuilder.append(String.format("%02X ", byteChar));
        }
        Log.d(TAG, String.format("Eye color Data being Sent: %s", stringBuilder.toString()));

        return tx;
    }

    public byte[] returnRequestSignalsTx(byte activate){


        //DRCommandTypeRequestSignals = '6',
        //DRCommandTypeAutoMode = '7',

        //DRCommandTypeRequestSignals = '6',
        String command = "6";
        byte[] b1 = command.getBytes(Charset.forName("UTF-8")); //convert string command to bytes

        byte[] tx = new byte[14]; // initiate 14 byte tx packet
        tx[0] = b1[0];  //set packet 1 to cmd type
        tx[1] = activate;  //if activate = 0, will turn off, if 1 will send signals

        // this loop translates to string for logging purposes
        final StringBuilder stringBuilder = new StringBuilder(tx.length);
        for (byte byteChar : tx) {
            stringBuilder.append(String.format("%02X ", byteChar));
        }
        Log.d(TAG, String.format("activate signals Sent: %s", stringBuilder.toString()));

        return tx;
    }

    public byte[] returnAutoModeTx(byte mode){

        //DRCommandTypeAutoMode = '7',
        String command = "7";
        byte[] b1 = command.getBytes(Charset.forName("UTF-8")); //convert string command to bytes

        byte[] tx = new byte[14]; // initiate 14 byte tx packet
        tx[0] = b1[0];  //set packet 1 to cmd type
        tx[1] = mode;  //if activate = 0, will turn off, if 1 will send signals

        // this loop translates to string for logging purposes
        final StringBuilder stringBuilder = new StringBuilder(tx.length);
        for (byte byteChar : tx) {
            stringBuilder.append(String.format("%02X ", byteChar));
        }
        Log.d(TAG, String.format("activate signals Sent: %s", stringBuilder.toString()));

        return tx;
    }

    private static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private static byte[] shortToBytes( short data ) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(data);
        return buffer.array();
    }

}