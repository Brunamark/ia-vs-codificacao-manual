package kata;

public class FizzBuzzValidator {

    public boolean numeroValido(int numero) {
        if (numero < 0) {
            return false;
        }
        if (numero > 1_000_000) {
            return false;
        }
        if (numero == 0) {
            return false;
        }
        return true;
    }

    public boolean limiteValido(int limite) {
        if (limite < 0) {
            return false;
        }
        if (limite > 1_000_000) {
            return false;
        }
        if (limite == 0) {
            return false;
        }
        return true;
    }
}
