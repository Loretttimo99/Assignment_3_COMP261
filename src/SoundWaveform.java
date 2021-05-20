

// DO NOT DISTRIBUTE THIS FILE TO STUDENTS
import ecs100.UI;
import ecs100.UIFileChooser;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/*
  getAudioInputStream
  -> getframelength,
  -> read into byteArray of 2x that many bytes
  -> convert to array of doubles in reversed pairs of bytes (signed)
  -> scale #FFFF to +/- 300

  array of doubles
   -> unscale  +/- 300  to #FFFF (
   -> convert to array of bytes (pairs little endian, signed)
   -> convert to inputStream
   -> convert to AudioInputStream
   -> write to file.
 */

public class SoundWaveform{

    public static final double MAX_VALUE = 300;
    public static final int SAMPLE_RATE = 44100;
    public static final int MAX_SAMPLES = SAMPLE_RATE/100;   // samples in 1/100 sec

    public static final int GRAPH_LEFT = 10;
    public static final int ZERO_LINE = 310;
    public static final int X_STEP = 2;            //pixels between samples
    public static final int GRAPH_WIDTH = MAX_SAMPLES*X_STEP;

    private ArrayList<Double> waveform = new ArrayList<Double>();   // the displayed waveform
    private ArrayList<ComplexNumber> spectrum = new ArrayList<ComplexNumber>(); // the spectrum: length/mod of each X(k)
    private ComplexNumber c = new ComplexNumber();

    private boolean waveform_flag = false;

    /**
     * Displays the waveform.
     */
    public void displayWaveform(){
        waveform_flag = true;
        if (this.waveform == null){ //there is no data to display
            UI.println("No waveform to display");
            return;
        }
        UI.clearText();
        UI.println("Printing, please wait...");

        UI.clearGraphics();

        // draw x axis (showing where the value 0 will be)
        UI.setColor(Color.black);
        UI.drawLine(GRAPH_LEFT, ZERO_LINE, GRAPH_LEFT + GRAPH_WIDTH , ZERO_LINE);

        // plot points: blue line between each pair of values
        UI.setColor(Color.blue);

        double x = GRAPH_LEFT;
        for (int i=1; i<this.waveform.size(); i++){
            double y1 = ZERO_LINE - this.waveform.get(i-1);
            double y2 = ZERO_LINE - this.waveform.get(i);
            if (i>MAX_SAMPLES){UI.setColor(Color.red);}
            UI.drawLine(x, y1, x+X_STEP, y2);
            x = x + X_STEP;
        }

        UI.println("Printing completed!");
    }

    /**
     * Displays the spectrum. Scale to the range of +/- 300.
     */
    public void displaySpectrum() {
        waveform_flag = false;
        if (this.spectrum == null){ //there is no data to display
            UI.println("No spectrum to display");
            return;
        }
        UI.clearText();
        UI.println("Printing, please wait...");

        UI.clearGraphics();

        // calculate the mode of each element
        ArrayList<Double> spectrumMod = new ArrayList<Double>();
        double max = 0;
        for (int i = 0; i < spectrum.size(); i++) {
            if (i == MAX_SAMPLES)
                break;

            double value = spectrum.get(i).mod();
            max = Math.max(max, value);
            spectrumMod.add(spectrum.get(i).mod());
        }

        double scaling = 300/max;
        for (int i = 0; i < spectrumMod.size(); i++) {
            spectrumMod.set(i, spectrumMod.get(i)*scaling);
        }

        // draw x axis (showing where the value 0 will be)
        UI.setColor(Color.black);
        UI.drawLine(GRAPH_LEFT, ZERO_LINE, GRAPH_LEFT + GRAPH_WIDTH , ZERO_LINE);

        // plot points: blue line between each pair of values
        UI.setColor(Color.blue);

        double x = GRAPH_LEFT;
        for (int i=1; i<spectrumMod.size(); i++){
            double y1 = ZERO_LINE;
            double y2 = ZERO_LINE - spectrumMod.get(i);
            if (i>MAX_SAMPLES){UI.setColor(Color.red);}
            UI.drawLine(x, y1, x+X_STEP, y2);
            x = x + X_STEP;
        }

        UI.println("Printing completed!");
    }

