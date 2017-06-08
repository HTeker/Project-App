package com.weatheradviceapp.helpers;

import com.weatheradviceapp.R;

/**
 * Icons for the weather conditions on OpenWeatherMap:
 *   http://www.openweathermap.org/weather-conditions
 *
 * This class can also be used to get background images for the icon or the current activity for
 * example.
 */
public class WeatherImageMapper {

    public static int getWeatherIconResource(String icon, int weatherId) {

        // When one of the following conditions is met return with the dedicated icon
        switch(weatherId) {
            case 202:
            case 212:
                return R.drawable.w202;
            case 500:
                return R.drawable.w10d;
            case 502:
            case 503:
            case 504:
                return R.drawable.w09;
            case 511:
                return R.drawable.w511;
            case 602:
                return R.drawable.w602;
        }

        // Otherwise get the image for the weather icon
        switch(icon) {
            case "01d":
                return R.drawable.w01d;
            case "01n":
                return R.drawable.w01n;
            case "02d":
                return R.drawable.w02d;
            case "02n":
                return R.drawable.w02n;
            case "03d":
                return R.drawable.w03d;
            case "03n":
                return R.drawable.w03n;
            case "04d":
            case "04n":
                return R.drawable.w04d;
            case "09d":
            case "09n":
                return R.drawable.w09;
            case "10d":
                return R.drawable.w10d;
            case "10n":
                return R.drawable.w10n;
            case "11d":
                return R.drawable.w11d;
            case "11n":
                return R.drawable.w11n;
            case "13d":
                return R.drawable.w13d;
            case "13n":
                return R.drawable.w13n;
            case "50d":
            case "50n":
                return R.drawable.w50;

            default:
                return R.drawable.w01d;
        }
    }

    public static int getWeatherBackgroundResource(String icon, int weatherId) {

        // When one of the following conditions is met return with the dedicated icon
        switch(weatherId) {
            case 202:
            case 212:
                return R.drawable.bg_thunder;
            case 500:
            case 502:
            case 503:
            case 504:
                return R.drawable.bg_rain;
            case 511:
            case 602:
                return R.drawable.bg_snow;
        }

        // Otherwise get the image for the weather icon
        switch(icon) {
            case "01d":
            case "01n":
            case "02d":
            case "02n":
            case "03d":
            case "03n":
                return R.drawable.bg_sunny;
            case "04d":
            case "04n":
                return R.drawable.bg_clouds;
            case "09d":
            case "09n":
            case "10d":
            case "10n":
                return R.drawable.bg_rain;
            case "11d":
            case "11n":
                return R.drawable.bg_thunder;
            case "13d":
            case "13n":
                return R.drawable.bg_snow;
            case "50d":
            case "50n":
                return R.drawable.bg_fog;

            default:
                return R.drawable.bg_sunny;
        }
    }

    public static int getWeatherForegroundColor(String icon, int weatherId) {
        switch(getWeatherBackgroundResource(icon, weatherId)) {
            case R.drawable.bg_thunder:
            case R.drawable.bg_rain:
            case R.drawable.bg_clouds:
                return R.color.colorTextLight;
            default:
                return R.color.colorTextDark;
        }
    }
}
