package merelo.com.cerveceame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class bbdd extends SQLiteOpenHelper {


    private static final int VERSION_BASEDATOS = 1;
    private static final String NOMBRE_BASEDATOS = "cervezas.db";
    private static final String TABLA_PROBAR ="CREATE TABLE IF NOT EXISTS probar " +
            " (_id INTEGER PRIMARY KEY)";
    private static final String TABLA_CONTACTOS ="CREATE TABLE IF NOT EXISTS cervezas " +
            " (_id INTEGER PRIMARY KEY, foto TEXT, marca TEXT, nombre TEXT, tipo TEXT, " +
            "descripcion TEXT, estrellas INTEGER, nVisitas INTEGER, pais TEXT, defecto INTEGER," +
            "version INTEGER)";
    private static final String TABLA_SERVIDOR ="CREATE TABLE IF NOT EXISTS servidor " +
            " (marca TEXT, nombre TEXT, estrellas INTEGER, fecha TEXT)";

    public bbdd(Context context) {
        super(context, NOMBRE_BASEDATOS, null, VERSION_BASEDATOS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLA_CONTACTOS);
        db.execSQL(TABLA_PROBAR);
        db.execSQL(TABLA_SERVIDOR);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void crearTablas(){
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL(TABLA_CONTACTOS);
        db.execSQL(TABLA_PROBAR);
        db.execSQL(TABLA_SERVIDOR);
        db.close();
    }

    //Devuelve true si la tabla está vacía
    public boolean comprobarVacio(){
        boolean vacio=true;

        SQLiteDatabase db=getReadableDatabase();
        String[] campos={"_id"};
        Cursor c=db.query("cervezas",campos,null,null,null,null,null,null);

        if(c.moveToFirst())
            vacio=false;

        c.close();
        db.close();

        return vacio;
    }


    //defecto=1 cerveza añadida manualmente
    //defecto=0 cerveza por defecto
    public boolean insertarCerveza(String foto, String marca, String nombre, String tipo,
                                   String descripcion, int estrellas, int nVisitas, String pais,
                                   int defecto, int version){
        SQLiteDatabase db = getWritableDatabase();
        long salida=0;

        if (db != null) {
            ContentValues valores = new ContentValues();
            valores.put("foto", foto);
            valores.put("marca", marca);
            valores.put("nombre", nombre);
            valores.put("tipo", tipo);
            valores.put("descripcion", descripcion);
            valores.put("estrellas", estrellas);
            valores.put("nVisitas", nVisitas);
            valores.put("pais", pais);
            valores.put("defecto", defecto);
            valores.put("version", version);
            salida = db.insert("cervezas", null, valores);
            db.close();
        }

        return (salida>0);
    }

    //Devuelve el mayor id de la base de datos
    public int lastId(){
        int id=0;

        SQLiteDatabase db = getReadableDatabase();
        String[] valores = {"_id"};
        Cursor c = db.query("cervezas",valores,null,null,null,null,"_id");
        c.moveToLast();
        id=c.getInt(0);

        db.close();
        c.close();
        return id;
    }

    //Obtiene la cerveza según el id que indiquemos
    public Cerveza getCervezaById(int id){
        Cerveza cerveza;

        SQLiteDatabase db = getReadableDatabase();
        String[] valores_recuperar = {"_id","foto","marca","nombre","tipo","descripcion",
                "estrellas","nVisitas","pais","defecto"};
        Cursor c = db.query("cervezas", valores_recuperar, "_id="+id, null, null, null, null, null);
        c.moveToFirst();
        cerveza = new Cerveza(c.getInt(0), c.getString(1), c.getString(2),
                c.getString(3), c.getString(4), c.getString(5), c.getInt(6),
                c.getInt(7), c.getString(8), c.getInt(9));
        db.close();
        c.close();
        return cerveza;
    }

    //Obtiene el id de la cerveza según marca y nombre que demos
    public int getIdByName(String marca,String nombre){
        int id=-1;
        SQLiteDatabase db = getReadableDatabase();
        String []selector={marca,nombre};
        String[] valores_recuperar = {"_id"};
        Cursor c = db.query("cervezas", valores_recuperar, "marca=? and nombre=?", selector, null, null, null, null);
        if(c.moveToFirst()) {
            id = c.getInt(0);
        }
        db.close();
        c.close();
        return id;
    }

    //Modifica la cerveza con los valores que le indiquemos
    public void modificarCerveza(int _id, String foto,String marca, String nombre, String tipo,
                                 String descripcion, int estrellas, String pais){
        SQLiteDatabase db = getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("foto",foto);
        cv.put("marca",marca);
        cv.put("nombre",nombre);
        cv.put("tipo",tipo);
        cv.put("descripcion", descripcion);
        cv.put("estrellas",estrellas);
        cv.put("pais", pais);
        db.update("cervezas", cv, "_id='" + _id + "'", null);
        db.close();
    }

    //Actualiza la cerveza segun marca y nombre
    public void actualizarCervezaPorMarca(String marca,String nombre,String descripcion,int estrellas){
        SQLiteDatabase db = getReadableDatabase();
        String []selector={marca,nombre};
        ContentValues cv = new ContentValues();
        cv.put("descripcion", descripcion);
        cv.put("estrellas",estrellas);
        db.update("cervezas", cv, "marca=? and nombre=?", selector);
        db.close();
    }

    //Aumenta el número de visitas de una cerveza
    public void incrementaVisita(int id, int nVisitas){
        SQLiteDatabase db = getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nVisitas",nVisitas);
        db.update("cervezas", cv, "_id='"+id+"'",null);
    }

    //Elimina una cerveza de la base de datos
    public void borrarCerveza(int id){
        String[] idd={String.valueOf(id)};
        SQLiteDatabase db =getReadableDatabase();
        db.delete("cervezas", "_id=?", idd);
        if(!comprobarCervezaProbar(id))
            db.delete("probar", "_id=?", idd);
    }

    //Devuelve una lista de cervezas según los parámetros que le pasemos
    public ArrayList<Cerveza> listaCervezasFiltro(String marca, String nombre, String tipo,
                                                  String pais, int minEstrellas, int maxEstrellas, int propias){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Cerveza> lista_cerveza = new ArrayList<Cerveza>();

        boolean hayMarca=false;
        boolean hayNombre=false;
        boolean insertar=false;

        String[] valores_recuperar = {"_id","foto","marca","nombre","tipo","descripcion",
                "estrellas","nVisitas","pais","defecto"};

        String where="estrellas>=? and estrellas<=?";

        List<String> camposList = new ArrayList<String>();
        camposList.add(minEstrellas+"");
        camposList.add(maxEstrellas+"");

        String[] campos;

        if(propias==1){
            where=where+" and defecto=?";
            camposList.add(propias+"");
        }

        if(pais.compareTo("Sel país")!=0&&pais.compareTo("------------------")!=0){
            where=where+" and pais=?";
            camposList.add(pais);
        }

        if(tipo.compareTo("Sel tipo")!=0){
            where=where+" and tipo=?";
            camposList.add(tipo);
        }

        campos=new String[ camposList.size() ];
        camposList.toArray(campos);

        if(marca.compareTo("")!=0)
            hayMarca=true;
        if(nombre.compareTo("")!=0)
            hayNombre=true;

        Cursor c = db.query("cervezas", valores_recuperar, where, campos, null, null, "marca", null);


        if(c.moveToFirst()) {
            do {
                if(!hayMarca&&!hayNombre) {
                    insertar=true;
                }else if(hayMarca&&hayNombre){
                    if((c.getString(2).toLowerCase()).indexOf(marca)!=-1 && (c.getString(3).toLowerCase()).indexOf(nombre)!=-1){
                        insertar=true;
                    }
                }else if(hayNombre){
                    if((c.getString(3).toLowerCase()).indexOf(nombre)!=-1){
                        insertar=true;
                    }
                }else{
                    if((c.getString(2).toLowerCase()).indexOf(marca)!=-1) {
                        insertar = true;
                    }
                }

                if(insertar){
                    /*if(probar==20){
                        if(Arrays.asList(listaProbar).contains(c.getInt(0))) {
                            Cerveza cerveza = new Cerveza(c.getInt(0), c.getString(1), c.getString(2),
                                    c.getString(3), c.getString(4), c.getString(5), c.getInt(6),
                                    c.getInt(7), c.getString(8), c.getInt(9));
                            lista_cerveza.add(cerveza);
                        }
                    }else {*/
                        Cerveza cerveza = new Cerveza(c.getInt(0), c.getString(1), c.getString(2),
                                c.getString(3), c.getString(4), c.getString(5), c.getInt(6),
                                c.getInt(7), c.getString(8), c.getInt(9));
                        lista_cerveza.add(cerveza);
                    //}
                }
                insertar=false;
            } while (c.moveToNext());
        }
        db.close();
        c.close();
        return lista_cerveza;
    }

    //Devuelve una lista de las cervezas más visitadas
    public ArrayList<Cerveza> masVisitadas(){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Cerveza> lista_cerveza = new ArrayList<Cerveza>();
        String[] valores_recuperar = {"_id","foto","marca","nombre","tipo","descripcion",
                "estrellas","nVisitas","pais","defecto"};

        int cuenta=0;

        Cursor c = db.query("cervezas", valores_recuperar, null,null, null, null, "nVisitas DESC", null);

        if(c.moveToFirst()) {
            do {
                Cerveza cerveza = new Cerveza(c.getInt(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5), c.getInt(6),
                        c.getInt(7), c.getString(8), c.getInt(9));
                lista_cerveza.add(cerveza);
                cuenta++;

            } while (c.moveToNext()&&cuenta<10);

        }

        db.close();
        c.close();
        return lista_cerveza;
    }

    //Devuelve una lista con las cervezas que tienen 5 estrellas
    public ArrayList<Cerveza> cincoEstrellas(){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Cerveza> lista_cerveza = new ArrayList<Cerveza>();
        String[] valores_recuperar = {"_id","foto","marca","nombre","tipo","descripcion",
                "estrellas","nVisitas","pais","defecto"};
        String[] estrellas={"5"};

        Cursor c = db.query("cervezas", valores_recuperar, "estrellas=?",estrellas, null, null, "marca", null);

        if(c.moveToFirst()) {
            do {
                Cerveza cerveza = new Cerveza(c.getInt(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5), c.getInt(6),
                        c.getInt(7), c.getString(8), c.getInt(9));
                lista_cerveza.add(cerveza);

            } while (c.moveToNext());

        }

        db.close();
        c.close();
        return lista_cerveza;
    }

    //Devuelve lista con cervezas puntuadas y numero de puntuadas por estrella
    public int[] cervezasPuntuadas(){
        SQLiteDatabase db = getReadableDatabase();
        int estrellas[]=new int[6];
        estrellas[0]=0;
        estrellas[1]=0;
        estrellas[2]=0;
        estrellas[3]=0;
        estrellas[4]=0;
        estrellas[5]=0;

        String[] valores_recuperar = {"_id","estrellas"};
        Cursor c = db.query("cervezas", valores_recuperar, "estrellas>0",null, null, null, null, null);

        if(c.moveToFirst()) {
            do {
                estrellas[0]++;
                estrellas[c.getInt(1)]++;
            } while (c.moveToNext());

        }

        db.close();
        c.close();
        return estrellas;
    }

    //Devuelve numero de cervezas totales y propias
    public int[] numeroCervezas(){
        SQLiteDatabase db = getReadableDatabase();
        int[] lista=new int[2];
        lista[0]=0; //numero de cervezas totales
        lista[1]=0; //numero de cervezas propias
        String[] valores_recuperar = {"_id","defecto"};
        Cursor c = db.query("cervezas", valores_recuperar, null, null, null, null, null, null);


        if(c.moveToFirst()) {
            do {
                lista[0]++;
                if(c.getInt(1)==1)
                    lista[1]++;
            } while (c.moveToNext());
        }
        db.close();
        c.close();
        return lista;
    }

    //Devuelve:
    //marca|nombre|tipo|descripcion|estrellas|pais|defecto|version
    public String getResumen(){
        SQLiteDatabase db = getReadableDatabase();
        String txt="";
        String[] valores_recuperar = {"marca","nombre","tipo","descripcion","estrellas","pais","defecto","version"};
        Cursor c = db.query("cervezas", valores_recuperar, "estrellas>0",null, null, null, null, null);
        String descr="";
        String marca="";
        String nombre="";

        if(c.moveToFirst()) {
            do {
                marca=c.getString(0);
                nombre=c.getString(1);
                descr=c.getString(3);
                descr=descr.replace("\n"," ");
                marca=marca.replace("\n"," ");
                nombre=nombre.replace("\n"," ");
                txt=txt+marca+"|"+nombre+"|"+
                        c.getString(2)+"|"+descr+"|"+c.getInt(4)+"|"+
                        c.getString(5)+"|"+c.getInt(6)+"|"+c.getInt(7)+"\n";
            } while (c.moveToNext());
            txt=txt.substring(0,txt.length()-1);
        }

        db.close();
        c.close();
        return txt;
    }

    //Datos de la forma (cada linea para una cerveza
    //marca|nombre|tipo|descripcion|estrellas|pais|defecto|version
    public int actualizarCopia(String datos, int versionActual){
        String []lineas=datos.split("\n");
        String []linea;
        int resultado=0; //0 correcto, 1 error en version, 2 error en el formato

        String marca;
        String nombre;
        String tipo;
        String descripcion;
        int estrellas;
        String pais;
        int defecto; //0 predefinida, 1 anadida
        int version;

        for(int i=0;i<lineas.length;i++) {
            linea = lineas[i].split("[|]");
            if (linea.length == 8) {
                marca = linea[0];
                nombre = linea[1];
                tipo = linea[2];
                descripcion = linea[3];
                estrellas = Integer.parseInt(linea[4]);
                pais = linea[5];
                defecto = Integer.parseInt(linea[6]);
                version = Integer.parseInt(linea[7]);

                //La anadimos
                if (version <= versionActual) {
                    if (defecto == 0) {
                        actualizarCervezaPorMarca(marca, nombre, descripcion, estrellas);
                    } else {
                        if(-1==getIdByName(marca,nombre))
                            insertarCerveza("cervedefecto", marca, nombre, tipo,
                                descripcion, estrellas, 0, pais, defecto, version);
                    }
                } else {
                    if(resultado==0)
                        resultado = 1; //la lista tiene cervezas con una version mayor a la actual
                }

            } else {
                resultado = 2+i;
            }
        }
        return resultado;
    }

    /*
     * METODOS PARA LA TABLA PROBAR
     */
    public boolean comprobarVacioProbar(){
        boolean vacio=true;

        SQLiteDatabase db=getReadableDatabase();
        String[] campos={"_id"};
        Cursor c=db.query("probar",campos,null,null,null,null,null,null);

        if(c.moveToFirst())
            vacio=false;

        c.close();
        db.close();

        return vacio;
    }

    public void borrarCervezaProbar(int id){
        String[] idd={String.valueOf(id)};
        SQLiteDatabase db =getReadableDatabase();
        db.delete("probar", "_id=?", idd);
        db.close();
    }

    public boolean insertarCervezaProbar(int id){
        SQLiteDatabase db = getWritableDatabase();
        long salida=0;

        if (db != null) {
            ContentValues valores = new ContentValues();
            valores.put("_id", id);
            salida = db.insert("probar", null, valores);
            db.close();
        }

        return (salida>0);
    }

    public boolean comprobarCervezaProbar(int id){
        boolean exists=false;

        String[] idd={String.valueOf(id)};
        SQLiteDatabase db=getReadableDatabase();
        String[] campos={"_id"};
        Cursor c=db.query("probar",campos,"_id=?",idd,null,null,null,null);

        if(c.moveToFirst())
            exists=true;

        c.close();
        db.close();

        return exists;
    }

    public int[] cervezasProbar(){
        int[] id= new int[0];

        SQLiteDatabase db=getReadableDatabase();
        String[] campos={"_id"};
        Cursor c=db.query("probar",campos,null,null,null,null,null,null);

        int i=0;
        if(c.moveToFirst()) {
            id=new int[c.getCount()];
            do {
                id[i]=c.getInt(0);
                i++;
            } while (c.moveToNext());
        }

        c.close();
        db.close();

        return id;
    }

    public void chk(){
        SQLiteDatabase db=getReadableDatabase();
        String[] campos={"_id"};
        Cursor c=db.query("probar",campos,null,null,null,null,null,null);

    }

    /*
     * METODOS PARA CONEXION CON SERVIDOR
     */

    public boolean insertarServidor(String marca, String nombre, int estrella, String fecha){
        SQLiteDatabase db = getWritableDatabase();
        long salida=0;

        if (db != null) {
            ContentValues valores = new ContentValues();
            valores.put("marca", marca);
            valores.put("nombre", nombre);
            valores.put("estrellas", estrella);
            valores.put("fecha", fecha);
            salida = db.insert("servidor", null, valores);
            db.close();
        }

        return (salida>0);
    }

    //funcion que devuelve una estructura con marca, nombre, estrella y fecha
    public String [][] cervesSinEnviar(){
        String [][] estructura;

        SQLiteDatabase db=getReadableDatabase();
        String[] campos={"marca","nombre","estrellas","fecha"};
        Cursor c=db.query("servidor",campos,null,null,null,null,null,null);

        int i=0;
        if(c.moveToFirst()) {
            estructura=new String[c.getCount()][4];
            do {
                estructura[i][0]=c.getString(0);
                estructura[i][1]=c.getString(1);
                estructura[i][2]=""+c.getInt(2);
                estructura[i][3]=c.getString(3);
                i++;
            } while (c.moveToNext());
        }else
            estructura=null;

        c.close();
        db.close();

        return estructura;
    }

    public void vaciarServidor(){
        SQLiteDatabase db=getReadableDatabase();
        db.execSQL("delete from servidor");
        db.close();
    }

    public String [][] getCervezasPuntuadasPredefinidas(){
        SQLiteDatabase db = getWritableDatabase();
        String[] campos={"marca","nombre","estrellas"};
        String where="estrellas>0 and defecto=0";
        Cursor c = db.query("cervezas", campos, where, null, null, null, null, null);

        String [][] cerves=null;

        int i=0;
        if(c.moveToFirst()) {
            cerves = new String[c.getCount()][3];
            do {
                cerves[i][0] = c.getString(0);
                cerves[i][1] = c.getString(1);
                cerves[i][2] = "" + c.getInt(2);
                i++;
            } while (c.moveToNext());
        }

        return cerves;
    }
}