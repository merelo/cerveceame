package merelo.com.cerveceame;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Merelo on 04/11/2017.
 */

public class EstadisticasActivity extends AppCompatActivity {
    private bbdd baseDatos;
    private int[] listaEstrellas;
    private int[] listaTodos;
    private int i=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseDatos = new bbdd(getApplicationContext());

        setContentView(R.layout.estadisticas_layout);

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

        datosCervezas();
    }
    @Override
    protected void onResume(){
        super.onResume();
        if(listaEstrellas[0]!=0) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    int tiempo;
                    if(listaEstrellas[0]<=10)
                        tiempo=100;
                    else
                        tiempo=1000/listaEstrellas[0];
                    try {
                        while (!isInterrupted() && i <= listaEstrellas[0]) {
                            Thread.sleep(tiempo);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    actualizar();
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                    }
                }
            };
            t.start();
        }
    }

    public void datosCervezas(){
        listaEstrellas=baseDatos.cervezasPuntuadas();
        listaTodos=baseDatos.numeroCervezas();
        ((TextView) findViewById(R.id.totales)).setText(listaTodos[0] + "");
        ((TextView) findViewById(R.id.propias)).setText(listaTodos[1] + "");
        ((TextView) findViewById(R.id.defecto)).setText((listaTodos[0]-listaTodos[1]) + "");
    }
    public void actualizar(){
        if(i<=listaEstrellas[0])
            ((TextView) findViewById(R.id.puntuadas)).setText(i + "");
        if(i<=listaEstrellas[1])
            ((TextView) findViewById(R.id.unaestrellas)).setText(i + "");
        if(i<=listaEstrellas[2])
            ((TextView) findViewById(R.id.dosestrellas)).setText(i + "");
        if(i<=listaEstrellas[3])
            ((TextView) findViewById(R.id.tresestrellas)).setText(i + "");
        if(i<=listaEstrellas[4])
            ((TextView) findViewById(R.id.cuatroestrellas)).setText(i + "");
        if(i<=listaEstrellas[5])
            ((TextView) findViewById(R.id.cincoestrellas)).setText(i + "");

        i++;
    }
}