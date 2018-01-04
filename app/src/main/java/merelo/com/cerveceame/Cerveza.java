package merelo.com.cerveceame;

/**
 * Created by Miguel on 11/09/2016.
 * Clase que almacena los datos de una cerveza
 */
public class Cerveza {
    int id;
    String foto;
    String marca;
    String nombre;
    String tipo;
    String descripcion;
    int estrellas;
    int nVisitas;
    String pais;
    int defecto;

    public Cerveza(int id, String foto,String marca,String nombre,String tipo,String descripcion,
                   int estrellas,int nVisitas,String pais,int defecto){
        this.id=id;
        this.foto=foto;
        this.marca=marca;
        this.nombre=nombre;
        this.tipo=tipo;
        this.descripcion=descripcion;
        this.estrellas=estrellas;
        this.nVisitas=nVisitas;
        this.pais=pais;
        this.defecto=defecto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getEstrellas() {
        return estrellas;
    }

    public void setEstrellas(int estrellas) {
        this.estrellas = estrellas;
    }

    public int getnVisitas() {
        return nVisitas;
    }

    public void setnVisitas(int nVisitas) {
        this.nVisitas = nVisitas;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public int getDefecto() {
        return defecto;
    }

    public void setDefecto(int defecto) {
        this.defecto = defecto;
    }
}
