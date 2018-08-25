package com.eleven.acoustic.util;

public class Complex {
	private double m_real;
    private double m_image;

    public Complex() {
        m_image = 0.0;
        m_real = 0.0;
    }

    public static Complex[] QPSK = creatQPSK();

    public static Complex BPSK0 = new Complex(-1.0, 0.0);
    public static Complex BPSK1 = new Complex(1.0, 0.0);

    public static Complex ZERO = new Complex(0.0, 0.0);

    public Complex(double real, double image) {
        m_real = real;
        m_image = image;
    }

    public void setReal(double real) {
        m_real = real;
    }

    public void setImage(double image){
        m_image = image;
    }

    public double getReal(){
        return m_real;
    }

    public double getImage(){
        return m_image;
    }

    public double getAmplitude() {
        return Math.sqrt(Math.pow(m_image, 2) + Math.pow(m_real, 2));
    }

    public static Complex add(Complex lhs, Complex rhs) {
        Complex result = new Complex(lhs.getReal() + rhs.getReal(), lhs.getImage() + rhs.getImage());
        return result;
    }

    public static Complex multiply(Complex lhs, Complex rhs) {
        Complex result = new Complex();
        result.setReal(lhs.getReal()*rhs.getReal() - lhs.getImage()*rhs.getImage());
        result.setImage(lhs.getReal()*rhs.getImage() + lhs.getImage()*rhs.getReal());
        return result;
    }

    public static Complex minus(Complex lhs, Complex rhs) {
        Complex result = new Complex(lhs.getReal() - rhs.getReal(), lhs.getImage() - rhs.getImage());
        return result;
    }
    
    public Complex conjugate() {
    	return new Complex(m_real, -m_image);
    }
    
    public Complex times(double args) {
    	return new Complex(args*m_real, args*m_image);
    }

    private static Complex[] creatQPSK() {
        Complex[] qpsk = new Complex[4];
        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    qpsk[i] = new Complex(1.0,1.0);
                    break;
                case 1:
                    qpsk[i] = new Complex(-1.0,1.0);
                    break;
                case 2:
                    qpsk[i] = new Complex(-1.0,-1.0);
                    break;
                case 3:
                    qpsk[i] = new Complex(1.0,-1.0);
                    break;
            }
        }
        return qpsk;
    }
}
