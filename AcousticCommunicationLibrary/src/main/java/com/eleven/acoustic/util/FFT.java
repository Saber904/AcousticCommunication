package com.eleven.acoustic.util;
import java.util.Arrays;

/**
 * Created by Eleven on 2015/12/29.
 */
public class FFT {
    private static double[] bitReverseCopy(double[] data, int len){
        int n = (int)Math.ceil(Math.log10(len) / Math.log10(2));
        double[] temp = Arrays.copyOf(data, len);
        for (int l = 0; l < len; l++) {
            int m = l;
            for (int i = 0, j = n - 1; i < j ; i++, j--){
                if (((m >> i & 0x01) ^ (m >> j & 0x01)) == 1){
                    m ^= 1 << i;
                    m ^= 1 << j;
                }
            }
            if (m != l) {
                temp[l] = data[m];
            }
        }
        return temp;
    }

    private static Complex[] bitReverseCopy(Complex[] data, int len){
        int n = (int)Math.ceil(Math.log10(len) / Math.log10(2));
        Complex[] temp = Arrays.copyOf(data, len);
        for (int l = 0; l < len; l++) {
            int m = l;
            for (int i = 0, j = n - 1; i < j ; i++, j--){
                if (((m >> i & 0x01) ^ (m >> j & 0x01)) == 1){
                    m ^= 1 << i;
                    m ^= 1 << j;
                }
            }
            if (m != l) {
                temp[m] = data[l];
            }
        }
        return temp;
    }

    public static double[] rFFT(double[] data, int len) {
        data = Arrays.copyOf(data, len);
        data = bitReverseCopy(data, len);
        Complex[] complex_fft = new Complex[len];
        for (int i = 0; i < len; ++i){
            complex_fft[i] = new Complex(data[i], 0.0);
        }
        int n = (int)Math.ceil(Math.log10(len) / Math.log10(2));
        for (int i = 1; i <= n; i++) {
            int m = 1 << i;
            Complex W_m = new Complex(Math.cos(2*Math.PI/m), -Math.sin(2*Math.PI/m));

            for (int j = 0; j < len; j += m) {
                Complex W = new Complex(1.0, 0.0);
                for (int k = 0; k < m/2; k++){
                    Complex t = Complex.multiply(W, complex_fft[j + k + m/2]);
                    Complex u = complex_fft[j + k];
                    complex_fft[j + k] = Complex.add(u, t);
                    complex_fft[j + k + m/2] = Complex.minus(u, t);
                    W = Complex.multiply(W, W_m);
                }
            }
        }

        double[] fft_data = new double[len];

        for (int i = 0; i < len; i++) {
            fft_data[i] = complex_fft[i].getAmplitude();
        }
//        fft_data[0] = complex_fft[0].getReal();
//        fft_data[1] = complex_fft[len / 2].getReal();
//        for (int i = 1; i < len / 2; i++){
//            fft_data[2 * i] = complex_fft[i].getReal();
//            fft_data[2 * i + 1] = complex_fft[i].getImage();
//        }

        return fft_data;
    }


    public static Complex[] cFFT(Complex[] data, int len) {
        Complex[] complex_fft = Arrays.copyOf(data, len);
        complex_fft = bitReverseCopy(complex_fft, len);
        int n = (int)Math.ceil(Math.log10(len) / Math.log10(2));
        for (int i = 1; i <= n; i++) {
            int m = 1 << i;
            Complex W_m = new Complex(Math.cos(2*Math.PI/m), -Math.sin(2*Math.PI/m));

            for (int j = 0; j < len; j += m) {
                Complex W = new Complex(1.0, 0.0);
                for (int k = 0; k < m/2; k++){
                    Complex t = Complex.multiply(W, complex_fft[j + k + m/2]);
                    Complex u = complex_fft[j + k];
                    complex_fft[j + k] = Complex.add(u, t);
                    complex_fft[j + k + m/2] = Complex.minus(u, t);
                    W = Complex.multiply(W, W_m);
                }
            }
        }
        return complex_fft;
    }

    public static Complex[] cFFT(double[] data, int len) {
        data = Arrays.copyOf(data, len);
        data = bitReverseCopy(data, len);
        Complex[] complex_fft = new Complex[len];
        for (int i = 0; i < len; ++i){
            complex_fft[i] = new Complex(data[i], 0.0);
        }
        int n = (int)Math.ceil(Math.log10(len) / Math.log10(2));
        for (int i = 1; i <= n; i++) {
            int m = 1 << i;
            Complex W_m = new Complex(Math.cos(2*Math.PI/m), -Math.sin(2*Math.PI/m));

            for (int j = 0; j < len; j += m) {
                Complex W = new Complex(1.0, 0.0);
                for (int k = 0; k < m/2; k++){
                    Complex t = Complex.multiply(W, complex_fft[j + k + m/2]);
                    Complex u = complex_fft[j + k];
                    complex_fft[j + k] = Complex.add(u, t);
                    complex_fft[j + k + m/2] = Complex.minus(u, t);
                    W = Complex.multiply(W, W_m);
                }
            }
        }
        return complex_fft;
    }

    public static Complex[] iFFT(Complex[] fre_data) {
        int N = fre_data.length;
        Complex[] time_data = new Complex[N];

        for (int i = 0; i < N; i++) {
            time_data[i] = fre_data[i].conjugate();
        }

        time_data = cFFT(time_data, N);

        for (int i = 0; i < N; i++) {
            time_data[i] = time_data[i].conjugate();
        }

        for (int i = 0; i < N; i++) {
            time_data[i] = time_data[i].times(1.0/N);
        }

        return time_data;
    }
}