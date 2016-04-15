package bt.cxp_btdata;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button bConnect, bDisconnect, bXminus, bXplus;
    ToggleButton tbLock, tbScroll, tbStream;
    static boolean Lock, AutoScrollX, Stream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //change orientation and window look
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //change background color
        LinearLayout background = (LinearLayout) findViewById(R.id.bg);
        background.setBackgroundColor(Color.BLACK);

        init();
        Buttoninit();
    }

    void init(){

    }

    void Buttoninit(){
        bConnect = (Button) findViewById(R.id.bConnect);
        bConnect.setOnClickListener(this);
        bDisconnect = (Button) findViewById(R.id.bDisconnect);
        bDisconnect.setOnClickListener(this);
        bXminus = (Button) findViewById(R.id.bXminus);
        bXminus.setOnClickListener(this);
        bXplus = (Button) findViewById(R.id.bXplus);
        bXplus.setOnClickListener(this);
        tbLock = (ToggleButton) findViewById(R.id.tbLock);
        tbLock.setOnClickListener(this);
        tbScroll = (ToggleButton) findViewById(R.id.tbScroll);
        tbScroll.setOnClickListener(this);
        tbStream = (ToggleButton) findViewById(R.id.tbStream);
        tbStream.setOnClickListener(this);

        Lock = true;
        AutoScrollX = true;
        Stream = false;
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.bConnect:
                Intent clientIntent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(clientIntent);
                break;
            case R.id.bDisconnect:
                BluetoothActivity.disconnect();
                break;
            case R.id.bXminus:
                break;
            case R.id.bXplus:
                break;
            case R.id.tbLock:
                if (tbLock.isChecked()){
                    Lock = true;
                } else {
                    Lock = false;
                }
                break;
            case R.id.tbScroll:
                if (tbScroll.isChecked()){
                    AutoScrollX = true;
                } else{
                    AutoScrollX = false;
                }
                break;
            case R.id.tbStream:
                if (tbStream.isChecked()){
                    if(BluetoothActivity.connectedThread != null){
                        BluetoothActivity.connectedThread.write("E");
                    }
                }else {
                    if(BluetoothActivity.connectedThread != null){
                        BluetoothActivity.connectedThread.write("Q");
                    }
                }
                break;
            default:

        }
    }

}
