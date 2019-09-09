import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Diet {

    BufferedReader br;
    PrintWriter out;
    StringTokenizer st;
    boolean eof;

    int solveDietProblem(int n, int m, BigDecimal A[][], BigDecimal[] b, BigDecimal[] c, BigDecimal[] x) {
        // create extended system of equations
        BigDecimal[][] extendedA = new BigDecimal[n + m + 1][m];
        BigDecimal[] extendedB = new BigDecimal[n + m + 1];
        System.arraycopy(b, 0, extendedB, 0, n);
        Arrays.fill(extendedB, n, n + m, BigDecimal.ZERO);
        extendedB[n + m] = new BigDecimal(1e9);
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, extendedA[i], 0, m);
        }

        for (int i = n; i < n + m; i++) {
            Arrays.fill(extendedA[i], BigDecimal.ZERO);
            extendedA[i][i - n] = new BigDecimal(1);
        }
        // add last inequality a1 + a2 + ... + < 1e9;
        Arrays.fill(extendedA[n + m], BigDecimal.ONE);

        BigDecimal maxRes = new BigDecimal(-Double.MAX_VALUE);
        BigDecimal[] maxResult = new BigDecimal[m];

        List<int[]> res = getCombinations(n + 1, m);
        for (int[] coef : res) {
            Equation equation = getEquation(extendedA, extendedB, coef);
            byte decision = 0;
            BigDecimal[] result = null;
            try {
                result = solveEquation(equation);
            } catch (MyException e) {
                if (equation.b[e.des].setScale(10).compareTo(BigDecimal.ZERO) == 0) {
                    // infinity solutions
                    decision = 1;
                } else {
                    // inconsistent
                    decision = -1;
                }
            }

            if (decision == 0) {
                if (isValidResult(result, A, b)) {
                    BigDecimal r = getFunctionResult(result, c);
                    if (r.compareTo(maxRes) == 1) {
                        if (Arrays.stream(coef).boxed().collect(Collectors.toList()).contains(n + m)) {
                            return 1;
                        }
                        maxRes = r;
                        System.arraycopy(result, 0, maxResult, 0, m);
                    }
                }
            } else if (decision == 1 && Arrays.stream(coef).boxed().collect(Collectors.toList()).contains(n + m)) {
                return decision;
            }
        }
        if (maxRes.compareTo(new BigDecimal(-Double.MAX_VALUE)) == 1) {
            System.arraycopy(maxResult, 0, x, 0, m);
            return 0;
        } else {
            return -1;
        }
    }

    private List<int[]> getCombinations(int n, int m) {
        int[] source = IntStream.range(0, n + m).toArray();
        int data[] = new int[m];

        List<int[]> res = new ArrayList<>();
        combinationUtil(source, data, 0, n + m - 1, 0, m, res);

        return res;
    }

    private Equation getEquation(BigDecimal extendedA[][], BigDecimal[] extendedB, int[] coef) {
        int m = coef.length;
        BigDecimal[][] a = new BigDecimal[m][m];
        BigDecimal[] bf = new BigDecimal[m];
        for (int i = 0; i < m; i++) {
            System.arraycopy(extendedA[coef[i]], 0, a[i], 0, m);
            bf[i] = extendedB[coef[i]];
        }
        return new Equation(a, bf);
    }

    void printOutArray(double[] arr) {
        for (int j = 0; j < arr.length; j++) {
            System.out.print(arr[j] + " ");
        }
        System.out.println();
    }

    public static double roundAvoid(double value) {
        double scale = Math.pow(10, 3);
        return Math.round(value * scale) / scale;
    }

    boolean isValidResult(BigDecimal[] result, BigDecimal[][] A, BigDecimal[] b) {
        for (int i = 0; i < result.length; i++) {
            if (result[i].compareTo(BigDecimal.ZERO) == -1) {
                return false;
            }
        }
        for (int i = 0; i < A.length; i++) {
            if (b[i].compareTo(getFunctionResult(result, A[i]).setScale(10, RoundingMode.HALF_UP)) == -1) {
                return false;
            }
        }
        return true;
    }

    BigDecimal getFunctionResult(BigDecimal[] result, BigDecimal[] c) {
        BigDecimal res = BigDecimal.ZERO;
        for (int i = 0; i < result.length; i++) {
            res = res.add(result[i].multiply(c[i]));
        }

        return res;
    }

    void solve() throws IOException {
        int n = nextInt();
        int m = nextInt();
        BigDecimal[][] A = new BigDecimal[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                A[i][j] = nextBigDecimal();
            }
        }
        BigDecimal[] b = new BigDecimal[n];
        for (int i = 0; i < n; i++) {
            b[i] = nextBigDecimal();
        }
        BigDecimal[] c = new BigDecimal[m];
        for (int i = 0; i < m; i++) {
            c[i] = nextBigDecimal();
        }
        BigDecimal[] ansx = new BigDecimal[m];
        int anst = solveDietProblem(n, m, A, b, c, ansx);
        if (anst == -1) {
            out.printf("No solution\n");
            return;
        }
        if (anst == 0) {
            out.printf("Bounded solution\n");
            for (int i = 0; i < m; i++) {
                out.printf("%.18f%c", ansx[i], i + 1 == m ? '\n' : ' ');
            }
            return;
        }
        if (anst == 1) {
            out.printf("Infinity\n");
            return;
        }
    }

    Diet() throws IOException {
        br = new BufferedReader(new InputStreamReader(System.in));
        out = new PrintWriter(System.out);
        solve();
        out.close();
    }

    public static void main(String[] args) throws IOException {
        new Diet();
    }

    String nextToken() {
        while (st == null || !st.hasMoreTokens()) {
            try {
                st = new StringTokenizer(br.readLine());
            } catch (Exception e) {
                eof = true;
                return null;
            }
        }
        return st.nextToken();
    }

    int nextInt() throws IOException {
        return Integer.parseInt(nextToken());
    }

    BigDecimal nextBigDecimal() {
        return new BigDecimal(nextToken());
    }

    static void combinationUtil(int arr[], int data[], int start,
                                int end, int index, int r, List<int[]> result) {
        // Current combination is ready to be printed, print it
        if (index == r) {
            result.add(Arrays.copyOfRange(data, 0, r));
            return;
        }

        // replace index with all possible elements. The condition
        // "end-i+1 >= r-index" makes sure that including one element
        // at index will make a combination with remaining elements
        // at remaining positions
        for (int i = start; i <= end && end - i + 1 >= r - index; i++) {
            data[index] = arr[i];
            combinationUtil(arr, data, i + 1, end, index + 1, r, result);
        }
    }

    static BigDecimal[] solveEquation(Equation equation) {
        BigDecimal a[][] = equation.a;
        BigDecimal b[] = equation.b;
        int size = a.length;

        for (int step = 0; step < size; ++step) {
            Position pivot_element = selectPivotElement(a, step);
            swapLines(a, b, step, pivot_element);
            processPivotElement(a, b, step);
        }

        BigDecimal[] x = new BigDecimal[size];
        for (int i = size - 1; i >= 0; i--) {
            BigDecimal sum = BigDecimal.ZERO;
            for (int j = i + 1; j < size; j++) {
                sum = sum.add(a[i][j].multiply(x[j]));
            }
            x[i] = b[i].subtract(sum).divide(a[i][i], MathContext.DECIMAL64);
        }
        return x;
    }

    static Position selectPivotElement(BigDecimal a[][], int step) {
        int n = a.length;
        int max = step;
        for (int i = step + 1; i < n; i++) {
            if (a[i][step].abs().compareTo(a[max][step].abs()) == 1) {
                max = i;
            }
        }
        if (a[max][step].setScale(10, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) == 0) {
            throw new MyException(step);
        }
        return new Position(max, max);
    }

    static void swapLines(BigDecimal a[][], BigDecimal b[], int step, Position pivot_element) {
        int max = pivot_element.raw;
        BigDecimal[] temp = a[step];
        a[step] = a[max];
        a[max] = temp;
        BigDecimal t = b[step];
        b[step] = b[max];
        b[max] = t;
    }

    static void processPivotElement(BigDecimal a[][], BigDecimal b[], int p) {
        int n = a.length;
        for (int i = p + 1; i < n; i++) {
            BigDecimal alpha = a[i][p].divide(a[p][p], MathContext.DECIMAL64);
            b[i] = b[i].subtract(b[p].multiply(alpha));
            for (int j = p; j < n; j++) {
                a[i][j] = a[i][j].subtract(alpha.multiply(a[p][j]));
            }
        }
    }

    static void printCombination(int arr[], int n, int r) {
        // A temporary array to store all combination one by one
        int data[] = new int[r];

        List<int[]> res = new ArrayList<>();
        // Print all combination using temprary array 'data[]'
        combinationUtil(arr, data, 0, n - 1, 0, r, res);
    }

    class Equation {
        Equation(BigDecimal a[][], BigDecimal b[]) {
            this.a = a;
            this.b = b;
        }

        BigDecimal a[][];
        BigDecimal b[];
    }

    static class MyException extends RuntimeException {
        Integer des;

        public MyException(Integer des) {
            this.des = des;
        }
    }

    static class Position {
        Position(int column, int raw) {
            this.column = column;
            this.raw = raw;
        }

        int column;
        int raw;
    }
}
