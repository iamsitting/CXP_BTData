package bt.cxp_btdata;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.LegendAlign;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onBackPressed(){
        if(BluetoothActivity.connectedThread != null){
            BluetoothActivity.connectedThread.write("Q");
            super.onBackPressed();
        }
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case BluetoothActivity.SUCCESS_CONNECT:
                    BluetoothActivity.connectedThread = new BluetoothActivity.ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_SHORT).show();
                    String s = "Successfully Connected";
                    BluetoothActivity.connectedThread.start();
                    break;
                case BluetoothActivity.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String strIncom = new String(readBuf, 0, 5);

                    if(strIncom.indexOf('s')==0 && strIncom.indexOf('.')==2){
                        strIncom = strIncom.replace("s","");
                        if (isFloatNumber(strIncom)){
                            Series.appendData(new GraphView.GraphViewData(graph2LastXValue, Double.parseDouble(strIncom)), AutoScrollX);

                            if (graph2LastXValue >= Xview && Lock == true){
                                Series.resetData(new GraphView.GraphViewData[] {});
                                graph2LastXValue = 0;
                            } else{
                                graph2LastXValue += 1;
                            }

                            if (Lock == true){
                                graphView.setViewPort(0, Xview);
                            } else {
                                graphView.setViewPort(graph2LastXValue-Xview, Xview);
                            }

                            //refresh
                            GraphView.removeView(graphView);
                            GraphView.addView(graphView);
                        }

                    }
                    break;
            }
        }

        public boolean isFloatNumber(String num){
            try{
                Double.parseDouble(num);
            } catch (NumberFormatException nfe){
                return false;
            }
            return true;
        }

    };

    Button bConnect, bDisconnect, bXminus, bXplus;
    ToggleButton tbLock, tbScroll, tbStream;
    static boolean Lock, AutoScrollX, Stream;

    //setup GraphView
    static LinearLayout GraphView;
    static GraphView graphView;
    static GraphViewSeries Series;
    private static double graph2LastXValue = 0;
    private static int Xview = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //change orientation and window look
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //change background color
        LinearLayout background = (LinearLayout) findViewById(R.id.bg);
        background.setBackgroundColor(Color.BLACK);

        init();
        Buttoninit();
    }

    void init(){
        GraphView = (LinearLayout) findViewById(R.id.Graph);
        Series = new GraphViewSeries("Signal ",
                new GraphViewSeries.GraphViewStyle(Color.YELLOW, 2),
                new GraphView.GraphViewData[] {new GraphView.GraphViewData(0,0)});
        graphView = new LineGraphView(this, "Graph");

        graphView.setViewPort(0, Xview);
        graphView.setScrollable(true);
        graphView.setScalable(true);
        graphView.setShowLegend(true);
        graphView.setLegendAlign(LegendAlign.BOTTOM);
        graphView.setManualYAxis(true);
        graphView.setManualYAxisBounds(5, 0);
        graphView.addSeries(Series);
        GraphView.addView(graphView);

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
                startActivity(new Intent("android.intent.action.BT1"));

                break;
            case R.id.bDisconnect:
                BluetoothActivity.disconnect();
                break;
            case R.id.bXminus:
                if (Xview > 1) Xview--;
                break;
            case R.id.bXplus:
                if (Xview < 30) Xview++;
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
