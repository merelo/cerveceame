package merelo.com.cerveceame;
import android.app.Application;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public class ServicioWeb extends Application {
    private static ServicioWeb sInstance;
    private RequestQueue mRequestQueue;
    @Override
    public void onCreate(){
        super.onCreate();
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        sInstance = this;
    }
    public synchronized static ServicioWeb getsInstance(){
        return sInstance;
    }
    public RequestQueue getRequestQueue(){
        return mRequestQueue;
    }
}