    public void dft() {
        UI.clearText();
        UI.println("DFT in process, please wait...");


        for (int k = 0; k < waveform.size(); k++) { //iteration through each value in the waveform
            ComplexNumber Xk = new ComplexNumber(0, 0); //A new variable to store the transformed value
            for (int n = 0; n < waveform.size(); n++){ //Used to sum each value from the first to final sample
                double Xn = waveform.get(n);
                ComplexNumber XnComplex = new ComplexNumber(Xn,0); //transforming the value of the waveform into complex form
                ComplexNumber power = new ComplexNumber(0, -1*n*k*((2*Math.PI)/waveform.size())); //the number taken used as the power for e
                ComplexNumber e = c.exponential(power);//exponential of previous complex number
                ComplexNumber sum = c.multiply(XnComplex,e);//multiplication of x(n) and e to make the transformed value for one sample
                Xk = c.add(Xk,sum);//summing over all samples in the waveform
            }
            spectrum.add(k,Xk);//adds new transformed value to the frequency domain spectrum
        }

        UI.println("DFT completed!");
        waveform.clear();
    }

    public void idft() {
        UI.clearText();
        UI.println("IDFT in process, please wait...");


        for (int n = 0; n < spectrum.size(); n++){//iterates through each frequency in the spectrum
            double Xn = 0;//placeholder for the sum
            ComplexNumber XnCmplx = new ComplexNumber(0,0);
            for (int k = 0; k < spectrum.size(); k++){//Used to sum each samples inverse result
                ComplexNumber Xk = spectrum.get(k);//stores the frequency
                ComplexNumber power = new ComplexNumber(0,n*k*((2*Math.PI)/spectrum.size()));//number taken used as the power of e
                ComplexNumber e = c.exponential(power);//takes exponential of previous complex number
                ComplexNumber sum = c.multiply(Xk,e);//sums the result
                XnCmplx = c.add(XnCmplx,sum);//adds the samples result to the sum
            }
            Xn = XnCmplx.getRe()/spectrum.size();//Only uses the real numbers, as rounding errors may leave small imaginary numbers which may be ignored, also scales the waveform by N samples
            waveform.add(n,Xn);//Adds the real number back to the waveform
        }

        UI.println("IDFT completed!");

        spectrum.clear();
    }

    public void fft() {
        UI.clearText();
        UI.println("FFT in process, please wait...");


        //This is a check to see whether the waveform is of a size where it is a power of 2.
        //This is done by equating the double of log base 2 of waveforms size with it's int.
        //The rounding of the int will then result in the nearest whole number, thus if they do not match then the waveforms size is not to a power of 2
        while((Math.log10(waveform.size())/Math.log10(2)) != (int)(Math.log10(waveform.size())/Math.log10(2))){
            waveform.remove(waveform.size()-1);//Trims the waveform until it is at a power of 2
        }

        //Adds all values in the waveform to an Array of complex numbers
        //This was done to make recursive part of the program more streamlined after I encountered issues changing between complex and real numbers.
        ComplexNumber[] wave = new ComplexNumber[waveform.size()];
        for(int i = 0; i < waveform.size(); i++){
            ComplexNumber Xn = new ComplexNumber(waveform.get(i),0);
            wave[i] = Xn;
        }

        //Casts the returned array to a Arraylist and calls the recursive method
        ArrayList<ComplexNumber> Xk = new ArrayList(Arrays.asList(FastFT(wave)));
        spectrum = Xk;

        UI.println("FFT completed!");
        waveform.clear();
    }

    public ComplexNumber[] FastFT(ComplexNumber[] x){

        //Base case return point
        if(x.length == 1){
            ComplexNumber[] base = new ComplexNumber[]{x[0]};//returns the number in form of an Array
            return base;
        }

        //As a check for if the length of the waveform is a power of 2 has already occurred another check is not required

        //splits the waveform into even and odd components
        ComplexNumber[] xeven = new ComplexNumber[x.length/2];
        ComplexNumber[] xodd = new ComplexNumber[x.length/2];

        //splitting the waveform
        for(int i = 0; i < x.length/2; i++){
            xeven[i] = x[2*i];
            xodd[i] = x[2*i + 1];
        }

        //recursive calls
        ComplexNumber[] Xeven = FastFT(xeven);
        ComplexNumber[] Xodd = FastFT(xodd);

        //return arrary
        ComplexNumber[] Xk = new ComplexNumber[x.length];

        //This is were the true math is done
        for(int k = 0; k < x.length/2; k++){
            double power1 = -2*Math.PI*k/x.length;//Calculates W(k,N)
            double power2 = -2*Math.PI*(k+(x.length/2))/x.length;//Calculates W(k+N/2,N)
            ComplexNumber WKN1 = c.exponential(new ComplexNumber(0,power1));
            ComplexNumber WKN2 = c.exponential(new ComplexNumber(0,power2));
            Xk[k] = c.add(Xeven[k],c.multiply(Xodd[k],WKN1));//Adding the even and odd arrays after W(k,N) has been applied to the odd values
            Xk[k+x.length/2] = c.add(Xeven[k],c.multiply(Xodd[k],WKN2));//Adding the even and odd arrays for the second period of the waveform, using W(k+N/2,N) instead
        }
        return Xk;//returns the array of completed transforms
    }

