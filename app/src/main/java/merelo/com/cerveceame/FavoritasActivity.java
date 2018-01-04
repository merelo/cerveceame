package merelo.com.cerveceame;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Miguel on 21/09/2016.
 * Actividad que muestra las cervezas puntuadas con 5 estrellas
 */
public class FavoritasActivity extends AppCompatActivity {
    private bbdd baseDatos;
    static final int RETURN_DETALLE=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favoritas_layout);

        baseDatos=new bbdd(getApplicationContext());

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

        rellenar_layout();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RETURN_DETALLE) {
            rellenar_layout();
        }
    }

    private void rellenar_layout() {
        //rellenar layout
        ListView lista;


        final ArrayList<Cerveza> datos = baseDatos.cincoEstrellas();
        lista = (ListView) findViewById(R.id.favoritas);
        if(datos.size()==0){
            Toast.makeText(getApplicationContext(), "No hay ninguna cerveza con 5 estrellas", Toast.LENGTH_LONG).show();
            finish();
        }
        lista.setAdapter(new Lista_adaptador(this, R.layout.cerveza, datos) {
            @Override
            public void onEntrada(Object entrada, View view) {
                if (entrada != null) {
                    TextView id = (TextView) view.findViewById(R.id.idCerveza);
                    if (id != null)
                        id.setText(((Cerveza) entrada).getId() + "");

                    ImageView foto = (ImageView) view.findViewById(R.id.imagen);
                    if (foto != null) {
                        String nomFoto = ((Cerveza) entrada).getFoto();
                        if (((Cerveza) entrada).getDefecto() == 0) {
                            //busca en carpeta interna
                            int idFoto = getResources().getIdentifier("merelo.com.cerveceame:drawable/" + nomFoto, null, null);
                            foto.setImageResource(idFoto);
                        } else {
                            if (nomFoto.compareTo("cervedefecto") != 0) {
                                try {

                                    File imgFile = new File("/sdcard/cerveceame/" + nomFoto);

                                    if (imgFile.exists()) {
                                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                        foto.setImageBitmap(myBitmap);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //Toast.makeText(getApplicationContext(),"Error mostrando foto",Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                //Uri imgUri = Uri.parse("android.resource://com.merelo.cerveceame/drawable/"+nomFoto);
                                /*Bitmap myBitmap = BitmapFactory.decodeFile(dr);*/
                                int idFoto = getResources().getIdentifier("merelo.com.cerveceame:drawable/" + nomFoto, null, null);
                                //foto.setImageURI(null);
                                foto.setImageResource(idFoto);
                            }
                            nomFoto = null;
                        }
                    }

                    TextView marca = (TextView) view.findViewById(R.id.marca);
                    if (marca != null)
                        marca.setText(((Cerveza) entrada).getMarca());

                    TextView nombre = (TextView) view.findViewById(R.id.nombre);
                    if (nombre != null)
                        nombre.setText(((Cerveza) entrada).getNombre());

                    TextView tipo = (TextView) view.findViewById(R.id.tipo);
                    if (tipo != null) {
                        tipo.setText(((Cerveza) entrada).getTipo());

                    }

                    RatingBar estrellas = (RatingBar) view.findViewById(R.id.estrellas);
                    if (estrellas != null) {
                        estrellas.setRating(((Cerveza) entrada).getEstrellas());
                        estrellas.setOnTouchListener(new View.OnTouchListener() {
                            public boolean onTouch(View v, MotionEvent event) {
                                return true;
                            }
                        });
                        estrellas.setFocusable(false);
                    }
                }
            }
        });
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                TextView idV = (TextView) view.findViewById(R.id.idCerveza);
                int idd = Integer.parseInt(idV.getText().toString());
                Intent intent = new Intent(getApplicationContext(), DetalleActivity.class);
                intent.putExtra("id", idd);
                startActivityForResult(intent, RETURN_DETALLE);
            }
        });
        lista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
            }
        });

    }
}
