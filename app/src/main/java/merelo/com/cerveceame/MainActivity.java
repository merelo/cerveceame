package merelo.com.cerveceame;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Actividad pricipal de la aplicación.
 * Llama al layout activity_main.xml
 * Al iniciar esta actividad se actualiza la base de datos, en caso de nueva instalación de la app.
 * Tras esto se realiza una consulta a un servidor comprobando si existe una actualización.
 */
public class MainActivity extends AppCompatActivity {
    private bbdd baseDatos; //Objeto para trabajar con la base de datos
    final int ultimaVersion=12;  //Variable para llevar un control interno de la versión y para añadir las nuevas cervezas
                                //que traiga la actualización


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        baseDatos=new bbdd(getApplicationContext());

        //Añadimos la barra superior
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffbb33")));
        getSupportActionBar().setCustomView(R.layout.barra);


        cargar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //Se define los elementos del menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.copiaSeguridad) { //Se lanza actividad preferencias
            Intent intent = new Intent(this, copiarActivity.class);
            startActivity(intent);
            return true;
        }else if(id==R.id.anadirCopia){
            Intent intent = new Intent(this, AnadirPlantillaActivity.class);
            startActivity(intent);
            return true;
        }else if(id==R.id.quiensoy){
            Intent intent = new Intent(this, QuienSoyActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    private void cargar(){
        //Vemos la versión actual y si ha cambiado
        SharedPreferences prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
        int version = prefs.getInt("version", ultimaVersion);
        String idUser = prefs.getString("iduser",null);
        SharedPreferences.Editor editor = prefs.edit();

        String linea;
        String[] lineaAux;
        String clave;


        //creamos las tablas, por si no existieran
        baseDatos.crearTablas();
        //Si la base de datos está vacía, la rellenamos con las cervezas del fichero /app/src/res/raw/cerveza.txt
        if(baseDatos.comprobarVacio()) {
            try
            {
                //Cada línea del fichero incluye varios campos
                //id|nombreImagen(sin la extensión)|marca cerveza|nombre cerveza|tipo de cerveza|descripcion|0|0|país|0|versión
                //La imagen debe estar almacenada en /app/src/res/drawable. Debe ser en formato png
                //tipo de cerveza se corresponde con las del spinner de FiltroActivity.java y AnadirActivity.java
                //Versión es un número, si al actualizar es superior a la versión que teniamos, se añadiran estas cervezas a la base de datos
                InputStream fraw = getApplicationContext().getResources().openRawResource(R.raw.cerveza);

                BufferedReader bf =
                        new BufferedReader(new InputStreamReader(fraw));


                while((linea=bf.readLine())!=null){
                    lineaAux = linea.split("[|]");
                    baseDatos.insertarCerveza(lineaAux[1], lineaAux[2], lineaAux[3], lineaAux[4],
                            lineaAux[5], Integer.parseInt(lineaAux[6]), Integer.parseInt(lineaAux[7]),
                            lineaAux[8], Integer.parseInt(lineaAux[9]),Integer.parseInt(lineaAux[10]));
                }

                fraw.close();
            }
            catch (Exception ex)
            {
                Log.e("Ficheros", "Error al leer fichero desde recurso raw");
            }
        }else if(version!=ultimaVersion){ //si la versión cambia
            editor.putInt("actDisp",0);
            editor.apply();
            //actualizar base datos
            try
            {
                InputStream fraw = getApplicationContext().getResources().openRawResource(R.raw.cerveza);

                BufferedReader bf =
                        new BufferedReader(new InputStreamReader(fraw));


                while((linea=bf.readLine())!=null){
                    lineaAux = linea.split("[|]");
                    if(Integer.parseInt(lineaAux[10])>version) {
                        baseDatos.insertarCerveza(lineaAux[1], lineaAux[2], lineaAux[3], lineaAux[4],
                                lineaAux[5], Integer.parseInt(lineaAux[6]), Integer.parseInt(lineaAux[7]),
                                lineaAux[8], Integer.parseInt(lineaAux[9]), Integer.parseInt(lineaAux[10]));
                    }
                }

                fraw.close();
            }
            catch (Exception ex)
            {
                Log.e("Ficheros", "Error al leer fichero desde recurso raw");
            }
        }

        editor.putInt("version", ultimaVersion);
        editor.apply();


        if(idUser==null){
            clave=generarClave();
            editor.putString("iduser", clave);
            editor.apply();
            idUser=clave;
            //comprobar cervezas puntuadas y enviarlas, si hay error, almacenarlas en base de datos no enviados
            String[][] cervezas = baseDatos.getCervezasPuntuadasPredefinidas();
            if (cervezas != null) {
                for(int i=0;i<cervezas.length;i++) {
                    envioJSON(idUser, cervezas[i][0], cervezas[i][1], cervezas[i][2], cervezas[i][3]);
                }
            }
        }else {
            //comprobar si hay cervezas en base de datos no enviados y enviarlos
            String[][] cervezas = baseDatos.cervesSinEnviar();
            if (cervezas != null) {
                baseDatos.vaciarServidor();
                for(int i=0;i<cervezas.length;i++) {
                    envioJSON(idUser, cervezas[i][0], cervezas[i][1], cervezas[i][2], cervezas[i][3]);
                }
            }
        }

        String urlActualizacion=prefs.getString("urlDirAct","http://151.80.119.12/actualizarCerveceame/");

        //Realizamos una consulta para obtener la dirección en la que está la actualización
        consultaJSON(urlActualizacion, "index.html");
        //Comprobamos si hay una nueva actualización
        consultaJSON(urlActualizacion, "actualizacion.html");
    }

    public void consulta(View v){
        Intent intent = new Intent(this, FiltroActivity.class);
        startActivity(intent);
    }

    public void anadir(View v){
        Intent intent = new Intent(this, AnadirActivity.class);
        startActivity(intent);
    }

    public void buscarActualizacion(View v){
        SharedPreferences prefs =
                getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
        int actDisp=prefs.getInt("actDisp",0);
        String urlAct=prefs.getString("urlActu",null);

        if(actDisp==0||urlAct==null){
            Toast.makeText(getApplicationContext(),"No hay actualización disponible",Toast.LENGTH_SHORT).show();
        }else{
            mensajeActualizacion(urlAct);
        }


        //aceptar para ir a actualizacion
    }

    //Actividad desactivada en el layout activity_main
    public void masConsultadas(View v){
        Intent intent = new Intent(this, MasConsultadasActivity.class);
        startActivity(intent);
    }

    public void favoritas(View v){
        Intent intent = new Intent(this, FavoritasActivity.class);
        startActivity(intent);
    }

    public void estadisticas(View v){
        Intent intent = new Intent(this, EstadisticasActivity.class);
        startActivity(intent);
    }

    public void quiensoy(View v){
        Intent intent = new Intent(this, QuienSoyActivity.class);
        startActivity(intent);
    }

    public void favoritasUsuarios(View v){
        Intent intent = new Intent(this, FavoritasUsuariosActivity.class);
        startActivity(intent);
    }

    public void consultaJSON(String url, final String archivo){

        final String URL=url+archivo;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, (String) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Miramos si es correcto:
                        if(archivo.compareTo("index.html")==0){
                            analisisIndex(response);
                        }else if(archivo.compareTo("actualizacion.html")==0) {
                            analisisActualizacion(response);
                        }

                        Log.d("resp",response.toString());
                        VolleyLog.v("Response:%n %s", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                //Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
            }
        });
        // add the request object to the queue to be executed
        ServicioWeb.getsInstance().getRequestQueue().add(request);
    }

    public void envioJSON(String idUser,final String marca,final String nombre,final String estrellas,final String fecha) {
        String uri = "http://151.80.119.12/cerveceame/server/insertar.php?iduser=" + idUser + "&marca=" + marca +
                "&nombre=" + nombre + "&puntuacion=" + estrellas + "&fechausuario=" + fecha;

        uri=replaceURL(uri);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri, (String) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Miramos si es correcto:
                        try {
                            String respuesta = response.getString("estado");

                            if (respuesta.compareTo("ok") == 0) {
                                //Toast.makeText(getApplicationContext(),"Cerveza añadida",Toast.LENGTH_SHORT).show();
                            } else {
                                //Toast.makeText(getApplicationContext(), "Error en servidor", Toast.LENGTH_SHORT).show();
                                //insertar la cerveza en base de datos de no enviadas
                                baseDatos.insertarServidor(marca, nombre, Integer.parseInt(estrellas), fecha);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.d("resp", response.toString());
                        VolleyLog.v("Response:%n %s", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                //Toast.makeText(getApplicationContext(),"Error al enviar",Toast.LENGTH_SHORT).show();
                //insertar la cerveza en base de datos de no enviadas
                baseDatos.insertarServidor(marca, nombre, Integer.parseInt(estrellas), fecha);
            }
        });
        // add the request object to the queue to be executed
        ServicioWeb.getsInstance().getRequestQueue().add(request);
    }

    private void analisisIndex(JSONObject response) {
        try {
            String urlNueva = response.getString("url");

            if (urlNueva != null) {
                SharedPreferences prefs =
                        getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("urlDirAct", urlNueva);
                editor.apply();
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void analisisActualizacion(JSONObject response) {
        try {

            int actualizacion = response.getInt("actualizacion");
            SharedPreferences prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            if (actualizacion> ultimaVersion) {
                String urlNueva = response.getString("url");

                if (urlNueva != null) {
                    Toast.makeText(getApplicationContext(),"Hay una actualización disponible",Toast.LENGTH_SHORT).show();
                    editor.putString("urlActu", urlNueva);
                    editor.putInt("actDisp", 1);
                }
            }else{
                editor.putInt("actDisp", 0);
            }
            editor.apply();
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private void mensajeActualizacion(final String url){
        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                }
            }
        };
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Hay una nueva actualización.\nEsta actualización puede añadir marcas" +
                " de cervezas y/o mejoras.");
        alert.setTitle("¡Actualización!");
        alert.setPositiveButton("Actualizar", onClick);
        alert.setNegativeButton("Cancelar", onClick);
        alert.show();
    }

    public String generarClave(){
        char[] elementos={'0','1','2','3','4','5','6','7','8','9' ,'a',
                'b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t',
                'u','v','w','x','y','z'};

        char[] conjunto = new char[50];

        for(int i=0;i<50;i++){
            int el = (int)(Math.random()*36);
            conjunto[i] = (char)elementos[el];
        }
        return new String(conjunto);
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
}
