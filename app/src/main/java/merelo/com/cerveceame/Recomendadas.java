package merelo.com.cerveceame;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Merelo on 04/01/2018.
 */

public class Recomendadas extends AppCompatActivity {
    private bbdd baseDatos;
    private int numElemento=-1;
    static final int RETURN_DETALLE=2;
    private String [][] estructuraRespuesta=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recomendadas_layout);
        //setContentView(R.layout.activity_favorita_comunidad);

        SharedPreferences prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
        String idUser = prefs.getString("iduser",null);

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

        String url="http://151.80.119.12/cerveceame/server/sys.php?iduser="+idUser;
        consultaJSON(url);
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
            }
        }
    }

    public void rellenar_layout(){
        //rellenar layout

        ArrayList<Cerveza> datos = baseDatos.getCervezasFavoritasComunidad(estructuraRespuesta);

        ProgressBar simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        TextView textoConsulta = (TextView) findViewById(R.id.textConsultando);
        simpleProgressBar.setVisibility(View.GONE);
        textoConsulta.setVisibility(View.GONE);

        ArrayList<Cerveza> datosOrdenados=new ArrayList<Cerveza>();
        for(int i=0;i<estructuraRespuesta.length;i++) {
            for(int a=0;a<estructuraRespuesta.length;a++) {
                Cerveza cerv=datos.get(a);
                if (cerv.getMarca().compareTo(estructuraRespuesta[i][0])==0 &&
                        cerv.getNombre().compareTo(estructuraRespuesta[i][1])==0) {
                    datosOrdenados.add(cerv);
                    break;
                }
            }
        }

        ListView lista;
        lista = (ListView) findViewById(R.id.lista);
        lista.setAdapter(new Lista_adaptador(this, R.layout.cerveza_recomendadas, datosOrdenados) {
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

                    TextView nUsuarios=(TextView) view.findViewById(R.id.porcentaje);

                    String marcaS=((Cerveza) entrada).getMarca();
                    String nombreS=((Cerveza) entrada).getNombre();
                    double punt=0;
                    for(int i=0;i<estructuraRespuesta.length&&punt==0;i++) {
                        if(estructuraRespuesta[i][0].compareTo(marcaS)==0&&estructuraRespuesta[i][1].compareTo(nombreS)==0) {
                            punt = Double.parseDouble(estructuraRespuesta[i][2]);
                        }
                    }
                    if(punt!=0)
                        nUsuarios.setText(""+(int)punt+" %");
                    else
                        nUsuarios.setText("??? %");
                    //Dar color segun porcentaje
                    int r=0;
                    int g=232;
                    int b=0;
                    if(punt>50){
                        r=(int) Math.round(232-232*punt/100);;
                    }else if(punt==0){
                        r=232;
                        g=201;
                    }else{
                        r=232;
                        g = (int) Math.round(232*punt/50);
                    }
                    nUsuarios.setTextColor(Color.rgb(r, g, b));

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
                return true;
            }
        });
    }

    public void consultaJSON(String url){

        Log.d("URL",url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, (String) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Respuesta: ", response.toString());

                        analisisConsulta(response);
                        rellenar_layout();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                Toast.makeText(getApplicationContext(),"Error conectando con el servidor",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        // add the request object to the queue to be executed
        ServicioWeb.getsInstance().getRequestQueue().add(request);
    }

    private void analisisConsulta(JSONObject response) {
        String [][] estructura=null;
        int i=0;
        estructura=new String[10][3];
        Iterator<String> iter = response.keys();
        while (iter.hasNext()&&i<10) {
            String key = iter.next();
            try {
                JSONObject value = (JSONObject) response.get(key);
                estructura[i][0] = value.getString("marca");
                estructura[i][1] = value.getString("nombre");
                estructura[i][2] = value.getString("punt");
                i++;
            } catch (JSONException e) {
                // Algo chungo
            }
        }
        estructuraRespuesta=estructura;
    }

}
