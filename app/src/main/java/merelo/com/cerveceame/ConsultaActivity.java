package merelo.com.cerveceame;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import java.util.Arrays;

/**
 * Created by Miguel on 12/09/2016.
 * Actividad que recibe unos parámetros y forma una lista con las cervezas que cumplan con ellos
 */
public class ConsultaActivity extends AppCompatActivity{
    private bbdd baseDatos;
    private int numElemento=-1;
    static final int RETURN_DETALLE=2;
    private int defecto;

    private String marcaFiltro;
    private String nombreFiltro;
    private String tipoFiltro;
    private String paisFiltro;
    private int minEstrellasFiltro;
    private int maxEstrellasFiltro;
    private int propias;
    private int probar;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        baseDatos=new bbdd(getApplicationContext());

        setContentView(R.layout.activity_consulta);
        //filtros
        minEstrellasFiltro=getIntent().getExtras().getInt("minEstrellasFiltro");
        maxEstrellasFiltro=getIntent().getExtras().getInt("maxEstrellasFiltro");
        marcaFiltro=getIntent().getExtras().getString("marcaFiltro","");
        nombreFiltro=getIntent().getExtras().getString("nombreFiltro","");
        tipoFiltro=getIntent().getExtras().getString("tipoFiltro");
        paisFiltro=getIntent().getExtras().getString("paisFiltro");
        propias=getIntent().getExtras().getInt("mias");
        probar=getIntent().getExtras().getInt("probar");

        rellenar_layout();


        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffbb33")));
        getSupportActionBar().setCustomView(R.layout.barra_consulta);

        View view =getSupportActionBar().getCustomView();

        ImageButton imageButton= (ImageButton)view.findViewById(R.id.backConsulta);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.parseColor("#ffcc66"));
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RETURN_DETALLE) {
            if(numElemento!=-1) {
                ListView lista = (ListView) findViewById(R.id.lista);
                View v=lista.getChildAt(numElemento-lista.getFirstVisiblePosition());

                TextView id=(TextView)v.findViewById(R.id.idCerveza);

                Cerveza cerveza=baseDatos.getCervezaById(Integer.parseInt(id.getText().toString()));

                ((Lista_adaptador)lista.getAdapter()).setItem(numElemento,cerveza);

                ImageView foto = (ImageView) v.findViewById(R.id.imagen);
                if(foto!=null) {
                    String nomFoto = (cerveza).getFoto();
                    if ((cerveza).getDefecto() == 0) {
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
                            int idFoto = getResources().getIdentifier("merelo.com.cerveceame:drawable/" + nomFoto, null, null);
                            //foto.setImageURI(null);
                            foto.setImageResource(idFoto);
                        }
                        nomFoto = null;
                    }
                }

                RatingBar estrellas = (RatingBar) v.findViewById(R.id.estrellas);
                if (estrellas!=null) {
                    estrellas.setRating(cerveza.getEstrellas());
                    estrellas.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            return true;
                        }
                    });
                    estrellas.setFocusable(false);
                }

                TextView marca = (TextView) v.findViewById(R.id.marca);
                if (marca != null)
                    marca.setText(cerveza.getMarca());

                TextView nombre = (TextView) v.findViewById(R.id.nombre);
                if (nombre != null)
                    nombre.setText(cerveza.getNombre());

                TextView tipo = (TextView) v.findViewById(R.id.tipo);
                if (tipo != null) {
                    tipo.setText(cerveza.getTipo());

                }

                //numElemento = -1;
            }
            //rellenar_layout();
        }
    }

    private void rellenar_layout() {
        //rellenar layout
        ListView lista;
        int [] listaProbar=baseDatos.cervezasProbar();

        ArrayList<Cerveza> datosAux = baseDatos.listaCervezasFiltro(marcaFiltro,nombreFiltro,
                tipoFiltro,paisFiltro,minEstrellasFiltro,maxEstrellasFiltro,propias);

        ArrayList<Cerveza> datos=new ArrayList<>();
        int c=0;
        if(probar==1){
            if(listaProbar.length>0) {
                for (int i = 0; i < datosAux.size() && c < listaProbar.length; i++) {
                    for (int a = 0; a < listaProbar.length; a++) {
                        if (datosAux.get(i).getId() == listaProbar[a]) {
                            datos.add(datosAux.get(i));
                            a = listaProbar.length;
                            c++;
                        }
                    }
                }
            }
        }else{
            datos=datosAux;
        }

        lista = (ListView) findViewById(R.id.lista);
        if(datos.size()==0){
            Toast.makeText(getApplicationContext(),"La búsqueda no arroja resultados",Toast.LENGTH_LONG).show();
            finish();
        }
        lista.setAdapter(new Lista_adaptador(this, R.layout.cerveza, datos) {
            @Override
            public void onEntrada(Object entrada, View view) {
                if (entrada != null) {
                    TextView id= (TextView) view.findViewById(R.id.idCerveza);
                    if(id!=null)
                        id.setText(((Cerveza) entrada).getId()+"");

                    ImageView foto = (ImageView) view.findViewById(R.id.imagen);
                    if(foto!=null) {
                        String nomFoto=((Cerveza)entrada).getFoto();
                        if(((Cerveza)entrada).getDefecto()==0){
                            //busca en carpeta interna
                            int idFoto = getResources().getIdentifier("merelo.com.cerveceame:drawable/" + nomFoto, null, null);
                            foto.setImageResource(idFoto);
                        }else{
                            if(nomFoto.compareTo("cervedefecto")!=0){
                                try{

                                    File imgFile = new  File("/sdcard/cerveceame/"+nomFoto);

                                    if(imgFile.exists()){
                                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                        foto.setImageBitmap(myBitmap);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //Toast.makeText(getApplicationContext(),"Error mostrando foto",Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                int idFoto = getResources().getIdentifier("merelo.com.cerveceame:drawable/" + nomFoto, null, null);
                                foto.setImageResource(idFoto);
                            }
                            nomFoto=null;
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
                        tipo.setText(((Cerveza)entrada).getTipo());

                    }

                    RatingBar estrellas = (RatingBar) view.findViewById(R.id.estrellas);
                    if (estrellas!=null) {
                        estrellas.setRating(((Cerveza) entrada).getEstrellas());
                        estrellas.setOnTouchListener(new View.OnTouchListener() {
                            public boolean onTouch(View v, MotionEvent event) {
                                return true;
                            }
                        });
                        estrellas.setFocusable(false);
                    }

                    TextView defectTV=(TextView)view.findViewById(R.id.defectoCerveza);
                    if(defectTV!=null)
                        defectTV.setText(((Cerveza)entrada).getDefecto()+"");
                }
            }
        });
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                TextView idV = (TextView)view.findViewById(R.id.idCerveza);
                int idd=Integer.parseInt(idV.getText().toString());
                Intent intent = new Intent(getApplicationContext(), DetalleActivity.class);
                intent.putExtra("id",idd);
                numElemento=position;
                startActivityForResult(intent, RETURN_DETALLE);
            }
        });
        lista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView defTV=(TextView) view.findViewById(R.id.defectoCerveza);
                TextView idV = (TextView) view.findViewById(R.id.idCerveza);

                if(Integer.parseInt(defTV.getText().toString())==1) {
                    int idd = Integer.parseInt(idV.getText().toString());
                    longClick(idd);
                }
                return true;
            }
        });

    }

    private void longClick(final int id){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String [] items={"Borrar cerveza"};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        baseDatos.borrarCerveza(id);
                        rellenar_layout();
                        break;
                }
            }
        });
        builder.show();
    }
}
