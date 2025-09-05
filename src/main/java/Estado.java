import java.util.Arrays;
import java.util.Objects;

public class Estado {
	boolean[] bitMap;
    int[] acoes = {0,0,0};

	public Estado (boolean[] bitMap) {
        this.bitMap = bitMap ;
	}

    void mudarValor(int pos, int valor) {
        acoes[pos] = valor;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Estado estado = (Estado) o;
        return Objects.deepEquals(bitMap, estado.bitMap);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bitMap);
    }
}