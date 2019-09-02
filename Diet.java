import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

    int solveDietProblem(int n, int m, double A[][], double[] b, double[] c, double[] x) {
        // create extended system of equations
        double[][] extendedA = new double[n + m + 1][m];
        double[] extendedB = new double[n + m + 1];
        System.arraycopy(b, 0, extendedB, 0, n);
        Arrays.fill(extendedB, n, n + m, 0);
        extendedB[n + m] = 1e9;
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, extendedA[i], 0, m);
        }

        for (int i = n; i < n + m; i++) {
            Arrays.fill(extendedA[i], 0);
            extendedA[i][i - n] = 1;
        }
        // add last inequality a1 + a2 + ... + < 1e9;
        Arrays.fill(extendedA[n + m], 1);

        double maxRes = -Double.MAX_VALUE;
        double[] maxResult = new double[m];

        List<int[]> res = getCombinations(n + 1, m);
        for (int[] coef : res) {
            Equation equation = getEquation(extendedA, extendedB, coef);
            byte decision = 0;
            double[] result = null;
            try {
                result = solveEquation(equation);
            } catch (MyException e) {
                if (roundAvoid(equation.b[e.des]) == 0) {
                    // infinity solutions
                    decision = 1;
                } else {
                    // inconsistent
                    decision = -1;
                }
            }

//            System.out.println("decision: " + decision);
            if (decision == 0) {
//                printOutArray(result);
                if (isValidResult(result, A, b)) {
                    if (Arrays.stream(coef).boxed().collect(Collectors.toList()).contains(n + m)) {
                        return 1;
                    }
                    double r = getFunctionResult(result, c);
                    if (r > maxRes) {
                        maxRes = r;
                        System.arraycopy(result, 0, maxResult, 0, m);
                    }
                }
            } else if (decision == 1 && Arrays.stream(coef).boxed().collect(Collectors.toList()).contains(n + m)) {
                return decision;
            }
        }
//        System.out.println("Max result arr: " + maxRes);
//        printOutArray(maxResult);
        if (maxRes > -Double.MAX_VALUE) {
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

    private Equation getEquation(double extendedA[][], double[] extendedB, int[] coef) {
        int m = coef.length;
        double[][] a = new double[m][m];
        double[] bf = new double[m];
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

    boolean isValidResult(double[] result, double[][] A, double[] b) {
        for (int i = 0; i < result.length; i++) {
            if (roundAvoid(result[i]) < 0) {
                return false;
            }
        }
        for (int i = 0; i < A.length; i++) {
            if (b[i] < getFunctionResult(result, A[i])) {
                return false;
            }
        }
        return true;
    }

    double getFunctionResult(double[] result, double[] c) {
        int res = 0;
        for (int i = 0; i < result.length; i++) {
            res += result[i] * c[i];
        }

        return res;
    }

    void solve() throws IOException {
        int n = nextInt();
        int m = nextInt();
        double[][] A = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                A[i][j] = nextInt();
            }
        }
        double[] b = new double[n];
        for (int i = 0; i < n; i++) {
            b[i] = nextInt();
        }
        double[] c = new double[m];
        for (int i = 0; i < m; i++) {
            c[i] = nextInt();
        }
        double[] ansx = new double[m];
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

    static void combinationUtil(int arr[], int data[], int start,
                                int end, int index, int r, List<int[]> result) {
        // Current combination is ready to be printed, print it
        if (index == r) {
//            for (int j = 0; j < r; j++)
//                System.out.print(data[j] + " ");
            result.add(Arrays.copyOfRange(data, 0, r));
//            System.out.println();
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

    static double[] solveEquation(Equation equation) {
        double a[][] = equation.a;
        double b[] = equation.b;
        int size = a.length;

        for (int step = 0; step < size; ++step) {
            Position pivot_element = selectPivotElement(a, step);
            swapLines(a, b, step, pivot_element);
            processPivotElement(a, b, step);
        }

        double[] x = new double[size];
        for (int i = size - 1; i >= 0; i--) {
            double sum = 0.0;
            for (int j = i + 1; j < size; j++) {
                sum += a[i][j] * x[j];
            }
            x[i] = (b[i] - sum) / a[i][i];
        }
        return x;
    }

    static Position selectPivotElement(double a[][], int step) {
        int n = a.length;
        int max = step;
        for (int i = step + 1; i < n; i++) {
            if (Math.abs(a[i][step]) > Math.abs(a[max][step])) {
                max = i;
            }
        }
        if (roundAvoid(a[step][max]) == 0) {
            throw new MyException(step);
        }
        return new Position(max, max);
    }

    static void swapLines(double a[][], double b[], int step, Position pivot_element) {
        int max = pivot_element.raw;
        double[] temp = a[step];
        a[step] = a[max];
        a[max] = temp;
        double t = b[step];
        b[step] = b[max];
        b[max] = t;
    }

    static void processPivotElement(double a[][], double b[], int p) {
        int n = a.length;
        for (int i = p + 1; i < n; i++) {
            double alpha = a[i][p] / a[p][p];
            b[i] -= alpha * b[p];
            for (int j = p; j < n; j++) {
                a[i][j] -= alpha * a[p][j];
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
        Equation(double a[][], double b[]) {
            this.a = a;
            this.b = b;
        }

        double a[][];
        double b[];
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
