package merelo.com.cerveceame;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.method.KeyListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * Created by Miguel on 13/09/2016.
 * Esta actividad utiliza el mismo layout que DetalleActivity.java
 */
public class AnadirActivity extends AppCompatActivity {
    private bbdd baseDatos;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap imageBitmap;
    private int nuevaImagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseDatos = new bbdd(getApplicationContext());

        setContentView(R.layout.cerveza_layout);
        iniciarSpinner("Selec país", "Selec tipo");

        nuevaImagen=0;

        lectura();

        //Añade la barra superior
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffbb33")));
        getSupportActionBar().setCustomView(R.layout.barra_anadir);

        View view =getSupportActionBar().getCustomView();

        ImageButton imageButton= (ImageButton)view.findViewById(R.id.backAnadir);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.parseColor("#ffcc66"));
                finish();
            }
        });

        ImageButton boton=(ImageButton)view.findViewById(R.id.guardarAnadir);

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardar(v);
            }
        });
    }


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


    private void iniciarSpinner(String countryIni, String tipoIni){
        Locale.setDefault(new Locale("ES"));
        Locale[] locale = Locale.getAvailableLocales(); //Lista de países
        ArrayList<String> countries = new ArrayList<String>();
        String country;
        for( Locale loc : locale ){
            country = loc.getDisplayCountry();
            if( country.length() > 0 && !countries.contains(country) ){
                //Sacamos de la lista algunos países
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
        //Añadimos los países que queremos que salgan en la parte superior. Los hemos quitado en el bucle anterior.
        countries.add(0, countryIni);
        countries.add(1, "Alemania");
        countries.add(2, "Bélgica");
        countries.add(3, "España");
        countries.add(4, "Estados Unidos");
        countries.add(5, "Francia");
        countries.add(6, "Italia");
        countries.add(7, "México");
        countries.add(8, "Países Bajos");
        countries.add(9, "Portugal");
        countries.add(10, "Reino Unido");
        countries.add(11, "República Checa");
        countries.add(12, "------------------");


        Spinner citizenship = (Spinner)findViewById(R.id.paises);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.spinner_row, countries);
        citizenship.setAdapter(adapter);

        ArrayList<String> tipo = new ArrayList<>();
        tipo.add(tipoIni);

        //Tipos de cerveza definidas para la APP. Se corresponden con uno de los campos de /app/src/res/raw/cerveza.txt
        String[] tipoCerve={"Abadía","Altbier",
                "Barley Wine","Belgian Blonde Ale","Belgian Dark Ale","Belgian Strong Ale","Berliner Weisse (Trigo)","Bière de Garde",
                "Bitter Ale","Blond Ale","Bock","Brown Ale",
                "Con tequila",
                "Dark Lager (Negra)","Doppelbock","Dortmunder","Dubbel (Tostada)","Dunkelweizen (Tostada)",
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


    public void guardar(View v){
        v.setBackgroundColor(Color.parseColor("#ffcc66"));
        if(comprobarCampos()){
            EditText marca = (EditText)findViewById(R.id.marcaCerveza);
            EditText nombre = (EditText) findViewById(R.id.nombreCerveza);
            Spinner pais =(Spinner) findViewById(R.id.paises);
            Spinner tipo = (Spinner) findViewById(R.id.tipoCerveza);
            EditText textBox = (EditText) findViewById(R.id.textBox);
            RatingBar estrellas = (RatingBar) findViewById(R.id.estrellasCerveza);
            ImageView imagen = (ImageView) findViewById(R.id.imageCerveza);

            String fileName="cervedefecto";
            int id=baseDatos.lastId();
            id++;

            if(nuevaImagen==1)
                fileName=id+".png";

            String nombreE=nombre.getText().toString();
            if(nombreE.compareTo("Nombre")==0)
                nombreE="";

            String descripcion=textBox.getText().toString();
            if(textBox.getText().toString().compareTo("Descripción...")==0)
                descripcion="";

            baseDatos.insertarCerveza(fileName,marca.getText().toString(),nombreE,
                    tipo.getSelectedItem().toString(),descripcion,
                    (int)estrellas.getRating(),0,pais.getSelectedItem().toString(),1,0);


            //Si hemos hecho una foto, la almacenamos en la sdcard
            if(nuevaImagen==1) {
                File direct = new File(Environment.getExternalStorageDirectory() + "/cerveceame");

                if (!direct.exists()) {
                    File wallpaperDirectory = new File("/sdcard/cerveceame/");
                    wallpaperDirectory.mkdirs();
                }

                File file = new File(new File("/sdcard/cerveceame"), fileName);
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
            finish();
        }else{
            Toast.makeText(getApplicationContext(),"Los campos Marca, País y Tipo son obligatorios",Toast.LENGTH_SHORT).show();
            v.setBackgroundColor(Color.parseColor("#0080ff"));
        }
    }

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

    //Convierte los distintos campos en editables
    private void lectura(){
        EditText marca=(EditText)findViewById(R.id.marcaCerveza);
        EditText nombre=(EditText)findViewById(R.id.nombreCerveza);
        Spinner pais =(Spinner) findViewById(R.id.paises);
        Spinner tipo = (Spinner) findViewById(R.id.tipoCerveza);
        EditText textBox = (EditText) findViewById(R.id.textBox);
        RatingBar estrellas = (RatingBar) findViewById(R.id.estrellasCerveza);
        ImageView imagen = (ImageView) findViewById(R.id.imageCerveza);
        CheckBox probarCHK = (CheckBox) findViewById(R.id.quieroProbar);

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hacerFoto();
            }
        });

        marca.setTag(marca.getKeyListener());
        nombre.setTag(nombre.getKeyListener());
        textBox.setTag(textBox.getKeyListener());

        marca.setKeyListener((KeyListener) marca.getTag());
        nombre.setKeyListener((KeyListener) nombre.getTag());
        textBox.setKeyListener((KeyListener) textBox.getTag());

        //editable
        marca.setFocusable(true);
        marca.setFocusableInTouchMode(true);
        textBox.setFocusable(true);
        textBox.setFocusableInTouchMode(true);
        nombre.setFocusable(true);
        nombre.setFocusableInTouchMode(true);


        pais.setClickable(true);
        tipo.setClickable(true);
        estrellas.setClickable(true);

        //hace desaparecer el checkbox de probar
        probarCHK.setVisibility(View.GONE);
    }

    //No se utiliza
    private void bloquear(){
        EditText marca=(EditText)findViewById(R.id.marcaCerveza);
        EditText nombre=(EditText)findViewById(R.id.nombreCerveza);
        Spinner pais =(Spinner) findViewById(R.id.paises);
        Spinner tipo = (Spinner) findViewById(R.id.tipoCerveza);
        EditText textBox = (EditText) findViewById(R.id.textBox);
        RatingBar estrellas = (RatingBar) findViewById(R.id.estrellasCerveza);
        ImageView imagen = (ImageView) findViewById(R.id.imageCerveza);
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

        pais.setClickable(false);
        tipo.setClickable(false);
        estrellas.setClickable(false);
    }

    public void hacerFoto(){
        //Lanza la cámara
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //No se utiliza
    private void limpiarCampo(String campo,String tipo){
        if(tipo.compareTo("EditText")==0){
            EditText eText;
            if(campo.compareTo("Marca")==0)
                eText = (EditText) findViewById(R.id.marcaCerveza);
            else
                eText = (EditText) findViewById(R.id.nombreCerveza);
            String text=eText.getText().toString();
            if(text.isEmpty()){
                //eText.setText(campo);
                //eText.setTextColor(Color.parseColor("#a6a6a6"));
            }
        }else if(tipo.compareTo("TextBox")==0){
            EditText eText=(EditText) findViewById(R.id.textBox);
            String text=eText.getText().toString();
            if(text.isEmpty()){
                //eText.setText(campo);
                //eText.setTextColor(Color.parseColor("#a6a6a6"));
            }
        }
    }
}