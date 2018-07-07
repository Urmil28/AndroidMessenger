package com.ravensltd.ravens.ravenevar.Bot;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by Admin on 03/10/2017.
 */

public class Interpret {
    private String command,S;
    private Context context;
   // public Interpret(String s){
     //   S=s;
   // }
    public Interpret(Context context) {
        this.context = context;
    }

        public String run (String S){
        if(S.contains("<search>"))
        {
            String app = S.split("<search>")[1].split("</search>")[0];
            Uri uri = Uri.parse("http://www.google.com/#q="+app);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);


        }
        else if (S.contains("<camera>")){
            String returnString = "Opening Camera";
            Intent m_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            context.startActivity(m_intent);
            return returnString;

        }
        else if(S.contains("<url>")){
            String returnString = "Opening Url ";
            returnString = returnString + S.split("<url>")[1].split("</url>")[0];
            String app = S.split("<url>")[1].split("</url>")[0];
            Uri webpage = Uri.parse(app);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
            context.startActivity(webIntent);
            return returnString;
        }
        else if(S.contains("<dial>"))
        {
            String returnString = "Dialing ";
            returnString = returnString + S.split("<dial>")[1].split("</dial>")[0];
            String app = S.split("<dial>")[1].split("</dial>")[0];
            command="tel:"+app;
            Uri number = Uri.parse(command);
            Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
            context.startActivity(callIntent);
            return returnString;

        }
        else{

        }
        return S;
    }
}

