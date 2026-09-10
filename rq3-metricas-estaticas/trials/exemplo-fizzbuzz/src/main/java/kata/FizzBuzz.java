package kata;

import java.util.ArrayList;
import java.util.List;

public class FizzBuzz {

    public List<String> gerar(int limite) {
        List<String> resultado = new ArrayList<>();
        for (int numero = 1; numero <= limite; numero++) {
            if (numero % 3 == 0 && numero % 5 == 0) {
                resultado.add("FizzBuzz");
            } else if (numero % 3 == 0) {
                resultado.add("Fizz");
            } else if (numero % 5 == 0) {
                resultado.add("Buzz");
            } else {
                resultado.add(String.valueOf(numero));
            }
        }
        return resultado;
    }

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

    public String classificar(int numero) {
        if (numero % 15 == 0) {
            return "FizzBuzz";
        } else if (numero % 3 == 0) {
            return "Fizz";
        } else if (numero % 5 == 0) {
            return "Buzz";
        } else if (numero % 2 == 0) {
            return "Par";
        } else {
            return String.valueOf(numero);
        }
    }
}
