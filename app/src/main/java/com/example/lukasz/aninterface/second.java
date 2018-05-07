package com.example.lukasz.aninterface;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static java.lang.Math.pow;


public class second extends AppCompatActivity {

   /* Intent intent = new Intent(this, second.class);
        Bundle itemBundle = new Bundle();
        itemBundle.putParcelable("item_extra", mBTDevice);
        intent.putExtras(itemBundle);
        startActivity(intent);*/
   private Bitmap wysoki, niski, brak;
    private EditText edit;
    private String name;
    private View pionek;
    String odczytzpliku; //jest po to by zostaly wybrane wejscia
    private ArrayList<ImageButton> image = new ArrayList<>();
    boolean[] bits = new boolean[8];
    Context mContext;
    BroadcastReceiver mListen3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data=intent.getStringExtra("Message3");
            if(data.equals("0")){
                powrot(pionek);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wszystko);
        wysoki= BitmapFactory.decodeResource(getResources(), R.drawable.wysoki);
        niski= BitmapFactory.decodeResource(getResources(), R.drawable.niski);
        brak= BitmapFactory.decodeResource(getResources(), R.drawable.brak);
        edit=(EditText) findViewById(R.id.editText);
        image.add((ImageButton) findViewById(R.id.imageView0));
        image.add((ImageButton) findViewById(R.id.imageView1));
        image.add((ImageButton) findViewById(R.id.imageView2));
        image.add((ImageButton) findViewById(R.id.imageView3));
        image.add((ImageButton) findViewById(R.id.imageView4));
        image.add((ImageButton) findViewById(R.id.imageView5));
        image.add((ImageButton) findViewById(R.id.imageView6));
        image.add((ImageButton) findViewById(R.id.imageView7));
        for(int i=0;i<8;i++) {
                image.get(i).setImageBitmap(niski);
                image.get(i).setTag("niski");
        }
        try {
            powrot();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mListen3, new IntentFilter("incomingMessage3"));
    }

    private void powrot()throws IOException{
        FileOutputStream fos = openFileOutput("plik.txt", MODE_PRIVATE);
        fos.write("powrot".getBytes());
        fos.close();
    }

    public void zapisz(View view) throws IOException {
        boolean zezwolenie=false;
        name=edit.getText().toString();
        char[] charArray = name.toCharArray();
        if(name.length()>0) {
            for (int x = 0; x < name.length(); x++) {
try{
                if (!(charArray[x] >= 48 && charArray[x] < 58) || !(name.length() <= 4) || !(Integer.parseInt(name) >= 0 && Integer.parseInt(name) <= 1023)||(name.substring(0,1).equals("0")&&name.length()>=2)) {
                    Toast.makeText(this, "Wpisz poprawnie wartosc od 0 do 1023",
                            Toast.LENGTH_LONG).show();
                }
                else {
                    zezwolenie = true;
                }}
catch (NumberFormatException e)
{}
            }
        }
        else zezwolenie=true;
        int l_dzies=0;
        if(zezwolenie){
        // przygotuj tablice na podstawie tagów

        for(int i=0;i<8;i++) {
            if(image.get(i).getTag()=="niski")
                bits[i]=false;
            else bits[i]=true;
        }
        for(int x=0;x<8;x++)
            if(bits[x]==true){
                l_dzies+=pow(2,x);
            }
            if(name.length()==0){
                String zapis = String.valueOf(l_dzies) + "/" ;
                FileOutputStream fos = openFileOutput("plik.txt", MODE_PRIVATE);
                fos.write(zapis.getBytes());
                fos.close();
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                Log.d("zapisalem", zapis);
            }
            else {
                String zapis = String.valueOf(l_dzies) + "<" + name + ">";
                FileOutputStream fos = openFileOutput("plik.txt", MODE_PRIVATE);
                fos.write(zapis.getBytes());
                fos.close();
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
            }}
        /// kod pod spodem jest po to by zostaly wybrane wejscia
        FileOutputStream fos = openFileOutput("drugiplik.txt", MODE_PRIVATE);
        fos.write(String.valueOf(l_dzies).getBytes());
        fos.close();
    }

    public void powrot(View view){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
    public void change(View v) {

            ImageButton ikona = (ImageButton) v;
            name = getResources().getResourceEntryName(ikona.getId()).substring(9, 10);
        if(ikona.getTag()=="niski")
        {
            ikona.setImageBitmap(wysoki);
            ikona.setTag("wysoki");
        }
        else {
            ikona.setImageBitmap(niski);
            ikona.setTag("niski");
        }

        /////////////TEST////////////
        for(int i=0;i<8;i++) {
            if(image.get(i).getTag()=="niski")
                bits[i]=false;
            else bits[i]=true;
        }
        int l_dzies=0;
        for(int x=0;x<8;x++)
            if(bits[x]==true){
                l_dzies+=pow(2,x);
            }
        Intent incomingMessageIntent= new Intent("incomingMessage2");
       incomingMessageIntent.putExtra("Message2", String.valueOf(l_dzies));
       LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);
        /////////////////////
        }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            odczytzpliku=odczyt();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 7; i >= 0; i--) {
            bits[i] = (Integer.parseInt(odczytzpliku) & (1 << i)) != 0;
        }

            for(int x=0;x<8;x++) {
                if (bits[x] == true) {

                    image.get(x).setImageBitmap(wysoki);
                    image.get(x).setTag("wysoki");
                } else {
                    image.get(x).setImageBitmap(niski);
                    image.get(x).setTag("niski");
                }
            }
        Intent incomingMessageIntent= new Intent("incomingMessage2");
        incomingMessageIntent.putExtra("Message2", String.valueOf(odczytzpliku));
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);
        }
    /// kod pod spodem jest po to by zostaly wybrane wejscia
    private String odczyt()throws IOException {
        FileInputStream fis = openFileInput("drugiplik.txt");
        BufferedInputStream bis = new BufferedInputStream(fis);
        StringBuffer sb = new StringBuffer();
        while(bis.available() !=0){
            char c = (char) bis.read();
            sb.append(c);
        }
        return sb.toString();
    }
    }


