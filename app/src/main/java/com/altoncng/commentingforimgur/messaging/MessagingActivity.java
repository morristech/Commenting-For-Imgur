package com.altoncng.commentingforimgur.messaging;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.altoncng.commentingforimgur.R;
import com.altoncng.commentingforimgur.utils.dbLog;

/**
 * Created by Eye on 2/3/2016.
 */
public class MessagingActivity extends Activity {

    Button moveButton;
    Button unmoveButton;
    TextView moveTV;
    ObjectAnimator animator;
    ObjectAnimator unanimator;

    float movement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging_main);

        movement = pxFromDp(this, 170);

        moveButton = (Button) findViewById(R.id.moveButton);
        unmoveButton = (Button) findViewById(R.id.unmoveButton);
        moveTV = (TextView) findViewById(R.id.moveTV);

        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                moveTV.animate().translationXBy(-movement).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        dbLog.d("imgurLog", "imgurLog messaging side animation close");
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        moveTV.setTranslationX(moveTV.getTranslationX() - movement);
                        moveTV.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }

                }).start();
                unmoveButton.setClickable(true);
                moveButton.setClickable(false);
            }
        });

        unmoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                moveTV.setVisibility(View.INVISIBLE);
                //moveTV.setTranslationX(moveTV.getTranslationX()-movement);
                moveTV.setTranslationX(-movement);
                moveTV.setVisibility(View.VISIBLE);

                moveTV.animate().translationXBy(movement).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        dbLog.d("imgurLog", "imgurLog messaging side animation open");
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        moveTV.setTranslationX(0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {}

                    @Override
                    public void onAnimationRepeat(Animator animation) {}

                }).start();
                unmoveButton.setClickable(false);
                moveButton.setClickable(true);
            }
        });

        /*animator = ObjectAnimator.ofFloat(moveTV, "translationX", -200);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                moveTV.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.setDuration(2000);
        animator.setInterpolator(new OvershootInterpolator());

        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animator.start();
                unmoveButton.setClickable(true);
                moveButton.setClickable(false);
            }
        });

        unanimator = ObjectAnimator.ofFloat(moveTV, "translationX", 200);
        unanimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                moveTV.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        unanimator.setDuration(2000);
        unanimator.setInterpolator(new OvershootInterpolator());

        unmoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unanimator.start();
                unmoveButton.setClickable(false);
                moveButton.setClickable(true);
            }
        });*/

    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}
