package com.google.android.sockettestconnectionwithserverspringonefood;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private Socket socket;
    private String url = "http://192.168.53.103:3333/notification";
    private static final String TAG = MainActivity.class.getSimpleName();
    Button btnSend;
    EditText edt;
    ListView listView;
    TextView textView;
    List<String> stringList=new ArrayList<>();
    ArrayAdapter adapter;
   // String userID = "17";
    String userID = "1";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         btnSend = findViewById(R.id.btnSend);
         edt = findViewById(R.id.edtContent);
         listView = findViewById(R.id.listView);
        textView = findViewById(R.id.textViewUser);

        adapter= new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, stringList );
        listView.setAdapter(adapter);

        connectSocket();
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String text = edt.getText().toString();
                    if(edt.equals(""))
                        return;
                   /* String jsonString = new Gson().toJson(text);
                    JSONObject jsonObject = new JSONObject(text);*/

                /* or
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username);
                jsonObject.put("text", text);
                */

                    socket.emit("send", text, new Ack() {
                        @Override
                        public void call(Object... args) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    edt.setText("");
                                }
                            });
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void connectSocket(){
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.reconnection = true;
            socket = IO.socket(url,options);
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }

        socket.on(Socket.EVENT_CONNECT, this.onConnectEvent);
        socket.on(Socket.EVENT_DISCONNECT, this.onDisconnectEvent);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on("send", this.onSendEvent);
        socket.on("register", this.onRegisterEvent);
        socket.connect();
    }

    private Emitter.Listener onSendEvent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            String jsonString = args[0].toString();
            stringList.add(0,jsonString);
            Log.e(TAG, "onSend : " + jsonString);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });

        }
    };

    private Emitter.Listener onRegisterEvent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            String jsonString = args[0].toString();

            Log.e(TAG, "onRegister : " + jsonString);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(jsonString);
                }
            });

        }
    };

    private Emitter.Listener onConnectEvent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(TAG, "connected to sever");

            socket.emit("register", userID, new Ack(){
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "send userID "+userID+" to register");
                }
            });
        }
    };

    private Emitter.Listener onDisconnectEvent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(TAG, "disconnected to sever");
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Failed to connect", Toast.LENGTH_LONG).show();
                }
            });
        }
    };
}