package merelo.com.cerveceame;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by Miguel on 02/04/2017.
 * Información sobre el creador de la APP
 */
public class QuienSoyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiensoy);

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
    }

    public void twitter(View v){
        Intent intent;
        try{
            this.getPackageManager().getPackageInfo("com.twitter.android",0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=389672719"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }catch(Exception e){
            intent = new Intent(Intent.ACTION_VIEW,Uri.parse("https://twitter.com/MiguelMerelo"));
        }
        this.startActivity(intent);
    }

    public void linkedin(View v){
        Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.linkedin.com/in/miguel-merelo-hernández-388097131/"));
        this.startActivity(intent);
    }

    public void developer(View v){
        Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse("http://151.80.119.12/cerveceame"));
        this.startActivity(intent);
    }
}
