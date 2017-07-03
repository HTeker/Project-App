package com.weatheradviceapp.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.weatheradviceapp.R;
import com.weatheradviceapp.models.Advice;

public class AdviceVisualizer {

    private View adviceView;
    private ImageView adviceIcon;
    private TextView adviceText;
    private Advice advice;

    public AdviceVisualizer(LayoutInflater inflater, ViewGroup container) {
        // Initialize view.
        adviceView = inflater.inflate(R.layout.advice_layout, container);
        adviceIcon = (ImageView) adviceView.findViewById(R.id.adviceIcon);
        adviceText = (TextView) adviceView.findViewById(R.id.adviceText);
    }

    public void clearAdvice() {
        // Remove advice content.
        adviceIcon.setBackgroundResource(android.R.color.transparent);
        adviceIcon.setImageResource(android.R.color.transparent);
        adviceText.setText("");
        this.advice = null;
        this.hide();
    }

    public void showAdvice(Advice advice) {
        // Set advice data and show.
        this.advice = advice;
        adviceIcon.setBackgroundResource(R.drawable.rounded_rect);
        adviceIcon.setImageResource(advice.getAdviceIconResource());
        adviceText.setText(advice.toString());
        this.show();
    }

    public void findNewViews(LayoutInflater inflater, ViewGroup container) {

        // Update view references when we transitioned.
        adviceView = inflater.inflate(R.layout.advice_layout, container);
        adviceIcon = (ImageView) adviceView.findViewById(R.id.adviceIcon);
        adviceText = (TextView) adviceView.findViewById(R.id.adviceText);
        if (this.advice == null) {
            clearAdvice();
        } else {
            showAdvice(this.advice);
        }
    }

    public void hideText() {
        adviceText.setVisibility(View.GONE);
    }

    public void showText() {
        adviceText.setVisibility(View.VISIBLE);
    }

    public void hide() {
        adviceView.setVisibility(View.GONE);
    }

    public void show() {
        adviceView.setVisibility(View.VISIBLE);
    }
}
