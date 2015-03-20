package com.redbear.Simple_Dash;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Chat extends Activity {
    private final static String TAG = Chat.class.getSimpleName();

    public static final String EXTRAS_DEVICE = "EXTRAS_DEVICE";
    private TextView tv = null;
    private EditText et = null;
    private Button btn = null;
    private String mDeviceName;
    private String mDeviceAddress;
    private RBLService mBluetoothLeService;
    private Map<UUID, BluetoothGattCharacteristic> map = new HashMap<UUID, BluetoothGattCharacteristic>();

    private TextView angleTextView;
    private TextView powerTextView;
    private TextView directionTextView;
    // Importing also other views
    private JoystickView joystick;
    private CheckBox signalsBox;
    private boolean signalsChecked;
    private packetBuilder packetBuilder =  new packetBuilder();
    private boolean gyroDrive;
    private static final double MAX_PWM = 1;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((RBLService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (RBLService.ACTION_GATT_DISCONNECTED.equals(action)) {
            } else if (RBLService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                getGattService(mBluetoothLeService.getSupportedGattService());
            } else if (RBLService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getByteArrayExtra(RBLService.EXTRA_DATA));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second);


        angleTextView = (TextView) findViewById(R.id.angleTextView);
        powerTextView = (TextView) findViewById(R.id.powerTextView);
        directionTextView = (TextView) findViewById(R.id.directionTextView);


        //main activity thread
        addListenerJoystick();
        addListenerOnSignalsCheck();


        Intent intent = getIntent();

        mDeviceAddress = intent.getStringExtra(Device.EXTRA_DEVICE_ADDRESS);
        mDeviceName = intent.getStringExtra(Device.EXTRA_DEVICE_NAME);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent gattServiceIntent = new Intent(this, RBLService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


    }


    public void addListenerJoystick() {
    //Event listener that always returns the variation of the angle in degrees, motion power in percentage and direction of movement


        joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener()

    {

        @Override
        public void onValueChanged ( int angle, int power, int direction, double throttle, int directionLR){
        // TODO Auto-generated method stub
        angleTextView.setText(" " + String.valueOf(angle) + "°");
        powerTextView.setText(" " + String.valueOf(power) + "%");

        BluetoothGattCharacteristic characteristic = map
                .get(RBLService.UUID_BLE_SHIELD_TX);



            if (gyroDrive == false) {




        byte[] tx = packetBuilder.returnGyroDriveTx(power, angle);
        characteristic.setValue(tx);
        mBluetoothLeService.writeCharacteristic(characteristic);





        } else {

        double leftMotor = ( throttle + directionLR) * 255/100 * MAX_PWM;
        double  rightMotor = ( throttle - directionLR) * 255/100 * MAX_PWM;

                Log.d(TAG, String.format("notes Throttle Direction: %d %d", (int) throttle, directionLR));
                Log.d(TAG, String.format("notes LeftMOtor Rightmotor: %d %d", (int) leftMotor, (int) rightMotor));


                byte[] tx = packetBuilder.returnDirectDriveTx( (int) leftMotor, (int) rightMotor);
                characteristic.setValue(tx);
                mBluetoothLeService.writeCharacteristic(characteristic);


        }
    }
    }

    ,JoystickView.DEFAULT_LOOP_INTERVAL);
}

    public void addListenerOnSignalsCheck() {

        signalsBox = (CheckBox) findViewById(R.id.signalsCheckBox);

        signalsBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    Toast.makeText(Chat.this,
                            "Activating Signals", Toast.LENGTH_SHORT).show();

                    BluetoothGattCharacteristic characteristic = map
                            .get(RBLService.UUID_BLE_SHIELD_TX);

                    packetBuilder packetBuilder = new packetBuilder();
                    byte activate = (byte) 1;
                    byte[] tx = packetBuilder.returnRequestSignalsTx(activate);
                    characteristic.setValue(tx);
                    mBluetoothLeService.writeCharacteristic(characteristic);


                }
                signalsChecked = signalsBox.isChecked();
                gyroDrive = signalsBox.isChecked();
            }
        });





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

	@Override
	protected void onResume() {
		super.onResume();

		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			mBluetoothLeService.disconnect();
			mBluetoothLeService.close();

			System.exit(0);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStop() {
		super.onStop();

		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mBluetoothLeService.disconnect();
		mBluetoothLeService.close();

		System.exit(0);
	}

	private void displayData(byte[] byteArray) {
		if (byteArray != null) {

            SignalsPacket signalsPacket = new SignalsPacket();

            String[] signalData = signalsPacket.decodeData(byteArray);

            Log.d(TAG, String.format("Data being received after decode: %s, %s, %s, %s, %s, %s, %s, %s", signalData[0], signalData[1],signalData[2],signalData[3],signalData[4],signalData[5],signalData[6],signalData[7]));
		}
	}

	private void getGattService(BluetoothGattService gattService) {
		if (gattService == null)
			return;

		BluetoothGattCharacteristic characteristic = gattService
				.getCharacteristic(RBLService.UUID_BLE_SHIELD_TX);
		map.put(characteristic.getUuid(), characteristic);

		BluetoothGattCharacteristic characteristicRx = gattService
				.getCharacteristic(RBLService.UUID_BLE_SHIELD_RX);
		mBluetoothLeService.setCharacteristicNotification(characteristicRx,
				true);
		mBluetoothLeService.readCharacteristic(characteristicRx);
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(RBLService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(RBLService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(RBLService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(RBLService.ACTION_DATA_AVAILABLE);

		return intentFilter;
	}
}


/*
switch (direction) {
                    case JoystickView.FRONT:
                        directionTextView.setText(R.string.front_lab);
                        break;
                    case JoystickView.FRONT_RIGHT:
                        directionTextView.setText(R.string.front_right_lab);
                        break;
                    case JoystickView.RIGHT:
                        directionTextView.setText(R.string.right_lab);
                        break;
                    case JoystickView.RIGHT_BOTTOM:
                        directionTextView.setText(R.string.right_bottom_lab);
                        break;
                    case JoystickView.BOTTOM:
                        directionTextView.setText(R.string.bottom_lab);
                        break;
                    case JoystickView.BOTTOM_LEFT:
                        directionTextView.setText(R.string.bottom_left_lab);
                        break;
                    case JoystickView.LEFT:
                        directionTextView.setText(R.string.left_lab);
                        break;
                    case JoystickView.LEFT_FRONT:
                        directionTextView.setText(R.string.left_front_lab);
                        break;
                    default:
                        directionTextView.setText(R.string.center_lab);
                }
 */