    public ComplexNumber[] InvFastFT(ComplexNumber[] X){

        //This method was found @https://www.dsprelated.com/showarticle/800.php and from brief recollections of ECEN 220
        //In essence swapping the imaginary and real values of the spectrum allow a for FFT to act as an inverse, with the result swapping the imaginary and real values

        for(int i = 0; i < X.length; i++){//swaps the inputs for the FFT
            double img = X[i].getRe();
            double real = X[i].getIm();
            X[i].set(new ComplexNumber(real,img));//Sets a new complex number in place with the imaginary equal to the real and vice versa
        }

        ComplexNumber[] Xn;

        Xn = FastFT(X);//Calls the FFT on the spectrum
        return Xn;

    }


    public void ifft() {
        UI.clearText();
        UI.println("IFFT in process, please wait...");


        //This is a check to see whether the waveform is of a size where it is a power of 2.
        //This is done by equating the double of log base 2 of waveforms size with it's int.
        //The rounding of the int will then result in the nearest whole number, thus if they do not match then the waveforms size is not to a power of 2

        while((Math.log10(spectrum.size())/Math.log10(2)) != (int)(Math.log10(spectrum.size())/Math.log10(2))){
            spectrum.remove(spectrum.size()-1);//trims the spectrum so that it may be reversed, not technically needed as the FFT'd waveform should already be to the power of 2
        }


        //Creating an array of the spectrum so it can be passed to the inverting method
        ComplexNumber[] spec = new ComplexNumber[spectrum.size()];
        for(int k = 0; k < spectrum.size(); k++){
            spec[k] = spectrum.get(k);
        }

        ArrayList<ComplexNumber> Xn = new ArrayList(Arrays.asList(InvFastFT(spec)));//Inverse FFT call is made
        ArrayList<Double> xn = new ArrayList<>();

        for (ComplexNumber i : Xn) {//Returns the real values, This is shown as imaginary due to the method of inverting swapping the imaginary and real components of the complex number
            xn.add(i.getIm()/spectrum.size());//scales the waveform by 1/N samples
        }

        waveform = xn;//waveform is reformed


        UI.println("IFFT completed!");

        spectrum.clear();
    }



    /**
     * Save the wave form to a WAV file
     */
    public void doSave() {
        WaveformLoader.doSave(waveform, WaveformLoader.scalingForSavingFile);
    }

    /**
     * Load the WAV file.
     */
    public void doLoad() {
        UI.clearText();
        UI.println("Loading...");

        waveform = WaveformLoader.doLoad();

        this.displayWaveform();

        UI.println("Loading completed!");
    }

    public static void main(String[] args){
        SoundWaveform wfm = new SoundWaveform();
        //core
        UI.addButton("Display Waveform", wfm::displayWaveform);
        UI.addButton("Display Spectrum", wfm::displaySpectrum);
        UI.addButton("DFT", wfm::dft);
        UI.addButton("IDFT", wfm::idft);
        UI.addButton("FFT", wfm::fft);
        UI.addButton("IFFT", wfm::ifft);
        UI.addButton("Save", wfm::doSave);
        UI.addButton("Load", wfm::doLoad);
        UI.addButton("Quit", UI::quit);
        UI.setMouseMotionListener(wfm::doMouse);
        UI.setWindowSize(950, 630);
    }

    private int spectrum_index;
    private boolean pressed_flag = false;
    private double initial_v;

    private void doMouse(String s, double v, double v1) {
        System.out.println(s);
        System.out.println("v: " + v + ", v1: " + v1);
        if (spectrum.isEmpty() || !waveform_flag){
            return;
        }

        if (s.equalsIgnoreCase("pressed")){
            pressed_flag = true;
            int index = (int)((GRAPH_LEFT - v)/X_STEP);
            System.out.println("Spectrum size: " + spectrum.size() + ", index: " + index);
            if (index >= spectrum.size()){
                System.out.println("shit fucked: " + index);
                return;
            }
            spectrum_index = index;
            initial_v = v1;
            System.out.println(spectrum.get(index));
//            spectrum.get((int)((v - GRAPH_LEFT)/X_STEP));
        }else if (s.equalsIgnoreCase("released")){
            pressed_flag = false;
            double dv = v1 - initial_v;
            spectrum.get(spectrum_index).set(ComplexNumber.add(spectrum.get(spectrum_index), new ComplexNumber(dv, 0)));

        }

    }
}
