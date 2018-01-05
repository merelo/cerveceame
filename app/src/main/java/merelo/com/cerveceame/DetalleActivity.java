package merelo.com.cerveceame;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Miguel on 14/09/2016.
 */
public class DetalleActivity extends AppCompatActivity {
    private bbdd baseDatos;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int T_FLIP=300; //Tiempo de la acción de los botones de la barra superior
    private Bitmap imageBitmap;
    private ImageButton boton;
    private ImageButton botonBack;
    private int editable; //0 si no lo es

    //campos iniciales
    private String marcaIni;
    private String nombreIni;
    private int id;
    private String fotoIni;
    private String tipoIni;
    private String descripcionIni;
    private String paisIni;
    private int estrellasIni;
    private int nuevaImagen=0;
    private boolean probar=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseDatos = new bbdd(getApplicationContext());

        id=getIntent().getExtras().getInt("id");
        //pillar datos
        Cerveza cerveza=baseDatos.getCervezaById(id);

        marcaIni=cerveza.getMarca();
        nombreIni=cerveza.getNombre();
        fotoIni=cerveza.getFoto();
        tipoIni=cerveza.getTipo();
        descripcionIni=cerveza.getDescripcion();
        paisIni=cerveza.getPais();
        estrellasIni=cerveza.getEstrellas();
        probar=baseDatos.comprobarCervezaProbar(id);
        //probar=false;
        int nVisitas=cerveza.getnVisitas();
        nVisitas++;

        setContentView(R.layout.cerveza_layout);
        iniciarCampos(cerveza);

        baseDatos.incrementaVisita(id, nVisitas);
        lectura();

        //barra superior
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffbb33")));
        getSupportActionBar().setCustomView(R.layout.barra_detalle);

        View view = getSupportActionBar().getCustomView();

        botonBack = (ImageButton) view.findViewById(R.id.backDetalle);

        botonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.parseColor("#ffcc66"));
                guardarModificacion();
                finish();
            }
        });

        boton = (ImageButton) view.findViewById(R.id.editarDetalle);

        boton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                restaurarValorIni();
            }
        });
    }

    //Gestiona la respuesta de la cámara
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            ImageView imagen = (ImageView) findViewById(R.id.imageCerveza);
            nuevaImagen=1;
            imagen.setImageBitmap(imageBitmap);
        }
    }

    public void hacerFoto(){

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //Guarda los cambios
    private boolean guardarModificacion(){
        boolean result=true;
        if(comprobarCampos()){
            EditText marca = (EditText)findViewById(R.id.marcaCerveza);
            EditText nombre = (EditText) findViewById(R.id.nombreCerveza);
            Spinner pais =(Spinner) findViewById(R.id.paises);
            Spinner tipo = (Spinner) findViewById(R.id.tipoCerveza);
            EditText textBox = (EditText) findViewById(R.id.textBox);
            RatingBar estrellas = (RatingBar) findViewById(R.id.estrellasCerveza);
            CheckBox probarCHK = (CheckBox) findViewById(R.id.quieroProbar);
            if(probar!=probarCHK.isChecked()){
                if(probar)
                    baseDatos.borrarCervezaProbar(id);
                else
                    baseDatos.insertarCervezaProbar(id);
            }

            marcaIni=marca.getText().toString();
            nombreIni=nombre.getText().toString();
            paisIni=pais.getSelectedItem().toString();
            tipoIni=tipo.getSelectedItem().toString();
            descripcionIni=textBox.getText().toString();
            int estrellasAct=(int) estrellas.getRating();

            if(estrellasIni!=estrellasAct) {
                SharedPreferences prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
                String idUser = prefs.getString("iduser", null);
                consultaJSON(idUser,estrellasAct);
            }

            estrellasIni=estrellasAct;

            String fileName=fotoIni;

            if(nuevaImagen==1) {
                fileName = id + ".png";
                fotoIni=id+"";
            }

            String nombreE=nombre.getText().toString();
            if(nombreE.compareTo("Nombre")==0)
                nombreE="";

            String descripcion=textBox.getText().toString();
            if(textBox.getText().toString().compareTo("Descripción...")==0)
                descripcion="";

            //modificar
            baseDatos.modificarCerveza(id, fileName,marca.getText().toString(), nombreE,
                    tipo.getSelectedItem().toString(), descripcion,
                    (int) estrellas.getRating(), pais.getSelectedItem().toString());


            if(nuevaImagen==1) {
                File direct = new File(Environment.getExternalStorageDirectory() + "/cerveceame");

                if (!direct.exists()) {
                    File wallpaperDirectory = new File("/sdcard/cerveceame/");
                    wallpaperDirectory.mkdirs();
                }

                File file = new File(new File("/sdcard/cerveceame/"), fileName);
                if (file.exists()) {
                    file.delete();
                }
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else{
            Toast.makeText(getApplicationContext(),"Los campos Marca, País y Tipo son obligatorios",Toast.LENGTH_SHORT).show();
            result=false;
        }
        return result;
    }

    //Al cancelar, modifica los campos a los valores previos a su modificación
    private void restaurarValorIni(){
        EditText marca=(EditText)findViewById(R.id.marcaCerveza);
        EditText nombre=(EditText)findViewById(R.id.nombreCerveza);
        EditText textBox = (EditText) findViewById(R.id.textBox);
        RatingBar estrellas = (RatingBar) findViewById(R.id.estrellasCerveza);
        ImageView imagen = (ImageView) findViewById(R.id.imageCerveza);
        CheckBox probarCHK = (CheckBox) findViewById(R.id.quieroProbar);

        probarCHK.setChecked(probar);

        marca.setTextColor(Color.parseColor("#000000"));
        nombre.setTextColor(Color.parseColor("#000000"));
        textBox.setTextColor(Color.parseColor("#000000"));
        marca.setText(marcaIni);
        nombre.setText(nombreIni);
        textBox.setText(descripcionIni);
        estrellas.setRating(estrellasIni);

        nuevaImagen=0;

        if(editable==0||fotoIni.compareTo("cervedefecto")==0){
            int idFoto = getResources().getIdentifier("merelo.com.cerveceame:drawable/" + fotoIni, null, null);
            imagen.setImageResource(idFoto);
            //mirar en memoria interna
        }else{
            try{

                File imgFile = new  File("/sdcard/cerveceame/"+fotoIni);

                if(imgFile.exists()){
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                    imagen.setImageBitmap(myBitmap);

                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Error mostrando foto",Toast.LENGTH_SHORT).show();
            }
        }

        iniciarSpinner(paisIni, tipoIni);
    }

    private void iniciarCampos(Cerveza cerveza){
        EditText marca=(EditText)findViewById(R.id.marcaCerveza);
        EditText nombre=(EditText)findViewById(R.id.nombreCerveza);
        EditText textBox = (EditText) findViewById(R.id.textBox);
        RatingBar estrellas = (RatingBar) findViewById(R.id.estrellasCerveza);
        ImageView imagen = (ImageView) findViewById(R.id.imageCerveza);
        CheckBox probarCHK = (CheckBox) findViewById(R.id.quieroProbar);

        editable=cerveza.getDefecto();

        if(probar){
            probarCHK.setChecked(true);
        }

        imagen.setTag(1);

        marca.setText(cerveza.getMarca());
        nombre.setText(cerveza.getNombre());
        textBox.setText(cerveza.getDescripcion().toString());
        estrellas.setRating(cerveza.getEstrellas());

        if(editable==0||fotoIni.compareTo("cervedefecto")==0){
            int idFoto = getResources().getIdentifier("merelo.com.cerveceame:drawable/" + fotoIni, null, null);
            imagen.setImageResource(idFoto);
            //mirar en memoria interna
        }else{
            try{

                File imgFile = new  File("/sdcard/cerveceame/"+cerveza.getFoto());

                if(imgFile.exists()){
/*
                int targetW = foto.getWidth();
                int targetH = foto.getHeight();

                // Get the dimensions of the bitmap
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imgFile.getAbsolutePath(), bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                // Determine how much to scale down the image
                int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;
*/
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                    imagen.setImageBitmap(myBitmap);

                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Error mostrando foto",Toast.LENGTH_SHORT).show();
            }
        }

        iniciarSpinner(cerveza.getPais(), cerveza.getTipo());
    }

    private void iniciarSpinner(String countryIni, String tipoIni){
        Locale.setDefault(new Locale("ES"));
        Locale[] locale = Locale.getAvailableLocales();
        ArrayList<String> countries = new ArrayList<String>();
        String country;
        for( Locale loc : locale ){
            country = loc.getDisplayCountry();
            if( country.length() > 0 && !countries.contains(country) ){
                if(country.compareTo(countryIni)!=0&&!country.contains("Islas")&&country.compareTo("Latinoamérica")!=0
                        &&!country.contains("San ")&&!country.contains("Isla ")&&!country.contains("Ceuta y Melilla")
                        &&!country.contains("República del Congo")&&country.compareTo("Bélgica")!=0&&country.compareTo("Alemania")!=0
                        &&country.compareTo("Países Bajos")!=0&&country.compareTo("República Checa")!=0&&country.compareTo("España")!=0
                        &&country.compareTo("Estados Unidos")!=0&&country.compareTo("Francia")!=0&&country.compareTo("Portugal")!=0
                        &&country.compareTo("Italia")!=0&&country.compareTo("México")!=0&&country.compareTo("Reino Unido")!=0)
                    countries.add( country );
            }
        }
        Collections.sort(countries, String.CASE_INSENSITIVE_ORDER);
        countries.add(0, countryIni);
        String []mainCountries={"Alemania","Bélgica","España","Estados Unidos","Francia","Italia","México",
                "Países Bajos","Portugal","Reino Unido","República Checa"};
        int a=1;
        for(int i=0;i<mainCountries.length;i++){
            if(mainCountries[i].compareTo(countryIni)==0) {
                a--;
            }else
                countries.add(i+a,mainCountries[i]);
        }
        countries.add(11 + a, "------------------");

        Spinner citizenship = (Spinner)findViewById(R.id.paises);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.spinner_row, countries);
        citizenship.setAdapter(adapter);

        ArrayList<String> tipo = new ArrayList<>();
        tipo.add(tipoIni);
        String[] tipoCerve={"Abadía","Altbier",
                "Barley Wine","Belgian Blonde Ale","Belgian Dark Ale","Belgian Strong Ale","Berliner Weisse (Trigo)","Bière de Garde",
                "Bitter Ale","Blond Ale","Bock","Brown Ale",
                "Con tequila",
                "Dark Lager (Negra)","Doppelbock","Dortmunder","Dubbel (Tostada)","Dunkel (Negra)","Dunkelweizen (Tostada)",
                "Eisbock",
                "Faro","Fruit Beer",
                "Gueuze",
                "Kölsch","Kriek (Fruta)",
                "Maibock","Märzen","Mild Ale","Münchner Dunkel","Münchner Hell",
                "Lager","Lambic (Fruta)",
                "Old Ale","Old Brown","Otro",
                "Pale Ale","Pale Lager","Pilsen","Porter (Tostada)",
                "Quadrupel",
                "Radler","Rauchbier","Red Ale",
                "Schwarzbier (Negra)","Scotch Ale","Sin Alcohol","Sparkling Ale","Steam beer","Steinbier","Stout (Negra)","Strong Bitter","Strong Lager",
                "Tripel",
                "Weizenbier (Trigo)","Weizenbock (Trigo)","Witbier (Trigo)"};
        for(int i=0;i<tipoCerve.length;i++){
            if(tipoCerve[i].compareTo(tipoIni)!=0)
                tipo.add(tipoCerve[i]);
        }

        citizenship=(Spinner)findViewById(R.id.tipoCerveza);
        adapter=new ArrayAdapter<String>(this,R.layout.spinner_row,tipo);
        citizenship.setAdapter(adapter);
    }

    private void lectura(){
        EditText marca=(EditText)findViewById(R.id.marcaCerveza);
        EditText nombre=(EditText)findViewById(R.id.nombreCerveza);
        Spinner pais =(Spinner) findViewById(R.id.paises);
        Spinner tipo = (Spinner) findViewById(R.id.tipoCerveza);
        //EditText textBox = (EditText) findViewById(R.id.textBox);
        //RatingBar estrellas = (RatingBar) findViewById(R.id.estrellasCerveza);
        ImageView imagen = (ImageView) findViewById(R.id.imageCerveza);

        if(editable==1) {
            imagen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hacerFoto();
                }
            });

            marca.setKeyListener((KeyListener) marca.getTag());
            nombre.setKeyListener((KeyListener) nombre.getTag());

            marca.setFocusable(true);
            marca.setFocusableInTouchMode(true);
            nombre.setFocusable(true);
            nombre.setFocusableInTouchMode(true);
            pais.setClickable(true);
            tipo.setClickable(true);
        }


        /*textBox.setKeyListener((KeyListener) textBox.getTag());

        //editable
        textBox.setFocusable(true);
        textBox.setFocusableInTouchMode(true);

        estrellas.setClickable(true);
        estrellas.setFocusable(true);
        estrellas.setFocusableInTouchMode(true);

        estrellas.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });*/
    }

    //Comprueba que los valores de los campos sean aceptables
    private boolean comprobarCampos(){
        boolean correcto=true;

        EditText marca = (EditText)findViewById(R.id.marcaCerveza);
        Spinner pais =(Spinner) findViewById(R.id.paises);
        Spinner tipo = (Spinner) findViewById(R.id.tipoCerveza);

        if(marca.getText().toString().isEmpty()||marca.getText().toString().trim().length()==0){
            correcto=false;
        }else if(pais.getSelectedItem().toString().compareTo("Selec país")==0||pais.getSelectedItem().toString().compareTo("------------------")==0){
            correcto=false;
        }else if(tipo.getSelectedItem().toString().compareTo("Selec tipo")==0){
            correcto=false;
        }

        return correcto;
    }


    public void consultaJSON(final String idUser,final int estrellasAct){
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String fecha=formatter.format(date);
        String uri = "http://151.80.119.12/cerveceame/server/insertar.php?iduser=" + idUser + "&marca=" + marcaIni +
                "&nombre=" + nombreIni + "&puntuacion=" + estrellasAct + "&fechausuario="+fecha;
        uri=replaceURL(uri);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri, (String) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Miramos si es correcto:
                        try {
                            String respuesta = response.getString("estado");

                            if (respuesta.compareTo("ok")==0) {
                            }else {
                                //Toast.makeText(getApplicationContext(), "Error en servidor", Toast.LENGTH_SHORT).show();
                                //insertar la cerveza en base de datos de no enviadas
                                baseDatos.insertarServidor(marcaIni, nombreIni, estrellasAct, fecha);
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                        Log.d("resp",response.toString());
                        VolleyLog.v("Response:%n %s", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                //insertar la cerveza en base de datos de no enviadas
                baseDatos.insertarServidor(marcaIni, nombreIni, estrellasAct, fecha);
            }
        });
        // add the request object to the queue to be executed
        ServicioWeb.getsInstance().getRequestQueue().add(request);
    }

    public String replaceURL(String txt){
        txt=txt.replace(" ","%20");
        txt=txt.replace("á","%C3%A1");
        txt=txt.replace("ä","%C3%A4");
        txt=txt.replace("é","%C3%A9");
        txt=txt.replace("è","%C3%A8");
        txt=txt.replace("ë","%C3%AB");
        txt=txt.replace("ê","%C3%AA");
        txt=txt.replace("í","%C3%AD");
        txt=txt.replace("ó","%C3%B3");
        txt=txt.replace("ö","%C3%B6");
        txt=txt.replace("ü","%C3%BC");
        txt=txt.replace("ñ","%C3%B1");
        txt=txt.replace("º","%C2%BA");
        return txt;
    }


    /****
     *
     * LAS SIGUIENTES FUNCIONES NO SE UTILIZAN EN LA NUEVA VERSION
     *
     */
    //Guarda los cambios y vuelve a la situación inicial
    private void guardar(View v){

        ObjectAnimator flip2 = ObjectAnimator.ofFloat(botonBack, "rotationY", 0f, 90f);
        flip2.setDuration(T_FLIP);
        flip2.start();

        ObjectAnimator flip = ObjectAnimator.ofFloat(v, "rotationY", 0f, 90f);
        flip.setDuration(T_FLIP);
        flip.start();

        flip.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                boton.setImageResource(R.drawable.guardar);
                boton.setScaleX(-1);
                boton.setBackgroundColor(Color.parseColor("#0080ff"));
                ObjectAnimator flip = ObjectAnimator.ofFloat(boton, "rotationY", 90f, 180f);
                flip.setDuration(T_FLIP);
                flip.start();


                botonBack.setImageResource(R.drawable.cancel);
                botonBack.setScaleX(-1);
                botonBack.setBackgroundColor(Color.parseColor("#e60000"));
                ObjectAnimator flip2 = ObjectAnimator.ofFloat(botonBack, "rotationY", 90f, 180f);
                flip2.setDuration(T_FLIP);
                flip2.start();

                botonBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v3) {
                        cancelar(v3);
                    }
                });

                boton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        if(guardarModificacion())
                            normal(v2);
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        lectura();
    }
    private void normal(View v){
        ObjectAnimator flip = ObjectAnimator.ofFloat(v, "rotationY", 180f, 270f);
        flip.setDuration(T_FLIP);
        flip.start();

        ObjectAnimator flip2 = ObjectAnimator.ofFloat(botonBack, "rotationY", 180f, 270f);
        flip2.setDuration(T_FLIP);
        flip2.start();


        flip.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                boton.setImageResource(R.drawable.edit);
                boton.setScaleX(1);
                ObjectAnimator flip = ObjectAnimator.ofFloat(boton, "rotationY", 270f, 360f);
                flip.setDuration(T_FLIP);
                flip.start();
                boton.setBackgroundColor(Color.parseColor("#00ffffff"));

                botonBack.setImageResource(R.drawable.flechaback);
                botonBack.setScaleX(1);
                ObjectAnimator flip2 = ObjectAnimator.ofFloat(botonBack, "rotationY", 270f, 360f);
                flip2.setDuration(T_FLIP);
                flip2.start();
                botonBack.setBackgroundColor(Color.parseColor("#00ffffff"));

                botonBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

                boton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        guardar(v2);
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        //guardar datos en bbdd
        //sustituir campos iniciales
        bloquear();
    }
    //Cancela los cambios y vuelve a la situación inicial
    private void cancelar(View v){
        ObjectAnimator flip = ObjectAnimator.ofFloat(boton, "rotationY", 180f, 270f);
        flip.setDuration(T_FLIP);
        flip.start();

        ObjectAnimator flip2 = ObjectAnimator.ofFloat(v, "rotationY", 180f, 270f);
        flip2.setDuration(T_FLIP);
        flip2.start();


        flip.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                boton.setImageResource(R.drawable.edit);
                boton.setScaleX(1);
                ObjectAnimator flip = ObjectAnimator.ofFloat(boton, "rotationY", 270f, 360f);
                flip.setDuration(T_FLIP);
                flip.start();
                boton.setBackgroundColor(Color.parseColor("#00ffffff"));

                botonBack.setImageResource(R.drawable.flechaback);
                botonBack.setScaleX(1);
                ObjectAnimator flip2 = ObjectAnimator.ofFloat(botonBack, "rotationY", 270f, 360f);
                flip2.setDuration(T_FLIP);
                flip2.start();
                botonBack.setBackgroundColor(Color.parseColor("#00ffffff"));

                botonBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

                boton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        guardar(v2);
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        //restaurar campos iniciales
        restaurarValorIni();
        bloquear();
    }
    private void bloquear(){
        EditText marca=(EditText)findViewById(R.id.marcaCerveza);
        EditText nombre=(EditText)findViewById(R.id.nombreCerveza);
        Spinner pais =(Spinner) findViewById(R.id.paises);
        Spinner tipo = (Spinner) findViewById(R.id.tipoCerveza);
        EditText textBox = (EditText) findViewById(R.id.textBox);
        RatingBar estrellas = (RatingBar) findViewById(R.id.estrellasCerveza);
        ImageView imagen = (ImageView) findViewById(R.id.imageCerveza);
        ScrollView contenedor=(ScrollView) findViewById(R.id.contenedor);
        //no editable


        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        marca.setTag(marca.getKeyListener());
        nombre.setTag(nombre.getKeyListener());
        textBox.setTag(textBox.getKeyListener());
        marca.setKeyListener(null);
        nombre.setKeyListener(null);
        textBox.setKeyListener(null);

        marca.setFocusable(false);
        nombre.setFocusable(false);
        textBox.setFocusable(false);
        marca.setFocusableInTouchMode(false);
        nombre.setFocusableInTouchMode(false);
        textBox.setFocusable(false);


        marca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        nombre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        textBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        contenedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        pais.setClickable(false);
        tipo.setClickable(false);
        estrellas.setClickable(false);
        estrellas.setFocusable(false);
        estrellas.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

    }
    private void limpiarCampo(String campo,String tipo){
        if(tipo.compareTo("EditText")==0){
            EditText eText;
            if(campo.compareTo("Marca")==0)
                eText = (EditText) findViewById(R.id.marcaCerveza);
            else
                eText = (EditText) findViewById(R.id.nombreCerveza);
            String text=eText.getText().toString();
            if(text.isEmpty()){
                eText.setText(campo);
                eText.setTextColor(Color.parseColor("#a6a6a6"));
            }
        }else if(tipo.compareTo("TextBox")==0){
            EditText eText=(EditText) findViewById(R.id.textBox);
            String text=eText.getText().toString();
            if(text.isEmpty()){
                eText.setText(campo);
                eText.setTextColor(Color.parseColor("#a6a6a6"));
            }
        }
    }
}