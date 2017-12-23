package com.a091517.ldr.nihuawocai;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
import static android.content.ContentValues.TAG;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by ldr on 2017/12/21.
 */

public class Play extends Activity {
    private TextView currentDrawer;
    private Paint drawPaint;
    private float posX, posY;
    private float paintWidth = 12;
    private int paintColor = Color.BLACK;
    private String localIP;
    private String remoteIP;
    private int localPort = 8002;
    private int remotePort = 8001;
    private int actionState;
    private static ClientSocket clientSocket;
    private static final float TOUCH_TOLERANCE = 4; // 在屏幕上移动4个像素后响应
    private static final float ERASE_WIDTH = 150;
    private static final int ACTION_DOWN = 10000;
    private static final int ACTION_MOVE = 10001;
    private static final int ACTION_UP = 10002;
    private static LinearLayout paletteView;
    private TextView currentWord;
    private TextView timeShow;
    private TextView currentRoomNumber;
    private EditText answer;
    private Button sendAnswerButton;
    private JSONObject jsonObject;
    private static final String CREATE_ROOM="create_room";
    private static final String CURRENT_DRAWER="current_drawer";
    private static final String SCORE_LIST="score_list";
    private static final String WORDS_USED="used_words";
    private ArrayList<TextView> guesserList;
    private ArrayList<TextView> scoreGuesserList;
    private ArrayList<Integer> scoreNumList; //各玩家分数
    private ArrayList<String> wordsUsed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play);
        Log.i(TAG,"onCreate");
        currentWord = (TextView) findViewById(R.id.currentWord);
        currentRoomNumber = (TextView) findViewById(R.id.currentRoomNumber);
        currentRoomNumber.setText(this.getIntent().getStringExtra(CREATE_ROOM));
        timeShow = (TextView) findViewById(R.id.timer);
        paletteView = (LinearLayout) findViewById(R.id.paletteView);
        paletteView.addView(new GameView(this));
        currentDrawer = (TextView) findViewById(R.id.playerNumber);


        updateGameStatus(); //10个textView传进入更新值，重新setText，
        //还有更新用过的词
        currentWord.setText("apple");
        answer = (EditText) findViewById(R.id.answer);
        sendAnswerButton = (Button) findViewById(R.id.sendAnswerButton);
        init();
        timer.start();

        clientSocket = new ClientSocket(this);
        ImageView menu_icon = new ImageView(this);
        Drawable menu_img = ContextCompat.getDrawable(this, R.drawable.icon_menu);
        menu_icon.setImageDrawable(menu_img);
        final FloatingActionButton actionButton = new FloatingActionButton.Builder(this).setContentView(menu_icon)
                .setPosition(FloatingActionButton.POSITION_RIGHT_CENTER).build();

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        ImageView red_Icon = new ImageView(this);
        Drawable red_img = ContextCompat.getDrawable(this, R.drawable.icon_red);
        red_Icon.setImageDrawable(red_img);
        SubActionButton red_button = itemBuilder.setContentView(red_Icon).build();

        ImageView yellow_Icon = new ImageView(this);
        Drawable yellow_img = ContextCompat.getDrawable(this, R.drawable.icon_yellow);
        yellow_Icon.setImageDrawable(yellow_img);
        SubActionButton yellow_button = itemBuilder.setContentView(yellow_Icon).build();

        ImageView blue_Icon = new ImageView(this);
        Drawable blue_img = ContextCompat.getDrawable(this, R.drawable.icon_blue);
        blue_Icon.setImageDrawable(blue_img);
        SubActionButton blue_button = itemBuilder.setContentView(blue_Icon).build();

        ImageView green_Icon = new ImageView(this);
        Drawable green_img = ContextCompat.getDrawable(this, R.drawable.icon_green);
        green_Icon.setImageDrawable(green_img);
        SubActionButton green_button = itemBuilder.setContentView(green_Icon).build();

        ImageView black_Icon = new ImageView(this);
        Drawable black_img = ContextCompat.getDrawable(this, R.drawable.icon_black);
        black_Icon.setImageDrawable(black_img);
        SubActionButton black_button = itemBuilder.setContentView(black_Icon).build();

        ImageView erase_Icon = new ImageView(this);
        Drawable erase_img = ContextCompat.getDrawable(this, R.drawable.icon_erase);
        erase_Icon.setImageDrawable(erase_img);
        SubActionButton erase_button = itemBuilder.setContentView(erase_Icon).build();

        ImageView width_Icon = new ImageView(this);
        Drawable width_img = ContextCompat.getDrawable(this, R.drawable.icon_width);
        width_Icon.setImageDrawable(width_img);
        SubActionButton width_button = itemBuilder.setContentView(width_Icon).build();

        final FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(erase_button)
                .addSubActionView(width_button)
                .addSubActionView(red_button)
                .addSubActionView(yellow_button)
                .addSubActionView(blue_button)
                .addSubActionView(green_button)
                .addSubActionView(black_button)
                .setRadius(300)
                .setStartAngle(90)
                .setEndAngle(270)
                .attachTo(actionButton).build();

        init();

        localIP = clientSocket.getIp(this);
        remoteIP = "192.168.43.239";
        new Thread(new GameDataThread()).start();

        red_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: red_button");
                paintColor = Color.RED;
                drawPaint.setColor(paintColor);
                drawPaint.setStrokeWidth(paintWidth);
                drawPaint.setXfermode(null);
                actionMenu.close(true);
            }
        });

        yellow_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: yellow_button");
                paintColor = Color.YELLOW;
                drawPaint.setColor(paintColor);
                drawPaint.setStrokeWidth(paintWidth);
                drawPaint.setXfermode(null);
                actionMenu.close(true);
            }
        });

        blue_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: blue_button");
                paintColor = Color.BLUE;
                drawPaint.setColor(paintColor);
                drawPaint.setStrokeWidth(paintWidth);
                drawPaint.setXfermode(null);
                actionMenu.close(true);
            }
        });

        green_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: green_button");
                paintColor = Color.GREEN;
                drawPaint.setColor(paintColor);
                drawPaint.setStrokeWidth(paintWidth);
                drawPaint.setXfermode(null);
                actionMenu.close(true);
            }
        });

        black_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: black_button");
                paintColor = Color.BLACK;
                drawPaint.setColor(paintColor);
                drawPaint.setStrokeWidth(paintWidth);
                drawPaint.setXfermode(null);
                actionMenu.close(true);
            }
        });

        erase_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: erase_button");
                drawPaint.setStrokeWidth(ERASE_WIDTH);
                paintColor = Color.WHITE;
                drawPaint.setColor(paintColor);
                //drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                actionMenu.close(true);
            }
        });
        //
        width_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: width_button");
                final WidthSeekBar widthSeekBar = new WidthSeekBar(Play.this, (int) paintWidth);
                widthSeekBar.widthSeekBar(new WidthSeekBar.WidthListener() {
                    @Override
                    public void transWidth() {
                        paintWidth = widthSeekBar.getWidth();
                        drawPaint.setStrokeWidth(paintWidth);
                    }
                });
                widthSeekBar.show();
                drawPaint.setXfermode(null);
                actionMenu.close(true);
            }
        });

        //如果结果正确，手动修改当前activity中的变量，出现新词/新提示语
        //如果错误，消息提醒
        sendAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "sendAnswerButtonClick");
                String correctAnswer = currentWord.getText().toString();
                String guessAnswer = answer.getText().toString();
                int winnerId = 2; //假设一直是玩家2猜对
                int winnerTextViewId = -1;  //玩家2对应的Textview显示栏
                //如果玩家2本轮不是画画者，分数显示就要改
                for (int i = 0; i < 5; ++i) {
                    int tmp = Integer.parseInt(guesserList.get(i).getText().toString());
                    if (tmp == winnerId) {
                        winnerTextViewId = i;
                        break;
                    }
                }
                Log.i(TAG,"winnerTextViewId"+String.valueOf(winnerTextViewId));
                if (winnerTextViewId != -1) {
                    int winnerScoreUpdate = Integer.parseInt(scoreGuesserList.get(winnerTextViewId).getText().toString());
                    Log.i(TAG, String.valueOf(winnerScoreUpdate));
                    if (guessAnswer.equals(correctAnswer) == true) {
                        winnerScoreUpdate += 5;
                        scoreNumList.set(winnerId - 1, winnerScoreUpdate); // 更新胜者分数
                        Log.i(TAG, scoreNumList.get(0).toString());
                        Log.i(TAG, scoreNumList.get(1).toString());
                        Log.i(TAG, scoreNumList.get(2).toString());
                        Log.i(TAG, scoreNumList.get(3).toString());
                        Log.i(TAG, scoreNumList.get(4).toString());
                        Log.i(TAG, scoreNumList.get(5).toString());
                        scoreGuesserList.get(winnerTextViewId).setText(String.valueOf(winnerScoreUpdate));
                        wordsUsed.add(currentWord.getText().toString());
                    } else
                        Toast.makeText(Play.this, "wrong answer", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateGameStatus(){
        int currentDrawerId=this.getIntent().getIntExtra(CURRENT_DRAWER,1); //默认当前画画的是玩家1
        currentDrawer.setText(String.valueOf(currentDrawerId));
        guesserList=new ArrayList<TextView>();
        Log.i(TAG,"updateGameStatus");
        guesserList.add((TextView)findViewById(R.id.guesser_1));
        guesserList.add((TextView)findViewById(R.id.guesser_2));
        guesserList.add((TextView)findViewById(R.id.guesser_3));
        guesserList.add((TextView)findViewById(R.id.guesser_4));
        guesserList.add((TextView)findViewById(R.id.guesser_5));
        scoreGuesserList=new ArrayList<TextView>();
        scoreGuesserList.add((TextView)findViewById(R.id.score_guesser_1));
        scoreGuesserList.add((TextView)findViewById(R.id.score_guesser_2));
        scoreGuesserList.add((TextView)findViewById(R.id.score_guesser_3));
        scoreGuesserList.add((TextView)findViewById(R.id.score_guesser_4));
        scoreGuesserList.add((TextView)findViewById(R.id.score_guesser_5));

        scoreNumList=this.getIntent().getIntegerArrayListExtra(SCORE_LIST);  //6位玩家的分数，下标对应
        int j=0;
        for(int i=0;i<5;++i){
            if((j+1)==currentDrawerId)  //当前轮画画的玩家信息不在下方显示
                ++j;
            guesserList.get(i).setText(String.valueOf(j+1));
            scoreGuesserList.get(i).setText(String.valueOf(scoreNumList.get(j)));
            ++j;
        }
        wordsUsed=this.getIntent().getStringArrayListExtra(WORDS_USED);
    }

    private CountDownTimer timer=new CountDownTimer(30000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            timeShow.setText(millisUntilFinished/1000+"秒");
        }

        @Override
        public void onFinish() { //时间到，换下一个玩家画画,销毁当前activity，新开activity
            timeShow.setText("时间到！");
            timeShow.setTextColor(Color.RED);
            int nextDrawer;
            nextDrawer=Integer.parseInt(currentDrawer.getText().toString())+1;
            if(nextDrawer==7)
                nextDrawer-=6;
            Intent intent=new Intent(Play.this,Play.class);
            intent.putExtra(CURRENT_DRAWER,nextDrawer);
            intent.putExtra(SCORE_LIST,scoreNumList);
            intent.putExtra(WORDS_USED,wordsUsed);
            intent.putExtra(CREATE_ROOM,currentRoomNumber.getText());
            startActivity(intent);
            finish();
        }
    };

    private void init() {
        drawPaint = new Paint();
        drawPaint.setColor(paintColor); // 设置颜色
        drawPaint.setStrokeWidth(paintWidth); //设置笔宽
        drawPaint.setAntiAlias(true); // 抗锯齿
        drawPaint.setDither(true); // 防抖动
        drawPaint.setStyle(Paint.Style.STROKE); // 设置画笔类型，STROKE空心
        drawPaint.setStrokeJoin(Paint.Join.ROUND); // 设置连接处样式
        drawPaint.setStrokeCap(Paint.Cap.ROUND); // 设置笔头样式
    }

    public class GameView extends View {
        private Bitmap drawBitmap;
        private Canvas drawCanvas;
        private Path drawPath;
        private Paint drawBitmapPaint;

        public GameView(Context context) {
            super(context);
            drawPath = new Path();
            drawBitmapPaint = new Paint(Paint.DITHER_FLAG); // 抗抖动选项
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            drawBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); // 每个像素8bytes存储
            drawCanvas = new Canvas(drawBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE); // 设置背景颜色
            canvas.drawBitmap(drawBitmap, 0, 0, drawBitmapPaint);
            canvas.drawPath(drawPath, drawPaint);
        }

        private void touch_down(float x, float y) {
            drawPath.reset();
            drawPath.moveTo(x, y);
            posX = x;
            posY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - posX);
            float dy = Math.abs(y - posY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                drawPath.quadTo(posX, posY, (x + posX) / 2, (y + posY) / 2);
                posX = x;
                posY = y;
            }
        }

        private void touch_up() {
            drawPath.lineTo(posX, posY);
            drawCanvas.drawPath(drawPath, drawPaint);
            drawPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    actionState = ACTION_DOWN;
                    touch_down(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    actionState = ACTION_MOVE;
                    touch_move(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    actionState = ACTION_UP;
                    touch_up();
                    break;
            }
            postInvalidate();
            return true;
        }
    }

    public class GameDataThread implements Runnable {
        private JSONObject gameData;

        @Override
        public void run() {
            while (true) {
                while (true) {
                    gameData = new JSONObject();
                    try {
                        gameData.put("posX", posX);
                        gameData.put("posY", posY);
                        gameData.put("color", paintColor);
                        gameData.put("width", paintWidth);
                        gameData.put("actionState", actionState);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    clientSocket.InfoSender(remotePort, remoteIP, gameData.toString());
                    /*
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    */
                }
            }
        }
    }
}