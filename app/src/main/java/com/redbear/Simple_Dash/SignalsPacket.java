package com.redbear.Simple_Dash;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by agillies on 1/27/2015.



 */
public class SignalsPacket {

  private int cmdType;
    private int mode;
    private int yaw;
    private int ambientLight;
    private int proxLeft;
    private int proxRight;
    private int mtrA1;
    private int mtrA2;
    private int mtrB1;
    private int mtrB2;
    private static  final int PACKET_SIZE = 14;


    public SignalsPacket(){



}


    public String[] decodeData(byte[] rx){

       // String text1 = new String(nameBytes); //for translating back bytes to string
        String[] Data = new String[10];

        byte[] ByteMessageType = {0};


        char MessageType = '1';

        MessageType = (char) rx[0];
        Data[0] = String.valueOf(MessageType);

        //DRMessageTypeName = '1',
        // [type "1" - 1]  [robot Type - 0-255 - 1] [robot color - 0-255 - 1] [code version - 0-255 - 1] [name - string - 10, terminated with a null character]
        if (MessageType == '1') {

        byte robotType = rx[1];
        byte robotColor = rx[2];
        byte codeVersion = rx[3];


        String robotName = new String(Arrays.copyOfRange(rx, 4, 13));
        String robotColorString;

            switch (robotColor) {
                case 0:  robotColorString = "Color Undefined";
                    break;
                case 1:  robotColorString = "Blue";
                    break;
                case 2:  robotColorString = "Red";
                    break;
                case 3:  robotColorString = "Green";
                    break;
                case 4:  robotColorString = "Yellow";
                    break;
                case 5:  robotColorString = "Black";
                    break;
                case 6:  robotColorString = "Orange";
                    break;
                default: robotColorString = "Undefined";

            }

           Data[1] = robotName;
            Data[2] = robotColorString;
            Data[3] = String.valueOf((int) robotType);
            Data[4] = String.valueOf((int) codeVersion);
        }
        //DRMessageTypeSignals = '2',
        // [type - "2" - 1] [ mode - 0-10 - 1] [ yaw - 0-1024 - 2] [ambient light - 0-1024 - 2] [proxLeft - 0-1024 - 2] [proxRight - 0-1024 - 2] [mtrA1 - 0-255 - 1] [mtrA2 - 0-255 - 1] [mtrB1 - 0-255 - 1] [mtrB2 - 0-255 - 1]
        else if (MessageType == '2') {

            int[] intArray = new int[14];


            intArray[0] = (int) rx [0] ; //cmdtype
            intArray[1] = (int) rx [1] ; //mode

            byte[] yawBytes = new byte[2];
            yawBytes[0] = rx [2];
            yawBytes [1] = rx [3];

            byte[] lightBytes = new byte[2];
            lightBytes[0] = rx [4];
            lightBytes [1] = rx [5];

            byte[] proxLeftBytes = new byte[2];
            proxLeftBytes[0] = rx [6];
            proxLeftBytes [1] = rx [7];

            byte[] proxRightBytes = new byte[2];
            proxRightBytes[0] = rx [8];
            proxRightBytes [1] = rx [9];

            mtrA1 = (int) rx[10] & 0xFF;
            mtrA2 = (int) rx[11]& 0xFF;
            mtrB1 = (int) rx[12]& 0xFF;
            mtrB2 = (int) rx[13]& 0xFF;

            yaw = (int) bytesToShort( yawBytes );
            ambientLight= (int) bytesToShort( lightBytes );
            proxLeft = (int) bytesToShort( proxLeftBytes );
           proxRight = (int) bytesToShort( proxRightBytes );

            intArray[2] = yaw;
            intArray[3] = ambientLight;
            intArray[4] = proxLeft;
            intArray[5] = proxRight;
            intArray[6] = mtrA2-mtrA1;
            intArray[7] = mtrB2-mtrB1;

            Data[1] = Integer.toString(intArray[1]); //mode
            Data[2] = Integer.toString(yaw);
            Data[3] = Integer.toString(ambientLight);
            Data[4] = Integer.toString(proxLeft);
            Data[5] = Integer.toString(proxRight);
            Data[6] = Integer.toString(intArray[6]); //leftMotor (right side of body)
            Data[7] = Integer.toString(intArray[7]); //right motor (left side of body)

        }
        //DRMessageTypeAutoRunComplete = '3'
        else if (MessageType == '3') {

            Data[1] = "1";

        }
        else {

            Data[0] = "No Data Found";

                };








        return Data;
    }



    private static short bytesToShort( byte[] data ) {
        ByteBuffer buffer = ByteBuffer.allocate(3);

        buffer.put(data);
        short newData = buffer.getShort(0);
        return newData;
    }

}
