import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;


public class Main {

    public static class Parser {
       // private static final List<String> operations = List.of("->", "|", "&");
        public static List<String> operations = new ArrayList<>();
        private static List<Integer> nextPriority = new ArrayList<>();
        private static final int MAX_PRIORITY = 3;
        private int pos;
        private  char[] s;


        public Parser(char[] s) {
            this.s = s;
            operations.add("->");
            operations.add("|");
            operations.add("&");
            nextPriority.add(0);
            nextPriority.add(2);
            nextPriority.add(3);
        }

        public void skipWhitespaces() {
            while (pos < s.length && Character.isWhitespace(s[pos])) {
                pos++;
            }
        }

        public Expression parse(int priority) {
            if (priority == MAX_PRIORITY) {
                return parseToken();
            }

            Expression expression = parse(priority + 1);
            while (true) {
                skipWhitespaces();
                final String expr = findExpression(operations.get(priority));
                if (expr == null) {
                    break;
                }
                expression = makeExpression(expr, expression, parse(nextPriority.get(priority)));
            }
            return expression;
        }

        public String findExpression(String operation) {
            StringBuilder found = new StringBuilder();
            for (int i = 0; i < operation.length() && pos < s.length; i++, pos++) {
                found.append(s[pos]);
            }
            if (found.toString().equals(operation)) {
                return operation;
            }
            pos -= found.length();
            return null;
        }

        public Expression makeExpression(String operation, Expression left, Expression right) {
            Expression expression = null;
            switch (operation) {
                case "->": return expression = new BinaryExpression(left, right, (a, b) -> !a || b, operation);
                case "|": return expression = new BinaryExpression(left, right, (a, b) -> a | b, operation);
                case "&": return expression = new BinaryExpression(left, right, (a, b) -> a & b, operation);
            }
            return expression;
        }

        private Expression findVariable() {
            StringBuilder var = new StringBuilder();
            while (pos < s.length && (Character.isLetterOrDigit(s[pos]) || s[pos] == '\'')) {
                var.append(s[pos++]);
            }

            return new Variable(var.toString());
        }

        public Expression parseToken() {
            skipWhitespaces();
            if (pos < s.length && s[pos] == '!') {
                pos++;
                return new UnaryExpression(parseToken(), x -> !x, "!");
            } else if (pos < s.length && s[pos] == '(') {
                pos++;
                final Expression expression = parse(0);
                pos++;
                return expression;
            } else { // Variable
                return findVariable();
            }
        }

        public boolean equal_Expression(Expression e1, Expression e2){
            return true;
        }

        public void setS(char[] s) {
            this.s = s;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String givens = scanner.nextLine();
        givens = givens.trim();
        char[] chars1 = givens.toCharArray();
        StringBuilder builder = new StringBuilder();
        Set<String> vars = new HashSet<>();
        char[] first = "a->b->a".toCharArray();
        char[] second = "a->b->a".toCharArray();
        char[] third = "a->b->a".toCharArray();
        char[] fourth = "a->b->a".toCharArray();
        Parser parser1 = new Parser(first);

        for (char c : chars1) {
            if(c == '|'){
                break;
            }
            if (!Character.isLetterOrDigit(c) && c != '\'' || Character.isWhitespace(c)) {
                if (!builder.toString().equals("")) {
                    vars.add(builder.toString());

                }
                builder = new StringBuilder();
            }else {
                builder.append(c);
                if(c == chars1[chars1.length - 1]){
                    vars.add(builder.toString());
                }
            }
        }
        System.out.println(vars.toString());
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            char[] chars = line.toCharArray();
            Parser parser = new Parser(chars);
            Expression expression = parser.parse(0);
        }
    }

    public static abstract class Expression {
        protected final Expression left, right;

        public Expression(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        public abstract boolean eval(Map<String, Boolean> vars);
    }

    public static class BinaryExpression extends Expression {
        private final BiFunction<Boolean, Boolean, Boolean> operation;
        private final String operationSign;

        public BinaryExpression(Expression left, Expression right, BiFunction<Boolean, Boolean, Boolean> operation, String operationSign) {
            super(left, right);
            this.operation = operation;
            this.operationSign = operationSign;
        }

        @Override
        public boolean eval(Map<String, Boolean> vars) {
            return operation.apply(left.eval(vars), right.eval(vars));
        }

        @Override
        public String toString() {
            return "(" + operationSign + "," + left.toString() + "," + right.toString() + ")";
        }
    }

    public static class UnaryExpression extends Expression {
        private final Function<Boolean, Boolean> operation;
        private final String operationSign;

        public UnaryExpression(Expression left, Function<Boolean, Boolean> operation, String operationSign) {
            super(left, null);
            this.operation = operation;
            this.operationSign = operationSign;
        }

        @Override
        public boolean eval(Map<String, Boolean> vars) {
            return operation.apply(left.eval(vars));
        }

        @Override
        public String toString() {
            return "(" + operationSign + left.toString() + ")";
        }
    }

    public static class Variable extends Expression {
        private final String var;

        public Variable(String var) {
            super(null, null);
            this.var = var;
        }

        @Override
        public boolean eval(Map<String, Boolean> vars) {
            return vars.get(var);
        }

        @Override
        public String toString() {
            return var;
        }
    }
}
