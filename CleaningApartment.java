import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class CleaningApartment {
    private final InputReader reader;
    private final OutputWriter writer;

    public CleaningApartment(InputReader reader, OutputWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public static void main(String[] args) {
        InputReader reader = new InputReader(System.in);
        OutputWriter writer = new OutputWriter(System.out);
        new CleaningApartment(reader, writer).run();
        writer.writer.flush();
    }

    class Edge {
        int from;
        int to;
    }

    class ConvertHampathToSat {
        int numVertices;
        Edge[] edges;

        List<String> clauses = new ArrayList<>();

        ConvertHampathToSat(int n, int m) {
            numVertices = n;
            edges = new Edge[m];
            for (int i = 0; i < m; ++i) {
                edges[i] = new Edge();
            }
        }

        void printEquisatisfiableSatFormula() {
//            int totalClauses = 4 * numVertices + 3 * edges.length;
            int totalVariables = numVertices * numVertices;

            printOneNodeAppearsOnce();
            printEachPosAssignForOneNode();
            printNoNodeTwice();
            printNoSamePosition();
            printNoEdgeNeibour();

            writer.printf("%d %d\n", clauses.size(), totalVariables);

            for (String clause : clauses) {
                writer.printf(clause);
            }
        }

        int realVertexNum(int v, int pos) {
            return numVertices * (v - 1) + pos;
        }

        // 1) one node appear once
        // N clauses
        void printOneNodeAppearsOnce() {
            for (int i = 1; i <= numVertices; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 1; j <= numVertices; j++) {
                    sb.append(String.format("%d ", realVertexNum(i, j)));
                }
                sb.append("0\n");
                clauses.add(sb.toString());
            }
        }

        // 2) each position only assign for one Node
        // N clauses
        void printEachPosAssignForOneNode() {
            for (int i = 1; i <= numVertices; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 1; j <= numVertices; j++) {
                    sb.append(String.format("%d ", realVertexNum(j, i)));
                }
                sb.append("0\n");
                clauses.add(sb.toString());
            }
        }

        //        3. no node appear on different position twice
        void printNoNodeTwice() {
            for (int i = 1; i <= numVertices; i++) {
                for (int j = 1; j <= numVertices; j++) {
                    for (int k = j + 1; k <= numVertices; k++) {
                        clauses.add(String.format("-%d -%d 0\n", realVertexNum(i, j), realVertexNum(i, k)));
                    }
                }
            }
        }

        //       4. no two node appear on the same position twice
        void printNoSamePosition() {
            for (int i = 1; i <= numVertices; i++) {
                for (int j = 1; j <= numVertices; j++) {
                    for (int k = j + 1; k <= numVertices; k++) {
                        clauses.add(String.format("-%d -%d 0\n", realVertexNum(j, i), realVertexNum(k, i)));
                    }
                }
            }
        }

        // 5 if no edge - no neibour
        void printNoEdgeNeibour() {
            for (int k = 1; k < numVertices; k++) {
                for (int i = 1; i <= numVertices; i++) {
                    for (int j = i + 1; j <= numVertices; j++) {
                        if (!isEdgeExist(i, j)) {
                            clauses.add(String.format("-%d -%d 0\n", realVertexNum(i, k), realVertexNum(j, k + 1)));
                        }
                        if (!isEdgeExist(j, i)) {
                            clauses.add(String.format("-%d -%d 0\n", realVertexNum(j, k), realVertexNum(i, k + 1)));
                        }
                    }
                }
            }
        }

        boolean isEdgeExist(int from, int to) {
            for (Edge edge : edges) {
                if (edge.from == from && edge.to == to) {
                    return true;
                }
            }
            return false;
        }
    }

    public void run() {
        int n = reader.nextInt();
        int m = reader.nextInt();

        ConvertHampathToSat converter = new ConvertHampathToSat(n, m);
        for (int i = 0; i < m; ++i) {
            converter.edges[i].from = reader.nextInt();
            converter.edges[i].to = reader.nextInt();
        }

        converter.printEquisatisfiableSatFormula();
    }

    static class InputReader {
        public BufferedReader reader;
        public StringTokenizer tokenizer;

        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream), 32768);
            tokenizer = null;
        }

        public String next() {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                try {
                    tokenizer = new StringTokenizer(reader.readLine());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }

        public int nextInt() {
            return Integer.parseInt(next());
        }

        public double nextDouble() {
            return Double.parseDouble(next());
        }

        public long nextLong() {
            return Long.parseLong(next());
        }
    }

    static class OutputWriter {
        public PrintWriter writer;

        OutputWriter(OutputStream stream) {
            writer = new PrintWriter(stream);
        }

        public void printf(String format, Object... args) {
            writer.print(String.format(Locale.ENGLISH, format, args));
        }
    }
}
