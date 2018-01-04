package merelo.com.cerveceame;

import android.app.ActionBar;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Merelo on 04/12/2017.
 */

public class copiarActivity extends AppCompatActivity {
    private bbdd baseDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copiar);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffbb33")));
        getSupportActionBar().setCustomView(R.layout.barra_consulta);

        ImageButton botonBack = (ImageButton) findViewById(R.id.backConsulta);

        botonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.parseColor("#ffcc66"));
                finish();
            }
        });

        baseDatos=new bbdd(getApplicationContext());

        String txt=baseDatos.getResumen();
        ((TextView)findViewById(R.id.datosCerves)).setText(txt);
    }

    public void copiar(View v){
        ClipboardManager clipManager;
        clipManager=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData;
        String txt=((TextView)findViewById(R.id.datosCerves)).getText().toString();
        clipData=ClipData.newPlainText("text",txt);
        if (clipManager != null) {
            clipManager.setPrimaryClip(clipData);
            Toast.makeText(getApplicationContext(),"Datos copiados al portapapeles",Toast.LENGTH_SHORT).show();
        }else
            Toast.makeText(getApplicationContext(),"Hay un error copiando al portapapeles",Toast.LENGTH_SHORT).show();
    }

    public void correo(View v){
        String txt=((TextView)findViewById(R.id.datosCerves)).getText().toString();
        Intent email=new Intent(Intent.ACTION_SEND);
        email.setData(Uri.parse("mailto:"));
        email.setType("text/plain");
        email.putExtra(Intent.EXTRA_SUBJECT,"Lista de cervezas. Cerveceame");
        email.putExtra(Intent.EXTRA_TEXT,txt);
        try{
            startActivity(Intent.createChooser(email,"Enviar email..."));
        }catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(getApplicationContext(),"No tienes cliente de email instalado.",Toast.LENGTH_SHORT).show();
        }
    }
    public void wasa(View v){
        String txt=((TextView)findViewById(R.id.datosCerves)).getText().toString();
        Intent wasa=new Intent(Intent.ACTION_SEND);
        wasa.setType("text/plain");
        wasa.putExtra(Intent.EXTRA_TEXT,txt);
        wasa.setPackage("com.whatsapp");
        try{
            startActivity(wasa);
        }catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(getApplicationContext(),"No tienes WhatsApp instalado.",Toast.LENGTH_SHORT).show();
        }
    }
    public void telegram(View v){
        String txt=((TextView)findViewById(R.id.datosCerves)).getText().toString();
        Intent wasa=new Intent(Intent.ACTION_SEND);
        wasa.setType("text/plain");
        wasa.putExtra(Intent.EXTRA_TEXT,txt);
        wasa.setPackage("org.telegram.messenger");
        try{
            startActivity(wasa);
        }catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(getApplicationContext(),"No tienes Telegram instalado.",Toast.LENGTH_SHORT).show();
        }
    }
}
