package merelo.com.cerveceame;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * Created by Miguel on 19/09/2016.
 * Actividad para realizar la consulta de cervezas.
 * Cuando introducimos los datos, lanza la actividad ConsultaActivity
 */
public class FiltroActivity extends AppCompatActivity {
    static final int RETURN_CONSULTA=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filtro);

        iniciarSpinner("Sel país", "Sel tipo");

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffbb33")));
        getSupportActionBar().setCustomView(R.layout.barra_filtro);

        View view =getSupportActionBar().getCustomView();

        ImageButton imageButton= (ImageButton)view.findViewById(R.id.backFiltro);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.parseColor("#ffcc66"));
                finish();
            }
        });

        ImageButton boton = (ImageButton)view.findViewById(R.id.buscarFiltro);
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.parseColor("#ffcc66"));
                filtrar(v);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RETURN_CONSULTA) {
            ImageButton boton = (ImageButton)findViewById(R.id.buscarFiltro);
            boton.setBackgroundColor(Color.parseColor("#ffbb33"));
        }
    }


    public void filtrar (View v){
        EditText marca = (EditText)findViewById(R.id.marcaFiltro);
        EditText nombre = (EditText) findViewById(R.id.nombreFiltro);
        Spinner pais =(Spinner) findViewById(R.id.paisFiltro);
        Spinner tipo = (Spinner) findViewById(R.id.tipoFiltro);
        EditText estrellasMin = (EditText) findViewById(R.id.minEstrellasFiltro);
        EditText estrellasMax = (EditText) findViewById(R.id.maxEstrellasFiltro);
        CheckBox misCervezas = (CheckBox) findViewById(R.id.misCervezas);
        CheckBox probar= (CheckBox) findViewById(R.id.probar);

        int minEstrellas=0;
        int maxEstrellas=5;
        int aux;
        String marcaFiltro=null;
        String nombreFiltro=null;
        String paisFiltro=null;
        String tipoFiltro=null;

        Intent intent = new Intent(getApplicationContext(), ConsultaActivity.class);

        if(!estrellasMin.getText().toString().isEmpty()) {
            minEstrellas = Integer.parseInt(estrellasMin.getText().toString());
        }

        if(!estrellasMax.getText().toString().isEmpty()) {
            maxEstrellas = Integer.parseInt(estrellasMax.getText().toString());
        }

        if (maxEstrellas<minEstrellas){
            aux=minEstrellas;
            minEstrellas=maxEstrellas;
            maxEstrellas=aux;
        }

        if(minEstrellas>5)
            minEstrellas=5;
        if(maxEstrellas>5){
            maxEstrellas=5;
        }

        //int
        intent.putExtra("maxEstrellasFiltro",maxEstrellas);
        intent.putExtra("minEstrellasFiltro", minEstrellas);

        //marca
        if(!marca.getText().toString().isEmpty())
            marcaFiltro=marca.getText().toString();

        if(marcaFiltro!=null)
            intent.putExtra("marcaFiltro",marcaFiltro.toLowerCase());

        //nombre
        if(!nombre.getText().toString().isEmpty())
            nombreFiltro=nombre.getText().toString();

        if(nombreFiltro!=null)
            intent.putExtra("nombreFiltro", nombreFiltro.toLowerCase());

        if(misCervezas.isChecked())
            intent.putExtra("mias",1);

        if(probar.isChecked())
            intent.putExtra("probar",1);

        //pais
        paisFiltro=pais.getSelectedItem().toString();
        intent.putExtra("paisFiltro", paisFiltro);

        //tipo
        tipoFiltro=tipo.getSelectedItem().toString();
        intent.putExtra("tipoFiltro", tipoFiltro);

        startActivityForResult(intent, RETURN_CONSULTA);
    }

    public void iniciarSpinner(String countryIni, String tipoIni){
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
        //countries.add(1, "------");
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

        Spinner citizenship = (Spinner)findViewById(R.id.paisFiltro);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.spinner_row, countries);
        citizenship.setAdapter(adapter);

        ArrayList<String> tipo = new ArrayList<>();
        tipo.add(tipoIni);
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

        citizenship=(Spinner)findViewById(R.id.tipoFiltro);
        adapter=new ArrayAdapter<String>(this,R.layout.spinner_row,tipo);
        citizenship.setAdapter(adapter);
    }

    public void limpiarMarca(View v){
        EditText marca = (EditText)findViewById(R.id.marcaFiltro);
        marca.setText("");
    }

    public void limpiarNombre(View v){
        EditText nombre = (EditText) findViewById(R.id.nombreFiltro);
        nombre.setText("");
    }
}
