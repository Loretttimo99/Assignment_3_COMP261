
public class ComplexNumber
{
    /**
     * The real, Re(z), part of the <code>ComplexNumber</code>.
     */
    private double real;
    /**
     * The imaginary, Im(z), part of the <code>ComplexNumber</code>.
     */
    private double imaginary;

    /**
     * Constructs a new <code>ComplexNumber</code> object with both real and imaginary parts 0 (z = 0 + 0i).
     */
    public ComplexNumber()
    {
        real = 0.0;
        imaginary = 0.0;
    }

    /**
     * Constructs a new <code>ComplexNumber</code> object.
     * @param real the real part, Re(z), of the complex number
     * @param imaginary the imaginary part, Im(z), of the complex number
     */
    public ComplexNumber(double real, double imaginary)
    {
        this.real = real;
        this.imaginary = imaginary;
    }

    /**
     * Sets the value of current complex number to the passed complex number.
     * @param z the complex number
     */
    public void set(ComplexNumber z)
    {
        this.real = z.real;
        this.imaginary = z.imaginary;
    }

    /**
     * The real part of <code>ComplexNumber</code>
     * @return the real part of the complex number
     */
    public double getRe()
    {
        return this.real;
    }

    /**
     * The imaginary part of <code>ComplexNumber</code>
     * @return the imaginary part of the complex number
     */
    public double getIm()
    {
        return this.imaginary;
    }

    /**
     * The modulus, magnitude or the absolute value of current complex number.
     * @return the magnitude or modulus of current complex number
     */
    public double mod()
    {
        return Math.sqrt(Math.pow(this.real,2) + Math.pow(this.imaginary,2));
    }

    /**
     * @return the complex number in x + yi format
     */
    @Override
    public String toString()
    {
        String re = this.real+"";
        String im = "";
        if(this.imaginary < 0)
            im = this.imaginary+"i";
        else
            im = "+"+this.imaginary+"i";
        return re+im;
    }

    /**
     * Checks if the passed <code>ComplexNumber</code> is equal to the current.
     * @param z the complex number to be checked
     * @return true if they are equal, false otherwise
     */
    @Override
    public final boolean equals(Object z)
    {
        if (!(z instanceof ComplexNumber))
            return false;
        ComplexNumber a = (ComplexNumber) z;
        return (real == a.real) && (imaginary == a.imaginary);
    }

    //Simple operations for complex numbers implemented into the program
    //These equations were taken from the lecture slides

    public static ComplexNumber add(ComplexNumber n1, ComplexNumber n2){
        double real = n1.getRe() + n2.getRe();
        double img = n1.getIm() + n2.getIm();
        return new ComplexNumber(real,img);
    }

    public ComplexNumber subtract(ComplexNumber n1, ComplexNumber n2){
        double real = n1.getRe() - n2.getRe();
        double img = n1.getIm() - n2.getIm();
        return new ComplexNumber(real,img);
    }

    public ComplexNumber multiply(ComplexNumber n1, ComplexNumber n2){
        double real = (n1.getRe()*n2.getRe()) - (n1.getIm()*n2.getIm());
        double img = (n1.getIm()*n2.getRe()) + (n1.getRe()*n2.getIm());
        return new ComplexNumber(real,img);
    }

    public ComplexNumber divide(ComplexNumber n1, ComplexNumber n2){
        double realnumerator = ((n1.getRe()*n2.getRe())+(n1.getIm())*n2.getIm());
        double denominator = ((n2.getRe()*n2.getRe()) + (n2.getIm()*n2.getIm()));
        double real = realnumerator/denominator;

        double imgnumerator = ((n1.getIm()*n2.getRe()) - (n1.getRe()*n2.getIm()));
        double img = imgnumerator/denominator;

        return new ComplexNumber(real,img);
    }

    public ComplexNumber exponential(ComplexNumber n1){
        double exp = Math.exp(n1.getRe());
        double sine = Math.sin(n1.getIm());
        double cosine = Math.cos(n1.getIm());
        double real = exp*cosine;
        double img = exp*sine;
        return new ComplexNumber(real,img);
    }


}