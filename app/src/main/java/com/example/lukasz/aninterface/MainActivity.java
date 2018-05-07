package com.example.lukasz.aninterface;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    private Bitmap wysoki, niski, brak;
    private TextView monitoring;
    public TextView polaczono, lista;
    public boolean aktywnosc;
    Button polacz, ustawienia;
    String odczytzpliku;
    public TextView bluetext;
    private boolean running;
    int discovertimer;
    boolean discoverflag,dlareceiverow;
    Context mContext;
    ArrayList<ImageButton> image = new ArrayList<>();
    boolean[] bits = new boolean[8];
    //////BT/////
    BluetoothAdapter mBluetoothAdapter;
    BluetoothConnectionService mBluetoothConnection;
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothDevice mBTDevice;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;

    ///////BT//////

    public void polacz(View v) {
        enableDisableBT();
    }

    public void ustawienia(View v) throws IOException {
        Intent i = new Intent(this, second.class);
        String wiadomosc=odczytdwa();
        i.putExtra("wiadomosc", wiadomosc);///jest po to by zostaly wybrane wejscia
        startActivity(i);
    }


    public void setimages(String ustaw){
    int y = Integer.parseInt(ustaw);
    for (int i = 7; i >= 0; i--) {
        bits[i] = (y & (1 << i)) != 0;
    }
    for (int i = 0; i < 8; i++) {
        if (bits[i]) {
            image.get(i).setImageBitmap(wysoki);
        } else image.get(i).setImageBitmap(niski);
    }
    mBluetoothConnection.flag = false;
    monitoring.setText("Podgląd aktywny");
    monitoring.setTextColor(Color.parseColor("#ff99cc00"));
    }
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        if(dlareceiverow) {
            unregisterReceiver(mBroadcastReceiver1);
            unregisterReceiver(mBroadcastReceiver3);
        }
        unregisterReceiver(mBroadcastReceiver4);
        mBluetoothAdapter.cancelDiscovery();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mListen);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        bluetext.setText("Bluetooth \n wyłączony");
                        bluetext.setTextColor(Color.parseColor("#ffcc0000"));
                        polacz.setText("Połącz");
                        polaczono.setVisibility(TextView.INVISIBLE);
                        ustawienia.setEnabled(false);
                        monitoring.setText("Podgląd nieaktywny");
                        monitoring.setTextColor(Color.parseColor("#ffcc0000"));
                        mBTDevices = new ArrayList<>();
                        lvNewDevices.setAdapter(null);
                        for(int i=0;i<8;i++)
                        image.get(i).setImageBitmap(brak);
                        lista.setVisibility(View.INVISIBLE);

                        try {
                            FileOutputStream fos = openFileOutput("drugiplik.txt", MODE_PRIVATE);
                            fos.write(String.valueOf(0).getBytes());
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent incomingMessageIntent= new Intent("incomingMessage3");
                        incomingMessageIntent.putExtra("Message3", String.valueOf(0));

                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);
                        break;


                    case BluetoothAdapter.STATE_ON:
                        dlareceiverow=true;// by nie wyskakiwał bład przy wyrejestrowywaniu
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        bluetext.setText("Bluetooth \n włączony");
                        bluetext.setTextColor(Color.parseColor("#ff00ddff"));
                        polacz.setText("Rozłącz");
                        Discover();
                        discoverflag=true;
                        lista.setVisibility(TextView.VISIBLE);
                        break;

                }
            }
        }
    };
    /**
     * Broadcast Receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire.
     */
    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                if(device.getName().equals("HC-06")||device.getName().equals("HC-05"))
                discoverflag=false;
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
                lista.setVisibility(TextView.INVISIBLE);

            }
        }
    };

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    mBTDevice = mDevice;

                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                    polaczono.setVisibility(TextView.INVISIBLE);
                }
            }
        }
    };
    BroadcastReceiver mListen = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data=intent.getStringExtra("Message");
            setimages(data);

        }
    };
    BroadcastReceiver mListen2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data=intent.getStringExtra("Message2");
                data = "u" + data + "k";
                mBluetoothConnection.write(data.getBytes(Charset.defaultCharset()));
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aktywnosc=false;
        wysoki= BitmapFactory.decodeResource(getResources(), R.drawable.wysoki);
        niski= BitmapFactory.decodeResource(getResources(), R.drawable.niski);
        brak= BitmapFactory.decodeResource(getResources(), R.drawable.brak);
        polaczono=(TextView) findViewById(R.id.textView8);
        monitoring=(TextView) findViewById(R.id.textView9);
        bluetext=(TextView) findViewById(R.id.bluetext);
        lista=(TextView) findViewById(R.id.lista);
        polacz=(Button) findViewById(R.id.button1);
        ustawienia=(Button) findViewById(R.id.button2);
        polaczono.setVisibility(TextView.INVISIBLE);
        lista.setVisibility(TextView.INVISIBLE);
        image.add((ImageButton) findViewById(R.id.imageView0));
        image.add((ImageButton) findViewById(R.id.imageView1));
        image.add((ImageButton) findViewById(R.id.imageView2));
        image.add((ImageButton) findViewById(R.id.imageView3));
        image.add((ImageButton) findViewById(R.id.imageView4));
        image.add((ImageButton) findViewById(R.id.imageView5));
        image.add((ImageButton) findViewById(R.id.imageView6));
        image.add((ImageButton) findViewById(R.id.imageView7));
        ustawienia.setEnabled(false);
        ///////BT/////////
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();
        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);
        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, BTIntent);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        lvNewDevices.setOnItemClickListener(MainActivity.this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mListen, new IntentFilter("incomingMessage"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mListen2, new IntentFilter("incomingMessage2"));
        //////////BT/////////
               if(mBluetoothAdapter.isEnabled())
        {
            bluetext.setText("Bluetooth \n włączony");
            bluetext.setTextColor(Color.parseColor("#ff00ddff"));
            polacz.setText("Rozłącz");
            Discover();
            discoverflag=true;
            lista.setVisibility(TextView.VISIBLE);
        }
        else {
                   bluetext.setText("Bluetooth \n wyłączony");
               }
        runTimer2();
/// kod pod spodem jest po to by zostaly wybrane wejscia
        try {FileOutputStream fos = openFileOutput("drugiplik.txt", MODE_PRIVATE);
            fos.write("0".getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /////////////////////////
    }
    ////BLUETOOTH STUFF ///
    public void enableDisableBT(){
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
        }
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }

    }
    public void Discover() {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }
    public void startConnection(){
        startBTConnection(mBTDevice,MY_UUID_INSECURE);
    }

    /**
     * starting chat service method
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

        mBluetoothConnection.startClient(device,uuid);
    }
    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();
        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(i).createBond();
            mBTDevice = mBTDevices.get(i);
         mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
        }
       startConnection();
        polaczono.setVisibility(TextView.VISIBLE);
        ustawienia.setEnabled(true);
        lvNewDevices.setAdapter(null);
        runTimer();

    }

    private void powrot()throws IOException {
        FileOutputStream fos = openFileOutput("plik.txt", MODE_PRIVATE);
        fos.write("powrot".getBytes());
        fos.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            odczytzpliku=odczyt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(aktywnosc) {
            if(odczytzpliku.equals("powrot")){
                    lista.setVisibility(TextView.INVISIBLE);
                    bluetext.setText("Bluetooth \n włączony");
                    bluetext.setTextColor(Color.parseColor("#ff00ddff"));
                    polacz.setText("Rozłącz");
            }
            else {
                sendbybluetooth(odczytzpliku);
                try {
                    powrot();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            }
        if(!(mBluetoothAdapter.isEnabled())){
            bluetext.setText("Bluetooth \n wyłączony");
            bluetext.setTextColor(Color.parseColor("#ffcc0000"));
            polacz.setText("Połącz");
        }
        aktywnosc=true;
        }

    private void sendbybluetooth(String odczytzpliku) {
        ArrayList<Character> chars = new ArrayList<Character>();
        for (char c : odczytzpliku.toCharArray()) {
            chars.add(c);
        }

        if(chars.contains('/')){
            odczytzpliku="u"+odczytzpliku.substring(0,odczytzpliku.length()-1)+"k";
            byte[] bytes=odczytzpliku.getBytes(Charset.defaultCharset());
           mBluetoothConnection.write(bytes);
            //Toast.makeText(this,odczytzpliku,Toast.LENGTH_LONG).show();
        }
        else{

            int index1=chars.indexOf('<');
            int index2=chars.indexOf('>');
            String part1="u"+odczytzpliku.substring(0,index1)+"k";
            String part2="a"+odczytzpliku.substring(index1+1,index2)+"z";
            // najpierw wyslij informacje o wejsciach
            byte[] bytes=part1.getBytes(Charset.defaultCharset());
            mBluetoothConnection.write(bytes);
            // a teraz wartosc sygnału analogowego
            bytes=part2.getBytes(Charset.defaultCharset());
            mBluetoothConnection.write(bytes);
          //  Toast.makeText(this, part1+" oraz "+part2,
            //        Toast.LENGTH_LONG).show();
        }

    }

    private String odczyt()throws IOException {
        FileInputStream fis = openFileInput("plik.txt");
        BufferedInputStream bis = new BufferedInputStream(fis);
        StringBuffer sb = new StringBuffer();
        while(bis.available() !=0){
            char c = (char) bis.read();
            sb.append(c);
        }
        return sb.toString();
    }
    private String odczytdwa()throws IOException {
        FileInputStream fis = openFileInput("drugiplik.txt");
        BufferedInputStream bis = new BufferedInputStream(fis);
        StringBuffer sb = new StringBuffer();
        while(bis.available() !=0){
            char c = (char) bis.read();
            sb.append(c);
        }
        return sb.toString();
    }
   private void runTimer() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(!mBluetoothConnection.running)
                    startagain();
             //  if(mBluetoothConnection.flag)
              // setimages(mBluetoothConnection.message);
                handler.postDelayed(this, 500);
            }
        });
    }
    private void runTimer2() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(discoverflag&&!polacz.getText().equals("Połącz")) {
                    polaczono.setVisibility(TextView.INVISIBLE);
                    ustawienia.setEnabled(false);
                    monitoring.setText("Podgląd nieaktywny");
                    monitoring.setTextColor(Color.parseColor("#ffcc0000"));
                    mBTDevices = new ArrayList<>();
                    lvNewDevices.setAdapter(null);
                    Discover();
                }
                handler.postDelayed(this, 5000);
            }
        });
    }
    private void startagain() {
        mBluetoothConnection.running=true;
        polaczono.setVisibility(TextView.INVISIBLE);
        ustawienia.setEnabled(false);
        monitoring.setText("Podgląd nieaktywny");
        monitoring.setTextColor(Color.parseColor("#ffcc0000"));
        mBTDevices = new ArrayList<>();
        lvNewDevices.setAdapter(null);
        for(int i=0;i<8;i++)
            image.get(i).setImageBitmap(brak);
        if(mBluetoothAdapter.isEnabled())
        {
            Discover();
            discoverflag=true;
            lista.setVisibility(TextView.VISIBLE);
        }
    }
}
