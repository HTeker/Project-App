package com.weatheradviceapp.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.weatheradviceapp.R;
import com.weatheradviceapp.helpers.WeatherImageMapper;
import com.weatheradviceapp.models.Advice;

public class AdviceVisualizer {

    private View adviceView;
    private ImageView adviceIcon;
    private TextView adviceText;

    private ViewGroup container;


    public AdviceVisualizer(LayoutInflater inflater, ViewGroup container) {

        this.container = container;

        // Initialize view
        adviceView = inflater.inflate(R.layout.advice_layout, container);
        adviceIcon = (ImageView) adviceView.findViewById(R.id.adviceIcon);
        adviceText = (TextView) adviceView.findViewById(R.id.adviceText);
    }

    public void clearAdvice() {
        adviceIcon.setImageResource(android.R.color.transparent);
        adviceText.setText("");
    }

    public void showAdvice(Advice advice) {
        adviceIcon.setImageResource(advice.getAdviceIconResource());
        adviceText.setText(advice.toString());
    }
}
