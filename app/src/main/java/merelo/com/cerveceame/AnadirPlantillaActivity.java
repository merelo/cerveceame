package merelo.com.cerveceame;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Merelo on 08/12/2017.
 */

public class AnadirPlantillaActivity extends AppCompatActivity {
    private bbdd baseDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pegar);

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
    }

    public void actualizar(View v){
        SharedPreferences prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
        int version = prefs.getInt("version", 1);

        String datos=((TextView)findViewById(R.id.infoCervezas)).getText().toString();
        int result=baseDatos.actualizarCopia(datos,version);

        if(result==0)
            Toast.makeText(getApplicationContext(),"Actualización correcta",Toast.LENGTH_SHORT).show();
        else if(result==1)
            Toast.makeText(getApplicationContext(),"Alguna de la cerveza no está en esta versión de la APP",Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(),"Hay un error de formato en la línea "+(result-2)+".",Toast.LENGTH_SHORT).show();

    }
}
