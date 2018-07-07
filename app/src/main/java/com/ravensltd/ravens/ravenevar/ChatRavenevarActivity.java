package com.ravensltd.ravens.ravenevar;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.ravensltd.ravens.ravenevar.Adapter.RavenevarAdapter;
import com.ravensltd.ravens.ravenevar.Bot.Interpret;
import com.ravensltd.ravens.ravenevar.Pojo.RavenevarMsgItem;
import com.ravensltd.ravens.R;

import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.Graphmaster;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.PCAIMLProcessorExtension;
import org.alicebot.ab.Timer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ChatRavenevarActivity extends AppCompatActivity {

    public Toolbar mToolBar;

    public Context mContext ;
    private Interpret interpret;
    private ListView mListView;
    private FloatingActionButton mButtonSend;
    private EditText mEditTextMessage;
    private ImageView mImageView;
    public Bot bot;
    public static Chat chat;
    private RavenevarAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ravenevar);

        mToolBar=(Toolbar)findViewById(R.id.chat_ravenevar_toolabar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("RaveNevaR");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //// TODO: 7/10/17  change list view to recycler
        mListView = (ListView) findViewById(R.id.chat_ravenevar_listview);
        mButtonSend = (FloatingActionButton) findViewById(R.id.chat_ravenevar_send);
        mEditTextMessage = (EditText) findViewById(R.id.chat_ravenevar_edittext);
        mAdapter = new RavenevarAdapter(this, new ArrayList<RavenevarMsgItem>());
        mListView.setAdapter(mAdapter);
        mContext=this;

        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEditTextMessage.getText().toString();
                //bot
                String response = chat.multisentenceRespond(mEditTextMessage.getText().toString());

                if(response.contains("<oob>")){
                    interpret=new Interpret(mContext);
                    response =interpret.run(response);
                }
                Log.d("myTag1", response);
                if (TextUtils.isEmpty(message)) {
                    return;
                }
                sendMessage(message);
                mimicOtherMessage(response);
                mEditTextMessage.setText("");
                mListView.setSelection(mAdapter.getCount() - 1);
            }
        });
        //checking SD card availablility
        boolean a = isSDCARDAvailable();
        //receiving the assets from the app directory
        AssetManager assets = getResources().getAssets();
        File jayDir = new File(Environment.getExternalStorageDirectory().toString() + "/hari/bots/Hari");

        boolean b = jayDir.mkdirs();
        if (jayDir.exists()) {
            //Reading the file
            try {
                for (String dir : assets.list("Hari")) {
                    File subdir = new File(jayDir.getPath() + "/" + dir);
                    boolean subdir_check = subdir.mkdirs();
                    for (String file : assets.list("Hari/" + dir)) {
                        Log.d("myTag2","Hari/" + dir + "/" + file );
                        File f = new File(jayDir.getPath() + "/" + dir + "/" + file);
                        if (f.exists()) {
                            continue;
                        }
                        InputStream in;
                        OutputStream out ;
                        in = assets.open("Hari/" + dir + "/" + file);
                        out = new FileOutputStream(jayDir.getPath() + "/" + dir + "/" + file);
                        //copy file from assets to the mobile's SD card or any secondary memory
                        copyFile(in, out);
                        in.close();
                        in = null;
                        out.flush();
                        out.close();
                        out = null;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //get the working directory
        MagicStrings.root_path = Environment.getExternalStorageDirectory().toString() + "/hari";
        System.out.println("Working Directory = " + MagicStrings.root_path);
        AIMLProcessor.extension =  new PCAIMLProcessorExtension();
        //Assign the AIML files to bot for processing
        bot = new Bot("Hari", MagicStrings.root_path, "chat");
        chat = new Chat(bot);
        String[] args = null;
        mainFunction(args);

    }

    private void sendMessage(String message) {
        RavenevarMsgItem ravenevarMsgItem = new RavenevarMsgItem(message, true, false);
        mAdapter.add(ravenevarMsgItem);

        //mimicOtherMessage(message);
    }

    private void mimicOtherMessage(String message) {
        RavenevarMsgItem ravenevarMsgItem = new RavenevarMsgItem(message, false, false);
        mAdapter.add(ravenevarMsgItem);
    }

    private void sendMessage() {
        RavenevarMsgItem ravenevarMsgItem = new RavenevarMsgItem(null, true, true);
        mAdapter.add(ravenevarMsgItem);

        mimicOtherMessage();
    }

    private void mimicOtherMessage() {
        RavenevarMsgItem ravenevarMsgItem = new RavenevarMsgItem(null, false, true);
        mAdapter.add(ravenevarMsgItem);
    }
    //check SD card availability
    public static boolean isSDCARDAvailable(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)? true :false;
    }
    //copying the file
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
    //Request and response of user and the bot
    public static void mainFunction (String[] args) {
        MagicBooleans.trace_mode = false;
        System.out.println("trace mode = " + MagicBooleans.trace_mode);
        Graphmaster.enableShortCuts = true;
        Timer timer = new Timer();
        String request = "Hello.";
        String response = chat.multisentenceRespond(request);


        System.out.println("Human: "+request);
        System.out.println("Robot: " + response);
    }

}